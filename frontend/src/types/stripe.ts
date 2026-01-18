/**
 * Stripe-related type definitions
 */

// ============= Seller Account (Stripe Connect) =============

export interface CreateSellerAccountRequest {
  // No additional fields needed - uses JWT token
}

export interface CreateSellerAccountResponse {
  sellerId: string; // Stripe account ID (acct_xxxx)
  onboardingUrl: string; // URL to complete Stripe onboarding
  dashboardUrl: string; // URL to Stripe Express dashboard
}

// ============= Buyer Account (Stripe Customer) =============

export interface CreateStripeCustomerRequest {
  line1: string;
  line2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
}

export interface CreateStripeCustomerResponse {
  customerId: string; // Stripe customer ID (cus_xxxx)
}

// ============= Setup Intent =============

export interface SetupIntentRequest {
  customerId: string;
}

export interface SetupIntentResponse {
  clientSecret: string; // For Stripe.js confirmCardSetup
  setupIntentId: string; // seti_xxxx
}

// ============= Payment Method =============

export interface AttachPaymentMethodRequest {
  paymentMethodId: string; // pm_xxxx from Stripe
}

export interface AttachPaymentMethodResponse {
  success: boolean;
  paymentMethodId: string;
}

export interface PaymentMethod {
  id: string; // pm_xxxx
  brand: string; // 'visa', 'mastercard', etc.
  last4: string; // Last 4 digits
  expMonth: number;
  expYear: number;
  isDefault: boolean;
}

export interface PaymentMethodsResponse {
  paymentMethods: PaymentMethod[];
}
