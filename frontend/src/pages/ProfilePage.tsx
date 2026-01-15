import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../hooks/useAuth';
import { UserProfile } from '../shared/UserProfile';
import { BecomeSellerButton } from '../shared/BecomeSellerButton';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { getUserProfile } from '../api/user';

/**
 * Profile Page
 *
 * Displays user profile information after authentication
 */
export const ProfilePage = () => {
  const navigate = useNavigate();
  const { getUserProfile: getKeycloakProfile, user } = useAuth();
  const keycloakProfile = getKeycloakProfile();

  const { data: backendProfile, isLoading, error } = useQuery({
    queryKey: ['userProfile'],
    queryFn: getUserProfile,
  });

  if (!keycloakProfile || !user) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="flex items-center justify-center py-20">
          <div className="text-center">
            <p className="text-gray-600">Loading profile...</p>
          </div>
        </div>
        <Footer />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">My Account</h1>
          <p className="text-gray-600 mt-2">Manage your profile and account settings</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Profile Information */}
          <div className="lg:col-span-2 space-y-6">
            <UserProfile profile={keycloakProfile} />

            {/* Backend Profile Details */}
            <div className="bg-white rounded-lg shadow-md p-6">
              <h2 className="text-xl font-bold text-gray-900 mb-4">Account Details</h2>

              {isLoading && (
                <p className="text-gray-600">Loading account details...</p>
              )}

              {error && (
                <p className="text-red-600">Failed to load account details</p>
              )}

              {backendProfile && (
                <div className="space-y-4">
                  <div className="pb-3 border-b border-gray-200">
                    <label className="block text-sm font-medium text-gray-500 mb-1">User ID</label>
                    <p className="text-sm text-gray-900 font-mono">{backendProfile.id}</p>
                  </div>

                  <div className="pb-3 border-b border-gray-200">
                    <label className="block text-sm font-medium text-gray-500 mb-1">Customer ID</label>
                    <p className="text-sm text-gray-900 font-mono">
                      {backendProfile.stripeCustomerId || <span className="text-gray-400">Not set up</span>}
                    </p>
                  </div>

                  <div className="pb-3 border-b border-gray-200">
                    <label className="block text-sm font-medium text-gray-500 mb-1">Payment Method</label>
                    <p className="text-sm text-gray-900 font-mono">
                      {backendProfile.stripePaymentMethodId || <span className="text-gray-400">No payment method</span>}
                    </p>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-500 mb-1">Seller Account</label>
                    {backendProfile.stripeSellerAccountId ? (
                      <div>
                        <p className="text-sm text-gray-900 font-mono">{backendProfile.stripeSellerAccountId}</p>
                        <span className={`inline-block mt-1 text-xs px-2 py-0.5 rounded ${
                          backendProfile.sellerAccountEnabled
                            ? 'bg-green-100 text-green-700'
                            : 'bg-yellow-100 text-yellow-700'
                        }`}>
                          {backendProfile.sellerAccountEnabled ? 'Enabled' : 'Pending Onboarding'}
                        </span>
                      </div>
                    ) : (
                      <p className="text-sm text-gray-400">Not a seller</p>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Quick Actions */}
          <div className="space-y-4">
            <div className="bg-white rounded-lg shadow-md p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h3>
              <div className="space-y-2">
                {backendProfile?.stripePaymentMethodId ? (
                  <div className="w-full px-4 py-2 bg-gray-50 rounded-lg">
                    <div className="flex items-center justify-between">
                      <span className="text-gray-700">Payment Method</span>
                      <span className="text-green-600 text-sm font-medium">Set Up</span>
                    </div>
                    <p className="text-xs text-gray-500 mt-1">
                      Your payment method is configured and ready to use.
                    </p>
                  </div>
                ) : (
                  <button
                    onClick={() => navigate('/profile/payments/setup')}
                    className="w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
                  >
                    Set Up Payment Method
                  </button>
                )}
                {!backendProfile?.stripeSellerAccountId && <BecomeSellerButton />}
                <button className="w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors">
                  My Bids
                </button>
                <button className="w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors">
                  My Orders
                </button>
              </div>
            </div>

            {/* Account Status */}
            <div className="bg-white rounded-lg shadow-md p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Account Status</h3>
              <div className="space-y-3">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">Buyer Account</span>
                  <span className={`text-sm font-medium ${backendProfile?.stripeCustomerId ? 'text-green-600' : 'text-gray-400'}`}>
                    {backendProfile?.stripeCustomerId ? 'Active' : 'Not Set Up'}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">Payment Method</span>
                  <span className={`text-sm font-medium ${backendProfile?.stripePaymentMethodId ? 'text-green-600' : 'text-gray-400'}`}>
                    {backendProfile?.stripePaymentMethodId ? 'Configured' : 'Not Set Up'}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">Seller Account</span>
                  <span className={`text-sm font-medium ${
                    backendProfile?.sellerAccountEnabled
                      ? 'text-green-600'
                      : backendProfile?.stripeSellerAccountId
                        ? 'text-yellow-600'
                        : 'text-gray-400'
                  }`}>
                    {backendProfile?.sellerAccountEnabled
                      ? 'Active'
                      : backendProfile?.stripeSellerAccountId
                        ? 'Pending'
                        : 'Not Set Up'}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
};
