package edu.fi.muni.cz.marketplace.order.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import edu.fi.muni.cz.marketplace.order.aggregate.TrackingStatusMilestone;
import edu.fi.muni.cz.marketplace.order.command.UpdateTrackingStatusCommand;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = OrderWebHookController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "ship24.webhook-secret=test-secret-123")
class OrderWebHookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CommandGateway commandGateway;

  private static final String VALID_SECRET = "test-secret-123";
  private static final String WEBHOOK_ENDPOINT = "/api/v1/webhooks/orders/ship24";

  @Test
  void handleShip24Webhook_validRequestWithSingleEvent_dispatchesCommand() throws Exception {
    UUID orderId = UUID.randomUUID();
    String eventId = "event-001";

    String payload = """
        {
          "trackings": [
            {
              "tracker": {
                "trackerId": "tracker-123",
                "trackingNumber": "TRACK123",
                "shipmentReference": "%s"
              },
              "events": [
                {
                  "eventId": "%s",
                  "status": "Delivered to the addressee",
                  "occurrenceDatetime": "2025-03-04T17:12:57Z",
                  "statusCode": "DE",
                  "statusCategory": "delivered",
                  "statusMilestone": "delivered"
                }
              ]
            }
          ]
        }
        """.formatted(orderId, eventId);

    mockMvc.perform(post(WEBHOOK_ENDPOINT)
        .with(csrf())
        .header("Authorization", "Bearer " + VALID_SECRET)
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
        .andExpect(status().isOk())
        .andExpect(content().string("Webhook processed successfully"));

    ArgumentCaptor<UpdateTrackingStatusCommand> commandCaptor = ArgumentCaptor
        .forClass(UpdateTrackingStatusCommand.class);
    verify(commandGateway, times(1)).send(commandCaptor.capture());

    UpdateTrackingStatusCommand command = commandCaptor.getValue();
    assertEquals(orderId, command.getOrderId());
    assertEquals(eventId, command.getEventId());
    assertEquals(TrackingStatusMilestone.DELIVERED, command.getStatusMilestone());
    assertEquals("Delivered to the addressee", command.getEventStatus());
    assertEquals(Instant.parse("2025-03-04T17:12:57Z"), command.getEventOccurredAt());
  }

  @Test
  void handleShip24Webhook_multipleTrackings_dispatchesMultipleCommands() throws Exception {
    UUID orderId1 = UUID.randomUUID();
    UUID orderId2 = UUID.randomUUID();

    String payload = """
        {
          "trackings": [
            {
              "tracker": {
                "trackerId": "tracker-1",
                "trackingNumber": "TRACK001",
                "shipmentReference": "%s"
              },
              "events": [
                {
                  "eventId": "event-001",
                  "status": "Delivered",
                  "occurrenceDatetime": "2025-03-04T17:12:57Z",
                  "statusCode": "DE",
                  "statusCategory": "delivered",
                  "statusMilestone": "delivered"
                }
              ]
            },
            {
              "tracker": {
                "trackerId": "tracker-2",
                "trackingNumber": "TRACK002",
                "shipmentReference": "%s"
              },
              "events": [
                {
                  "eventId": "event-002",
                  "status": "In transit",
                  "occurrenceDatetime": "2025-03-04T18:30:00Z",
                  "statusCode": "IT",
                  "statusCategory": "in_transit",
                  "statusMilestone": "in_transit"
                }
              ]
            }
          ]
        }
        """.formatted(orderId1, orderId2);

    mockMvc.perform(post(WEBHOOK_ENDPOINT)
        .with(csrf())
        .header("Authorization", "Bearer " + VALID_SECRET)
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
        .andExpect(status().isOk());

    ArgumentCaptor<UpdateTrackingStatusCommand> commandCaptor = ArgumentCaptor
        .forClass(UpdateTrackingStatusCommand.class);
    verify(commandGateway, times(2)).send(commandCaptor.capture());

    List<UpdateTrackingStatusCommand> commands = commandCaptor.getAllValues();
    assertEquals(2, commands.size());

    assertEquals(orderId1, commands.get(0).getOrderId());
    assertEquals("event-001", commands.get(0).getEventId());
    assertEquals(TrackingStatusMilestone.DELIVERED, commands.get(0).getStatusMilestone());

    assertEquals(orderId2, commands.get(1).getOrderId());
    assertEquals("event-002", commands.get(1).getEventId());
    assertEquals(TrackingStatusMilestone.IN_TRANSIT, commands.get(1).getStatusMilestone());
  }

  @Test
  void handleShip24Webhook_sameShipmentMultipleEvents_dispatchesCommandForEachEvent()
      throws Exception {
    UUID orderId = UUID.randomUUID();

    // Each tracking update contains exactly ONE event
    String payload = """
        {
          "trackings": [
            {
              "tracker": {
                "trackerId": "tracker-123",
                "trackingNumber": "TRACK123",
                "shipmentReference": "%s"
              },
              "events": [
                {
                  "eventId": "event-001",
                  "status": "In transit",
                  "occurrenceDatetime": "2025-03-04T17:12:57Z",
                  "statusCode": "IT",
                  "statusCategory": "in_transit",
                  "statusMilestone": "in_transit"
                }
              ]
            },
            {
              "tracker": {
                "trackerId": "tracker-123",
                "trackingNumber": "TRACK123",
                "shipmentReference": "%s"
              },
              "events": [
                {
                  "eventId": "event-002",
                  "status": "Out for delivery",
                  "occurrenceDatetime": "2025-03-04T18:30:00Z",
                  "statusCode": "OD",
                  "statusCategory": "out_for_delivery",
                  "statusMilestone": "out_for_delivery"
                }
              ]
            }
          ]
        }
        """.formatted(orderId, orderId);

    mockMvc.perform(post(WEBHOOK_ENDPOINT)
        .with(csrf())
        .header("Authorization", "Bearer " + VALID_SECRET)
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
        .andExpect(status().isOk());

    ArgumentCaptor<UpdateTrackingStatusCommand> commandCaptor = ArgumentCaptor
        .forClass(UpdateTrackingStatusCommand.class);
    verify(commandGateway, times(2)).send(commandCaptor.capture());

    List<UpdateTrackingStatusCommand> commands = commandCaptor.getAllValues();
    assertEquals(2, commands.size());

    assertEquals(orderId, commands.get(0).getOrderId());
    assertEquals("event-001", commands.get(0).getEventId());
    assertEquals(TrackingStatusMilestone.IN_TRANSIT, commands.get(0).getStatusMilestone());

    assertEquals(orderId, commands.get(1).getOrderId());
    assertEquals("event-002", commands.get(1).getEventId());
    assertEquals(TrackingStatusMilestone.OUT_FOR_DELIVERY, commands.get(1).getStatusMilestone());
  }

  @Test
  void handleShip24Webhook_invalidWebhookSecret_returnsUnauthorized() throws Exception {
    UUID orderId = UUID.randomUUID();

    String payload = """
        {
          "trackings": [
            {
              "tracker": {
                "trackerId": "tracker-1",
                "trackingNumber": "TRACK001",
                "shipmentReference": "%s"
              },
              "events": [
                {
                  "eventId": "event-001",
                  "status": "Delivered",
                  "occurrenceDatetime": "2025-03-04T17:12:57Z",
                  "statusCode": "DE",
                  "statusCategory": "delivered",
                  "statusMilestone": "delivered"
                }
              ]
            }
          ]
        }
        """.formatted(orderId);

    mockMvc.perform(post(WEBHOOK_ENDPOINT)
        .with(csrf())
        .header("Authorization", "Bearer wrong-secret")
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Invalid webhook secret"));

    verify(commandGateway, never()).send(any());
  }

  @Test
  void handleShip24Webhook_missingAuthorizationHeader_returnsUnauthorized() throws Exception {
    UUID orderId = UUID.randomUUID();

    String payload = """
        {
          "trackings": [
            {
              "tracker": {
                "trackerId": "tracker-1",
                "trackingNumber": "TRACK001",
                "shipmentReference": "%s"
              },
              "events": [
                {
                  "eventId": "event-001",
                  "status": "Delivered",
                  "occurrenceDatetime": "2025-03-04T17:12:57Z",
                  "statusCode": "DE",
                  "statusCategory": "delivered",
                  "statusMilestone": "delivered"
                }
              ]
            }
          ]
        }
        """.formatted(orderId);

    mockMvc.perform(post(WEBHOOK_ENDPOINT)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Invalid webhook secret"));

    verify(commandGateway, never()).send(any());
  }

  @Test
  void handleShip24Webhook_invalidShipmentReference_logsErrorAndContinuesProcessing()
      throws Exception {
    UUID validOrderId = UUID.randomUUID();

    String payload = """
        {
          "trackings": [
            {
              "tracker": {
                "trackerId": "tracker-1",
                "trackingNumber": "TRACK001",
                "shipmentReference": "not-a-valid-uuid"
              },
              "events": [
                {
                  "eventId": "event-001",
                  "status": "Delivered",
                  "occurrenceDatetime": "2025-03-04T17:12:57Z",
                  "statusCode": "DE",
                  "statusCategory": "delivered",
                  "statusMilestone": "delivered"
                }
              ]
            },
            {
              "tracker": {
                "trackerId": "tracker-2",
                "trackingNumber": "TRACK002",
                "shipmentReference": "%s"
              },
              "events": [
                {
                  "eventId": "event-002",
                  "status": "In transit",
                  "occurrenceDatetime": "2025-03-04T17:12:57Z",
                  "statusCode": "IT",
                  "statusCategory": "in_transit",
                  "statusMilestone": "in_transit"
                }
              ]
            }
          ]
        }
        """.formatted(validOrderId);

    mockMvc.perform(post(WEBHOOK_ENDPOINT)
        .with(csrf())
        .header("Authorization", "Bearer " + VALID_SECRET)
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
        .andExpect(status().isOk())
        .andExpect(content().string("Webhook processed successfully"));

    ArgumentCaptor<UpdateTrackingStatusCommand> commandCaptor = ArgumentCaptor
        .forClass(UpdateTrackingStatusCommand.class);
    verify(commandGateway, times(1)).send(commandCaptor.capture());

    UpdateTrackingStatusCommand command = commandCaptor.getValue();
    assertEquals(validOrderId, command.getOrderId());
    assertEquals("event-002", command.getEventId());
  }

  @Test
  void handleShip24Webhook_allStatusMilestones_mapsCorrectly() throws Exception {
    UUID orderId = UUID.randomUUID();

    // Each tracking update contains exactly ONE event - testing all milestone types
    String payload = """
        {
          "trackings": [
            {
              "tracker": {"trackerId": "t1", "trackingNumber": "TRACK001", "shipmentReference": "%s"},
              "events": [{"eventId": "event-001", "status": "Info received", "occurrenceDatetime": "2025-03-04T17:12:57Z", "statusCode": "IR", "statusCategory": "info_received", "statusMilestone": "info_received"}]
            },
            {
              "tracker": {"trackerId": "t1", "trackingNumber": "TRACK001", "shipmentReference": "%s"},
              "events": [{"eventId": "event-002", "status": "In transit", "occurrenceDatetime": "2025-03-04T17:12:57Z", "statusCode": "IT", "statusCategory": "in_transit", "statusMilestone": "in_transit"}]
            },
            {
              "tracker": {"trackerId": "t1", "trackingNumber": "TRACK001", "shipmentReference": "%s"},
              "events": [{"eventId": "event-003", "status": "Out for delivery", "occurrenceDatetime": "2025-03-04T17:12:57Z", "statusCode": "OD", "statusCategory": "out_for_delivery", "statusMilestone": "out_for_delivery"}]
            },
            {
              "tracker": {"trackerId": "t1", "trackingNumber": "TRACK001", "shipmentReference": "%s"},
              "events": [{"eventId": "event-004", "status": "Failed attempt", "occurrenceDatetime": "2025-03-04T17:12:57Z", "statusCode": "FA", "statusCategory": "failed_attempt", "statusMilestone": "failed_attempt"}]
            },
            {
              "tracker": {"trackerId": "t1", "trackingNumber": "TRACK001", "shipmentReference": "%s"},
              "events": [{"eventId": "event-005", "status": "Available for pickup", "occurrenceDatetime": "2025-03-04T17:12:57Z", "statusCode": "AP", "statusCategory": "available_for_pickup", "statusMilestone": "available_for_pickup"}]
            },
            {
              "tracker": {"trackerId": "t1", "trackingNumber": "TRACK001", "shipmentReference": "%s"},
              "events": [{"eventId": "event-006", "status": "Delivered", "occurrenceDatetime": "2025-03-04T17:12:57Z", "statusCode": "DE", "statusCategory": "delivered", "statusMilestone": "delivered"}]
            },
            {
              "tracker": {"trackerId": "t1", "trackingNumber": "TRACK001", "shipmentReference": "%s"},
              "events": [{"eventId": "event-007", "status": "Exception", "occurrenceDatetime": "2025-03-04T17:12:57Z", "statusCode": "EX", "statusCategory": "exception", "statusMilestone": "exception"}]
            },
            {
              "tracker": {"trackerId": "t1", "trackingNumber": "TRACK001", "shipmentReference": "%s"},
              "events": [{"eventId": "event-008", "status": "Pending", "occurrenceDatetime": "2025-03-04T17:12:57Z", "statusCode": "PE", "statusCategory": "pending", "statusMilestone": "pending"}]
            }
          ]
        }
        """
        .formatted(orderId, orderId, orderId, orderId, orderId, orderId, orderId, orderId);

    mockMvc.perform(post(WEBHOOK_ENDPOINT)
        .with(csrf())
        .header("Authorization", "Bearer " + VALID_SECRET)
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
        .andExpect(status().isOk());

    ArgumentCaptor<UpdateTrackingStatusCommand> commandCaptor = ArgumentCaptor
        .forClass(UpdateTrackingStatusCommand.class);
    verify(commandGateway, times(8)).send(commandCaptor.capture());

    List<UpdateTrackingStatusCommand> commands = commandCaptor.getAllValues();
    assertEquals(8, commands.size());

    assertEquals(TrackingStatusMilestone.INFO_RECEIVED, commands.get(0).getStatusMilestone());
    assertEquals(TrackingStatusMilestone.IN_TRANSIT, commands.get(1).getStatusMilestone());
    assertEquals(TrackingStatusMilestone.OUT_FOR_DELIVERY, commands.get(2).getStatusMilestone());
    assertEquals(TrackingStatusMilestone.FAILED_ATTEMPT, commands.get(3).getStatusMilestone());
    assertEquals(TrackingStatusMilestone.AVAILABLE_FOR_PICKUP,
        commands.get(4).getStatusMilestone());
    assertEquals(TrackingStatusMilestone.DELIVERED, commands.get(5).getStatusMilestone());
    assertEquals(TrackingStatusMilestone.EXCEPTION, commands.get(6).getStatusMilestone());
    assertEquals(TrackingStatusMilestone.PENDING, commands.get(7).getStatusMilestone());
  }

  @Test
  void handleShip24Webhook_complexScenarioLikeDocumentation_processesAllEventsCorrectly()
      throws Exception {
    UUID orderId1 = UUID.randomUUID();
    UUID orderId2 = UUID.randomUUID();

    // This JSON matches the format from the user's documentation example
    // Each tracking update contains exactly ONE event
    String payload = """
        {
          "trackings": [
            {
              "tracker": {
                "trackerId": "26148317-7502-d3ac-44a9-546d240ac0dd",
                "trackingNumber": "S24DEMO456393",
                "shipmentReference": "%s"
              },
              "events": [
                {
                  "eventId": "ee8ebe96-4eae-4a91-9a99-8f3afa6a0f46",
                  "status": "Delivered to the addressee",
                  "occurrenceDatetime": "2025-03-04T17:12:57Z",
                  "statusCode": "DE",
                  "statusCategory": "delivered",
                  "statusMilestone": "delivered"
                }
              ]
            },
            {
              "tracker": {
                "trackerId": "cf9f566a-493b-d686-41f3-b085bb4e429f",
                "trackingNumber": "S24DEMO987452",
                "shipmentReference": "%s"
              },
              "events": [
                {
                  "eventId": "e726b49a-bea6-26a8-4610-553548ca765b",
                  "status": "Arrived in destination country",
                  "occurrenceDatetime": "2025-02-03T12:17:00Z",
                  "statusCode": "AD",
                  "statusCategory": "in_transit",
                  "statusMilestone": "in_transit"
                }
              ]
            },
            {
              "tracker": {
                "trackerId": "cf9f566a-493b-d686-41f3-b085bb4e429f",
                "trackingNumber": "S24DEMO987452",
                "shipmentReference": "%s"
              },
              "events": [
                {
                  "eventId": "bbc4af45-9c99-749b-4945-e3211568ddb7",
                  "status": "In transit",
                  "occurrenceDatetime": "2025-02-02T20:10:00Z",
                  "statusCode": "IT",
                  "statusCategory": "in_transit",
                  "statusMilestone": "in_transit"
                }
              ]
            }
          ]
        }
        """.formatted(orderId1, orderId2, orderId2);

    mockMvc.perform(post(WEBHOOK_ENDPOINT)
        .with(csrf())
        .header("Authorization", "Bearer " + VALID_SECRET)
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
        .andExpect(status().isOk())
        .andExpect(content().string("Webhook processed successfully"));

    ArgumentCaptor<UpdateTrackingStatusCommand> commandCaptor = ArgumentCaptor
        .forClass(UpdateTrackingStatusCommand.class);
    verify(commandGateway, times(3)).send(commandCaptor.capture());

    List<UpdateTrackingStatusCommand> commands = commandCaptor.getAllValues();
    assertEquals(3, commands.size());

    assertEquals(orderId1, commands.get(0).getOrderId());
    assertEquals("ee8ebe96-4eae-4a91-9a99-8f3afa6a0f46", commands.get(0).getEventId());
    assertEquals(TrackingStatusMilestone.DELIVERED, commands.get(0).getStatusMilestone());
    assertEquals("Delivered to the addressee", commands.get(0).getEventStatus());
    assertEquals(Instant.parse("2025-03-04T17:12:57Z"), commands.get(0).getEventOccurredAt());

    assertEquals(orderId2, commands.get(1).getOrderId());
    assertEquals("e726b49a-bea6-26a8-4610-553548ca765b", commands.get(1).getEventId());
    assertEquals(TrackingStatusMilestone.IN_TRANSIT, commands.get(1).getStatusMilestone());
    assertEquals("Arrived in destination country", commands.get(1).getEventStatus());
    assertEquals(Instant.parse("2025-02-03T12:17:00Z"), commands.get(1).getEventOccurredAt());

    assertEquals(orderId2, commands.get(2).getOrderId());
    assertEquals("bbc4af45-9c99-749b-4945-e3211568ddb7", commands.get(2).getEventId());
    assertEquals(TrackingStatusMilestone.IN_TRANSIT, commands.get(2).getStatusMilestone());
    assertEquals("In transit", commands.get(2).getEventStatus());
    assertEquals(Instant.parse("2025-02-02T20:10:00Z"), commands.get(2).getEventOccurredAt());
  }
}
