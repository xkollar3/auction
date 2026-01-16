import { useState, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Plus, Clock, Coins, Tag, ImagePlus, X, Upload, Loader2 } from 'lucide-react';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { useAuth } from '../hooks/useAuth';
import { getUserProfile } from '../api/user';
import { getSellerShopAuctions, uploadAuctionImages, getAuctionImages } from '../api/auction';
import type { AuctionStatus } from '../types/auction';

const formatTimeRemaining = (endTime: string): string => {
  const end = new Date(endTime);
  const now = new Date();
  const diff = end.getTime() - now.getTime();

  if (diff <= 0) return 'Ended';

  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

  if (days > 0) return `${days}d ${hours}h`;
  if (hours > 0) return `${hours}h ${minutes}m`;
  return `${minutes}m`;
};

const formatPrice = (price: number): string => {
  return new Intl.NumberFormat('cs-CZ', {
    style: 'currency',
    currency: 'CZK',
    minimumFractionDigits: 0,
  }).format(price);
};

interface SelectedImage {
  file: File;
  preview: string;
}

export const SellerDashboardPage = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const [statusFilter, setStatusFilter] = useState<AuctionStatus>('ACTIVE');
  const [imageModalOpen, setImageModalOpen] = useState(false);
  const [selectedAuctionId, setSelectedAuctionId] = useState<string | null>(null);
  const [selectedAuctionTitle, setSelectedAuctionTitle] = useState<string>('');
  const [selectedImages, setSelectedImages] = useState<SelectedImage[]>([]);
  const [existingImages, setExistingImages] = useState<string[]>([]);
  const [isLoadingImages, setIsLoadingImages] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleOpenImageModal = async (auctionId: string, auctionTitle: string, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setSelectedAuctionId(auctionId);
    setSelectedAuctionTitle(auctionTitle);
    setSelectedImages([]);
    setExistingImages([]);
    setUploadError(null);
    setImageModalOpen(true);

    // Load existing images
    setIsLoadingImages(true);
    try {
      const response = await getAuctionImages(auctionId);
      setExistingImages(response.images || []);
    } catch (error) {
      console.error('Failed to load existing images:', error);
    } finally {
      setIsLoadingImages(false);
    }
  };

  const handleCloseImageModal = () => {
    setImageModalOpen(false);
    setSelectedAuctionId(null);
    setSelectedAuctionTitle('');
    selectedImages.forEach(img => URL.revokeObjectURL(img.preview));
    setSelectedImages([]);
    setExistingImages([]);
    setUploadError(null);
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;

    const newImages: SelectedImage[] = Array.from(files).map(file => ({
      file,
      preview: URL.createObjectURL(file),
    }));

    setSelectedImages(prev => [...prev, ...newImages]);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleRemoveImage = (index: number) => {
    setSelectedImages(prev => {
      const updated = [...prev];
      URL.revokeObjectURL(updated[index].preview);
      updated.splice(index, 1);
      return updated;
    });
  };

  const handleSaveImages = async () => {
    if (!selectedAuctionId || selectedImages.length === 0) return;

    setIsUploading(true);
    setUploadError(null);

    try {
      const files = selectedImages.map(img => img.file);
      await uploadAuctionImages(selectedAuctionId, files);

      // Invalidate queries to refresh data
      queryClient.invalidateQueries({ queryKey: ['auctionImages', selectedAuctionId] });

      handleCloseImageModal();
    } catch (error) {
      console.error('Failed to upload images:', error);
      setUploadError('Failed to upload images. Please try again.');
    } finally {
      setIsUploading(false);
    }
  };

  const { data: profile, isLoading: profileLoading } = useQuery({
    queryKey: ['userProfile'],
    queryFn: getUserProfile,
  });

  const { data: auctions, isLoading: auctionsLoading, error } = useQuery({
    queryKey: ['sellerAuctions', user?.id, statusFilter],
    queryFn: () => getSellerShopAuctions(user!.id, statusFilter),
    enabled: !!profile?.sellerAccountEnabled && !!user?.id,
  });

  if (profileLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col">
        <Header />
        <div className="flex items-center justify-center py-20 flex-1">
          <p className="text-gray-600">Loading...</p>
        </div>
        <Footer />
      </div>
    );
  }

  if (!profile?.sellerAccountEnabled) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col">
        <Header />
        <main className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-12 flex-1 w-full">
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <h1 className="text-2xl font-bold text-gray-900 mb-4">Seller Account Required</h1>
            <p className="text-gray-600 mb-6">
              You need an enabled seller account to access the dashboard.
              Please complete your seller onboarding first.
            </p>
            <button
              onClick={() => navigate('/profile')}
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              Go to Profile
            </button>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 flex-1 w-full">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Seller Dashboard</h1>
            <p className="text-gray-600 mt-2">Manage your auction listings</p>
          </div>
          <Link
            to="/seller/post-item"
            className="flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <Plus className="h-5 w-5" />
            Post New Item
          </Link>
        </div>

        {/* Status Filter Tabs */}
        <div className="flex gap-2 mb-6">
          <button
            onClick={() => setStatusFilter('ACTIVE')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              statusFilter === 'ACTIVE'
                ? 'bg-blue-600 text-white'
                : 'bg-white text-gray-700 hover:bg-gray-100'
            }`}
          >
            Active Auctions
          </button>
          <button
            onClick={() => setStatusFilter('CLOSED')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              statusFilter === 'CLOSED'
                ? 'bg-blue-600 text-white'
                : 'bg-white text-gray-700 hover:bg-gray-100'
            }`}
          >
            Closed Auctions
          </button>
        </div>

        {/* Auctions List */}
        {auctionsLoading ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-gray-600">Loading auctions...</p>
          </div>
        ) : error ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-red-600">Failed to load auctions</p>
          </div>
        ) : auctions && auctions.length > 0 ? (
          <div className="grid gap-4">
            {auctions.map((auction) => (
              <div
                key={auction.id}
                className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow"
              >
                <div className="flex justify-between items-start">
                  <Link to={`/auction/${auction.id}`} className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="text-lg font-semibold text-gray-900 hover:text-blue-600">{auction.title}</h3>
                      <span className={`text-xs px-2 py-0.5 rounded ${
                        auction.status === 'ACTIVE'
                          ? 'bg-green-100 text-green-700'
                          : 'bg-gray-100 text-gray-700'
                      }`}>
                        {auction.status}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600 line-clamp-2 mb-3">{auction.description}</p>
                    <div className="flex items-center gap-6 text-sm">
                      <div className="flex items-center gap-1 text-gray-500">
                        <Tag className="h-4 w-4" />
                        <span>{auction.category}</span>
                      </div>
                      <div className="flex items-center gap-1 text-gray-500">
                        <Coins className="h-4 w-4" />
                        <span>Starting: {formatPrice(auction.startingPrice)}</span>
                      </div>
                      {auction.status === 'ACTIVE' && (
                        <div className="flex items-center gap-1 text-orange-600">
                          <Clock className="h-4 w-4" />
                          <span>{formatTimeRemaining(auction.auctionEndTime)}</span>
                        </div>
                      )}
                    </div>
                  </Link>
                  <div className="text-right ml-6 flex flex-col items-end gap-2">
                    <div>
                      <div className="text-sm text-gray-500 mb-1">{auction.bidCount} bids</div>
                      <div className="text-lg font-bold text-gray-900">
                        {auction.highestBidAmount
                          ? formatPrice(auction.highestBidAmount)
                          : 'No bids yet'}
                      </div>
                    </div>
                    {auction.status === 'ACTIVE' && (
                      <button
                        onClick={(e) => handleOpenImageModal(auction.id, auction.title, e)}
                        className="flex items-center gap-1 px-3 py-1.5 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
                      >
                        <ImagePlus className="h-4 w-4" />
                        Add Images
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-gray-600 mb-4">
              {statusFilter === 'ACTIVE'
                ? "You don't have any active auctions."
                : "You don't have any closed auctions."}
            </p>
            {statusFilter === 'ACTIVE' && (
              <Link
                to="/seller/post-item"
                className="inline-flex items-center gap-2 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
              >
                <Plus className="h-5 w-5" />
                Post Your First Item
              </Link>
            )}
          </div>
        )}
      </main>
      <Footer />

      {/* Image Upload Modal */}
      {imageModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div
            className="absolute inset-0 bg-black/50"
            onClick={handleCloseImageModal}
          />
          <div className="relative bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-hidden flex flex-col">
            <div className="flex items-center justify-between p-6 border-b">
              <div>
                <h2 className="text-xl font-bold text-gray-900">Add Images</h2>
                <p className="text-sm text-gray-500 mt-1">{selectedAuctionTitle}</p>
              </div>
              <button
                onClick={handleCloseImageModal}
                className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="p-6 flex-1 overflow-y-auto">
              {/* File Input */}
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                multiple
                onChange={handleFileSelect}
                className="hidden"
              />

              {/* Existing Images */}
              {isLoadingImages ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
                  <span className="ml-2 text-gray-500">Loading images...</span>
                </div>
              ) : existingImages && existingImages.length > 0 && (
                <div className="mb-6">
                  <h3 className="text-sm font-medium text-gray-700 mb-3">
                    Current Images ({existingImages.length})
                  </h3>
                  <div className="grid grid-cols-3 gap-4">
                    {existingImages.map((url, index) => (
                      <div key={index} className="relative">
                        <img
                          src={url}
                          alt={`Image ${index + 1}`}
                          className="w-full h-32 object-cover rounded-lg"
                        />
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Upload Area */}
              <button
                onClick={() => fileInputRef.current?.click()}
                disabled={isUploading}
                className="w-full border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-blue-400 hover:bg-blue-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Upload className="h-10 w-10 text-gray-400 mx-auto mb-3" />
                <p className="text-gray-600 font-medium">Click to select images</p>
                <p className="text-sm text-gray-400 mt-1">PNG, JPG, GIF up to 10MB each</p>
              </button>

              {/* Selected Images Preview */}
              {selectedImages.length > 0 && (
                <div className="mt-6">
                  <h3 className="text-sm font-medium text-gray-700 mb-3">
                    New Images to Upload ({selectedImages.length})
                  </h3>
                  <div className="grid grid-cols-3 gap-4">
                    {selectedImages.map((image, index) => (
                      <div key={index} className="relative group">
                        <img
                          src={image.preview}
                          alt={`Preview ${index + 1}`}
                          className="w-full h-32 object-cover rounded-lg"
                        />
                        <button
                          onClick={() => handleRemoveImage(index)}
                          disabled={isUploading}
                          className="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity disabled:opacity-50"
                        >
                          <X className="h-4 w-4" />
                        </button>
                        <p className="text-xs text-gray-500 mt-1 truncate">{image.file.name}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Upload Error */}
              {uploadError && (
                <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                  <p className="text-sm text-red-600">{uploadError}</p>
                </div>
              )}
            </div>

            <div className="flex justify-end gap-3 p-6 border-t bg-gray-50">
              <button
                onClick={handleCloseImageModal}
                disabled={isUploading}
                className="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                onClick={handleSaveImages}
                disabled={selectedImages.length === 0 || isUploading}
                className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isUploading && <Loader2 className="h-4 w-4 animate-spin" />}
                {isUploading ? 'Uploading...' : 'Upload Images'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
