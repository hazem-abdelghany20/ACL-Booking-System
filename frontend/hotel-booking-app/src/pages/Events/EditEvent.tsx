import { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import type { FormEvent } from 'react';
import eventService from '../../services/eventService';
import type { Event, Category } from '../../types';

const EditEvent = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  
  // Form state
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [venue, setVenue] = useState('');
  const [date, setDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [seats, setSeats] = useState('');
  const [price, setPrice] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  
  useEffect(() => {
    const fetchEventAndCategories = async () => {
      if (!id) return;
      
      setLoading(true);
      
      try {
        const [eventData, categoriesData] = await Promise.all([
          eventService.getEventById(Number(id)),
          eventService.getAllCategories()
        ]);
        
        // Populate form with existing event data
        setName(eventData.name || '');
        setDescription(eventData.description || '');
        setVenue(eventData.venue || '');
        setDate(eventData.date?.split('T')[0] || '');
        setStartTime(eventData.startTime || '');
        setEndTime(eventData.endTime || '');
        setSeats(eventData.seats?.toString() || '');
        setPrice(eventData.price?.toString() || '');
        setCategoryId(eventData.categoryId?.toString() || '');
        setImageUrl(eventData.imageUrl || '');
        
        setCategories(categoriesData);
        setLoading(false);
      } catch (err: any) {
        setError('Failed to load event data. Please try again later.');
        setLoading(false);
        console.error(err);
      }
    };
    
    fetchEventAndCategories();
  }, [id]);
  
  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    
    if (!id) return;
    
    setLoading(true);
    setError(null);
    
    // Validation
    if (!name.trim()) {
      setError('Event name is required');
      setLoading(false);
      return;
    }
    
    try {
      const eventData = {
        name,
        description,
        venue,
        date,
        startTime,
        endTime,
        seats: seats ? parseInt(seats, 10) : undefined,
        price: price ? parseFloat(price) : undefined,
        categoryId: categoryId ? parseInt(categoryId, 10) : undefined,
        imageUrl: imageUrl || undefined,
      };
      
      await eventService.updateEvent(Number(id), eventData);
      navigate(`/events/${id}`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update event. Please try again.');
      setLoading(false);
    }
  };
  
  if (loading && !name) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
      </div>
    );
  }
  
  return (
    <div>
      <div className="mb-8">
        <Link to={`/events/${id}`} className="text-blue-600 hover:underline flex items-center">
          ← Back to event
        </Link>
      </div>
      
      <h1 className="text-3xl font-bold mb-6">Edit Event</h1>
      
      {error && (
        <div className="bg-red-100 text-red-700 p-4 rounded-md mb-6">
          {error}
        </div>
      )}
      
      <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-md p-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Event Name */}
          <div className="md:col-span-2">
            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
              Event Name*
            </label>
            <input
              type="text"
              id="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="form-input"
              disabled={loading}
              required
            />
          </div>
          
          {/* Description */}
          <div className="md:col-span-2">
            <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
              Description*
            </label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="form-input min-h-[100px]"
              disabled={loading}
              required
            />
          </div>
          
          {/* Venue */}
          <div>
            <label htmlFor="venue" className="block text-sm font-medium text-gray-700 mb-1">
              Venue*
            </label>
            <input
              type="text"
              id="venue"
              value={venue}
              onChange={(e) => setVenue(e.target.value)}
              className="form-input"
              disabled={loading}
              required
            />
          </div>
          
          {/* Category */}
          <div>
            <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-1">
              Category
            </label>
            <select
              id="category"
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              className="form-input"
              disabled={loading}
            >
              <option value="">Select a category</option>
              {categories.map(category => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </div>
          
          {/* Date */}
          <div>
            <label htmlFor="date" className="block text-sm font-medium text-gray-700 mb-1">
              Date*
            </label>
            <input
              type="date"
              id="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              className="form-input"
              disabled={loading}
              required
            />
          </div>
          
          {/* Start Time */}
          <div>
            <label htmlFor="startTime" className="block text-sm font-medium text-gray-700 mb-1">
              Start Time
            </label>
            <input
              type="time"
              id="startTime"
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
              className="form-input"
              disabled={loading}
            />
          </div>
          
          {/* End Time */}
          <div>
            <label htmlFor="endTime" className="block text-sm font-medium text-gray-700 mb-1">
              End Time
            </label>
            <input
              type="time"
              id="endTime"
              value={endTime}
              onChange={(e) => setEndTime(e.target.value)}
              className="form-input"
              disabled={loading}
            />
          </div>
          
          {/* Seats */}
          <div>
            <label htmlFor="seats" className="block text-sm font-medium text-gray-700 mb-1">
              Total Seats*
            </label>
            <input
              type="number"
              id="seats"
              value={seats}
              onChange={(e) => setSeats(e.target.value)}
              min="1"
              className="form-input"
              disabled={loading}
              required
            />
          </div>
          
          {/* Price */}
          <div>
            <label htmlFor="price" className="block text-sm font-medium text-gray-700 mb-1">
              Price
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-500">$</span>
              <input
                type="number"
                id="price"
                value={price}
                onChange={(e) => setPrice(e.target.value)}
                min="0"
                step="0.01"
                className="form-input pl-8"
                disabled={loading}
                placeholder="0.00"
              />
            </div>
            <p className="text-sm text-gray-500 mt-1">Leave empty for free events</p>
          </div>
          
          {/* Image URL */}
          <div className="md:col-span-2">
            <label htmlFor="imageUrl" className="block text-sm font-medium text-gray-700 mb-1">
              Image URL
            </label>
            <input
              type="url"
              id="imageUrl"
              value={imageUrl}
              onChange={(e) => setImageUrl(e.target.value)}
              className="form-input"
              disabled={loading}
              placeholder="https://example.com/image.jpg"
            />
          </div>
        </div>
        
        <div className="mt-8 flex justify-end space-x-4">
          <Link
            to={`/events/${id}`}
            className="btn-secondary"
          >
            Cancel
          </Link>
          <button
            type="submit"
            className="btn-primary"
            disabled={loading}
          >
            {loading ? (
              <span className="inline-block h-5 w-5 animate-spin rounded-full border-2 border-solid border-current border-r-transparent" />
            ) : (
              'Update Event'
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default EditEvent; 