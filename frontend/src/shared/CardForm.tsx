import { useState } from 'react';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { confirmCardSetup } from '../lib/stripe';

interface CardFormProps {
  clientSecret: string;
  onSuccess: (paymentMethodId: string) => void;
  onError: (error: string) => void;
}

/**
 * Card Form Component
 *
 * Uses Stripe Elements to collect card information securely
 * Confirms the SetupIntent and returns the payment method ID
 */
export const CardForm = ({ clientSecret, onSuccess, onError }: CardFormProps) => {
  const stripe = useStripe();
  const elements = useElements();
  const [isProcessing, setIsProcessing] = useState(false);
  const [cardComplete, setCardComplete] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!stripe || !elements) {
      onError('Stripe has not loaded yet. Please try again.');
      return;
    }

    const cardElement = elements.getElement(CardElement);
    if (!cardElement) {
      onError('Card element not found');
      return;
    }

    setIsProcessing(true);

    try {
      // Confirm the card setup
      const result = await confirmCardSetup(clientSecret, {
        card: cardElement,
        billing_details: {
          name: 'Customer Name', // You can collect this from a form field if needed
        },
      });

      if (result.error) {
        onError(result.error.message || 'Failed to save card');
      } else if (result.setupIntent?.payment_method) {
        // Extract payment method ID
        const paymentMethodId =
          typeof result.setupIntent.payment_method === 'string'
            ? result.setupIntent.payment_method
            : result.setupIntent.payment_method.id;

        onSuccess(paymentMethodId);
      } else {
        onError('No payment method returned from Stripe');
      }
    } catch (error) {
      onError(error instanceof Error ? error.message : 'An error occurred');
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Card Information
        </label>
        <div className="p-3 border border-gray-300 rounded-lg">
          <CardElement
            options={{
              style: {
                base: {
                  fontSize: '16px',
                  color: '#1a1a1a',
                  fontFamily: 'Inter, system-ui, sans-serif',
                  '::placeholder': {
                    color: '#9ca3af',
                  },
                },
                invalid: {
                  color: '#dc2626',
                },
              },
            }}
            onChange={(e) => setCardComplete(e.complete)}
          />
        </div>
      </div>

      <div className="p-3 bg-blue-50 rounded-lg text-sm text-blue-800">
        <p>🔒 Your card information is encrypted and secure. We never store card details.</p>
      </div>

      <button
        type="submit"
        disabled={!stripe || !cardComplete || isProcessing}
        className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
      >
        {isProcessing ? 'Processing...' : 'Save Payment Method'}
      </button>
    </form>
  );
};
