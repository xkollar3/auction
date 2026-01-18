import { useQuery } from '@tanstack/react-query';
import { Package, Truck, CheckCircle, Clock, XCircle, AlertCircle } from 'lucide-react';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { getMyPurchases } from '../api/order';
import type { OrderStatus } from '../types/order';

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

export const MyPurchasesPage = () => {
  const { data: orders, isLoading, error } = useQuery({
    queryKey: ['myPurchases'],
    queryFn: getMyPurchases,
  });

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 flex-1 w-full">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">My Purchases</h1>
          <p className="text-gray-600 mt-2">Track orders from auctions you've won</p>
        </div>

        {isLoading ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-gray-600">Loading your purchases...</p>
          </div>
        ) : error ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-red-600">Failed to load your purchases</p>
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
                          <p className="font-mono text-gray-900">{order.id}</p>
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
                      <div className="text-lg font-bold text-gray-900">
                        {formatPrice(order.amount)}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-gray-600 mb-4">You don't have any purchases yet.</p>
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
};
