import { Link } from 'react-router-dom';

const Footer = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-gray-800 text-white py-8">
      <div className="container mx-auto px-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <h3 className="text-xl font-semibold mb-4">Hotel Booking</h3>
            <p className="text-gray-400">
              Find and book the perfect event venue for your next meeting, conference, or celebration.
            </p>
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-4">Quick Links</h3>
            <ul className="space-y-2">
              <li>
                <Link to="/" className="text-gray-400 hover:text-white">Home</Link>
              </li>
              <li>
                <Link to="/events" className="text-gray-400 hover:text-white">Events</Link>
              </li>
              <li>
                <Link to="/login" className="text-gray-400 hover:text-white">Login</Link>
              </li>
              <li>
                <Link to="/register" className="text-gray-400 hover:text-white">Register</Link>
              </li>
            </ul>
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-4">Contact</h3>
            <p className="text-gray-400 mb-2">Email: info@hotelbooking.com</p>
            <p className="text-gray-400 mb-2">Phone: +1 (123) 456-7890</p>
            <p className="text-gray-400">Address: 123 Booking Street, City</p>
          </div>
        </div>
        <div className="border-t border-gray-700 mt-8 pt-6 text-center text-gray-400">
          <p>&copy; {currentYear} Hotel Booking System. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer; 