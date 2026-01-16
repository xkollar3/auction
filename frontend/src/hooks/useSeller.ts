import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createSellerAccount } from '../api/stripe';
import { useAuthStore } from '../stores/authStore';

/**
 * Hook for seller account creation
 */
export const useSeller = () => {
  const { setSellerId } = useAuthStore();
  const queryClient = useQueryClient();

  const createSellerAccountMutation = useMutation({
    mutationFn: createSellerAccount,
    onSuccess: (data) => {
      // Store seller ID in auth store
      setSellerId(data.sellerId);
      // Invalidate profile query so the page refreshes with new data
      queryClient.invalidateQueries({ queryKey: ['userProfile'] });
      console.log('Seller account created:', data);
    },
    onError: (error) => {
      console.error('Failed to create seller account:', error);
    },
  });

  return {
    createSellerAccount: createSellerAccountMutation.mutate,
    isCreating: createSellerAccountMutation.isPending,
    sellerData: createSellerAccountMutation.data,
    error: createSellerAccountMutation.error,
  };
};
