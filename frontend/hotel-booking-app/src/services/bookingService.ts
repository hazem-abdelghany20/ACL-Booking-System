import api from './api';
import type { Booking, BookingRequest } from '../types';

const BOOKINGS_BASE_URL = '/api/bookings';

export const bookingService = {
  /**
   * Get all bookings for the current user
   */
  getUserBookings: async (): Promise<Booking[]> => {
    const response = await api.get<Booking[]>(BOOKINGS_BASE_URL);
    return response.data;
  },

  /**
   * Get a booking by ID
   */
  getBookingById: async (id: number): Promise<Booking> => {
    const response = await api.get<Booking>(`${BOOKINGS_BASE_URL}/${id}`);
    return response.data;
  },

  /**
   * Create a new booking
   */
  createBooking: async (bookingData: BookingRequest): Promise<Booking> => {
    const response = await api.post<Booking>(BOOKINGS_BASE_URL, bookingData);
    return response.data;
  },

  /**
   * Cancel a booking
   */
  cancelBooking: async (id: number): Promise<Booking> => {
    const response = await api.delete<Booking>(`${BOOKINGS_BASE_URL}/${id}`);
    return response.data;
  },

  /**
   * Update a booking (e.g., modify number of seats)
   */
  updateBooking: async (id: number, bookingData: Partial<BookingRequest>): Promise<Booking> => {
    const response = await api.put<Booking>(`${BOOKINGS_BASE_URL}/${id}`, bookingData);
    return response.data;
  }
};

export default bookingService; 