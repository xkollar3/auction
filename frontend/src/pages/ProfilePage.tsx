import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { UserProfile } from '../shared/UserProfile';
import { BecomeSellerButton } from '../shared/BecomeSellerButton';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';

/**
 * Profile Page
 *
 * Displays user profile information after authentication
 */
export const ProfilePage = () => {
  const navigate = useNavigate();
  const { getUserProfile, user } = useAuth();
  const profile = getUserProfile();

  if (!profile || !user) {
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
          <div className="lg:col-span-2">
            <UserProfile profile={profile} />
          </div>

          {/* Quick Actions */}
          <div className="space-y-4">
            <div className="bg-white rounded-lg shadow-md p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h3>
              <div className="space-y-2">
                <button
                  onClick={() => navigate('/profile/payments')}
                  className="w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
                >
                  Payment Methods
                </button>
                {!user?.sellerId && <BecomeSellerButton />}
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
                  <span className="text-sm font-medium text-green-600">
                    {user.customerId ? 'Active' : 'Not Set Up'}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">Seller Account</span>
                  <span className="text-sm font-medium text-green-600">
                    {user.sellerId ? 'Active' : 'Not Set Up'}
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
