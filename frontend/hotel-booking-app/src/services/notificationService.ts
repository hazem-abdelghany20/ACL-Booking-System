import api from './api';
import type { Notification } from '../types';

const NOTIFICATIONS_BASE_URL = '/api/notifications';

export const notificationService = {
  /**
   * Get all notifications for the current user
   */
  getUserNotifications: async (): Promise<Notification[]> => {
    const response = await api.get<Notification[]>(NOTIFICATIONS_BASE_URL);
    return response.data;
  },

  /**
   * Mark a notification as read
   */
  markAsRead: async (id: number): Promise<Notification> => {
    const response = await api.put<Notification>(`${NOTIFICATIONS_BASE_URL}/${id}/read`, {});
    return response.data;
  },

  /**
   * Delete a notification
   */
  deleteNotification: async (id: number): Promise<void> => {
    await api.delete(`${NOTIFICATIONS_BASE_URL}/${id}`);
  },

  /**
   * Mark all notifications as read
   */
  markAllAsRead: async (): Promise<void> => {
    await api.put(`${NOTIFICATIONS_BASE_URL}/read-all`, {});
  },

  /**
   * Get unread notification count
   */
  getUnreadCount: async (): Promise<number> => {
    const response = await api.get<{ count: number }>(`${NOTIFICATIONS_BASE_URL}/unread-count`);
    return response.data.count;
  }
};

export default notificationService; 