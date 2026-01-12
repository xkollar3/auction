import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createCustomerSchema } from '../schemas/customer';
import { type CreateStripeCustomerRequest } from '../types/stripe';

interface AddressFormProps {
  onSubmit: (data: CreateStripeCustomerRequest) => void;
  isSubmitting?: boolean;
}

/**
 * Address Form Component
 *
 * Collects customer address for Stripe customer creation
 */
export const AddressForm = ({ onSubmit, isSubmitting = false }: AddressFormProps) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CreateStripeCustomerRequest>({
    resolver: zodResolver(createCustomerSchema),
    defaultValues: {
      country: 'US',
    },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label htmlFor="line1" className="block text-sm font-medium text-gray-700 mb-1">
          Address Line 1 *
        </label>
        <input
          {...register('line1')}
          type="text"
          id="line1"
          placeholder="123 Main Street"
          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        {errors.line1 && (
          <p className="mt-1 text-sm text-red-600">{errors.line1.message}</p>
        )}
      </div>

      <div>
        <label htmlFor="line2" className="block text-sm font-medium text-gray-700 mb-1">
          Address Line 2
        </label>
        <input
          {...register('line2')}
          type="text"
          id="line2"
          placeholder="Apt 4B (optional)"
          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label htmlFor="city" className="block text-sm font-medium text-gray-700 mb-1">
            City *
          </label>
          <input
            {...register('city')}
            type="text"
            id="city"
            placeholder="New York"
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          {errors.city && (
            <p className="mt-1 text-sm text-red-600">{errors.city.message}</p>
          )}
        </div>

        <div>
          <label htmlFor="state" className="block text-sm font-medium text-gray-700 mb-1">
            State/Province *
          </label>
          <input
            {...register('state')}
            type="text"
            id="state"
            placeholder="NY"
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          {errors.state && (
            <p className="mt-1 text-sm text-red-600">{errors.state.message}</p>
          )}
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label htmlFor="postalCode" className="block text-sm font-medium text-gray-700 mb-1">
            Postal Code *
          </label>
          <input
            {...register('postalCode')}
            type="text"
            id="postalCode"
            placeholder="10001"
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          {errors.postalCode && (
            <p className="mt-1 text-sm text-red-600">{errors.postalCode.message}</p>
          )}
        </div>

        <div>
          <label htmlFor="country" className="block text-sm font-medium text-gray-700 mb-1">
            Country Code *
          </label>
          <input
            {...register('country')}
            type="text"
            id="country"
            placeholder="US"
            maxLength={2}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          {errors.country && (
            <p className="mt-1 text-sm text-red-600">{errors.country.message}</p>
          )}
        </div>
      </div>

      <button
        type="submit"
        disabled={isSubmitting}
        className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
      >
        {isSubmitting ? 'Creating Account...' : 'Continue to Payment Setup'}
      </button>
    </form>
  );
};
