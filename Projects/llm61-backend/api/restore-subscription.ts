import type { VercelRequest, VercelResponse } from '@vercel/node';
import Stripe from 'stripe';

const stripe = new Stripe(process.env.STRIPE_SECRET_KEY!);

const TIER_RANK: Record<string, number> = {
    free: 0,
    starter: 1,
    intermediate: 2,
    advanced: 3,
};

export default async function handler(req: VercelRequest, res: VercelResponse) {
    if (req.method !== 'POST') {
        return res.status(405).json({ error: 'Method not allowed' });
    }

    const { auth0Sub } = req.body as { auth0Sub?: string };
    if (!auth0Sub) {
        return res.status(400).json({ error: 'auth0Sub is required' });
    }

    try {
        // List recent payments and filter by auth0Sub in metadata
        // (Stripe's search API has a 5-min index lag — list is more reliable for fresh payments)
        const all = await stripe.paymentIntents.list({ limit: 100 });

        // Only match payments explicitly tagged with this user's auth0Sub
        const userPayments = all.data.filter(
            (pi) => pi.status === 'succeeded' && pi.metadata.auth0Sub === auth0Sub
        );

        if (userPayments.length === 0) {
            return res.status(200).json({
                tier: 'free',
                tierPurchasedAt: 0,
                paymentCount: 0,
            });
        }

        // Find highest tier ever purchased; for that tier, take the most recent payment timestamp
        let highestRank = 0;
        let highestTier = 'free';
        let highestTierPurchasedAt = 0;

        for (const pi of userPayments) {
            const tier = pi.metadata.tier || 'free';
            const rank = TIER_RANK[tier] ?? 0;
            const tsMs = pi.created * 1000;
            if (rank > highestRank) {
                highestRank = rank;
                highestTier = tier;
                highestTierPurchasedAt = tsMs;
            } else if (rank === highestRank && tsMs > highestTierPurchasedAt) {
                highestTierPurchasedAt = tsMs;
            }
        }

        return res.status(200).json({
            tier: highestTier,
            tierPurchasedAt: highestTierPurchasedAt,
            paymentCount: userPayments.length,
        });
    } catch (err) {
        const message = err instanceof Error ? err.message : 'Unknown error';
        return res.status(500).json({ error: message });
    }
}
