import { loadStripe, type Stripe } from "@stripe/stripe-js";

/**
 * Stripe client instance
 *
 * Initialized with publishable key from environment variable:
 * - VITE_STRIPE_PUBLISHABLE_KEY: Stripe publishable key (pk_test_...)
 */
let stripePromise: Promise<Stripe | null>;

/**
 * Get or initialize Stripe instance
 *
 * @returns Promise resolving to Stripe instance
 */
export const getStripe = (): Promise<Stripe | null> => {
  if (!stripePromise) {
    const publishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY;

    if (!publishableKey) {
      console.error(
        "Stripe publishable key not found in environment variables",
      );
      return Promise.resolve(null);
    }

    stripePromise = loadStripe(publishableKey);
  }

  return stripePromise;
};

/**
 * Stripe Elements options for consistent styling
 */
export const stripeElementsOptions = {
  fonts: [
    {
      cssSrc:
        "https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap",
    },
  ],
  appearance: {
    theme: "stripe" as const,
    variables: {
      colorPrimary: "#0066cc",
      colorBackground: "#ffffff",
      colorText: "#1a1a1a",
      colorDanger: "#dc2626",
      fontFamily: "Inter, system-ui, sans-serif",
      borderRadius: "8px",
    },
  },
};

/**
 * Helper function to confirm card setup with Stripe
 *
 * @param clientSecret - Setup intent client secret from backend
 * @param cardElement - Stripe CardElement instance
 * @returns Promise with setup intent result
 */
export const confirmCardSetup = async (
  clientSecret: string,
  paymentMethod: { card: any; billing_details?: any },
) => {
  const stripe = await getStripe();

  if (!stripe) {
    throw new Error("Stripe is not initialized");
  }

  return stripe.confirmCardSetup(clientSecret, {
    payment_method: paymentMethod,
  });
};
