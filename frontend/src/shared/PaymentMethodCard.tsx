import { CreditCard } from 'lucide-react';
import { type PaymentMethod } from '../types/stripe';

interface PaymentMethodCardProps {
  paymentMethod: PaymentMethod;
}

/**
 * Payment Method Card Component
 *
 * Displays a single payment method with card brand, last 4 digits, and expiry
 */
export const PaymentMethodCard = ({ paymentMethod }: PaymentMethodCardProps) => {
  const { brand, last4, expMonth, expYear, isDefault } = paymentMethod;

  // Card brand icon color
  const brandColors: Record<string, string> = {
    visa: 'text-blue-600',
    mastercard: 'text-orange-600',
    amex: 'text-blue-700',
    discover: 'text-orange-500',
  };

  const brandColor = brandColors[brand.toLowerCase()] || 'text-gray-600';

  return (
    <div className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:border-gray-300 transition-colors">
      <div className="flex items-center gap-3">
        <div className={`p-2 bg-gray-100 rounded ${brandColor}`}>
          <CreditCard className="h-6 w-6" />
        </div>
        <div>
          <div className="flex items-center gap-2">
            <p className="font-medium text-gray-900 capitalize">
              {brand} •••• {last4}
            </p>
            {isDefault && (
              <span className="px-2 py-0.5 text-xs font-medium bg-blue-100 text-blue-800 rounded">
                Default
              </span>
            )}
          </div>
          <p className="text-sm text-gray-500">
            Expires {String(expMonth).padStart(2, '0')}/{expYear}
          </p>
        </div>
      </div>
    </div>
  );
};
