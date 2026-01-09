import { useState, useEffect, useCallback, useRef } from 'react';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { SearchBar } from '../shared/SearchBar';
import { CategoryFilter } from '../shared/CategoryFilter';
import { SortSelect } from '../shared/SortSelect';
import { ListingGrid } from '../shared/ListingGrid';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { EmptyState } from '../shared/EmptyState';
import { useListingsFilter } from '../hooks/useListingsFilter';
import { useInfiniteScroll } from '../hooks/useInfiniteScroll';
import { fetchListings } from '../mocks/listingsApi';
import type { ListingCardData } from '../types/listing';

export function ListingsPage() {
  const { filters, setQuery, setCategory, setSortBy, nextPage } = useListingsFilter();
  const [listings, setListings] = useState<ListingCardData[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [totalCount, setTotalCount] = useState(0);

  // Track current page for infinite scroll
  const currentPageRef = useRef(1);

  // Extract filter values for dependency tracking
  const { query, category, sortBy } = filters;

  // Initial load and filter changes
  useEffect(() => {
    let cancelled = false;

    const loadListings = async () => {
      setIsLoading(true);
      currentPageRef.current = 1;
      try {
        const result = await fetchListings({ query, category, sortBy, page: 1 });
        if (!cancelled) {
          setListings(result.listings);
          setHasMore(result.hasMore);
          setTotalCount(result.totalCount);
        }
      } catch (error) {
        console.error('Failed to fetch listings:', error);
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    };

    loadListings();
    return () => {
      cancelled = true;
    };
  }, [query, category, sortBy]);

  // Load more for infinite scroll
  const loadMore = useCallback(async () => {
    if (isLoadingMore || !hasMore) return;

    setIsLoadingMore(true);
    const nextPageNum = currentPageRef.current + 1;
    try {
      const result = await fetchListings({ query, category, sortBy, page: nextPageNum });
      setListings((prev) => [...prev, ...result.listings]);
      setHasMore(result.hasMore);
      currentPageRef.current = nextPageNum;
      nextPage();
    } catch (error) {
      console.error('Failed to load more listings:', error);
    } finally {
      setIsLoadingMore(false);
    }
  }, [query, category, sortBy, isLoadingMore, hasMore, nextPage]);

  const { observerRef } = useInfiniteScroll({
    onLoadMore: loadMore,
    hasMore,
    isLoading: isLoadingMore,
  });

  const handlePlaceBid = (listingId: string) => {
    console.log('Place bid clicked for listing:', listingId);
  };

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Header />

      <main className="flex-1">
        {/* Page Header */}
        <div className="bg-white border-b border-gray-200">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
            <h1 className="text-2xl lg:text-3xl font-bold text-gray-900 mb-2">
              Live Auctions
            </h1>
            <p className="text-gray-500">
              {isLoading ? 'Loading...' : `${totalCount} auctions available`}
            </p>
          </div>
        </div>

        {/* Filter Bar */}
        <div className="bg-white border-b border-gray-200 sticky top-16 z-40">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="flex-1">
                <SearchBar
                  initialValue={query || ''}
                  onSearch={setQuery}
                  placeholder="Search auctions..."
                />
              </div>
              <div className="flex gap-3">
                <CategoryFilter
                  value={category || ''}
                  onChange={setCategory}
                />
                <SortSelect
                  value={sortBy}
                  onChange={setSortBy}
                />
              </div>
            </div>
          </div>
        </div>

        {/* Listings Grid */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {isLoading ? (
            <div className="py-16">
              <LoadingSpinner size="lg" />
            </div>
          ) : listings.length === 0 ? (
            <EmptyState />
          ) : (
            <>
              <ListingGrid listings={listings} onPlaceBid={handlePlaceBid} />

              {/* Infinite Scroll Sentinel */}
              <div ref={observerRef} className="py-8">
                {isLoadingMore && (
                  <LoadingSpinner size="md" className="py-4" />
                )}
                {!hasMore && listings.length > 0 && (
                  <p className="text-center text-gray-500 py-4">
                    You've reached the end of the listings
                  </p>
                )}
              </div>
            </>
          )}
        </div>
      </main>

      <Footer />
    </div>
  );
}
