package com.example.llm61.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.llm61.data.local.AppDatabase
import com.example.llm61.data.local.UserRepository
import com.example.llm61.data.payment.PaymentRepository
import kotlinx.coroutines.launch

private fun tierRank(tier: String) = when (tier) {
    "starter" -> 1; "intermediate" -> 2; "advanced" -> 3; else -> 0
}
private fun tier(name: String) = name.replaceFirstChar { it.uppercase() }

class UpgradeViewModel(application: Application) : AndroidViewModel(application) {

    private val paymentRepo = PaymentRepository()
    private val userRepo = UserRepository(AppDatabase.getInstance(application).userDao())

    var pendingTier by mutableStateOf<String?>(null)
        private set
    var clientSecret by mutableStateOf<String?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var paymentSuccessful by mutableStateOf(false)
        private set

    // Restore-flow state
    var isRestoring by mutableStateOf(false)
        private set
    var restoreMessage by mutableStateOf<String?>(null)
        private set
    var restoreError by mutableStateOf<String?>(null)
        private set

    fun startPayment(
        tier: String,
        currentTier: String,
        currentTierPurchasedAt: Long,
        auth0Sub: String
    ) {
        pendingTier = tier
        isLoading = true
        error = null
        paymentSuccessful = false
        viewModelScope.launch {
            paymentRepo.createPaymentIntent(tier, currentTier, currentTierPurchasedAt, auth0Sub).fold(
                onSuccess = { secret ->
                    clientSecret = secret
                    isLoading = false
                },
                onFailure = { err ->
                    error = err.message ?: "Failed to start payment"
                    isLoading = false
                    pendingTier = null
                }
            )
        }
    }

    fun onPaymentSucceeded(userId: Long) {
        val tier = pendingTier ?: return
        viewModelScope.launch {
            userRepo.updateTier(userId, tier)
            paymentSuccessful = true
            clientSecret = null
            pendingTier = null
        }
    }

    fun onPaymentCanceled() {
        clientSecret = null
        pendingTier = null
    }

    fun onPaymentFailed(message: String) {
        error = message
        clientSecret = null
        pendingTier = null
    }

    fun reset() {
        clientSecret = null
        error = null
        paymentSuccessful = false
        pendingTier = null
        restoreMessage = null
        restoreError = null
    }

    fun restoreSubscription(
        userId: Long,
        auth0Sub: String,
        cancelledAt: Long,
        scheduledTier: String,
        currentTier: String
    ) {
        if (auth0Sub.isBlank()) {
            restoreError = "Not signed in"
            return
        }
        isRestoring = true
        restoreError = null
        restoreMessage = null
        viewModelScope.launch {
            paymentRepo.restoreSubscription(auth0Sub).fold(
                onSuccess = { response ->
                    val cycleMs = 30L * 24 * 60 * 60 * 1000
                    val withinBillingPeriod = response.tierPurchasedAt > 0L &&
                            System.currentTimeMillis() < response.tierPurchasedAt + cycleMs
                    val hasPendingChange = cancelledAt > 0L
                    val isCancelToFree = hasPendingChange && scheduledTier == "free"

                    when {
                        response.tier == "free" || response.paymentCount == 0 ->
                            restoreError = "No previous purchases found for your account"

                        // Cancelled to free AND billing period has fully expired — permanently blocked
                        isCancelToFree && !withinBillingPeriod ->
                            restoreError = "Your subscription was cancelled and has ended. Purchase a new plan to re-subscribe."

                        // Same tier, no pending change — already correct
                        response.tier == currentTier && !hasPendingChange ->
                            restoreMessage = "Your ${tier(response.tier)} plan is already active — no restore needed."

                        // Same tier but has a pending scheduled change — still within billing period,
                        // so clear the scheduled change (user wants to keep their paid plan)
                        response.tier == currentTier && hasPendingChange && withinBillingPeriod -> {
                            userRepo.resumeSubscription(userId, currentTier)
                            restoreMessage = "Your ${tier(response.tier)} plan is active. Scheduled change has been cleared."
                        }

                        // Tier mismatch — restore to what Stripe says was paid for
                        tierRank(response.tier) > tierRank(currentTier) -> {
                            userRepo.restoreTier(userId, response.tier, response.tierPurchasedAt)
                            if (hasPendingChange && withinBillingPeriod)
                                userRepo.resumeSubscription(userId, response.tier)
                            restoreMessage = "Fixed: restored your ${tier(response.tier)} plan (was showing ${tier(currentTier)})."
                        }

                        else -> {
                            userRepo.restoreTier(userId, response.tier, response.tierPurchasedAt)
                            if (hasPendingChange && withinBillingPeriod)
                                userRepo.resumeSubscription(userId, response.tier)
                            restoreMessage = "Fixed: corrected to ${tier(response.tier)} plan (was showing ${tier(currentTier)})."
                        }
                    }
                    isRestoring = false
                },
                onFailure = { err ->
                    restoreError = err.message ?: "Failed to restore subscription"
                    isRestoring = false
                }
            )
        }
    }

    fun clearRestoreMessages() {
        restoreMessage = null
        restoreError = null
    }

    // Cancel subscription state
    var cancelScheduled by mutableStateOf(false)
        private set
    var isCancelling by mutableStateOf(false)
        private set

    fun cancelSubscription(userId: Long) {
        isCancelling = true
        cancelScheduled = false
        viewModelScope.launch {
            userRepo.scheduleCancellation(userId)
            cancelScheduled = true
            isCancelling = false
        }
    }

    fun scheduleDowngrade(userId: Long, targetTier: String) {
        isCancelling = true
        cancelScheduled = false
        viewModelScope.launch {
            userRepo.scheduleDowngrade(userId, targetTier)
            cancelScheduled = true
            isCancelling = false
        }
    }

    fun clearCancelState() {
        cancelScheduled = false
    }

    var isResuming by mutableStateOf(false)
        private set
    var resumeSuccessful by mutableStateOf(false)
        private set

    fun resumeSubscription(userId: Long, currentTier: String) {
        isResuming = true
        resumeSuccessful = false
        viewModelScope.launch {
            userRepo.resumeSubscription(userId, currentTier)
            resumeSuccessful = true
            isResuming = false
        }
    }

    fun clearResumeState() { resumeSuccessful = false }
}