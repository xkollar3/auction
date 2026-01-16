import { api } from "../lib/api";
import { getUserProfile } from "./user";
import {
  type CreateSellerAccountResponse,
  type CreateStripeCustomerRequest,
  type CreateStripeCustomerResponse,
  type SetupIntentResponse,
  type AttachPaymentMethodResponse,
  type PaymentMethodsResponse,
} from "../types/stripe";

/**
 * Create Stripe seller account (Connect)
 *
 * @returns Seller account details with onboarding URL
 */
export const createSellerAccount =
  async (): Promise<CreateSellerAccountResponse> => {
    // Create seller account - backend uses sendAndWait so account is ready when this returns
    await api.post("/api/users/me/create-seller-account");

    // Fetch profile to get the seller account details
    const profile = await getUserProfile();

    return {
      sellerId: profile.stripeSellerAccountId || "",
      onboardingUrl: profile.stripeOnboardingLink || "",
      dashboardUrl: "",
    };
  };

/**
 * Create Stripe customer with address
 * This is an async operation - we poll for completion
 *
 * @param address - Customer address details
 * @returns Customer ID after polling for completion
 */
export const createStripeCustomer = async (
  address: CreateStripeCustomerRequest,
): Promise<CreateStripeCustomerResponse> => {
  // Send the create customer request (returns 202 Accepted)
  await api.post("/api/users/me/create-stripe-customer", address);

  // Poll for customer creation to complete
  const maxAttempts = 10;
  const delayMs = 1000;

  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    await new Promise((resolve) => setTimeout(resolve, delayMs));

    const profile = await getUserProfile();
    if (profile.stripeCustomerId) {
      return { customerId: profile.stripeCustomerId };
    }
  }

  throw new Error("Timed out waiting for customer creation");
};

/**
 * Create SetupIntent for payment method
 *
 * @returns SetupIntent client secret
 */
export const createSetupIntent = async (): Promise<SetupIntentResponse> => {
  const response = await api.post<SetupIntentResponse>(
    "/api/users/me/setup-payment-intent"
  );
  return response.data;
};

/**
 * Attach payment method to customer
 *
 * @param paymentMethodId - Payment method ID from Stripe.js
 * @returns Success response
 */
export const attachPaymentMethod = async (
  paymentMethodId: string,
): Promise<AttachPaymentMethodResponse> => {
  await api.post("/api/users/me/payment-methods", { paymentMethodId });
  return {
    success: true,
    paymentMethodId,
  };
};

/**
 * Get customer's saved payment methods
 * Note: This requires backend support - currently returns empty
 *
 * @returns List of payment methods
 */
export const getPaymentMethods = async (): Promise<PaymentMethodsResponse> => {
  // Backend doesn't have a list endpoint yet, return empty
  return {
    paymentMethods: [],
  };
};
