import {
  type CreateSellerAccountResponse,
  type CreateStripeCustomerRequest,
  type CreateStripeCustomerResponse,
  type SetupIntentResponse,
  type AttachPaymentMethodResponse,
  type PaymentMethodsResponse,
} from "../types/stripe";

const API_BASE = "/api/user/stripe";

/**
 * Create Stripe seller account (Connect)
 *
 * @returns Seller account details with onboarding URLs
 */
export const createSellerAccount =
  async (): Promise<CreateSellerAccountResponse> => {
    // TODO: Replace with real API call
    // const response = await axios.post(`${API_BASE}/seller-account`);
    // return response.data;

    // Mock response - replace these values with real ones from your backend
    await new Promise((resolve) => setTimeout(resolve, 1000)); // Simulate network delay

    return {
      sellerId: "acct_1234567890abcdef",
      onboardingUrl:
        "https://connect.stripe.com/express/onboarding/REPLACE_WITH_REAL_URL",
      dashboardUrl:
        "https://dashboard.stripe.com/test/connect/accounts/acct_1234567890abcdef",
    };
  };

/**
 * Create Stripe customer with address
 *
 * @param address - Customer address details
 * @returns Customer ID
 */
export const createStripeCustomer = async (
  address: CreateStripeCustomerRequest,
): Promise<CreateStripeCustomerResponse> => {
  // TODO: Replace with real API call
  // const response = await axios.post(`${API_BASE}/customer`, address);
  // return response.data;

  // Mock response - replace customerId with real one from your backend
  await new Promise((resolve) => setTimeout(resolve, 1000)); // Simulate network delay

  return {
    customerId: "cus_TgHkwE58wZorRB",
  };
};

/**
 * Create SetupIntent for payment method
 *
 * @param customerId - Stripe customer ID
 * @returns SetupIntent client secret
 */
export const createSetupIntent = async (
  customerId: string,
): Promise<SetupIntentResponse> => {
  // TODO: Replace with real API call
  // const response = await axios.post(`${API_BASE}/setup-intent`, { customerId });
  // return response.data;

  // Mock response - REPLACE THESE WITH REAL VALUES FROM YOUR BACKEND
  await new Promise((resolve) => setTimeout(resolve, 500)); // Simulate network delay

  return {
    // Replace this client secret with a real one from your Stripe backend
    clientSecret:
      "seti_1SolqECvL7lAvScrbisWr6EY_secret_TmKVHddCQ7cRrwsLiil6p4EVB0IRGcz",
    setupIntentId: "seti_1SolqECvL7lAvScrbisWr6EY",
  };
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
  // TODO: Replace with real API call
  // const response = await axios.post(`${API_BASE}/payment-method`, { paymentMethodId });
  // return response.data;

  // Mock response
  await new Promise((resolve) => setTimeout(resolve, 500)); // Simulate network delay

  return {
    success: true,
    paymentMethodId,
  };
};

/**
 * Get customer's saved payment methods
 *
 * @returns List of payment methods
 */
export const getPaymentMethods = async (): Promise<PaymentMethodsResponse> => {
  // TODO: Replace with real API call
  // const response = await axios.get(`${API_BASE}/payment-methods`);
  // return response.data;

  // Mock response - replace with real data from your backend
  await new Promise((resolve) => setTimeout(resolve, 500)); // Simulate network delay

  return {
    paymentMethods: [
      {
        id: "pm_1234567890abcdef",
        brand: "visa",
        last4: "4242",
        expMonth: 12,
        expYear: 2025,
        isDefault: true,
      },
      {
        id: "pm_0987654321fedcba",
        brand: "mastercard",
        last4: "5555",
        expMonth: 6,
        expYear: 2026,
        isDefault: false,
      },
    ],
  };
};
