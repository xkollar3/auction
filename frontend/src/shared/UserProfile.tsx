import { User, Mail, Phone } from 'lucide-react';
import { type ProfileData } from '../types/profile';

interface UserProfileProps {
  profile: ProfileData;
}

/**
 * User Profile Component
 *
 * Displays user profile information in a read-only card format
 */
export const UserProfile = ({ profile }: UserProfileProps) => {
  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-2xl font-bold text-gray-900 mb-6">Profile Information</h2>

      <div className="space-y-4">
        {/* Name */}
        <div className="flex items-start gap-3 pb-4 border-b border-gray-200">
          <User className="h-5 w-5 text-gray-400 mt-0.5" />
          <div className="flex-1">
            <label className="block text-sm font-medium text-gray-500 mb-1">Full Name</label>
            <p className="text-lg text-gray-900">{profile.name}</p>
          </div>
        </div>

        {/* Email */}
        <div className="flex items-start gap-3 pb-4 border-b border-gray-200">
          <Mail className="h-5 w-5 text-gray-400 mt-0.5" />
          <div className="flex-1">
            <label className="block text-sm font-medium text-gray-500 mb-1">Email Address</label>
            <p className="text-lg text-gray-900">{profile.email}</p>
            {profile.emailVerified !== undefined && (
              <p className={`text-sm mt-1 ${profile.emailVerified ? 'text-green-600' : 'text-amber-600'}`}>
                {profile.emailVerified ? 'Email is verified' : 'Please verify your email'}
              </p>
            )}
          </div>
        </div>

        {/* Phone */}
        {profile.phone && (
          <div className="flex items-start gap-3 pb-4 border-b border-gray-200">
            <Phone className="h-5 w-5 text-gray-400 mt-0.5" />
            <div className="flex-1">
              <label className="block text-sm font-medium text-gray-500 mb-1">Phone Number</label>
              <p className="text-lg text-gray-900">{profile.phone}</p>
            </div>
          </div>
        )}

        {/* Account ID (for reference) */}
        <div className="flex items-start gap-3 pt-2">
          <div className="flex-1">
            <label className="block text-sm font-medium text-gray-500 mb-1">Account ID</label>
            <p className="text-sm text-gray-600 font-mono">{profile.id}</p>
          </div>
        </div>
      </div>

      <div className="mt-6 p-4 bg-blue-50 rounded-lg">
        <p className="text-sm text-blue-800">
          ℹ️ Profile information is sourced from your authentication provider and is read-only.
          To update your details, please manage your account through the authentication provider.
        </p>
      </div>
    </div>
  );
};
