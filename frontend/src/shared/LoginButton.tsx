import { useAuth } from '../hooks/useAuth';

interface LoginButtonProps {
  className?: string;
  children?: React.ReactNode;
}

/**
 * Login Button Component
 *
 * Redirects user to Keycloak login page
 */
export const LoginButton = ({ className = '', children }: LoginButtonProps) => {
  const { login } = useAuth();

  return (
    <button
      onClick={login}
      className={`px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors ${className}`}
    >
      {children || 'Login'}
    </button>
  );
};
