import { Link } from 'react-router-dom';
import { UserPlus, Search, TrendingUp, Gift } from 'lucide-react';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { ListingCard } from '../shared/ListingCard';
import { CategoryCard } from '../shared/CategoryCard';
import { HowItWorksStep } from '../shared/HowItWorksStep';
import { mockListings, mockCategories } from '../mocks/listings';

export function HomePage() {
  const handlePlaceBid = (listingId: string) => {
    console.log('Place bid clicked for listing:', listingId);
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1">
        {/* Hero Section */}
        <section className="bg-gradient-to-r from-blue-600 to-blue-700 text-white py-16 lg:py-24">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
            <h1 className="text-3xl md:text-4xl lg:text-5xl font-bold mb-4">
              Discover Unique Items, Bid<br className="hidden sm:block" /> with Confidence
            </h1>
            <p className="text-blue-100 text-lg max-w-2xl mx-auto mb-8">
              Join thousands of bidders in live auctions. Find treasures, win deals,<br className="hidden sm:block" />
              and become part of the excitement.
            </p>

            {/* CTA Buttons */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center mb-12">
              <Link
                to="/register"
                className="bg-white text-blue-600 px-6 py-3 rounded-lg font-semibold hover:bg-blue-50 transition-colors"
              >
                Create Free Account
              </Link>
              <Link
                to="/listings"
                className="border-2 border-white text-white px-6 py-3 rounded-lg font-semibold hover:bg-white/10 transition-colors"
              >
                Browse Auctions
              </Link>
            </div>

            {/* Stats Row */}
            <div className="flex flex-col sm:flex-row gap-8 sm:gap-16 justify-center text-center">
              <div>
                <div className="text-2xl lg:text-3xl font-bold flex items-center justify-center gap-2">
                  <TrendingUp className="w-6 h-6" />
                  12,543
                </div>
                <div className="text-blue-200 text-sm">Live Auctions</div>
              </div>
              <div>
                <div className="text-2xl lg:text-3xl font-bold flex items-center justify-center gap-2">
                  <UserPlus className="w-6 h-6" />
                  85,000+
                </div>
                <div className="text-blue-200 text-sm">Active Bidders</div>
              </div>
              <div>
                <div className="text-2xl lg:text-3xl font-bold flex items-center justify-center gap-2">
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  247
                </div>
                <div className="text-blue-200 text-sm">Ending Today</div>
              </div>
            </div>
          </div>
        </section>

        {/* Live Auctions Section */}
        <section className="py-12 lg:py-16 bg-gray-50">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between items-center mb-8">
              <div>
                <h2 className="text-2xl lg:text-3xl font-bold text-gray-900">Live Auctions</h2>
                <p className="text-gray-500 mt-1">Bid on items before they're gone - ending soon!</p>
              </div>
              <Link
                to="/listings"
                className="text-blue-600 hover:text-blue-700 font-medium hidden sm:block"
              >
                View All
              </Link>
            </div>

            {/* Listings Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {mockListings.map((listing) => (
                <ListingCard
                  key={listing.id}
                  listing={listing}
                  onPlaceBid={handlePlaceBid}
                />
              ))}
            </div>

            {/* Mobile View All */}
            <div className="mt-8 text-center sm:hidden">
              <Link
                to="/listings"
                className="text-blue-600 hover:text-blue-700 font-medium"
              >
                View All Auctions
              </Link>
            </div>
          </div>
        </section>

        {/* CTA Banner */}
        <section className="bg-blue-600 py-12">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
            <h2 className="text-2xl lg:text-3xl font-bold text-white mb-3">
              Want to place a bid?
            </h2>
            <p className="text-blue-100 mb-6">
              Create a free account to start bidding on thousands of items
            </p>
            <Link
              to="/register"
              className="inline-block bg-white text-blue-600 px-8 py-3 rounded-lg font-semibold hover:bg-blue-50 transition-colors"
            >
              Sign Up Now - It's Free
            </Link>
          </div>
        </section>

        {/* Browse by Category Section */}
        <section className="py-12 lg:py-16 bg-gray-50">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-10">
              <h2 className="text-2xl lg:text-3xl font-bold text-gray-900 mb-2">
                Browse by Category
              </h2>
              <p className="text-gray-500">Find exactly what you're looking for</p>
            </div>

            {/* Categories Grid */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 lg:gap-6">
              {mockCategories.map((category) => (
                <CategoryCard key={category.id} category={category} />
              ))}
            </div>
          </div>
        </section>

        {/* How It Works Section */}
        <section id="how-it-works" className="py-12 lg:py-16">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-12">
              <h2 className="text-2xl lg:text-3xl font-bold text-gray-900 mb-2">
                How It Works
              </h2>
              <p className="text-gray-500">Get started in 4 simple steps</p>
            </div>

            {/* Steps */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8 lg:gap-12 mb-12">
              <HowItWorksStep
                stepNumber={1}
                icon={UserPlus}
                title="Create Account"
                description="Sign up for free in less than a minute. No credit card required."
              />
              <HowItWorksStep
                stepNumber={2}
                icon={Search}
                title="Browse Auctions"
                description="Explore thousands of items across multiple categories and find your treasures."
              />
              <HowItWorksStep
                stepNumber={3}
                icon={TrendingUp}
                title="Place Your Bids"
                description="Bid on items you love. Our system keeps you updated in real-time."
              />
              <HowItWorksStep
                stepNumber={4}
                icon={Gift}
                title="Win & Enjoy"
                description="Win the auction and get your item delivered right to your door."
              />
            </div>

            {/* CTA Button */}
            <div className="text-center">
              <Link
                to="/register"
                className="inline-block bg-blue-600 text-white px-8 py-3 rounded-lg font-semibold hover:bg-blue-700 transition-colors"
              >
                Get Started Now
              </Link>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
}
