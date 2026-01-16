import { useState, useRef, type ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Clock, Sun, Calendar, CalendarDays, ImagePlus, X } from 'lucide-react';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { getUserProfile } from '../api/user';
import { addAuctionItem, uploadAuctionImages } from '../api/auction';
import { addAuctionItemSchema, AUCTION_CATEGORIES, type AddAuctionItemFormData } from '../schemas/auction';

type AuctionDuration = 'one_hour' | 'one_day' | 'one_week' | 'end_of_month';

const AUCTION_DURATIONS: { value: AuctionDuration; label: string; shortDesc: string; description: string; icon: ReactNode }[] = [
  { value: 'one_hour', label: '1 Hour', shortDesc: 'Quick auction', description: 'Ends in exactly one hour from now', icon: <Clock className="w-5 h-5" /> },
  { value: 'one_day', label: '1 Day', shortDesc: 'Until tomorrow', description: 'Ends at midnight tomorrow', icon: <Sun className="w-5 h-5" /> },
  { value: 'one_week', label: '1 Week', shortDesc: '7 days', description: 'Ends in one week at midnight', icon: <Calendar className="w-5 h-5" /> },
  { value: 'end_of_month', label: 'End of Month', shortDesc: 'This month', description: 'Ends on the last day of this month at midnight', icon: <CalendarDays className="w-5 h-5" /> },
];

const calculateEndTime = (duration: AuctionDuration): string => {
  const now = new Date();

  switch (duration) {
    case 'one_hour':
      return new Date(now.getTime() + 60 * 60 * 1000).toISOString();
    case 'one_day': {
      const tomorrow = new Date(now);
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(23, 59, 59, 999);
      return tomorrow.toISOString();
    }
    case 'one_week': {
      const nextWeek = new Date(now);
      nextWeek.setDate(nextWeek.getDate() + 7);
      nextWeek.setHours(23, 59, 59, 999);
      return nextWeek.toISOString();
    }
    case 'end_of_month': {
      const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0);
      endOfMonth.setHours(23, 59, 59, 999);
      return endOfMonth.toISOString();
    }
  }
};

export const PostAuctionItemPage = () => {
  const navigate = useNavigate();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [selectedDuration, setSelectedDuration] = useState<AuctionDuration | ''>('');
  const [selectedImages, setSelectedImages] = useState<File[]>([]);
  const [isUploadingImages, setIsUploadingImages] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const { data: profile, isLoading: profileLoading } = useQuery({
    queryKey: ['userProfile'],
    queryFn: getUserProfile,
  });

  const { register, handleSubmit, formState: { errors } } = useForm<AddAuctionItemFormData>({
    resolver: zodResolver(addAuctionItemSchema),
    defaultValues: {
      category: undefined,
    },
  });

  const mutation = useMutation({
    mutationFn: addAuctionItem,
    onSuccess: async (data) => {
      // Upload images if any were selected
      if (selectedImages.length > 0) {
        setIsUploadingImages(true);
        try {
          await uploadAuctionImages(data.auctionItemId, selectedImages);
        } catch (error) {
          console.error('Failed to upload images:', error);
          // Still navigate even if image upload fails - item was created
        } finally {
          setIsUploadingImages(false);
        }
      }
      navigate(`/auction/${data.auctionItemId}`);
    },
    onError: (error) => {
      setSubmitError(error instanceof Error ? error.message : 'Failed to create auction item');
    },
  });

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = event.target.files;
    if (files) {
      const newFiles = Array.from(files).filter(
        file => file.type.startsWith('image/')
      );
      setSelectedImages(prev => [...prev, ...newFiles]);
    }
    // Reset input so the same file can be selected again
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const removeImage = (index: number) => {
    setSelectedImages(prev => prev.filter((_, i) => i !== index));
  };

  const onSubmit = (data: AddAuctionItemFormData) => {
    if (!selectedDuration) {
      setSubmitError('Please select an auction duration');
      return;
    }
    setSubmitError(null);
    mutation.mutate({
      ...data,
      auctionEndTime: calculateEndTime(selectedDuration),
    });
  };

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
              You need an enabled seller account to post auction items.
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
      <main className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-12 flex-1 w-full">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Post New Auction Item</h1>
          <p className="text-gray-600 mt-2">Fill in the details to list your item for auction</p>
        </div>

        <div className="bg-white rounded-lg shadow-md p-6">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <div>
              <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
                Title
              </label>
              <input
                type="text"
                id="title"
                {...register('title')}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="e.g., Vintage Fender Stratocaster 1965"
              />
              {errors.title && (
                <p className="mt-1 text-sm text-red-600">{errors.title.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
                Description
              </label>
              <textarea
                id="description"
                rows={5}
                {...register('description')}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="Describe your item in detail..."
              />
              {errors.description && (
                <p className="mt-1 text-sm text-red-600">{errors.description.message}</p>
              )}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="startingPrice" className="block text-sm font-medium text-gray-700 mb-1">
                  Starting Price (Kč)
                </label>
                <input
                  type="number"
                  id="startingPrice"
                  min="1"
                  step="0.01"
                  {...register('startingPrice')}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="100"
                />
                {errors.startingPrice && (
                  <p className="mt-1 text-sm text-red-600">{errors.startingPrice.message}</p>
                )}
              </div>

              <div>
                <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-1">
                  Category
                </label>
                <select
                  id="category"
                  {...register('category')}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="">Select a category</option>
                  {AUCTION_CATEGORIES.map((cat) => (
                    <option key={cat} value={cat}>
                      {cat.charAt(0) + cat.slice(1).toLowerCase()}
                    </option>
                  ))}
                </select>
                {errors.category && (
                  <p className="mt-1 text-sm text-red-600">{errors.category.message}</p>
                )}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">
                Auction Duration
              </label>
              <div className="grid grid-cols-2 gap-3">
                {AUCTION_DURATIONS.map((duration) => (
                  <button
                    key={duration.value}
                    type="button"
                    onClick={() => setSelectedDuration(duration.value)}
                    title={duration.description}
                    className={`relative group p-4 rounded-xl border-2 text-left transition-all ${
                      selectedDuration === duration.value
                        ? 'border-blue-500 bg-blue-50 ring-2 ring-blue-200'
                        : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                        selectedDuration === duration.value
                          ? 'bg-blue-500 text-white'
                          : 'bg-gray-100 text-gray-500 group-hover:bg-gray-200'
                      }`}>
                        {duration.icon}
                      </div>
                      <div>
                        <p className={`font-semibold ${
                          selectedDuration === duration.value ? 'text-blue-700' : 'text-gray-900'
                        }`}>
                          {duration.label}
                        </p>
                        <p className="text-xs text-gray-500">{duration.shortDesc}</p>
                      </div>
                    </div>
                    {/* Tooltip */}
                    <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-2 bg-gray-900 text-white text-xs rounded-lg opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none whitespace-nowrap z-10">
                      {duration.description}
                      <div className="absolute top-full left-1/2 -translate-x-1/2 border-4 border-transparent border-t-gray-900" />
                    </div>
                  </button>
                ))}
              </div>
              {!selectedDuration && submitError?.includes('duration') && (
                <p className="mt-2 text-sm text-red-600">Please select an auction duration</p>
              )}
            </div>

            {/* Image Upload Section */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">
                Product Images (Optional)
              </label>
              <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileSelect}
                accept="image/*"
                multiple
                className="hidden"
              />

              {/* Image Preview Grid */}
              {selectedImages.length > 0 && (
                <div className="grid grid-cols-4 gap-3 mb-4">
                  {selectedImages.map((file, index) => (
                    <div key={index} className="relative group aspect-square">
                      <img
                        src={URL.createObjectURL(file)}
                        alt={`Preview ${index + 1}`}
                        className="w-full h-full object-cover rounded-lg border border-gray-200"
                      />
                      <button
                        type="button"
                        onClick={() => removeImage(index)}
                        className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                      >
                        <X className="w-4 h-4" />
                      </button>
                    </div>
                  ))}
                </div>
              )}

              {/* Add Images Button */}
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="w-full border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-400 hover:bg-blue-50 transition-colors"
              >
                <ImagePlus className="w-8 h-8 text-gray-400 mx-auto mb-2" />
                <p className="text-sm text-gray-600">
                  {selectedImages.length > 0 ? 'Add more images' : 'Click to add images'}
                </p>
                <p className="text-xs text-gray-400 mt-1">
                  PNG, JPG up to 10MB each
                </p>
              </button>
            </div>

            {submitError && (
              <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-sm text-red-600">{submitError}</p>
              </div>
            )}

            <div className="flex gap-4">
              <button
                type="button"
                onClick={() => navigate(-1)}
                disabled={mutation.isPending || isUploadingImages}
                className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={mutation.isPending || isUploadingImages}
                className="flex-1 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {mutation.isPending ? 'Creating...' : isUploadingImages ? 'Uploading Images...' : 'Create Auction'}
              </button>
            </div>
          </form>
        </div>
      </main>
      <Footer />
    </div>
  );
};
