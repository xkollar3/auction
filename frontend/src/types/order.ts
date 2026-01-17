/**
 * Order status
 */
export type OrderStatus =
  | 'FUNDS_RESERVED'
  | 'TRACKING_PENDING'
  | 'TRACKING_IN_PROGRESS'
  | 'DELIVERED'
  | 'REFUND_PENDING'
  | 'CANCELLED'
  | 'COMPLETED';

/**
 * Tracking status milestone
 */
export type TrackingStatusMilestone =
  | 'INFO_RECEIVED'
  | 'IN_TRANSIT'
  | 'OUT_FOR_DELIVERY'
  | 'FAILED_ATTEMPT'
  | 'AVAILABLE_FOR_PICKUP'
  | 'DELIVERED'
  | 'EXCEPTION';

/**
 * Order response from API
 */
export interface OrderResponse {
  id: string;
  buyerId: string;
  sellerId: string;
  status: OrderStatus;
  amount: number;
  reservedAt: string;
  trackingNumber: string | null;
  trackingStatusMilestone: TrackingStatusMilestone | null;
  trackingEventStatus: string | null;
  trackingLastUpdatedAt: string | null;
  completedAt: string | null;
  cancelledAt: string | null;
}

/**
 * Enter tracking number request
 */
export interface EnterTrackingNumberRequest {
  trackingNumber: string;
}

/**
 * Enter tracking number response
 */
export interface EnterTrackingNumberResponse {
  orderId: string;
}
