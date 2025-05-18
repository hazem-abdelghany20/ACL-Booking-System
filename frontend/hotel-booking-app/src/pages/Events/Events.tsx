import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import eventService from '../../services/eventService';
import type { Event, Category, EventSearchParams } from '../../types';
import { useAuth } from '../../contexts/AuthContext';

const Events = () => {
  const [events, setEvents] = useState<Event[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  
  // Search filters
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<number | undefined>(undefined);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  
  const { isAuthenticated, hasRole } = useAuth();
  const isAdmin = isAuthenticated && hasRole('ADMIN');
  
  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setLoading(true);
        const params: EventSearchParams = {};
        
        if (searchQuery) params.query = searchQuery;
        if (selectedCategory) params.category = selectedCategory;
        if (startDate) params.startDate = startDate;
        if (endDate) params.endDate = endDate;
        
        const eventsData = await eventService.searchEvents(params);
        setEvents(eventsData);
        setLoading(false);
      } catch (err: any) {
        setError('Failed to load events');
        setLoading(false);
        console.error(err);
      }
    };

    const fetchCategories = async () => {
      try {
        const categoriesData = await eventService.getAllCategories();
        setCategories(categoriesData);
      } catch (err: any) {
        console.error('Failed to load categories:', err);
      }
    };

    fetchEvents();
    fetchCategories();
  }, [searchQuery, selectedCategory, startDate, endDate]);

  const handleSearch = () => {
    // The effect will trigger the search when the state changes
  };

  const resetFilters = () => {
    setSearchQuery('');
    setSelectedCategory(undefined);
    setStartDate('');
    setEndDate('');
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">Events</h1>
        {isAdmin && (
          <Link to="/events/create" className="btn-primary">
            Create Event
          </Link>
        )}
      </div>

      {/* Search and Filters */}
      <div className="bg-gray-100 p-4 rounded-lg mb-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-1">
              Search
            </label>
            <input
              type="text"
              id="search"
              placeholder="Search events..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="form-input"
            />
          </div>
          
          <div>
            <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-1">
              Category
            </label>
            <select
              id="category"
              value={selectedCategory || ''}
              onChange={(e) => setSelectedCategory(e.target.value ? Number(e.target.value) : undefined)}
              className="form-input"
            >
              <option value="">All Categories</option>
              {categories.map(category => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </div>
          
          <div>
            <label htmlFor="startDate" className="block text-sm font-medium text-gray-700 mb-1">
              Start Date
            </label>
            <input
              type="date"
              id="startDate"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="form-input"
            />
          </div>
          
          <div>
            <label htmlFor="endDate" className="block text-sm font-medium text-gray-700 mb-1">
              End Date
            </label>
            <input
              type="date"
              id="endDate"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="form-input"
            />
          </div>
        </div>
        
        <div className="flex justify-end mt-4 space-x-2">
          <button
            onClick={resetFilters}
            className="btn-secondary"
          >
            Reset Filters
          </button>
          <button
            onClick={handleSearch}
            className="btn-primary"
          >
            Search
          </button>
        </div>
      </div>
      
      {/* Events Listing */}
      {loading ? (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
        </div>
      ) : error ? (
        <div className="bg-red-100 text-red-700 p-4 rounded-md">{error}</div>
      ) : events.length === 0 ? (
        <div className="text-center py-12 bg-gray-50 rounded-lg">
          <p className="text-xl text-gray-600">No events found matching your criteria.</p>
          <button
            onClick={resetFilters}
            className="mt-4 btn-secondary"
          >
            Clear Filters
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {events.map((event) => (
            <div key={event.id} className="card">
              <div className="h-48 bg-gray-300">
                {event.imageUrl ? (
                  <img 
                    src={event.imageUrl} 
                    alt={event.name}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center bg-gray-200">
                    <span className="text-gray-500">No image available</span>
                  </div>
                )}
              </div>
              <div className="p-4">
                <h3 className="text-xl font-semibold mb-2">{event.name}</h3>
                <p className="text-gray-600 mb-3 line-clamp-2">{event.description}</p>
                {event.date && (
                  <p className="text-gray-600 mb-2">
                    <span className="font-medium">Date:</span> {new Date(event.date).toLocaleDateString()}
                  </p>
                )}
                {event.venue && (
                  <p className="text-gray-600 mb-2">
                    <span className="font-medium">Venue:</span> {event.venue}
                  </p>
                )}
                <div className="flex justify-between items-center mt-4">
                  <span className="text-blue-600 font-medium">
                    {event.price ? `$${event.price}` : 'Free'}
                  </span>
                  <Link 
                    to={`/events/${event.id}`}
                    className="btn-primary"
                  >
                    View Details
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Events; 