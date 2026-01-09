import { useState, useCallback, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import type { SortOption, ListingFilterParams } from '../types/listing';

export function useListingsFilter() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [page, setPageState] = useState(1);

  // Derive filters from URL params (source of truth for query, category, sortBy)
  const filters = useMemo<ListingFilterParams>(() => ({
    query: searchParams.get('q') || undefined,
    category: searchParams.get('category') || undefined,
    sortBy: (searchParams.get('sort') as SortOption) || 'ending_soon',
    page,
  }), [searchParams, page]);

  const setQuery = useCallback((query: string) => {
    const params = new URLSearchParams(searchParams);
    if (query) {
      params.set('q', query);
    } else {
      params.delete('q');
    }
    setSearchParams(params, { replace: true });
    setPageState(1);
  }, [searchParams, setSearchParams]);

  const setCategory = useCallback((category: string) => {
    const params = new URLSearchParams(searchParams);
    if (category) {
      params.set('category', category);
    } else {
      params.delete('category');
    }
    setSearchParams(params, { replace: true });
    setPageState(1);
  }, [searchParams, setSearchParams]);

  const setSortBy = useCallback((sortBy: SortOption) => {
    const params = new URLSearchParams(searchParams);
    if (sortBy && sortBy !== 'ending_soon') {
      params.set('sort', sortBy);
    } else {
      params.delete('sort');
    }
    setSearchParams(params, { replace: true });
    setPageState(1);
  }, [searchParams, setSearchParams]);

  const setPage = useCallback((newPage: number) => {
    setPageState(newPage);
  }, []);

  const nextPage = useCallback(() => {
    setPageState((prev) => prev + 1);
  }, []);

  const resetPage = useCallback(() => {
    setPageState(1);
  }, []);

  return {
    filters,
    setQuery,
    setCategory,
    setSortBy,
    setPage,
    nextPage,
    resetPage,
  };
}
