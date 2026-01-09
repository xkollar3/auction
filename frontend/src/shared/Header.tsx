import { Link, useNavigate } from 'react-router-dom';
import { Search, Bell, Gavel } from 'lucide-react';
import { useState } from 'react';

export function Header() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/listings?q=${encodeURIComponent(searchQuery.trim())}`);
    } else {
      navigate('/listings');
    }
  };

  const handleNotificationClick = () => {
    console.log('Notification bell clicked');
  };

  const handleCategoriesClick = () => {
    console.log('Categories clicked');
  };

  return (
    <header className="sticky top-0 z-50 bg-white border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2">
            <Gavel className="h-6 w-6 text-blue-600" />
            <span className="text-xl font-bold">
              <span className="text-blue-600">BidFlow</span>
              <span className="text-gray-600">Auctions</span>
            </span>
          </Link>

          {/* Navigation */}
          <nav className="hidden md:flex items-center gap-6">
            <button
              onClick={handleCategoriesClick}
              className="text-gray-600 hover:text-gray-900 font-medium"
            >
              Categories
            </button>
            <Link
              to="/#how-it-works"
              className="text-gray-600 hover:text-gray-900 font-medium"
            >
              How it Works
            </Link>
            <Link
              to="/seller/dashboard"
              className="text-gray-600 hover:text-gray-900 font-medium"
            >
              Sell
            </Link>
          </nav>

          {/* Search Bar */}
          <form onSubmit={handleSearch} className="hidden lg:flex items-center flex-1 max-w-md mx-8">
            <div className="relative w-full">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search auctions..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </form>

          {/* Right Section */}
          <div className="flex items-center gap-4">
            <button
              onClick={handleNotificationClick}
              className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-full"
            >
              <Bell className="h-5 w-5" />
            </button>
            <button
              onClick={() => navigate('/login')}
              className="text-gray-600 hover:text-gray-900 font-medium"
            >
              Log In
            </button>
            <button
              onClick={() => navigate('/register')}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-blue-700 transition-colors"
            >
              Sign Up
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
