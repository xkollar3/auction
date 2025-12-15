# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Auction Marketplace is a Spring Boot 4.0 application implementing event-sourced CQRS architecture using Axon Framework. The application handles user registration with Keycloak integration, and is designed as a distributed system with separated command and query responsibilities.

**Base package:** `edu.fi.muni.cz.marketplace`
**Note:** Original package name `auction-marketplace` is invalid in Java; using `auction_marketplace` instead.

## Tech Stack

- **Java 25** with Lombok
- **Spring Boot 4.0.0** (Web MVC, Security, Data JPA, DevTools)
- **Axon Framework 4.12.2** (CQRS/Event Sourcing)
- **Keycloak 26.0.7** (OAuth2/OpenID Connect)
- **PostgreSQL 17** (Read model persistence)
- **AxonServer** (Event Store and Message Routing)
- **OpenTelemetry** (Observability - disabled in dev)

## Development Commands

### Build and Run
```bash
# Build the project
./mvnw clean package

# Run the application (dev profile)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run with Docker dependencies
docker-compose up -d  # Starts AxonServer, Keycloak, PostgreSQL
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Testing
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuctionMarketplaceApplicationTests

# Run specific test method
./mvnw test -Dtest=ClassName#methodName
```

### Docker Services
```bash
# Start all services (AxonServer:8024/8124, Keycloak:8089, PostgreSQL:5432)
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f [service-name]

# Reset all data
docker-compose down -v
```

## Application Architecture

### CQRS/Event Sourcing with Axon Framework

The application uses strict CQRS separation with event sourcing for the write model and projections for read models.

**Command Flow:**
1. REST Controller receives request → CommandGateway
2. Dispatch Interceptor validates command (e.g., nickname uniqueness check)
3. Aggregate handles command → emits event(s)
4. Event stored in AxonServer event store
5. Aggregate state rebuilt from events via @EventSourcingHandler

**Query Flow:**
1. Events published to Event Handlers in Processing Groups
2. Event Handlers update read model projections (JPA entities)
3. Query handlers read from projections
4. Subscription queries enable reactive updates

### Key Domain: User Registration

**Aggregates** (`user/aggregate/`):
- `User` - Event-sourced aggregate root with lifecycle: Register → Assign Keycloak ID → Complete/Fail
- `UserNickname` - Value object with nickname + discriminator (Discord-style)

**Commands** (`user/command/`):
- `RegisterUserCommand` - Initiate registration
- `AssignKeycloakUserIdCommand` - Link Keycloak user after creation
- `FailUserRegistrationCommand` - Mark registration as failed

**Events** (`user/event/`):
- `UserRegistrationInitiatedEvent` - User registration process started
- `UserKeycloakIdAssignedEvent` - Keycloak integration successful
- `UserRegistrationFailedEvent` - Registration process failed

**Sagas** (`user/saga/`):
- `UserRegistrationSaga` - Orchestrates user registration with Keycloak
  - @StartSaga on UserRegistrationInitiatedEvent
  - Creates user in Keycloak via KeycloakUserService
  - Dispatches AssignKeycloakUserIdCommand on success or FailUserRegistrationCommand on error
  - @EndSaga on UserKeycloakIdAssignedEvent or UserRegistrationFailedEvent
  - **Critical pattern:** Sagas handle external system integration and keep aggregates pure

**Event Handlers** (`user/query/`):
- `UserNicknameReadModel` + Handler (@ProcessingGroup "user_nicknames") - Maintains nickname availability projection for command-side validation
- `UserRegistrationStatusReadModel` + Handler - Tracks registration status for subscription queries

**Subscription Query Pattern:**
Controller uses `QueryGateway.subscriptionQuery()` to wait for async registration completion:
1. Send RegisterUserCommand
2. Subscribe to registration status updates
3. Block until UserKeycloakIdAssignedEvent or UserRegistrationFailedEvent
4. Return final status to client

### Directory Structure by Bounded Context

```
src/main/java/edu/fi/muni/cz/marketplace/
├── user/              # User registration context (IMPLEMENTED)
│   ├── aggregate/     # Event-sourced aggregates (@Aggregate)
│   ├── command/       # Commands and dispatch interceptors
│   ├── event/         # Domain events
│   ├── saga/          # Process managers (@Saga)
│   ├── query/         # Read models, repositories, event handlers (@ProcessingGroup)
│   ├── controller/    # REST endpoints
│   ├── service/       # External integrations (Keycloak)
│   ├── dto/           # Request/Response objects
│   └── exception/     # Domain exceptions
├── auction/           # Auction context (PLACEHOLDER)
├── order/             # Order context (PLACEHOLDER)
├── winner/            # Winner selection context (PLACEHOLDER)
└── config/            # Cross-cutting configuration
```

### Configuration

**application-dev.yaml** (active profile):
- Server port: 8081
- Postgres: localhost:5432/auction_marketplace (auction_user/auction_password)
- Keycloak: localhost:8089/realms/auction-marketplace
- AxonServer: localhost:8124 (standalone mode, devmode enabled)
- JPA: ddl-auto=create-drop, show-sql=true
- Telemetry: disabled in dev

**Processing Groups** (Axon event handlers):
- `user_nicknames` - subscribing mode (for command-side nickname validation lookup table)

### Security

OAuth2 Resource Server with JWT validation:
- Public endpoints: `/actuator/**`, `/health/**`, `/api/users/**`
- All other endpoints require JWT from Keycloak
- Stateless sessions (no JSESSIONID)
- CSRF disabled (REST API)

Keycloak Admin Client configured for programmatic user creation.

## Important Patterns

### Dispatch Interceptors for Validation
`UserRegistrationDispatchInterceptor` validates nickname uniqueness BEFORE command execution by querying read model. This prevents invalid commands from reaching aggregates.

### Sagas (Process Managers)
Sagas coordinate business processes that involve external systems or span multiple aggregates. They listen to events and dispatch commands to orchestrate workflows.

**UserRegistrationSaga example:**
1. @StartSaga on UserRegistrationInitiatedEvent - begins the registration workflow
2. Calls KeycloakUserService to create external user account
3. On success: dispatches AssignKeycloakUserIdCommand
4. On failure: dispatches FailUserRegistrationCommand
5. @EndSaga on UserKeycloakIdAssignedEvent or UserRegistrationFailedEvent - completes the workflow

**Key characteristics:**
- Use @Saga annotation on the class
- @SagaEventHandler methods with associationProperty to track the saga instance
- Inject dependencies with @Autowired (marked transient to avoid serialization)
- @StartSaga begins a saga instance
- @EndSaga completes and removes the saga instance
- Sagas dispatch commands via CommandGateway to update aggregates
- Keep aggregates pure by moving side effects (external API calls, third-party integrations) to Sagas

### Subscription Queries for Async Response
Controllers use Axon subscription queries to convert async event-driven flows into synchronous HTTP responses with timeouts.

### Processing Groups with Subscribing Mode
Processing groups with subscribing mode (@ProcessingGroup + axon.eventhandling.processors.<name>.mode=subscribing) are used when you need **transactionality and consistency** between command handling and event processing.

Subscribing processors create and maintain projections immediately after an event has been applied in the **same thread**, ensuring direct consistency. These projections are **lookup tables owned by the command side only** - they should NOT be exposed via Query API.

**Use case:** Validating uniqueness constraints (e.g., UserNicknameReadModel checks nickname availability before RegisterUserCommand execution via dispatch interceptor).

**Default mode:** Tracking event processors are sufficient for query-side projections that don't require immediate consistency.

### Value Objects for Domain Logic
`UserNickname` encapsulates nickname parsing and formatting logic (nickname#discriminator format).

## Coding Policies

### Axon Objects (Commands and Events)

**Always use Java classes** with `@Value` annotation for Commands and Events. This ensures proper serialization/deserialization by Axon Framework while providing immutability.

**Use Lombok `@Value` annotation:**
- Creates immutable objects (all fields are final)
- Generates all-args constructor automatically
- Generates getters for all fields
- Generates equals(), hashCode(), and toString()
- Makes the class final

**DO NOT use `@Builder`** - builders are error-prone and can leave fields null. Use all-args constructors directly.

**Example Command:**
```java
@Value
public class RegisterUserCommand {
    UUID id;
    String nickname;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    String password;
}
```

**Example Event:**
```java
@Value
public class UserKeycloakIdAssignedEvent {
    UUID id;
    String keycloakUserId;
}
```

**Why not records?** Events must be serializable across different Axon versions and storage backends. Java classes with explicit constructors provide better compatibility and control over serialization.

### Request/Response DTOs

**Always use Java records** for REST API request and response objects. Records reduce boilerplate, provide immutability, and generate all-args constructors automatically.

**Example Request DTO:**
```java
public record RegisterUserRequest(
    String nickname,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    String password
) {}
```

**Example Response DTO:**
```java
public record UserRegistrationResponse(
    UUID id,
    String nickname,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    String keycloakUserId,
    RegistrationStatus status,
    String errorMessage
) {}
```

Records provide compile-time safety by requiring all fields in the constructor, avoiding null field issues common with builders.

## Service Dependencies

**AxonServer** (port 8024/8124):
- Event store for event-sourced aggregates
- Command and query message routing
- Must be running before application starts
- Web UI: http://localhost:8024

**Keycloak** (port 8089):
- Identity and access management
- Realm: `auction-marketplace`
- Admin credentials: admin/admin
- Admin Console: http://localhost:8089

**PostgreSQL** (port 5432):
- Read model projections only (not event store)
- Database recreated on each startup (ddl-auto=create-drop)

## Adding New Bounded Contexts

When implementing auction/order/winner contexts:

1. Create package structure: `{context}/{aggregate,command,event,saga,query,controller,service,dto,exception}`
2. Define aggregates with @Aggregate, @AggregateIdentifier, @CommandHandler, @EventSourcingHandler
3. Create commands (use Java classes with @Value - see Coding Policies)
4. Create events (use Java classes with @Value - include aggregate ID)
5. Create DTOs (use Java records for requests/responses - see Coding Policies)
6. Create Sagas (@Saga) for workflows involving external systems or multiple aggregates (see Sagas pattern)
7. Implement read model projections with @EventHandler
8. Add @ProcessingGroup with subscribing mode ONLY if you need command-side lookup tables (e.g., uniqueness validation)
9. Add query handlers for read operations
10. Use CommandGateway and QueryGateway in controllers, never call aggregates directly
