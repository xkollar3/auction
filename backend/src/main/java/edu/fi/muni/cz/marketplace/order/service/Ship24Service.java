package edu.fi.muni.cz.marketplace.order.service;

import org.springframework.stereotype.Service;

import edu.fi.muni.cz.marketplace.order.client.Ship24ApiClient;
import edu.fi.muni.cz.marketplace.order.client.dto.CreateTrackerRequest;
import edu.fi.muni.cz.marketplace.order.client.dto.CreateTrackerResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for interacting with Ship24 shipment tracking API.
 * <p>
 * This service abstracts the underlying Feign client and provides a clean interface
 * for creating and managing shipment trackers. It handles all API communication,
 * error translation, and response unwrapping.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Ship24Service {

  private final Ship24ApiClient ship24ApiClient;

  /**
   * Creates a shipment tracker in Ship24 for the given tracking number.
   * <p>
   * The tracker allows monitoring the shipment status through Ship24's tracking system.
   * The shipmentReference (typically your order ID) is stored with the tracker and will
   * be included in webhooks and API responses for correlation.
   * </p>
   *
   * @param trackingNumber    the carrier's tracking number (5-50 characters)
   * @param shipmentReference your internal reference for this shipment (e.g., order ID)
   * @return the Ship24 tracker ID
   * @throws Ship24ServiceException if tracker creation fails due to API errors, invalid input,
   *                                or network issues
   */
  public String createTracker(String trackingNumber, String shipmentReference) {
    log.info("Creating Ship24 tracker for tracking number: {}, shipment reference: {}",
        trackingNumber, shipmentReference);

    try {
      CreateTrackerRequest request = new CreateTrackerRequest(trackingNumber, shipmentReference);
      CreateTrackerResponse response = ship24ApiClient.createTracker(request);

      if (response == null || response.data() == null || response.data().tracker() == null) {
        throw new Ship24ServiceException("Ship24 API returned empty response");
      }

      String trackerId = response.data().tracker().trackerId();
      log.info("Successfully created Ship24 tracker with ID: {}", trackerId);

      return trackerId;

    } catch (FeignException.BadRequest e) {
      log.error("Invalid request to Ship24 API: {}", e.getMessage());
      throw new Ship24ServiceException(
          "Invalid tracking number or shipment reference: " + e.getMessage(), e);

    } catch (FeignException.Unauthorized e) {
      log.error("Ship24 API authentication failed: {}", e.getMessage());
      throw new Ship24ServiceException("Ship24 authentication failed", e);

    } catch (FeignException.TooManyRequests e) {
      log.error("Ship24 API rate limit exceeded: {}", e.getMessage());
      throw new Ship24ServiceException("Ship24 rate limit exceeded, try again later", e);

    } catch (FeignException e) {
      log.error("Ship24 API error (status {}): {}", e.status(), e.getMessage());
      throw new Ship24ServiceException(
          "Ship24 API error: " + e.getMessage(), e);

    } catch (Exception e) {
      log.error("Unexpected error creating Ship24 tracker: {}", e.getMessage());
      throw new Ship24ServiceException(
          "Failed to create tracker: " + e.getMessage(), e);
    }
  }
}
