package edu.fi.muni.cz.marketplace.auction_bidding.controller;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionItemCategory;
import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionStatus;
import edu.fi.muni.cz.marketplace.auction_bidding.command.AddAuctionItemCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.command.PlaceBidCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.AddAuctionItemRequest;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.AddAuctionItemResponse;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.AuctionItemResponse;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.BrowseAuctionItemsResponse;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.PlaceBidRequest;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.PlaceBidResponse;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.SellerAuctionItemResponse;
import edu.fi.muni.cz.marketplace.auction_bidding.query.AuctionItemReadModel;
import edu.fi.muni.cz.marketplace.auction_bidding.query.AuctionSortOption;
import edu.fi.muni.cz.marketplace.auction_bidding.query.BrowseAuctionItemsQuery;
import edu.fi.muni.cz.marketplace.auction_bidding.query.FindAuctionItemByIdQuery;
import edu.fi.muni.cz.marketplace.auction_bidding.query.FindSellerAuctionItemsQuery;
import edu.fi.muni.cz.marketplace.config.exception.HttpException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

  private final CommandGateway commandGateway;
  private final QueryGateway queryGateway;

  /**
   * Browse auction items with filtering, sorting, and pagination.
   */
  @GetMapping
  public ResponseEntity<BrowseAuctionItemsResponse> browseAuctions(
      @RequestParam(required = false) AuctionItemCategory category,
      @RequestParam(defaultValue = "ENDING_SOON") AuctionSortOption sort,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    @SuppressWarnings("unchecked")
    Page<AuctionItemReadModel> result = queryGateway.query(
        new BrowseAuctionItemsQuery(category, sort, search, page, size),
        ResponseTypes.instanceOf(Page.class)).join();

    var items = result.getContent().stream()
        .map(AuctionItemResponse::from)
        .toList();

    return ResponseEntity.ok(new BrowseAuctionItemsResponse(
        items,
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages()));
  }

  /**
   * Get an auction item by ID.
   */
  @GetMapping("/{auctionItemId}")
  public ResponseEntity<AuctionItemResponse> getAuctionItem(@PathVariable UUID auctionItemId) {
    AuctionItemReadModel readModel = queryGateway.query(
        new FindAuctionItemByIdQuery(auctionItemId),
        AuctionItemReadModel.class).join();

    if (readModel == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(AuctionItemResponse.from(readModel));
  }

  /**
   * Get seller's auction items filtered by status.
   * For ACTIVE items, sorted by ending soon first.
   */
  @GetMapping("/seller/dashboard")
  public ResponseEntity<List<SellerAuctionItemResponse>> getSellerAuctions(
      @RequestParam AuctionStatus status,
      @AuthenticationPrincipal Jwt jwt) {
    UUID sellerId = getUserId(jwt);
    log.info("Retrieving dashboard for seller: " + sellerId);

    List<AuctionItemReadModel> items = queryGateway.query(
        new FindSellerAuctionItemsQuery(sellerId, status),
        ResponseTypes.multipleInstancesOf(AuctionItemReadModel.class)).join();

    var response = items.stream()
        .map(SellerAuctionItemResponse::from)
        .toList();

    return ResponseEntity.ok(response);
  }

  /**
   * Create a new auction item. Only sellers with verified Stripe Connect accounts
   * can create auctions.
   */
  @PostMapping
  public ResponseEntity<AddAuctionItemResponse> addAuctionItem(
      @RequestBody AddAuctionItemRequest request,
      @AuthenticationPrincipal Jwt jwt) {

    UUID userId = getUserId(jwt);
    UUID auctionItemId = UUID.randomUUID();

    // Todo: check stripe id from jwt
    log.info("Creating auction item for seller: {}", userId);
    commandGateway.sendAndWait(new AddAuctionItemCommand(
        auctionItemId,
        userId,
        request.title(),
        request.description(),
        request.startingPrice(),
        request.category(),
        request.auctionEndTime()));

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new AddAuctionItemResponse(auctionItemId));
  }

  /**
   * Place a bid on an auction item.
   */
  @PostMapping("/{auctionItemId}/bids")
  public ResponseEntity<PlaceBidResponse> placeBid(
      @PathVariable UUID auctionItemId,
      @RequestBody PlaceBidRequest request,
      @AuthenticationPrincipal Jwt jwt) {

    UUID userId = getUserId(jwt);

    log.info("Placing bid on auction {} by bidder {}", auctionItemId, userId);
    PlaceBidResponse result = commandGateway.sendAndWait(new PlaceBidCommand(
        auctionItemId,
        UUID.randomUUID(),
        userId,
        request.bidAmount()));
    if (result == null) {
      return ResponseEntity.notFound().build();
    }
    if (!result.isAccepted()) {
      return ResponseEntity.badRequest().body(result);
    }

    return ResponseEntity.accepted().body(result);
  }

  private UUID getUserId(Jwt jwt) {
    String keycloakUserId = jwt.getSubject();
    if (keycloakUserId == null) {
      throw new HttpException(401, "No subject in token");
    }
    return UUID.fromString(keycloakUserId);
  }
}
