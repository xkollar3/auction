import { useSeller } from '../hooks/useSeller';
import { ExternalLink } from 'lucide-react';

interface BecomeSellerButtonProps {
  className?: string;
}

/**
 * Become a Seller Button
 *
 * Creates a Stripe Connect seller account and displays onboarding/dashboard links
 */
export const BecomeSellerButton = ({ className = '' }: BecomeSellerButtonProps) => {
  const { createSellerAccount, isCreating, sellerData, error } = useSeller();

  const handleClick = () => {
    createSellerAccount();
  };

  if (sellerData) {
    return (
      <div className="space-y-3">
        <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
          <p className="text-sm font-medium text-green-800 mb-2">
            ✓ Seller account created successfully!
          </p>
          <p className="text-xs text-green-700 mb-3">
            Seller ID: <span className="font-mono">{sellerData.sellerId}</span>
          </p>
          <div className="space-y-2">
            <a
              href={sellerData.onboardingUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm"
            >
              <ExternalLink className="h-4 w-4" />
              Complete Stripe Onboarding
            </a>
            <a
              href={sellerData.dashboardUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-2 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors text-sm"
            >
              <ExternalLink className="h-4 w-4" />
              View Stripe Dashboard
            </a>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <button
        onClick={handleClick}
        disabled={isCreating}
        className={`w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed ${className}`}
      >
        {isCreating ? 'Creating Seller Account...' : 'Become a Seller'}
      </button>
      {error && (
        <p className="text-sm text-red-600">
          Error: {error instanceof Error ? error.message : 'Failed to create seller account'}
        </p>
      )}
    </div>
  );
};
