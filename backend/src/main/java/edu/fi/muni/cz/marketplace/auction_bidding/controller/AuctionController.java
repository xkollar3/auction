package edu.fi.muni.cz.marketplace.auction_bidding.controller;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionItemCategory;
import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionStatus;
import edu.fi.muni.cz.marketplace.auction_bidding.command.AddAuctionItemCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.command.PlaceBidCommand;
import edu.fi.muni.cz.marketplace.auction_item.command.AddImagesToAuctionCommand;
import edu.fi.muni.cz.marketplace.auction_item.service.StorageService;
import edu.fi.muni.cz.marketplace.auction_item.dto.AuctionItemImagesResponse;
import edu.fi.muni.cz.marketplace.auction_item.query.AuctionItemImageReadModel;
import edu.fi.muni.cz.marketplace.auction_item.query.FindAuctionItemImagesQuery;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.AddAuctionItemRequest;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.AddAuctionItemResponse;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.AuctionItemDetailResponse;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.AuctionItemResponse;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.BrowseAuctionItemsResponse;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.BrowseAuctionItemsResult;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.PlaceBidRequest;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.PlaceBidResponse;
import edu.fi.muni.cz.marketplace.auction_bidding.query.AuctionItemReadModel;
import edu.fi.muni.cz.marketplace.auction_bidding.query.AuctionSortOption;
import edu.fi.muni.cz.marketplace.auction_bidding.query.BrowseAuctionItemsQuery;
import edu.fi.muni.cz.marketplace.auction_bidding.query.FindAuctionItemByIdQuery;
import edu.fi.muni.cz.marketplace.auction_bidding.query.FindBidderAuctionItemsQuery;
import edu.fi.muni.cz.marketplace.auction_bidding.query.FindSellerAuctionItemsQuery;
import edu.fi.muni.cz.marketplace.config.exception.HttpException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
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
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

  private static final String AUCTION_IMAGES_FOLDER = "auctions";

  private final CommandGateway commandGateway;
  private final QueryGateway queryGateway;
  private final StorageService storageService;

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
    log.info("Searching for auctions in browse endpoint");

    BrowseAuctionItemsResult result = queryGateway.query(
        new BrowseAuctionItemsQuery(category, sort, search, page, size),
        BrowseAuctionItemsResult.class).join();

    var items = result.getItems().stream()
        .map(AuctionItemResponse::from)
        .toList();

    return ResponseEntity.ok(new BrowseAuctionItemsResponse(
        items,
        result.getPage(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages()));
  }

  /**
   * Get an auction item by ID with recent bids.
   */
  @GetMapping("/{auctionItemId}")
  public ResponseEntity<AuctionItemDetailResponse> getAuctionItem(@PathVariable UUID auctionItemId) {
    AuctionItemReadModel readModel = queryGateway.query(
        new FindAuctionItemByIdQuery(auctionItemId),
        AuctionItemReadModel.class).join();

    if (readModel == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(AuctionItemDetailResponse.from(readModel));
  }

  /**
   * Get seller's auction items filtered by status.
   * For ACTIVE items, sorted by ending soon first.
   */
  @GetMapping("/seller/{sellerId}")
  public ResponseEntity<List<AuctionItemResponse>> getSellerAuctions(
      @PathVariable UUID sellerId,
      @RequestParam AuctionStatus status) {
    log.info("Retrieving auctions for seller: {}", sellerId);

    List<AuctionItemReadModel> items = queryGateway.query(
        new FindSellerAuctionItemsQuery(sellerId, status),
        ResponseTypes.multipleInstancesOf(AuctionItemReadModel.class)).join();

    var response = items.stream()
        .map(AuctionItemResponse::from)
        .toList();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/my-bids")
  public ResponseEntity<List<AuctionItemResponse>> getMyBids(
      @RequestParam AuctionStatus status,
      @AuthenticationPrincipal Jwt jwt) {
    UUID userId = getUserId(jwt);

    List<AuctionItemReadModel> items = queryGateway.query(
        new FindBidderAuctionItemsQuery(userId, status),
        ResponseTypes.multipleInstancesOf(AuctionItemReadModel.class)).join();

    var response = items.stream()
        .map(AuctionItemResponse::from)
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
    String firstName = jwt.getClaimAsString("given_name");
    String lastName = jwt.getClaimAsString("family_name");

    // Todo: check stripe id from jwt
    log.info("Creating auction item for seller: {}", userId);
    commandGateway.sendAndWait(new AddAuctionItemCommand(
        auctionItemId,
        userId,
        firstName,
        lastName,
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

  /**
   * Upload images for an auction item.
   */
  @PostMapping("/{auctionItemId}/images")
  public ResponseEntity<Void> uploadImages(
      @PathVariable UUID auctionItemId,
      @RequestParam("files") List<MultipartFile> files,
      @AuthenticationPrincipal Jwt jwt) throws IOException {

    log.info("Uploading {} images for auction {}", files.size(), auctionItemId);

    List<String> imageUrls = new ArrayList<>();
    for (MultipartFile file : files) {
      String url = storageService.uploadFile(file, AUCTION_IMAGES_FOLDER + "/" + auctionItemId);
      imageUrls.add(url);
    }

    commandGateway.sendAndWait(new AddImagesToAuctionCommand(auctionItemId, imageUrls));

    return ResponseEntity.accepted().build();
  }

  /**
   * Get images for an auction item.
   */
  @GetMapping("/{auctionItemId}/images")
  public ResponseEntity<AuctionItemImagesResponse> getImages(@PathVariable UUID auctionItemId) {
    log.info("Getting images for auction {}", auctionItemId);

    List<AuctionItemImageReadModel> images = queryGateway.query(
        new FindAuctionItemImagesQuery(auctionItemId),
        ResponseTypes.multipleInstancesOf(AuctionItemImageReadModel.class)).join();

    List<String> imageUrls = images.stream()
        .map(AuctionItemImageReadModel::getImageUrl)
        .toList();

    return ResponseEntity.ok(new AuctionItemImagesResponse(imageUrls));
  }

  private UUID getUserId(Jwt jwt) {
    String keycloakUserId = jwt.getSubject();
    if (keycloakUserId == null) {
      throw new HttpException(401, "No subject in token");
    }
    return UUID.fromString(keycloakUserId);
  }
}
