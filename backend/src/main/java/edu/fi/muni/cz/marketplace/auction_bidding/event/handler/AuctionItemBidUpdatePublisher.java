package edu.fi.muni.cz.marketplace.auction_bidding.event.handler;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import edu.fi.muni.cz.marketplace.auction_bidding.event.AuctionClosedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.HighestBidSetEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionItemBidUpdatePublisher {

  private final SimpMessagingTemplate messagingTemplate;
  private final ConcurrentHashMap<UUID, Set<String>> auctionSubscribers = new ConcurrentHashMap<>();

  @EventListener
  public void handleSubscribe(SessionSubscribeEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    var auctionIdHeaders = accessor.getNativeHeader("auctionItemId");
    if (auctionIdHeaders == null || auctionIdHeaders.isEmpty()) {
      return;
    }

    String sessionId = accessor.getSessionId();
    UUID auctionItemId = UUID.fromString(auctionIdHeaders.get(0));
    auctionSubscribers.computeIfAbsent(auctionItemId, k -> new CopyOnWriteArraySet<>()).add(sessionId);
    log.debug("Session {} subscribed to auction {}", sessionId, auctionItemId);
  }

  @EventListener
  public void handleDisconnect(SessionDisconnectEvent event) {
    String sessionId = event.getSessionId();
    auctionSubscribers.values().forEach(sessions -> sessions.remove(sessionId));
  }

  @EventHandler
  public void on(HighestBidSetEvent event) {
    UUID auctionItemId = event.getAuctionItemId();
    log.debug("Publishing bid update for auction {}", auctionItemId);

    BidUpdateMessage message = new BidUpdateMessage(
        auctionItemId,
        event.getBidAmount(),
        event.getBidderId(),
        event.getPlacedAt());

    messagingTemplate.convertAndSend("/topic/auction/" + auctionItemId, message);
  }

  @EventHandler
  public void on(AuctionClosedEvent event) {
    UUID auctionItemId = event.getAuctionItemId();
    log.debug("Publishing auction closed for auction {}", auctionItemId);

    AuctionClosedMessage message = new AuctionClosedMessage(auctionItemId);
    messagingTemplate.convertAndSend("/topic/auction/" + auctionItemId, message);
    auctionSubscribers.remove(auctionItemId);
  }

  public record BidUpdateMessage(UUID auctionItemId, BigDecimal bidAmount, UUID bidderId,
      Instant placedAt) {
  }

  public record AuctionClosedMessage(UUID auctionItemId) {
  }
}
