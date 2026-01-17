import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Package, Truck, CheckCircle, Clock, XCircle, AlertCircle, X } from 'lucide-react';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { getMySales, enterTrackingNumber } from '../api/order';
import type { OrderResponse, OrderStatus } from '../types/order';
import { enterTrackingNumberSchema, type EnterTrackingNumberFormData } from '../schemas/order';

const formatPrice = (price: number): string => {
  return new Intl.NumberFormat('cs-CZ', {
    style: 'currency',
    currency: 'CZK',
    minimumFractionDigits: 0,
  }).format(price);
};

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('cs-CZ', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const getStatusConfig = (status: OrderStatus): { label: string; color: string; icon: React.ReactNode } => {
  switch (status) {
    case 'FUNDS_RESERVED':
      return {
        label: 'Awaiting Shipment',
        color: 'bg-yellow-100 text-yellow-800',
        icon: <Clock className="h-4 w-4" />,
      };
    case 'TRACKING_PENDING':
      return {
        label: 'Tracking Pending',
        color: 'bg-blue-100 text-blue-800',
        icon: <Package className="h-4 w-4" />,
      };
    case 'TRACKING_IN_PROGRESS':
      return {
        label: 'In Transit',
        color: 'bg-blue-100 text-blue-800',
        icon: <Truck className="h-4 w-4" />,
      };
    case 'DELIVERED':
      return {
        label: 'Delivered',
        color: 'bg-green-100 text-green-800',
        icon: <CheckCircle className="h-4 w-4" />,
      };
    case 'COMPLETED':
      return {
        label: 'Completed',
        color: 'bg-green-100 text-green-800',
        icon: <CheckCircle className="h-4 w-4" />,
      };
    case 'REFUND_PENDING':
      return {
        label: 'Refund Pending',
        color: 'bg-orange-100 text-orange-800',
        icon: <AlertCircle className="h-4 w-4" />,
      };
    case 'CANCELLED':
      return {
        label: 'Cancelled',
        color: 'bg-red-100 text-red-800',
        icon: <XCircle className="h-4 w-4" />,
      };
    default:
      return {
        label: status,
        color: 'bg-gray-100 text-gray-800',
        icon: <Package className="h-4 w-4" />,
      };
  }
};

interface TrackingModalProps {
  order: OrderResponse;
  onClose: () => void;
  onSubmit: (trackingNumber: string) => void;
  isSubmitting: boolean;
  error: string | null;
}

const TrackingModal = ({ order, onClose, onSubmit, isSubmitting, error }: TrackingModalProps) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<EnterTrackingNumberFormData>({
    resolver: zodResolver(enterTrackingNumberSchema),
  });

  const handleFormSubmit = (data: EnterTrackingNumberFormData) => {
    onSubmit(data.trackingNumber);
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
        <div className="flex items-center justify-between p-4 border-b">
          <h3 className="text-lg font-semibold">Enter Tracking Number</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <form onSubmit={handleSubmit(handleFormSubmit)} className="p-4">
          <div className="mb-4">
            <p className="text-sm text-gray-600 mb-2">
              Order Amount: <span className="font-semibold">{formatPrice(order.amount)}</span>
            </p>
          </div>
          <div className="mb-4">
            <label htmlFor="trackingNumber" className="block text-sm font-medium text-gray-700 mb-1">
              Tracking Number
            </label>
            <input
              id="trackingNumber"
              type="text"
              {...register('trackingNumber')}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="Enter tracking number"
              disabled={isSubmitting}
            />
            {errors.trackingNumber && (
              <p className="mt-1 text-sm text-red-600">{errors.trackingNumber.message}</p>
            )}
          </div>
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-sm text-red-600">{error}</p>
            </div>
          )}
          <div className="flex gap-3 justify-end">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
              disabled={isSubmitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Submitting...' : 'Submit'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export const MySalesPage = () => {
  const queryClient = useQueryClient();
  const [selectedOrder, setSelectedOrder] = useState<OrderResponse | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const { data: orders, isLoading, error } = useQuery({
    queryKey: ['mySales'],
    queryFn: getMySales,
  });

  const trackingMutation = useMutation({
    mutationFn: ({ orderId, trackingNumber }: { orderId: string; trackingNumber: string }) =>
      enterTrackingNumber(orderId, trackingNumber),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['mySales'] });
      setSelectedOrder(null);
      setSubmitError(null);
    },
    onError: (err: Error) => {
      setSubmitError(err.message || 'Failed to submit tracking number');
    },
  });

  const handleTrackingSubmit = (trackingNumber: string) => {
    if (selectedOrder) {
      setSubmitError(null);
      trackingMutation.mutate({ orderId: selectedOrder.id, trackingNumber });
    }
  };

  const canEnterTracking = (status: OrderStatus) => {
    return status === 'FUNDS_RESERVED';
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 flex-1 w-full">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">My Sales</h1>
          <p className="text-gray-600 mt-2">Manage orders from your sold auctions</p>
        </div>

        {isLoading ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-gray-600">Loading your sales...</p>
          </div>
        ) : error ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-red-600">Failed to load your sales</p>
          </div>
        ) : orders && orders.length > 0 ? (
          <div className="grid gap-4">
            {orders.map((order) => {
              const statusConfig = getStatusConfig(order.status);
              return (
                <div
                  key={order.id}
                  className="bg-white rounded-lg shadow-md p-6"
                >
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-3">
                        <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${statusConfig.color}`}>
                          {statusConfig.icon}
                          {statusConfig.label}
                        </span>
                      </div>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                        <div>
                          <p className="text-gray-500">Order ID</p>
                          <p className="font-mono text-gray-900">{order.id.slice(0, 8)}...</p>
                        </div>
                        <div>
                          <p className="text-gray-500">Reserved At</p>
                          <p className="text-gray-900">{formatDate(order.reservedAt)}</p>
                        </div>
                        {order.trackingNumber && (
                          <div>
                            <p className="text-gray-500">Tracking Number</p>
                            <p className="font-mono text-gray-900">{order.trackingNumber}</p>
                          </div>
                        )}
                        {order.trackingEventStatus && (
                          <div>
                            <p className="text-gray-500">Tracking Status</p>
                            <p className="text-gray-900">{order.trackingEventStatus}</p>
                          </div>
                        )}
                        {order.trackingLastUpdatedAt && (
                          <div>
                            <p className="text-gray-500">Last Tracking Update</p>
                            <p className="text-gray-900">{formatDate(order.trackingLastUpdatedAt)}</p>
                          </div>
                        )}
                        {order.completedAt && (
                          <div>
                            <p className="text-gray-500">Completed At</p>
                            <p className="text-gray-900">{formatDate(order.completedAt)}</p>
                          </div>
                        )}
                        {order.cancelledAt && (
                          <div>
                            <p className="text-gray-500">Cancelled At</p>
                            <p className="text-gray-900">{formatDate(order.cancelledAt)}</p>
                          </div>
                        )}
                      </div>
                    </div>
                    <div className="text-right ml-6">
                      <div className="text-lg font-bold text-gray-900 mb-3">
                        {formatPrice(order.amount)}
                      </div>
                      {canEnterTracking(order.status) && (
                        <button
                          onClick={() => setSelectedOrder(order)}
                          className="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 transition-colors"
                        >
                          Enter Tracking
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-gray-600 mb-4">You don't have any sales yet.</p>
          </div>
        )}
      </main>
      <Footer />

      {selectedOrder && (
        <TrackingModal
          order={selectedOrder}
          onClose={() => {
            setSelectedOrder(null);
            setSubmitError(null);
          }}
          onSubmit={handleTrackingSubmit}
          isSubmitting={trackingMutation.isPending}
          error={submitError}
        />
      )}
    </div>
  );
};
