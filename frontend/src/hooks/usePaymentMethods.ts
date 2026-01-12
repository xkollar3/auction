import { useMutation, useQuery } from '@tanstack/react-query';
import {
  createSetupIntent,
  attachPaymentMethod,
  getPaymentMethods,
} from '../api/stripe';

/**
 * Hook for payment method management
 */
export const usePaymentMethods = (customerId?: string) => {
  // Query for fetching payment methods
  const paymentMethodsQuery = useQuery({
    queryKey: ['paymentMethods'],
    queryFn: getPaymentMethods,
    enabled: !!customerId, // Only fetch if customer ID exists
  });

  // Mutation for creating setup intent
  const setupIntentMutation = useMutation({
    mutationFn: (customerId: string) => createSetupIntent(customerId),
    onError: (error) => {
      console.error('Failed to create setup intent:', error);
    },
  });

  // Mutation for attaching payment method
  const attachPaymentMethodMutation = useMutation({
    mutationFn: (paymentMethodId: string) => attachPaymentMethod(paymentMethodId),
    onSuccess: () => {
      // Refetch payment methods after successful attachment
      paymentMethodsQuery.refetch();
    },
    onError: (error) => {
      console.error('Failed to attach payment method:', error);
    },
  });

  return {
    // Payment methods list
    paymentMethods: paymentMethodsQuery.data?.paymentMethods || [],
    isLoadingMethods: paymentMethodsQuery.isLoading,
    refetchMethods: paymentMethodsQuery.refetch,

    // Setup intent
    createSetupIntent: setupIntentMutation.mutate,
    isCreatingSetupIntent: setupIntentMutation.isPending,
    setupIntentData: setupIntentMutation.data,
    setupIntentError: setupIntentMutation.error,

    // Attach payment method
    attachPaymentMethod: attachPaymentMethodMutation.mutate,
    isAttachingPaymentMethod: attachPaymentMethodMutation.isPending,
    attachError: attachPaymentMethodMutation.error,
  };
};
