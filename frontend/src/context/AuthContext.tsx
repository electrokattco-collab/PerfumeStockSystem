import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { User } from '@/types';
import api from '@/services/api';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (userData: User) => void;
  logout: () => Promise<void>;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Check authentication status on app initialization
  useEffect(() => {
    const checkAuth = async () => {
      try {
        // Try to get current user from the /api/auth/me endpoint
        // The JWT cookie will be automatically sent with the request
        const response = await api.get('/auth/me');
        if (response.status === 200) {
          setUser(response.data);
        }
      } catch (error) {
        // User is not authenticated or session expired
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    checkAuth();
  }, []);

  /**
   * Called after successful login. Sets the user state.
   * Note: The JWT token is stored in an httpOnly cookie by the backend,
   * so we don't need to store it locally.
   */
  const login = (userData: User) => {
    setUser(userData);
  };

  /**
   * Logs out the user by calling the backend logout endpoint.
   * The backend clears the httpOnly cookie.
   */
  const logout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      console.error('Logout failed:', error);
    } finally {
      setUser(null);
    }
  };

  const hasRole = (role: string) => {
    return user?.role === role;
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
