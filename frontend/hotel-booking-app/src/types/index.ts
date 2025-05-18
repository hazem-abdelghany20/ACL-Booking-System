// User related interfaces
export interface User {
  id: number;
  username: string;
  email: string;
  roles?: string[];
}

export interface AuthResponse {
  accessToken: string;
  username: string;
  roles: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
}

// Event related interfaces
export interface Event {
  id: number;
  name: string;
  description: string;
  venue?: string;
  date?: string;
  startTime?: string;
  endTime?: string;
  seats?: number;
  availableSeats?: number;
  imageUrl?: string;
  price?: number;
  categoryId?: number;
  category?: Category;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
}

export interface EventSearchParams {
  query?: string;
  category?: number;
  startDate?: string;
  endDate?: string;
}

// Booking related interfaces
export interface Booking {
  id: number;
  eventId: number;
  userId: number;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
  reservationCode?: string;
  numberOfSeats?: number;
  specialRequirements?: string;
  bookingDate?: string;
  totalPrice?: number;
}

export interface BookingRequest {
  eventId: number;
  numberOfSeats: number;
  specialRequirements?: string;
}

// Notification related interfaces
export interface Notification {
  id: number;
  userId: number;
  message: string;
  read: boolean;
  createdAt?: string;
} 