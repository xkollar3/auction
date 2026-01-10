package edu.fi.muni.cz.marketplace.auction_bidding.controller;

import edu.fi.muni.cz.marketplace.auction_bidding.command.AddAuctionItemCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.command.PlaceBidCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.AddAuctionItemRequest;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.AddAuctionItemResponse;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.PlaceBidRequest;
import edu.fi.muni.cz.marketplace.config.exception.HttpException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

  private final CommandGateway commandGateway;

  /**
   * Create a new auction item. Only sellers with verified Stripe Connect accounts can create auctions.
   */
  @PostMapping
  public ResponseEntity<AddAuctionItemResponse> addAuctionItem(
      @RequestBody AddAuctionItemRequest request,
      @AuthenticationPrincipal Jwt jwt) {

    UUID userId = getUserId(jwt);
    UUID auctionItemId = UUID.randomUUID();

    log.info("Creating auction item for seller: {}", userId);
    commandGateway.sendAndWait(new AddAuctionItemCommand(
        auctionItemId,
        userId,
        request.title(),
        request.description(),
        request.startingPrice(),
        request.auctionEndTime()));

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new AddAuctionItemResponse(auctionItemId));
  }

  /**
   * Place a bid on an auction item.
   */
  @PostMapping("/{auctionItemId}/bids")
  public ResponseEntity<String> placeBid(
      @PathVariable UUID auctionItemId,
      @RequestBody PlaceBidRequest request,
      @AuthenticationPrincipal Jwt jwt) {

    UUID userId = getUserId(jwt);

    log.info("Placing bid on auction {} by bidder {}", auctionItemId, userId);
    try {
      commandGateway.sendAndWait(new PlaceBidCommand(
          auctionItemId,
          userId,
          request.bidAmount()));
    } catch (CommandExecutionException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }

    return ResponseEntity.status(HttpStatus.ACCEPTED).body("Bid placed successfully");
  }

  private UUID getUserId(Jwt jwt) {
    String keycloakUserId = jwt.getSubject();
    if (keycloakUserId == null) {
      throw new HttpException(401, "No subject in token");
    }
    return UUID.fromString(keycloakUserId);
  }
}
