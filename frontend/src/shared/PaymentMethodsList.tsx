import { PaymentMethodCard } from './PaymentMethodCard';
import { type PaymentMethod } from '../types/stripe';
import { CreditCard } from 'lucide-react';

interface PaymentMethodsListProps {
  paymentMethods: PaymentMethod[];
  isLoading?: boolean;
}

/**
 * Payment Methods List Component
 *
 * Displays a list of saved payment methods
 */
export const PaymentMethodsList = ({ paymentMethods, isLoading = false }: PaymentMethodsListProps) => {
  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (paymentMethods.length === 0) {
    return (
      <div className="text-center py-12">
        <CreditCard className="h-12 w-12 text-gray-400 mx-auto mb-4" />
        <h3 className="text-lg font-medium text-gray-900 mb-2">No Payment Methods</h3>
        <p className="text-gray-600 mb-4">Add a payment method to start bidding</p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {paymentMethods.map((paymentMethod) => (
        <PaymentMethodCard key={paymentMethod.id} paymentMethod={paymentMethod} />
      ))}
    </div>
  );
};
