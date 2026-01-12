import { useMutation } from "@tanstack/react-query";
import { createStripeCustomer } from "../api/stripe";
import { useAuthStore } from "../stores/authStore";
import { type CreateStripeCustomerRequest } from "../types/stripe";

/**
 * Hook for Stripe customer creation
 */
export const useCustomer = () => {
  const { setCustomerId } = useAuthStore();

  const createCustomerMutation = useMutation({
    mutationFn: (address: CreateStripeCustomerRequest) =>
      createStripeCustomer(address),
    onSuccess: (data) => {
      // Store customer ID in auth store
      setCustomerId(data.customerId);
      console.log("Customer created:", data);
    },
    onError: (error) => {
      console.error("Failed to create customer:", error);
    },
  });

  return {
    createCustomer: createCustomerMutation.mutate,
    isCreating: createCustomerMutation.isPending,
    customerData: createCustomerMutation.data,
    error: createCustomerMutation.error,
  };
};
