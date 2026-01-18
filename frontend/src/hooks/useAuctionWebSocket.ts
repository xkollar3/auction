import { useEffect, useLayoutEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client, type IMessage } from '@stomp/stompjs';
import { useAuth } from './useAuth';

export interface BidUpdateMessage {
  auctionItemId: string;
  bidAmount: number;
  bidderId: string;
  placedAt: string;
}

export interface AuctionClosedMessage {
  auctionItemId: string;
}

export type AuctionWebSocketMessage = BidUpdateMessage | AuctionClosedMessage;

function isBidUpdate(msg: AuctionWebSocketMessage): msg is BidUpdateMessage {
  return 'bidAmount' in msg;
}

function isAuctionClosed(msg: AuctionWebSocketMessage): msg is AuctionClosedMessage {
  return !('bidAmount' in msg);
}

interface UseAuctionWebSocketOptions {
  auctionItemId: string | undefined;
  enabled?: boolean;
  onBidUpdate?: (message: BidUpdateMessage) => void;
  onAuctionClosed?: (message: AuctionClosedMessage) => void;
}

export const useAuctionWebSocket = ({
  auctionItemId,
  enabled = true,
  onBidUpdate,
  onAuctionClosed,
}: UseAuctionWebSocketOptions) => {
  const { token } = useAuth();
  const clientRef = useRef<Client | null>(null);
  const connectedRef = useRef(false);

  // Use refs to store the latest callbacks to avoid stale closures
  // This prevents WebSocket reconnection when callbacks change
  const onBidUpdateRef = useRef(onBidUpdate);
  const onAuctionClosedRef = useRef(onAuctionClosed);

  // Update refs synchronously using useLayoutEffect to avoid race conditions
  // useLayoutEffect runs synchronously after DOM updates, before browser yields
  useLayoutEffect(() => {
    onBidUpdateRef.current = onBidUpdate;
    onAuctionClosedRef.current = onAuctionClosed;
  });

  useEffect(() => {
    if (!auctionItemId || !enabled) {
      return;
    }

    const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8081/ws';

    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      debug: (str) => {
        console.log('[STOMP]', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    client.onConnect = () => {
      console.log('Connected to auction WebSocket');
      connectedRef.current = true;

      client.subscribe(
        `/topic/auction/${auctionItemId}`,
        (message: IMessage) => {
          try {
            const parsed: AuctionWebSocketMessage = JSON.parse(message.body);

            if (isBidUpdate(parsed)) {
              console.log('Received bid update:', parsed);
              onBidUpdateRef.current?.(parsed);
            } else if (isAuctionClosed(parsed)) {
              console.log('Received auction closed:', parsed);
              onAuctionClosedRef.current?.(parsed);
            }
          } catch (error) {
            console.error('Failed to parse WebSocket message:', error);
          }
        },
        { auctionItemId }
      );
    };

    client.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message']);
      console.error('Details:', frame.body);
    };

    client.onWebSocketClose = (event) => {
      console.log('WebSocket closed:', event.code, event.reason);
      connectedRef.current = false;
    };

    client.onDisconnect = () => {
      console.log('Disconnected from auction WebSocket');
      connectedRef.current = false;
    };

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        console.log('Deactivating auction WebSocket');
        clientRef.current.deactivate();
        clientRef.current = null;
        connectedRef.current = false;
      }
    };
  }, [auctionItemId, enabled, token]);

  return {
    isConnected: connectedRef.current,
  };
};

export { isBidUpdate, isAuctionClosed };
