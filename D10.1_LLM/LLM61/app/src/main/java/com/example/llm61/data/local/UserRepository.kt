package com.example.llm61.data.local

class UserRepository(private val dao: UserDao) {

    /** Returns the user's local id, creating the row on first login. */
    suspend fun getOrCreateByAuth0Sub(
        auth0Sub: String,
        username: String,
        email: String
    ): Long {
        val existing = dao.findByAuth0Sub(auth0Sub)
        if (existing != null) {
            if (existing.username != username || existing.email != email) {
                dao.update(existing.copy(username = username, email = email))
            }
            return existing.id
        }
        return dao.insert(UserEntity(
            auth0Sub = auth0Sub,
            username = username,
            email = email
        ))
    }

    suspend fun updateTier(userId: Long, tier: String) {
        val purchasedAt = if (tier == "free") 0L else System.currentTimeMillis()
        dao.updateTierAndPurchaseDate(userId, tier, purchasedAt)
        // A new payment clears any pending scheduled change
        dao.updateCancelledAt(userId, 0)
        dao.updateScheduledTier(userId, tier)
    }

    suspend fun restoreTier(userId: Long, tier: String, tierPurchasedAt: Long) {
        dao.updateTierAndPurchaseDate(userId, tier, tierPurchasedAt)
        // Do NOT touch cancelledAt — restore never overrides a deliberate cancellation.
    }

    /** Schedule cancellation to free at end of billing period. */
    suspend fun scheduleCancellation(userId: Long) {
        dao.updateCancelledAt(userId, System.currentTimeMillis())
        dao.updateScheduledTier(userId, "free")
    }

    /** Schedule a downgrade to a lower tier at end of billing period. */
    suspend fun scheduleDowngrade(userId: Long, targetTier: String) {
        dao.updateCancelledAt(userId, System.currentTimeMillis())
        dao.updateScheduledTier(userId, targetTier)
    }

    /** Undo any pending scheduled change while still within the grace period. */
    suspend fun resumeSubscription(userId: Long, currentTier: String) {
        dao.updateCancelledAt(userId, 0)
        dao.updateScheduledTier(userId, currentTier)
    }

    /**
     * Called on every login. If a scheduled change has passed the billing period,
     * apply it now.
     * - Downgrade to specific tier: clears cancelledAt (new tier is legitimate, restore allowed)
     * - Cancellation to free: keeps cancelledAt > 0 to permanently block restore
     */
    suspend fun expireIfCancelled(
        userId: Long,
        tierPurchasedAt: Long,
        cancelledAt: Long,
        scheduledTier: String
    ) {
        if (cancelledAt <= 0L) return
        val cycleMs = 30L * 24 * 60 * 60 * 1000
        if (System.currentTimeMillis() >= tierPurchasedAt + cycleMs) {
            val isCancelToFree = scheduledTier == "free"
            val purchasedAt = if (isCancelToFree) 0L else System.currentTimeMillis()
            dao.updateTierAndPurchaseDate(userId, scheduledTier, purchasedAt)
            dao.updateScheduledTier(userId, scheduledTier)
            if (!isCancelToFree) {
                // Scheduled downgrade completed — normal tier now, restore is fine
                dao.updateCancelledAt(userId, 0)
            }
            // Cancellation to free: keep cancelledAt > 0 to block restore
        }
    }

    fun observeTier(userId: Long) = dao.observeTier(userId)
    fun observeTierPurchasedAt(userId: Long) = dao.observeTierPurchasedAt(userId)
    fun observeCancelledAt(userId: Long) = dao.observeCancelledAt(userId)
    fun observeScheduledTier(userId: Long) = dao.observeScheduledTier(userId)
}