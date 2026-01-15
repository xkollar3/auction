package edu.fi.muni.cz.marketplace.user.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.fi.muni.cz.marketplace.user.command.AddPaymentInformationCommand;
import edu.fi.muni.cz.marketplace.user.command.AssignStripeCustomerIdCommand;
import edu.fi.muni.cz.marketplace.user.command.AssignStripeSellerAccountIdCommand;
import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.command.RemoveUserCommand;
import edu.fi.muni.cz.marketplace.user.command.UpdateStripeSellerStatusCommand;
import edu.fi.muni.cz.marketplace.user.event.PaymentInformationAddedEvent;
import edu.fi.muni.cz.marketplace.user.event.StripeCustomerCreatedEvent;
import edu.fi.muni.cz.marketplace.user.event.StripeSellerAccountCreatedEvent;
import edu.fi.muni.cz.marketplace.user.event.StripeSellerStatusUpdatedEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRegisteredEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRemovedEvent;

class UserTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String KEYCLOAK_USER_ID = "keycloak-user-123";
    private static final String STRIPE_CUSTOMER_ID = "cus_123456";
    private static final String STRIPE_SELLER_ACCOUNT_ID = "acct_123456";
    private static final String PAYMENT_METHOD_ID = "pm_123456";

    private FixtureConfiguration<User> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(User.class);
    }

    @Test
    void registerUser_shouldEmitUserRegisteredEvent() {
        fixture.givenNoPriorActivity()
                .when(new RegisterUserCommand(USER_ID, KEYCLOAK_USER_ID))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new UserRegisteredEvent(USER_ID, KEYCLOAK_USER_ID))
                .expectState(user -> {
                    assertEquals(USER_ID, user.getId());
                    assertEquals(KEYCLOAK_USER_ID, user.getKeycloakUserId());
                });
    }

    @Test
    void assignStripeCustomerId_shouldEmitStripeCustomerCreatedEvent() {
        fixture.given(new UserRegisteredEvent(USER_ID, KEYCLOAK_USER_ID))
                .when(new AssignStripeCustomerIdCommand(USER_ID, STRIPE_CUSTOMER_ID))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new StripeCustomerCreatedEvent(USER_ID, STRIPE_CUSTOMER_ID))
                .expectState(user -> {
                    assertEquals(STRIPE_CUSTOMER_ID, user.getStripeCustomerId());
                });
    }

    @Test
    void assignStripeCustomerId_alreadyHasCustomerId_shouldThrowException() {
        String existingCustomerId = "cus_existing";
        String newCustomerId = "cus_new";

        fixture.given(
                new UserRegisteredEvent(USER_ID, KEYCLOAK_USER_ID),
                new StripeCustomerCreatedEvent(USER_ID, existingCustomerId))
                .when(new AssignStripeCustomerIdCommand(USER_ID, newCustomerId))
                .expectException(IllegalStateException.class)
                .expectNoEvents();
    }

    @Test
    void addPaymentInformation_shouldEmitPaymentInformationAddedEvent() {
        fixture.given(
                new UserRegisteredEvent(USER_ID, KEYCLOAK_USER_ID),
                new StripeCustomerCreatedEvent(USER_ID, STRIPE_CUSTOMER_ID))
                .when(new AddPaymentInformationCommand(USER_ID, PAYMENT_METHOD_ID))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new PaymentInformationAddedEvent(USER_ID, PAYMENT_METHOD_ID))
                .expectState(user -> {
                    assertEquals(PAYMENT_METHOD_ID, user.getStripePaymentMethodId());
                });
    }

    @Test
    void addPaymentInformation_noStripeCustomer_shouldThrowException() {
        fixture.given(new UserRegisteredEvent(USER_ID, KEYCLOAK_USER_ID))
                .when(new AddPaymentInformationCommand(USER_ID, PAYMENT_METHOD_ID))
                .expectException(IllegalStateException.class)
                .expectNoEvents();
    }

    @Test
    void assignStripeSellerAccountId_shouldEmitStripeSellerAccountCreatedEvent() {
        fixture.given(new UserRegisteredEvent(USER_ID, KEYCLOAK_USER_ID))
                .when(new AssignStripeSellerAccountIdCommand(USER_ID, STRIPE_SELLER_ACCOUNT_ID))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new StripeSellerAccountCreatedEvent(USER_ID, STRIPE_SELLER_ACCOUNT_ID))
                .expectState(user -> {
                    assertEquals(STRIPE_SELLER_ACCOUNT_ID, user.getStripeSellerAccountId());
                });
    }

    @Test
    void assignStripeSellerAccountId_alreadyHasSellerAccount_shouldThrowException() {
        String existingSellerAccountId = "acct_existing";
        String newSellerAccountId = "acct_new";

        fixture.given(
                new UserRegisteredEvent(USER_ID, KEYCLOAK_USER_ID),
                new StripeSellerAccountCreatedEvent(USER_ID, existingSellerAccountId))
                .when(new AssignStripeSellerAccountIdCommand(USER_ID, newSellerAccountId))
                .expectException(IllegalStateException.class)
                .expectNoEvents();
    }

    @Test
    void updateStripeSellerStatus_shouldEmitStripeSellerStatusUpdatedEvent() {
        fixture.given(
                new UserRegisteredEvent(USER_ID, KEYCLOAK_USER_ID),
                new StripeSellerAccountCreatedEvent(USER_ID, STRIPE_SELLER_ACCOUNT_ID))
                .when(new UpdateStripeSellerStatusCommand(USER_ID, true))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new StripeSellerStatusUpdatedEvent(USER_ID, true))
                .expectState(user -> {
                    assertTrue(user.isSellerAccountEnabled());
                });
    }

    @Test
    void updateStripeSellerStatus_statusUnchanged_shouldEmitNoEvents() {
        fixture.given(
                new UserRegisteredEvent(USER_ID, KEYCLOAK_USER_ID),
                new StripeSellerAccountCreatedEvent(USER_ID, STRIPE_SELLER_ACCOUNT_ID),
                new StripeSellerStatusUpdatedEvent(USER_ID, true))
                .when(new UpdateStripeSellerStatusCommand(USER_ID, true))
                .expectSuccessfulHandlerExecution()
                .expectNoEvents()
                .expectState(user -> {
                    assertTrue(user.isSellerAccountEnabled());
                });
    }

    @Test
    void removeUser_shouldEmitUserRemovedEventAndMarkDeleted() {
        fixture.given(new UserRegisteredEvent(USER_ID, KEYCLOAK_USER_ID))
                .when(new RemoveUserCommand(USER_ID))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new UserRemovedEvent(USER_ID, KEYCLOAK_USER_ID))
                .expectMarkedDeleted();
    }
}
