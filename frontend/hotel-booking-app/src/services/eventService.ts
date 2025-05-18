import api from './api';
import type { Event, EventSearchParams, Category } from '../types';

const EVENTS_BASE_URL = '/api/events';
const CATEGORIES_BASE_URL = '/api/categories';

export const eventService = {
  /**
   * Get all events
   */
  getAllEvents: async (): Promise<Event[]> => {
    const response = await api.get<Event[]>(EVENTS_BASE_URL);
    return response.data;
  },

  /**
   * Get public events (no authentication required)
   */
  getPublicEvents: async (): Promise<Event[]> => {
    const response = await api.get<Event[]>(`${EVENTS_BASE_URL}/public`);
    return response.data;
  },

  /**
   * Get a single event by ID
   */
  getEventById: async (id: number): Promise<Event> => {
    const response = await api.get<Event>(`${EVENTS_BASE_URL}/${id}`);
    return response.data;
  },

  /**
   * Search events with various filters
   */
  searchEvents: async (params: EventSearchParams): Promise<Event[]> => {
    const response = await api.get<Event[]>(EVENTS_BASE_URL, { params });
    return response.data;
  },

  /**
   * Create a new event (requires authentication)
   */
  createEvent: async (eventData: Partial<Event>): Promise<Event> => {
    const response = await api.post<Event>(EVENTS_BASE_URL, eventData);
    return response.data;
  },

  /**
   * Update an existing event (requires authentication)
   */
  updateEvent: async (id: number, eventData: Partial<Event>): Promise<Event> => {
    const response = await api.put<Event>(`${EVENTS_BASE_URL}/${id}`, eventData);
    return response.data;
  },

  /**
   * Delete an event (requires authentication)
   */
  deleteEvent: async (id: number): Promise<void> => {
    await api.delete(`${EVENTS_BASE_URL}/${id}`);
  },

  /**
   * Get all categories
   */
  getAllCategories: async (): Promise<Category[]> => {
    const response = await api.get<Category[]>(CATEGORIES_BASE_URL);
    return response.data;
  },

  /**
   * Get a category by ID
   */
  getCategoryById: async (id: number): Promise<Category> => {
    const response = await api.get<Category>(`${CATEGORIES_BASE_URL}/${id}`);
    return response.data;
  }
};

export default eventService; 