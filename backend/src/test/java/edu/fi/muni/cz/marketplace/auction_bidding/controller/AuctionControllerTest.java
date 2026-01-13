package edu.fi.muni.cz.marketplace.auction_bidding.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.fi.muni.cz.marketplace.auction_bidding.command.AddAuctionItemCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.command.PlaceBidCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.PlaceBidResponse;
import edu.fi.muni.cz.marketplace.config.SecurityConfig;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = AuctionController.class)
@Import(SecurityConfig.class)
class AuctionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CommandGateway commandGateway;

  @MockitoBean
  private JwtDecoder jwtDecoder;

  private static final String AUCTIONS_ENDPOINT = "/api/auctions";
  private static final UUID TEST_USER_ID = UUID.randomUUID();


  /**
   * Helper method to set up the mock to return a successful bid result.
   */
  private void givenBidWillBeAccepted() {
    when(commandGateway.<PlaceBidResponse>sendAndWait(any()))
        .thenReturn(PlaceBidResponse.success());
  }

  /**
   * Helper method to set up the mock to return a rejected bid result.
   */
  private void givenBidWillBeRejected(String reason) {
    when(commandGateway.<PlaceBidResponse>sendAndWait(any()))
        .thenReturn(PlaceBidResponse.failure(reason));
  }

  @Test
  void addAuctionItem_validRequest_createsAuctionAndReturnsCreated() throws Exception {
    Instant auctionEndTime = Instant.now().plus(7, ChronoUnit.DAYS);
    String auctionEndTimeStr = auctionEndTime.toString();

    String payload = """
        {
          "title": "Vintage Watch",
          "description": "A beautiful vintage watch from 1960",
          "startingPrice": 100.00,
          "auctionEndTime": "%s"
        }
        """.formatted(auctionEndTimeStr);

    mockMvc.perform(post(AUCTIONS_ENDPOINT)
            .with(csrf())
            .with(jwt().jwt(builder -> builder.subject(TEST_USER_ID.toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.auctionItemId").exists());

    ArgumentCaptor<AddAuctionItemCommand> commandCaptor = ArgumentCaptor
        .forClass(AddAuctionItemCommand.class);
    verify(commandGateway, times(1)).sendAndWait(commandCaptor.capture());

    AddAuctionItemCommand command = commandCaptor.getValue();
    assertEquals(TEST_USER_ID, command.getSellerId());
    assertEquals("Vintage Watch", command.getTitle());
    assertEquals("A beautiful vintage watch from 1960", command.getDescription());
    assertEquals(new BigDecimal("100.00"), command.getStartingPrice());
  }

  @Test
  void addAuctionItem_missingAuthentication_returnsUnauthorized() throws Exception {
    Instant auctionEndTime = Instant.now().plus(7, ChronoUnit.DAYS);

    String payload = """
        {
          "title": "Vintage Watch",
          "description": "A beautiful vintage watch from 1960",
          "startingPrice": 100.00,
          "auctionEndTime": "%s"
        }
        """.formatted(auctionEndTime.toString());

    // Without JWT, should fail
    mockMvc.perform(post(AUCTIONS_ENDPOINT)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isUnauthorized());

    verify(commandGateway, never()).sendAndWait(any());
  }

  @Test
  void placeBid_validBid_returnAccepted() throws Exception {
    // First create an auction item
    UUID auctionItemId = UUID.randomUUID();
    BigDecimal bidAmount = new BigDecimal("150.00");

    givenBidWillBeAccepted();

    String payload = """
        {
          "bidAmount": 150.00
        }
        """;

    mockMvc.perform(post(AUCTIONS_ENDPOINT + "/{auctionItemId}/bids", auctionItemId)
            .with(csrf())
            .with(jwt().jwt(builder -> builder.subject(TEST_USER_ID.toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isAccepted());

    ArgumentCaptor<Object> commandCaptor = ArgumentCaptor.forClass(Object.class);
    // Verify that command gateway was called only once for the PlaceBidCommand
    verify(commandGateway, times(1)).sendAndWait(commandCaptor.capture());

    PlaceBidCommand command = (PlaceBidCommand) commandCaptor.getAllValues().getFirst();
    assertEquals(auctionItemId, command.getAuctionItemId());
    assertEquals(TEST_USER_ID, command.getBidderId());
    assertEquals(bidAmount, command.getBidAmount());
  }

  @Test
  void placeBid_bidRejected_returnsBadRequest() throws Exception {
    // First create an auction item
    UUID auctionItemId = UUID.randomUUID();
    String rejectionReason = "Bid amount is too low or auction closed already";

    givenBidWillBeRejected(rejectionReason);

    String payload = """
        {
          "bidAmount": 50.00
        }
        """;

    mockMvc.perform(post(AUCTIONS_ENDPOINT + "/{auctionItemId}/bids", auctionItemId)
            .with(csrf())
            .with(jwt().jwt(builder -> builder.subject(TEST_USER_ID.toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isBadRequest());
  }

  @Test
  void placeBid_missingAuthentication_returnsUnauthorized() throws Exception {
    UUID auctionItemId = UUID.randomUUID();

    String payload = """
        {
          "bidAmount": 150.00
        }
        """;

    mockMvc.perform(post(AUCTIONS_ENDPOINT + "/{auctionItemId}/bids", auctionItemId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isUnauthorized());

    verify(commandGateway, never()).sendAndWait(any());
  }

  @Test
  void placeBid_multipleBidsFromSameUser_dispatchesMultipleCommands() throws Exception {
    // First create an auction item
    UUID auctionItemId = UUID.randomUUID();

    givenBidWillBeAccepted();

    String payload1 = """
        {
          "bidAmount": 150.00
        }
        """;

    String payload2 = """
        {
          "bidAmount": 200.00
        }
        """;

    // First bid
    mockMvc.perform(post(AUCTIONS_ENDPOINT + "/{auctionItemId}/bids", auctionItemId)
            .with(csrf())
            .with(jwt().jwt(builder -> builder.subject(TEST_USER_ID.toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload1))
        .andExpect(status().isAccepted());

    // Second bid
    mockMvc.perform(post(AUCTIONS_ENDPOINT + "/{auctionItemId}/bids", auctionItemId)
            .with(csrf())
            .with(jwt().jwt(builder -> builder.subject(TEST_USER_ID.toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload2))
        .andExpect(status().isAccepted());

    ArgumentCaptor<Object> commandCaptor = ArgumentCaptor.forClass(Object.class);
    verify(commandGateway, times(2)).sendAndWait(commandCaptor.capture());

    var allCommands = commandCaptor.getAllValues();
    PlaceBidCommand bid1 = (PlaceBidCommand) allCommands.getFirst();
    PlaceBidCommand bid2 = (PlaceBidCommand) allCommands.get(1);
    assertEquals(new BigDecimal("150.00"), bid1.getBidAmount());
    assertEquals(new BigDecimal("200.00"), bid2.getBidAmount());
  }

  @Test
  void placeBid_differentUsers_dispatchesCommandsWithDifferentBidderIds() throws Exception {
    // First create an auction item
    UUID auctionItemId = UUID.randomUUID();
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();

    givenBidWillBeAccepted();

    String payload = """
        {
          "bidAmount": 150.00
        }
        """;

    // First user's bid
    mockMvc.perform(post(AUCTIONS_ENDPOINT + "/{auctionItemId}/bids", auctionItemId)
            .with(csrf())
            .with(jwt().jwt(builder -> builder.subject(userId1.toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isAccepted());

    // Second user's bid
    mockMvc.perform(post(AUCTIONS_ENDPOINT + "/{auctionItemId}/bids", auctionItemId)
            .with(csrf())
            .with(jwt().jwt(builder -> builder.subject(userId2.toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isAccepted());

    ArgumentCaptor<Object> commandCaptor = ArgumentCaptor.forClass(Object.class);
    verify(commandGateway, times(2)).sendAndWait(commandCaptor.capture());

    var allCommands = commandCaptor.getAllValues();
    PlaceBidCommand bid1 = (PlaceBidCommand) allCommands.getFirst();
    PlaceBidCommand bid2 = (PlaceBidCommand) allCommands.get(1);
    assertEquals(userId1, bid1.getBidderId());
    assertEquals(userId2, bid2.getBidderId());
  }
}

