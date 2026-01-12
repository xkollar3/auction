import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { AddressForm } from '../shared/AddressForm';
import { CardForm } from '../shared/CardForm';
import { useCustomer } from '../hooks/useCustomer';
import { usePaymentMethods } from '../hooks/usePaymentMethods';
import { useAuth } from '../hooks/useAuth';
import { type CreateStripeCustomerRequest } from '../types/stripe';
import { CheckCircle } from 'lucide-react';

/**
 * Payment Setup Page
 *
 * Multi-step flow for setting up buyer payment methods:
 * 1. Collect address and create Stripe customer
 * 2. Create SetupIntent
 * 3. Collect card details via Stripe Elements
 * 4. Attach payment method to customer
 */
export const PaymentSetupPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { createCustomer, isCreating, customerData } = useCustomer();
  const {
    createSetupIntent,
    isCreatingSetupIntent,
    setupIntentData,
    attachPaymentMethod,
    isAttachingPaymentMethod,
  } = usePaymentMethods(user?.customerId);

  const [step, setStep] = useState<'address' | 'card' | 'success'>('address');
  const [error, setError] = useState<string | null>(null);

  // Step 1: Handle address submission
  const handleAddressSubmit = (address: CreateStripeCustomerRequest) => {
    setError(null);
    createCustomer(address, {
      onSuccess: (data) => {
        console.log('Customer created, creating setup intent...');
        // Automatically create setup intent after customer creation
        createSetupIntent(data.customerId, {
          onSuccess: () => {
            console.log('Setup intent created, moving to card step');
            setStep('card');
          },
          onError: (err) => {
            setError(err instanceof Error ? err.message : 'Failed to create setup intent');
          },
        });
      },
      onError: (err) => {
        setError(err instanceof Error ? err.message : 'Failed to create customer');
      },
    });
  };

  // Step 2: Handle card confirmation success
  const handleCardSuccess = (paymentMethodId: string) => {
    setError(null);
    console.log('Card confirmed, attaching payment method:', paymentMethodId);

    attachPaymentMethod(paymentMethodId, {
      onSuccess: () => {
        console.log('Payment method attached successfully');
        setStep('success');
      },
      onError: (err) => {
        setError(err instanceof Error ? err.message : 'Failed to attach payment method');
      },
    });
  };

  // Step 2: Handle card errors
  const handleCardError = (errorMessage: string) => {
    setError(errorMessage);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Payment Method Setup</h1>
          <p className="text-gray-600 mt-2">
            {step === 'address' && 'Enter your address to create your buyer account'}
            {step === 'card' && 'Add a payment method to your account'}
            {step === 'success' && 'Payment method added successfully'}
          </p>
        </div>

        {/* Progress Steps */}
        <div className="mb-8">
          <div className="flex items-center justify-center gap-4">
            <div className={`flex items-center gap-2 ${step !== 'address' ? 'text-green-600' : 'text-blue-600'}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${step !== 'address' ? 'bg-green-100' : 'bg-blue-100'}`}>
                {step !== 'address' ? <CheckCircle className="h-5 w-5" /> : '1'}
              </div>
              <span className="text-sm font-medium">Address</span>
            </div>
            <div className="w-16 h-0.5 bg-gray-300"></div>
            <div className={`flex items-center gap-2 ${step === 'success' ? 'text-green-600' : step === 'card' ? 'text-blue-600' : 'text-gray-400'}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${step === 'success' ? 'bg-green-100' : step === 'card' ? 'bg-blue-100' : 'bg-gray-100'}`}>
                {step === 'success' ? <CheckCircle className="h-5 w-5" /> : '2'}
              </div>
              <span className="text-sm font-medium">Card Details</span>
            </div>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm text-red-800">⚠️ {error}</p>
          </div>
        )}

        {/* Step Content */}
        <div className="bg-white rounded-lg shadow-md p-6">
          {step === 'address' && (
            <AddressForm
              onSubmit={handleAddressSubmit}
              isSubmitting={isCreating || isCreatingSetupIntent}
            />
          )}

          {step === 'card' && setupIntentData && (
            <CardForm
              clientSecret={setupIntentData.clientSecret}
              onSuccess={handleCardSuccess}
              onError={handleCardError}
            />
          )}

          {step === 'success' && (
            <div className="text-center py-8">
              <div className="mb-6">
                <CheckCircle className="h-16 w-16 text-green-600 mx-auto" />
              </div>
              <h2 className="text-2xl font-bold text-gray-900 mb-2">
                Payment Method Added!
              </h2>
              <p className="text-gray-600 mb-6">
                Your payment method has been saved securely. You can now place bids and make purchases.
              </p>
              <div className="space-y-3">
                <button
                  onClick={() => navigate('/profile/payments')}
                  className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  View Payment Methods
                </button>
                <button
                  onClick={() => navigate('/listings')}
                  className="w-full px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  Start Bidding
                </button>
              </div>
            </div>
          )}

          {(isCreating || isCreatingSetupIntent || isAttachingPaymentMethod) && (
            <div className="mt-4 flex items-center justify-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <p className="ml-3 text-gray-600">Processing...</p>
            </div>
          )}
        </div>

        {/* Helper Text */}
        {step === 'card' && (
          <div className="mt-6 p-4 bg-blue-50 rounded-lg text-sm text-blue-800">
            <p className="font-medium mb-1">Test Card Numbers (Development):</p>
            <ul className="list-disc list-inside space-y-1 text-xs">
              <li>Success: 4242 4242 4242 4242</li>
              <li>Requires authentication: 4000 0027 6000 3184</li>
              <li>Any future expiry date and any 3-digit CVC</li>
            </ul>
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
};
