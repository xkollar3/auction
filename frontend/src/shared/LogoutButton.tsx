import { useAuth } from '../hooks/useAuth';

interface LogoutButtonProps {
  className?: string;
  children?: React.ReactNode;
}

/**
 * Logout Button Component
 *
 * Logs out user and clears session
 */
export const LogoutButton = ({ className = '', children }: LogoutButtonProps) => {
  const { logout } = useAuth();

  return (
    <button
      onClick={logout}
      className={`px-4 py-2 text-gray-700 hover:text-gray-900 transition-colors ${className}`}
    >
      {children || 'Logout'}
    </button>
  );
};
