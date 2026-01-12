import { useNavigate } from 'react-router-dom';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { PaymentMethodsList } from '../shared/PaymentMethodsList';
import { usePaymentMethods } from '../hooks/usePaymentMethods';
import { useAuth } from '../hooks/useAuth';
import { Plus } from 'lucide-react';

/**
 * Payment Methods Page
 *
 * Displays user's saved payment methods
 */
export const PaymentMethodsPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { paymentMethods, isLoadingMethods } = usePaymentMethods(user?.customerId);

  const hasCustomerId = !!user?.customerId;

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Payment Methods</h1>
            <p className="text-gray-600 mt-2">Manage your saved payment methods</p>
          </div>
          {hasCustomerId && (
            <button
              onClick={() => navigate('/profile/payments/setup')}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              <Plus className="h-4 w-4" />
              Add Payment Method
            </button>
          )}
        </div>

        <div className="bg-white rounded-lg shadow-md p-6">
          {!hasCustomerId ? (
            <div className="text-center py-12">
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
          ) : (
            <PaymentMethodsList
              paymentMethods={paymentMethods}
              isLoading={isLoadingMethods}
            />
          )}
        </div>
      </main>
      <Footer />
    </div>
  );
};
