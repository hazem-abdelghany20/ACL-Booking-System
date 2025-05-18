import api from './api';
import type { AuthResponse, LoginRequest, SignupRequest, User } from '../types';

const AUTH_BASE_URL = '/api/auth';

export const authService = {
  /**
   * Login with username and password
   */
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>(`${AUTH_BASE_URL}/signin`, credentials);
    // Store the JWT token and user info in localStorage
    if (response.data.accessToken) {
      localStorage.setItem('token', response.data.accessToken);
      localStorage.setItem('user', JSON.stringify({
        username: response.data.username,
        roles: response.data.roles
      }));
    }
    return response.data;
  },

  /**
   * Register a new user
   */
  register: async (userData: SignupRequest): Promise<{ message: string }> => {
    const response = await api.post<{ message: string }>(`${AUTH_BASE_URL}/signup`, userData);
    return response.data;
  },

  /**
   * Logout the current user
   */
  logout: (): void => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  /**
   * Get the current user profile
   */
  getCurrentUser: async (): Promise<User> => {
    const response = await api.get<User>('/api/users/profile');
    return response.data;
  },

  /**
   * Check if user is logged in
   */
  isLoggedIn: (): boolean => {
    return !!localStorage.getItem('token');
  },

  /**
   * Get current user roles
   */
  getUserRoles: (): string[] => {
    const user = localStorage.getItem('user');
    if (!user) return [];
    return JSON.parse(user).roles || [];
  },

  /**
   * Check if the current user has a specific role
   */
  hasRole: (role: string): boolean => {
    const roles = authService.getUserRoles();
    return roles.includes(role);
  }
};

export default authService; 