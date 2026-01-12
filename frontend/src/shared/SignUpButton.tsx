import { useAuth } from '../hooks/useAuth';

interface SignUpButtonProps {
  className?: string;
  children?: React.ReactNode;
}

/**
 * Sign Up Button Component
 *
 * Redirects user to Keycloak registration page
 */
export const SignUpButton = ({ className = '', children }: SignUpButtonProps) => {
  const { register } = useAuth();

  return (
    <button
      onClick={register}
      className={`px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors ${className}`}
    >
      {children || 'Sign Up'}
    </button>
  );
};
