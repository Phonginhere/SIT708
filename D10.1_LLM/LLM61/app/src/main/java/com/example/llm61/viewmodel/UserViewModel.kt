package com.example.llm61.viewmodel

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.llm61.auth.Auth0Manager
import com.example.llm61.data.UserAnswer
import com.example.llm61.data.local.AppDatabase
import com.example.llm61.data.local.UserRepository
import kotlinx.coroutines.launch
import com.example.llm61.data.payment.PaymentRepository

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val paymentRepo = PaymentRepository()

    private val userRepo = UserRepository(
        AppDatabase.getInstance(application).userDao()
    )
    private val auth0 = Auth0Manager(application)

    var username by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var currentUserId by mutableStateOf<Long?>(null)
        private set
    var selectedInterests by mutableStateOf(setOf<String>())
        private set
    var isLoggedIn by mutableStateOf(false)
        private set
    var isAuthenticating by mutableStateOf(false)
        private set
    var authError by mutableStateOf<String?>(null)
        private set
    var auth0Sub by mutableStateOf("")
        private set
    var isSessionChecked by mutableStateOf(false)
        private set

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        auth0.tryRestoreSession(
            onSuccess = { info ->
                viewModelScope.launch {
                    try {
                        auth0Sub = info.sub
                        currentUserId = userRepo.getOrCreateByAuth0Sub(
                            auth0Sub = info.sub,
                            username = info.username,
                            email = info.email
                        )
                        startObservingTier(currentUserId!!)
                        userRepo.expireIfCancelled(currentUserId!!, tierPurchasedAt, cancelledAt, scheduledTier)
                        autoSyncTierFromStripe(currentUserId!!, info.sub)
                        username = info.username
                        email = info.email
                        isLoggedIn = true
                    } catch (e: Exception) {
                        android.util.Log.e("AuthDbg", "Silent restore DB failed", e)
                        auth0Sub = ""
                    } finally {
                        isSessionChecked = true
                    }
                }
            },
            onNoSession = {
                isSessionChecked = true
            }
        )
    }

    private suspend fun autoSyncTierFromStripe(userId: Long, sub: String) {
        if (sub.isBlank()) return
        try {
            paymentRepo.restoreSubscription(sub).fold(
                onSuccess = { response ->
                    if (response.tier == "free" || response.paymentCount == 0) return@fold
                    val cycleMs = 30L * 24 * 60 * 60 * 1000
                    val withinPeriod = response.tierPurchasedAt > 0L &&
                            System.currentTimeMillis() < response.tierPurchasedAt + cycleMs
                    if (!withinPeriod) return@fold
                    userRepo.restoreTier(userId, response.tier, response.tierPurchasedAt)
                    android.util.Log.d("AuthDbg", "Auto-synced tier: ${response.tier}")
                },
                onFailure = { e ->
                    android.util.Log.w("AuthDbg", "Auto tier sync failed: ${e.message}")
                }
            )
        } catch (e: Exception) {
            android.util.Log.w("AuthDbg", "Auto tier sync error", e)
        }
    }

    fun login(activity: Activity, onLoggedIn: () -> Unit) {
        isAuthenticating = true
        authError = null
        auth0.login(
            activity = activity,
            onSuccess = { info ->
                android.util.Log.d("AuthDbg", "UserVM got UserInfo: sub='${info.sub}' email='${info.email}'")
                viewModelScope.launch {
                    try {
                        // Set auth0Sub immediately so it is available even if the DB call fails
                        auth0Sub = info.sub
                        android.util.Log.d("AuthDbg", "Coroutine running, calling getOrCreateByAuth0Sub")
                        currentUserId = userRepo.getOrCreateByAuth0Sub(
                            auth0Sub = info.sub,
                            username = info.username,
                            email = info.email
                        )
                        android.util.Log.d("AuthDbg", "DB returned userId=$currentUserId, auth0Sub='$auth0Sub'")
                        startObservingTier(currentUserId!!)
                        // Apply any expired scheduled tier change
                        userRepo.expireIfCancelled(currentUserId!!, tierPurchasedAt, cancelledAt, scheduledTier)
                        autoSyncTierFromStripe(currentUserId!!, info.sub)   // ← NEW
                        username = info.username
                        email = info.email
                        isLoggedIn = true
                        isAuthenticating = false
                        onLoggedIn()
                    } catch (e: Exception) {
                        android.util.Log.e("AuthDbg", "DB operation failed after login", e)
                        auth0Sub = ""
                        authError = "Login failed: ${e.message}"
                        isAuthenticating = false
                    }
                }
            },
            onError = { msg ->
                authError = msg
                isAuthenticating = false
            },
            onCanceled = {
                isAuthenticating = false
                authError = null
            }
        )
    }

    fun logout(activity: Activity, onLoggedOut: () -> Unit) {
        auth0.logout(activity) {
            tierObserverJob?.cancel()
            tierPurchasedAtJob?.cancel()
            cancelledAtJob?.cancel()
            scheduledTierJob?.cancel()
            username = ""
            email = ""
            currentUserId = null
            selectedInterests = emptySet()
            isLoggedIn = false
            currentTier = "free"
            tierPurchasedAt = 0L
            cancelledAt = 0L
            scheduledTier = "free"
            auth0Sub = ""
            onLoggedOut()
        }
    }

    fun clearAuthError() { authError = null }

    fun toggleInterest(topic: String) {
        selectedInterests = when {
            topic in selectedInterests -> selectedInterests - topic
            selectedInterests.size < 10 -> selectedInterests + topic
            else -> selectedInterests
        }
    }

    fun completeOnboarding() { /* no-op now */ }

    var lastQuizAnswers by mutableStateOf<List<UserAnswer>>(emptyList())
        private set
    var lastQuizTopic by mutableStateOf("")
        private set

    fun submitQuiz(topic: String, answers: List<UserAnswer>) {
        lastQuizTopic = topic
        lastQuizAnswers = answers
    }

    var tierPurchasedAt by mutableStateOf(0L)
        private set
    var cancelledAt by mutableStateOf(0L)
        private set
    var scheduledTier by mutableStateOf("free")
        private set

    private var tierPurchasedAtJob: kotlinx.coroutines.Job? = null
    private var cancelledAtJob: kotlinx.coroutines.Job? = null
    private var scheduledTierJob: kotlinx.coroutines.Job? = null

    var currentTier by mutableStateOf("free")
        private set

    private var tierObserverJob: kotlinx.coroutines.Job? = null

    private fun startObservingTier(userId: Long) {
        tierObserverJob?.cancel()
        tierObserverJob = viewModelScope.launch {
            userRepo.observeTier(userId).collect { tier -> currentTier = tier ?: "free" }
        }
        tierPurchasedAtJob?.cancel()
        tierPurchasedAtJob = viewModelScope.launch {
            userRepo.observeTierPurchasedAt(userId).collect { ts -> tierPurchasedAt = ts ?: 0L }
        }
        cancelledAtJob?.cancel()
        cancelledAtJob = viewModelScope.launch {
            userRepo.observeCancelledAt(userId).collect { ts -> cancelledAt = ts ?: 0L }
        }
        scheduledTierJob?.cancel()
        scheduledTierJob = viewModelScope.launch {
            userRepo.observeScheduledTier(userId).collect { t -> scheduledTier = t ?: "free" }
        }
    }
}