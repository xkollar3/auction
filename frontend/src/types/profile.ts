/**
 * Profile data interface
 *
 * Read-only user profile information displayed on profile page
 */
export interface ProfileData {
  id: string;
  email: string;
  name: string;
  phone?: string;
  emailVerified?: boolean;
  createdAt?: string;
}
