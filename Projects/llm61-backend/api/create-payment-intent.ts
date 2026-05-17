import type { VercelRequest, VercelResponse } from '@vercel/node';
import Stripe from 'stripe';

const stripe = new Stripe(process.env.STRIPE_SECRET_KEY!);

const TIER_PRICES_AUD_CENTS: Record<string, number> = {
    free: 0,
    starter: 999,
    intermediate: 1999,
    advanced: 2999,
};

const CYCLE_DAYS = 30;
const MS_PER_DAY = 24 * 60 * 60 * 1000;

export default async function handler(req: VercelRequest, res: VercelResponse) {
    if (req.method !== 'POST') {
        return res.status(405).json({ error: 'Method not allowed' });
    }

    const {
        tier,
        currentTier = 'free',
        currentTierPurchasedAt = 0,
        auth0Sub = '',
    } = req.body as {
        tier?: string;
        currentTier?: string;
        currentTierPurchasedAt?: number;
        auth0Sub?: string;
    };

    if (!tier || !(tier in TIER_PRICES_AUD_CENTS)) {
        return res.status(400).json({ error: 'Invalid target tier' });
    }
    if (!(currentTier in TIER_PRICES_AUD_CENTS)) {
        return res.status(400).json({ error: 'Invalid current tier' });
    }

    const targetPrice = TIER_PRICES_AUD_CENTS[tier];
    const currentPrice = TIER_PRICES_AUD_CENTS[currentTier];

    let proratedCredit = 0;
    let daysRemaining = 0;
    if (currentTier !== 'free' && currentTierPurchasedAt > 0) {
        const daysElapsed = Math.max(0, (Date.now() - currentTierPurchasedAt) / MS_PER_DAY);
        daysRemaining = Math.max(0, CYCLE_DAYS - daysElapsed);
        proratedCredit = Math.round((currentPrice * daysRemaining) / CYCLE_DAYS);
    }

    const amount = Math.max(0, targetPrice - proratedCredit);

    if (amount <= 0) {
        return res.status(400).json({ error: 'No charge required — credit covers the upgrade' });
    }

    try {
        const paymentIntent = await stripe.paymentIntents.create({
            amount,
            currency: 'aud',
            automatic_payment_methods: { enabled: true },
            metadata: {
                tier,
                currentTier,
                proratedCredit: String(proratedCredit),
                daysRemaining: String(Math.floor(daysRemaining)),
                auth0Sub,
            },
        });
        return res.status(200).json({
            clientSecret: paymentIntent.client_secret,
            amountCharged: amount,
            proratedCredit,
            daysRemaining: Math.floor(daysRemaining),
        });
    } catch (err) {
        const message = err instanceof Error ? err.message : 'Unknown error';
        return res.status(500).json({ error: message });
    }
}
