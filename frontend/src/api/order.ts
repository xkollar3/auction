import { api } from '../lib/api';
import type {
  OrderResponse,
  EnterTrackingNumberRequest,
  EnterTrackingNumberResponse,
} from '../types/order';

/**
 * Get orders where the current user is the seller
 */
export const getMySales = async (): Promise<OrderResponse[]> => {
  const response = await api.get<OrderResponse[]>('/api/v1/orders/my-sales');
  return response.data;
};

/**
 * Get orders where the current user is the buyer
 */
export const getMyPurchases = async (): Promise<OrderResponse[]> => {
  const response = await api.get<OrderResponse[]>('/api/v1/orders/my-purchases');
  return response.data;
};

/**
 * Enter tracking number for an order
 */
export const enterTrackingNumber = async (
  orderId: string,
  trackingNumber: string
): Promise<EnterTrackingNumberResponse> => {
  const request: EnterTrackingNumberRequest = { trackingNumber };
  const response = await api.post<EnterTrackingNumberResponse>(
    `/api/v1/orders/${orderId}/tracking-number`,
    request
  );
  return response.data;
};
