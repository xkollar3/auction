package edu.fi.muni.cz.marketplace.user.command.handler;

import edu.fi.muni.cz.marketplace.user.command.AssignStripeSellerAccountIdCommand;
import edu.fi.muni.cz.marketplace.user.command.CreateStripeConnectedAccountCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

import edu.fi.muni.cz.marketplace.user.service.StripeApiClient;
import edu.fi.muni.cz.marketplace.user.service.dto.ConnectedAccountResponse;

/**
 * Handles the creation of a Stripe Connect Express account.
 * <p>
 * Orchestrates the flow:
 * 1. Creates a Stripe Connected Account.
 * 2. Dispatches {@link AssignStripeSellerAccountIdCommand} to store the account
 * ID.
 * 3. Creates and returns a Stripe Account Link for onboarding.
 * </p>
 */
@Service
public class CreateStripeConnectedAccountCommandHandler {

    private final StripeApiClient stripeClient;
    private final CommandGateway commandGateway;

    private final String refreshUrl;
    private final String returnUrl;

    public CreateStripeConnectedAccountCommandHandler(
            StripeApiClient stripeClient,
            CommandGateway commandGateway,
            @Value("${stripe.seller.refresh-url}") String refreshUrl,
            @Value("${stripe.seller.return-url}") String returnUrl) {
        this.stripeClient = stripeClient;
        this.commandGateway = commandGateway;
        this.refreshUrl = refreshUrl;
        this.returnUrl = returnUrl;
    }

    @CommandHandler
    public String on(CreateStripeConnectedAccountCommand command) {
        ConnectedAccountResponse accountResponse = stripeClient.createConnectedAccount(command.getId(),
                command.getEmail());

        commandGateway.send(new AssignStripeSellerAccountIdCommand(command.getId(), accountResponse.accountId()));

        return stripeClient.createAccountLink(accountResponse.accountId(), refreshUrl, returnUrl);
    }
}
