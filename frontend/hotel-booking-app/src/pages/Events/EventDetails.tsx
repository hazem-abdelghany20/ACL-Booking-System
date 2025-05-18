import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import eventService from '../../services/eventService';
import type { Event } from '../../types';
import { useAuth } from '../../contexts/AuthContext';

const EventDetails = () => {
  const { id } = useParams<{ id: string }>();
  const [event, setEvent] = useState<Event | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  
  const { isAuthenticated, hasRole } = useAuth();
  const navigate = useNavigate();
  
  const isAdmin = isAuthenticated && hasRole('ADMIN');
  
  useEffect(() => {
    const fetchEvent = async () => {
      if (!id) return;
      
      try {
        setLoading(true);
        const eventData = await eventService.getEventById(Number(id));
        setEvent(eventData);
        setLoading(false);
      } catch (err: any) {
        setError('Failed to load event details');
        setLoading(false);
        console.error(err);
      }
    };
    
    fetchEvent();
  }, [id]);
  
  const handleDelete = async () => {
    if (!id || !event) return;
    
    if (window.confirm(`Are you sure you want to delete "${event.name}"?`)) {
      try {
        await eventService.deleteEvent(Number(id));
        navigate('/events');
      } catch (err: any) {
        setError('Failed to delete event');
        console.error(err);
      }
    }
  };
  
  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
      </div>
    );
  }
  
  if (error) {
    return <div className="bg-red-100 text-red-700 p-4 rounded-md">{error}</div>;
  }
  
  if (!event) {
    return <div className="bg-yellow-100 text-yellow-700 p-4 rounded-md">Event not found</div>;
  }
  
  return (
    <div>
      <div className="mb-8">
        <Link to="/events" className="text-blue-600 hover:underline flex items-center">
          ← Back to events
        </Link>
      </div>
      
      <div className="bg-white rounded-lg shadow-md overflow-hidden">
        {/* Header Image */}
        <div className="h-64 bg-gray-300">
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
        
        {/* Event Details */}
        <div className="p-6">
          <div className="flex flex-col md:flex-row md:justify-between md:items-start mb-6">
            <div>
              <h1 className="text-3xl font-bold mb-2">{event.name}</h1>
              {event.category && (
                <div className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm inline-block mb-4">
                  {event.category.name}
                </div>
              )}
            </div>
            <div className="mt-4 md:mt-0">
              <p className="text-2xl font-bold text-blue-600">
                {event.price ? `$${event.price}` : 'Free'}
              </p>
              {event.availableSeats !== undefined && (
                <p className="text-sm text-gray-600 mt-1">
                  {event.availableSeats} seats available
                </p>
              )}
            </div>
          </div>
          
          {/* Description */}
          <div className="mb-8">
            <h2 className="text-xl font-semibold mb-2">Description</h2>
            <p className="text-gray-700 whitespace-pre-line">{event.description}</p>
          </div>
          
          {/* Event Information */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
            <div>
              <h2 className="text-xl font-semibold mb-4">Event Information</h2>
              <ul className="space-y-3">
                {event.date && (
                  <li className="flex items-start">
                    <span className="font-medium w-24">Date:</span>
                    <span>{new Date(event.date).toLocaleDateString()}</span>
                  </li>
                )}
                {event.startTime && (
                  <li className="flex items-start">
                    <span className="font-medium w-24">Start Time:</span>
                    <span>{event.startTime}</span>
                  </li>
                )}
                {event.endTime && (
                  <li className="flex items-start">
                    <span className="font-medium w-24">End Time:</span>
                    <span>{event.endTime}</span>
                  </li>
                )}
                {event.venue && (
                  <li className="flex items-start">
                    <span className="font-medium w-24">Venue:</span>
                    <span>{event.venue}</span>
                  </li>
                )}
                {event.seats !== undefined && (
                  <li className="flex items-start">
                    <span className="font-medium w-24">Total Seats:</span>
                    <span>{event.seats}</span>
                  </li>
                )}
              </ul>
            </div>
          </div>
          
          {/* Actions */}
          <div className="flex space-x-4">
            {isAuthenticated && (
              <Link 
                to={`/events/${event.id}/book`}
                className="btn-primary flex-1 text-center"
              >
                Book Now
              </Link>
            )}
            
            {isAdmin && (
              <>
                <Link 
                  to={`/events/${event.id}/edit`}
                  className="btn-secondary flex-1 text-center"
                >
                  Edit Event
                </Link>
                <button 
                  onClick={handleDelete}
                  className="btn-danger flex-1"
                >
                  Delete Event
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default EventDetails; 