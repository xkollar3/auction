package edu.fi.muni.cz.marketplace.user.controller;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Account;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;

import edu.fi.muni.cz.marketplace.user.command.UpdateStripeSellerStatusCommand;
import edu.fi.muni.cz.marketplace.user.query.UserReadModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for handling Stripe webhooks.
 *
 * Currently supported events:
 * - account.updated: Updates the seller status (enabled/disabled) based on
 * charges_enabled and payouts_enabled flags.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final CommandGateway commandGateway;
    private final UserReadModelRepository userReadModelRepository;

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestHeader("Stripe-Signature") String sigHeader,
            @RequestBody String payload) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe signature");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Webhook processing error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
        }

        if ("account.updated".equals(event.getType())) {
            handleAccountUpdated(event);
        }

        return ResponseEntity.ok("Received");
    }

    private void handleAccountUpdated(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;

        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            log.warn("Event deserialization failed, API version mismatch?");
            return;
        }

        Account account = (Account) stripeObject;
        String accountId = account.getId();

        boolean chargesEnabled = Boolean.TRUE.equals(account.getChargesEnabled());
        boolean payoutsEnabled = Boolean.TRUE.equals(account.getPayoutsEnabled());
        boolean isEnabled = chargesEnabled && payoutsEnabled;

        log.info("Received account.updated for account {}: charges={}, payouts={}", accountId, chargesEnabled,
                payoutsEnabled);

        userReadModelRepository.findByStripeSellerAccountId(accountId).ifPresentOrElse(
                user -> {
                    log.info("Updating seller status for user {} to {}", user.getId(), isEnabled);
                    commandGateway.send(new UpdateStripeSellerStatusCommand(user.getId(), isEnabled));
                },
                () -> log.warn("No user found for Stripe Connected Account ID: {}", accountId));
    }
}
