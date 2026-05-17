package com.example.llm61.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.llm61.viewmodel.UpgradeViewModel
import com.example.llm61.viewmodel.UserViewModel
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet

private data class TierInfo(
    val id: String,
    val name: String,
    val priceCents: Int,
    val tagline: String,
    val perks: List<String>,
    val isBestSeller: Boolean = false
)

private val TIERS = listOf(
    TierInfo(
        id = "starter",
        name = "Starter",
        priceCents = 999,
        tagline = "Step up your learning",
        perks = listOf(
            "Unlimited AI hints on quiz questions",
            "AI-generated weak-area summaries",
            "Save unlimited quiz history"
        )
    ),
    TierInfo(
        id = "intermediate",
        name = "Intermediate",
        priceCents = 1999,
        tagline = "For serious learners",
        perks = listOf(
            "Everything in Starter",
            "5 questions per quiz (up from 3)",
            "Custom 7-day AI study plans",
            "Priority Gemini model"
        ),
        isBestSeller = true
    ),
    TierInfo(
        id = "advanced",
        name = "Advanced",
        priceCents = 2999,
        tagline = "Master mode",
        perks = listOf(
            "Everything in Intermediate",
            "10 questions per quiz",
            "Export quiz history to PDF",
            "Unlimited AI study plans"
        )
    )
)

private fun tierRank(tier: String): Int = when (tier) {
    "advanced" -> 3
    "intermediate" -> 2
    "starter" -> 1
    else -> 0
}

private fun tierPriceCents(tier: String): Int = when (tier) {
    "starter" -> 999
    "intermediate" -> 1999
    "advanced" -> 2999
    else -> 0
}

private fun formatPrice(cents: Int): String {
    val dollars = cents / 100
    val rem = cents % 100
    return "A$$dollars.${rem.toString().padStart(2, '0')}"
}

private const val CYCLE_DAYS = 30
private const val MS_PER_DAY = 24L * 60 * 60 * 1000

private data class ProrationBreakdown(
    val targetPriceCents: Int,
    val proratedCreditCents: Int,
    val amountToChargeCents: Int,
    val daysRemaining: Int,
    val hasActiveCredit: Boolean
)

private fun calculateProration(
    currentTier: String,
    currentPriceCents: Int,
    targetPriceCents: Int,
    tierPurchasedAt: Long
): ProrationBreakdown {
    if (currentTier == "free" || tierPurchasedAt == 0L) {
        return ProrationBreakdown(
            targetPriceCents,
            proratedCreditCents = 0,
            amountToChargeCents = targetPriceCents,
            daysRemaining = 0,
            hasActiveCredit = false
        )
    }
    val now = System.currentTimeMillis()
    val daysElapsed = ((now - tierPurchasedAt).coerceAtLeast(0L)) / MS_PER_DAY.toDouble()
    val daysRemaining = (CYCLE_DAYS - daysElapsed).coerceAtLeast(0.0)
    val credit = (currentPriceCents * daysRemaining / CYCLE_DAYS).toInt()
    val amountToCharge = (targetPriceCents - credit).coerceAtLeast(0)
    return ProrationBreakdown(
        targetPriceCents = targetPriceCents,
        proratedCreditCents = credit,
        amountToChargeCents = amountToCharge,
        daysRemaining = daysRemaining.toInt(),
        hasActiveCredit = credit > 0
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeScreen(
    userViewModel: UserViewModel,
    onBackClick: () -> Unit
) {
    val upgradeVM: UpgradeViewModel = viewModel()
    val uid = userViewModel.currentUserId
    val currentTier = userViewModel.currentTier
    val tierPurchasedAt = userViewModel.tierPurchasedAt
    val cancelledAt = userViewModel.cancelledAt
    val scheduledTier = userViewModel.scheduledTier
    val hasScheduledChange = cancelledAt > 0L && currentTier != "free"
    val isCancelToFree = hasScheduledChange && scheduledTier == "free"
    val isScheduledDowngrade = hasScheduledChange && scheduledTier != "free" && scheduledTier != currentTier
    val expiryMs = tierPurchasedAt + 30L * 24 * 60 * 60 * 1000
    val dateFmt = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())
    val expiryDate = if (tierPurchasedAt > 0L) dateFmt.format(java.util.Date(expiryMs)) else ""

    if (uid == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> upgradeVM.onPaymentSucceeded(uid)
            is PaymentSheetResult.Canceled -> upgradeVM.onPaymentCanceled()
            is PaymentSheetResult.Failed -> upgradeVM.onPaymentFailed(
                result.error.message ?: "Payment failed"
            )
        }
    }

    // Present the sheet whenever clientSecret transitions to non-null
    LaunchedEffect(upgradeVM.clientSecret) {
        val secret = upgradeVM.clientSecret ?: return@LaunchedEffect
        paymentSheet.presentWithPaymentIntent(
            secret,
            PaymentSheet.Configuration(
                merchantDisplayName = "LLM61",
                allowsDelayedPaymentMethods = false
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upgrade Account") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "You are currently on the ${currentTier.replaceFirstChar { it.uppercase() }} plan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            // Restore Purchases section — hidden when cancellation is pending
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Re-installed or switched devices?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Restore your tier from past Stripe payments tied to your account.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    if (upgradeVM.restoreMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(upgradeVM.restoreMessage ?: "", style = MaterialTheme.typography.bodySmall)
                                TextButton(onClick = { upgradeVM.clearRestoreMessages() }) { Text("Dismiss") }
                            }
                        }
                    } else if (upgradeVM.restoreError != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(upgradeVM.restoreError ?: "", style = MaterialTheme.typography.bodySmall)
                                TextButton(onClick = { upgradeVM.clearRestoreMessages() }) { Text("Dismiss") }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                upgradeVM.restoreSubscription(uid, userViewModel.auth0Sub, cancelledAt, scheduledTier, currentTier)
                            },
                            enabled = !upgradeVM.isRestoring,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (upgradeVM.isRestoring) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Checking Stripe…")
                            } else {
                                Text("Restore Purchases")
                            }
                        }
                    }
                }
            }

            // Cancel / Cancellation-pending card — only shown when on a paid tier
            if (currentTier != "free") {
                Spacer(Modifier.height(8.dp))

                if (upgradeVM.cancelScheduled) {
                    LaunchedEffect(Unit) { upgradeVM.clearCancelState() }
                }

                if (hasScheduledChange) {
                    // Pending cancellation or downgrade — show info + resume option
                    if (upgradeVM.resumeSuccessful) {
                        LaunchedEffect(Unit) { upgradeVM.clearResumeState() }
                    }
                    val pendingTitle = if (isCancelToFree)
                        "Subscription cancellation scheduled"
                    else
                        "Downgrade to ${scheduledTier.replaceFirstChar { it.uppercase() }} scheduled"
                    val pendingBody = if (isCancelToFree)
                        "You have full ${currentTier.replaceFirstChar { it.uppercase() }} access until $expiryDate, then your account reverts to Free automatically."
                    else
                        "You keep ${currentTier.replaceFirstChar { it.uppercase() }} access until $expiryDate, then switch to ${scheduledTier.replaceFirstChar { it.uppercase() }} automatically."

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(pendingTitle, style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(pendingBody, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { upgradeVM.resumeSubscription(uid, currentTier) },
                                enabled = !upgradeVM.isResuming,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (upgradeVM.isResuming) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Resuming…")
                                } else {
                                    Text("Keep ${currentTier.replaceFirstChar { it.uppercase() }} Plan")
                                }
                            }
                        }
                    }
                } else {
                    // Active paid tier — show cancel option
                    var showCancelDialog by remember { mutableStateOf(false) }

                    if (showCancelDialog) {
                        AlertDialog(
                            onDismissRequest = { showCancelDialog = false },
                            icon = {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            },
                            title = { Text("Cancel subscription?") },
                            text = {
                                Text("You keep ${currentTier.replaceFirstChar { it.uppercase() }} access until $expiryDate, then your account reverts to Free. You can re-subscribe at any time by purchasing a new plan.")
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showCancelDialog = false
                                        upgradeVM.cancelSubscription(uid)
                                    }
                                ) {
                                    Text("Yes, cancel", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showCancelDialog = false }) { Text("Keep plan") }
                            }
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Cancel subscription",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "You keep access until $expiryDate, then revert to Free. You can re-subscribe at any time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                enabled = !upgradeVM.isCancelling,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                if (upgradeVM.isCancelling) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Cancelling…")
                                } else {
                                    Text("Cancel Subscription")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (upgradeVM.paymentSuccessful) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Payment successful!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Your account has been upgraded.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (upgradeVM.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Payment error",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            upgradeVM.error ?: "",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { upgradeVM.reset() }) {
                            Text("Dismiss")
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            TIERS.forEach { tier ->
                TierCard(
                    tier = tier,
                    currentTier = currentTier,
                    tierPurchasedAt = tierPurchasedAt,
                    scheduledTier = scheduledTier,
                    hasScheduledChange = hasScheduledChange,
                    isLoading = upgradeVM.isLoading && upgradeVM.pendingTier == tier.id,
                    isCancelling = upgradeVM.isCancelling,
                    onUpgrade = {
                        android.util.Log.d("UpgradeDbg", "auth0Sub at call site = '${userViewModel.auth0Sub}'")
                        upgradeVM.startPayment(tier.id, currentTier, tierPurchasedAt, userViewModel.auth0Sub)
                    },
                    onScheduleDowngrade = { upgradeVM.scheduleDowngrade(uid, tier.id) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun TierCard(
    tier: TierInfo,
    currentTier: String,
    tierPurchasedAt: Long,
    scheduledTier: String,
    hasScheduledChange: Boolean,
    isLoading: Boolean,
    isCancelling: Boolean,
    onUpgrade: () -> Unit,
    onScheduleDowngrade: () -> Unit
) {
    val currentRank = tierRank(currentTier)
    val targetRank = tierRank(tier.id)
    val currentPriceCents = tierPriceCents(currentTier)
    val canUpgrade = targetRank > currentRank
    val isCurrent = currentRank == targetRank
    val isLower = targetRank < currentRank
    val isUpgrade = currentRank in 1 until targetRank
    val isScheduledForThis = hasScheduledChange && scheduledTier == tier.id
    var showDowngradeDialog by remember { mutableStateOf(false) }

    if (showDowngradeDialog) {
        AlertDialog(
            onDismissRequest = { showDowngradeDialog = false },
            title = { Text("Switch to ${tier.name}?") },
            text = { Text("You keep your current plan until the end of your billing period, then switch to ${tier.name} automatically.") },
            confirmButton = {
                TextButton(onClick = { showDowngradeDialog = false; onScheduleDowngrade() }) {
                    Text("Schedule switch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDowngradeDialog = false }) { Text("Cancel") }
            }
        )
    }

    val proration = if (isUpgrade)
        calculateProration(currentTier, currentPriceCents, tier.priceCents, tierPurchasedAt)
    else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (tier.isBestSeller)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tier.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (tier.isBestSeller) {
                    Surface(color = Color(0xFFFFB300), shape = MaterialTheme.shapes.small) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "BEST SELLER",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                tier.tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            // Price display
            if (canUpgrade) {
                if (isUpgrade && proration != null) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            formatPrice(proration.amountToChargeCents),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            formatPrice(tier.priceCents),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    // Breakdown card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                "Prorated upgrade from ${currentTier.replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Row {
                                Text(
                                    "${tier.name} full price",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    formatPrice(tier.priceCents),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row {
                                Text(
                                    "Credit (${proration.daysRemaining} of $CYCLE_DAYS days left)",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "- ${formatPrice(proration.proratedCreditCents)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF388E3C)
                                )
                            }
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            Row {
                                Text(
                                    "You pay today",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    formatPrice(proration.amountToChargeCents),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // Free → paid: show full price, no proration
                    Text(
                        formatPrice(tier.priceCents),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "one-time payment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    formatPrice(tier.priceCents),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            tier.perks.forEach { perk ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("• ", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        perk,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(2.dp))
            }
            Spacer(Modifier.height(12.dp))

            when {
                isCurrent -> Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                    Text("Current Plan")
                }
                isLower -> OutlinedButton(
                    onClick = { if (!isScheduledForThis) showDowngradeDialog = true },
                    enabled = !isCancelling,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when {
                        isCancelling && isScheduledForThis -> {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Scheduling…")
                        }
                        isScheduledForThis -> Text("Switch to ${tier.name} scheduled ✓")
                        else -> Text("Switch to ${tier.name} at next billing")
                    }
                }
                isLoading -> Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Loading…")
                }
                else -> Button(
                    onClick = onUpgrade,
                    enabled = canUpgrade && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isUpgrade) "Upgrade to ${tier.name}" else "Choose ${tier.name}")
                }
            }
        }
    }
}