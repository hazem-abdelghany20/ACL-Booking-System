import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import eventService from '../services/eventService';
import type { Event } from '../types';

const Home = () => {
  const [featuredEvents, setFeaturedEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const events = await eventService.getPublicEvents();
        // Get up to 3 featured events
        setFeaturedEvents(events.slice(0, 3));
        setLoading(false);
      } catch (err: any) {
        setError('Failed to load events');
        setLoading(false);
        console.error(err);
      }
    };

    fetchEvents();
  }, []);

  return (
    <div>
      {/* Hero Section */}
      <section className="bg-blue-600 text-white py-16 rounded-lg mb-12">
        <div className="container mx-auto px-4">
          <div className="max-w-3xl">
            <h1 className="text-4xl md:text-5xl font-bold mb-6">Find and Book Amazing Events</h1>
            <p className="text-xl mb-8">
              Discover conferences, workshops, and special events at the best venues with our easy booking system.
            </p>
            <Link
              to="/events"
              className="inline-block px-6 py-3 bg-white text-blue-600 font-semibold rounded-md hover:bg-gray-100 transition-colors"
            >
              Find Events
            </Link>
          </div>
        </div>
      </section>

      {/* Featured Events Section */}
      <section className="mb-12">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold mb-8">Featured Events</h2>
          
          {loading ? (
            <div className="flex justify-center items-center h-64">
              <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
            </div>
          ) : error ? (
            <div className="bg-red-100 text-red-700 p-4 rounded-md">{error}</div>
          ) : featuredEvents.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-gray-500">No events available at the moment.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {featuredEvents.map((event) => (
                <div key={event.id} className="card overflow-hidden">
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
                    <div className="flex justify-between items-center">
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

          <div className="text-center mt-8">
            <Link
              to="/events"
              className="inline-block px-6 py-3 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 transition-colors"
            >
              View All Events
            </Link>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section className="bg-gray-100 py-12 rounded-lg mb-12">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold mb-8 text-center">How It Works</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {/* Step 1 */}
            <div className="text-center">
              <div className="w-16 h-16 bg-blue-600 text-white rounded-full flex items-center justify-center mx-auto mb-4 text-2xl font-bold">1</div>
              <h3 className="text-xl font-semibold mb-2">Find an Event</h3>
              <p className="text-gray-600">
                Browse our collection of events or use search filters to find the perfect event.
              </p>
            </div>
            {/* Step 2 */}
            <div className="text-center">
              <div className="w-16 h-16 bg-blue-600 text-white rounded-full flex items-center justify-center mx-auto mb-4 text-2xl font-bold">2</div>
              <h3 className="text-xl font-semibold mb-2">Book Your Spot</h3>
              <p className="text-gray-600">
                Secure your spot by making a booking with your desired number of seats.
              </p>
            </div>
            {/* Step 3 */}
            <div className="text-center">
              <div className="w-16 h-16 bg-blue-600 text-white rounded-full flex items-center justify-center mx-auto mb-4 text-2xl font-bold">3</div>
              <h3 className="text-xl font-semibold mb-2">Receive Confirmation</h3>
              <p className="text-gray-600">
                Get instant confirmation and event details sent to your email.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Home; 