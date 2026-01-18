import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { getUserProfile } from '../api/user';
import { CreditCard, CheckCircle } from 'lucide-react';

/**
 * Payment Methods Page
 *
 * Displays user's payment method status
 * Note: Card details cannot be fetched with publishable key
 */
export const PaymentMethodsPage = () => {
  const navigate = useNavigate();

  const { data: profile, isLoading } = useQuery({
    queryKey: ['userProfile'],
    queryFn: getUserProfile,
  });

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Payment Methods</h1>
          <p className="text-gray-600 mt-2">Your payment information</p>
        </div>

        <div className="bg-white rounded-lg shadow-md p-6">
          {isLoading ? (
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>
          ) : !profile?.stripeCustomerId ? (
            <div className="text-center py-12">
              <CreditCard className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                No Buyer Account
              </h3>
              <p className="text-gray-600 mb-6">
                Create a buyer account to add payment methods and start bidding
              </p>
              <button
                onClick={() => navigate('/profile/payments/setup')}
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
              >
                Set Up Buyer Account
              </button>
            </div>
          ) : profile?.stripePaymentMethodId ? (
            <div className="py-8">
              <div className="flex items-center justify-center mb-6">
                <div className="bg-green-100 rounded-full p-3">
                  <CheckCircle className="h-8 w-8 text-green-600" />
                </div>
              </div>
              <h3 className="text-lg font-medium text-gray-900 text-center mb-2">
                Payment Method Configured
              </h3>
              <p className="text-gray-600 text-center mb-6">
                Your payment method is set up and ready to use for bidding and purchases.
              </p>
              <div className="bg-gray-50 rounded-lg p-4 max-w-md mx-auto">
                <div className="flex items-center gap-3">
                  <CreditCard className="h-6 w-6 text-gray-500" />
                  <div>
                    <p className="text-sm font-medium text-gray-900">Payment Method ID</p>
                    <p className="text-xs text-gray-500 font-mono">{profile.stripePaymentMethodId}</p>
                  </div>
                </div>
              </div>
              <p className="text-xs text-gray-400 text-center mt-4">
                Card details are securely stored by Stripe and cannot be displayed here.
              </p>
            </div>
          ) : (
            <div className="text-center py-12">
              <CreditCard className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                No Payment Method
              </h3>
              <p className="text-gray-600 mb-6">
                Add a payment method to start bidding on auctions
              </p>
              <button
                onClick={() => navigate('/profile/payments/setup')}
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
              >
                Add Payment Method
              </button>
            </div>
          )}
        </div>

        <button
          onClick={() => navigate('/profile')}
          className="mt-6 text-blue-600 hover:text-blue-700 text-sm"
        >
          Back to Profile
        </button>
      </main>
      <Footer />
    </div>
  );
};
