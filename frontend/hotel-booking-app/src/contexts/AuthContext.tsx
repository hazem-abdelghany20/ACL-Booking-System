import { createContext, useState, useEffect, useContext } from 'react';
import type { ReactNode } from 'react';
import authService from '../services/authService';
import type { User, LoginRequest, SignupRequest } from '../types';

interface AuthContextType {
  isAuthenticated: boolean;
  user: User | null;
  loading: boolean;
  error: string | null;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: SignupRequest) => Promise<void>;
  logout: () => void;
  clearError: () => void;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(authService.isLoggedIn());
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // On mount, check if user is already logged in
  useEffect(() => {
    const initializeAuth = async () => {
      if (authService.isLoggedIn()) {
        try {
          const userData = await authService.getCurrentUser();
          setUser(userData);
          setIsAuthenticated(true);
        } catch (err) {
          console.error('Failed to fetch user data:', err);
          authService.logout();
          setIsAuthenticated(false);
        }
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (credentials: LoginRequest) => {
    setLoading(true);
    setError(null);
    try {
      await authService.login(credentials);
      const userData = await authService.getCurrentUser();
      setUser(userData);
      setIsAuthenticated(true);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed. Please check your credentials.');
      setIsAuthenticated(false);
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData: SignupRequest) => {
    setLoading(true);
    setError(null);
    try {
      await authService.register(userData);
      // Successfully registered, now login
      await login({
        username: userData.username,
        password: userData.password
      });
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
      setLoading(false);
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setIsAuthenticated(false);
  };

  const clearError = () => {
    setError(null);
  };

  const hasRole = (role: string) => {
    return authService.hasRole(role);
  };

  const value = {
    isAuthenticated,
    user,
    loading,
    error,
    login,
    register,
    logout,
    clearError,
    hasRole
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export default AuthContext; 