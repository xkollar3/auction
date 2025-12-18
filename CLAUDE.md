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

### Key Domain: User Management

**User Registration Flow** (Keycloak-First Strategy):
1. Frontend redirects user to Keycloak for account creation
2. User creates account directly in Keycloak
3. Keycloak issues JWT token containing Keycloak user ID
4. Frontend extracts Keycloak user ID from JWT
5. Frontend calls `/api/users/register` with Keycloak user ID
6. Backend creates User aggregate associated with existing Keycloak user ID

**Aggregates** (`user/aggregate/`):
- `User` - Event-sourced aggregate root with simple lifecycle: Register with Keycloak ID
  - Stores aggregate ID and Keycloak user ID
  - No complex state machine - user already exists in Keycloak

**Commands** (`user/command/`):
- `RegisterUserCommand` - Associate internal User aggregate with existing Keycloak user ID
  - Parameters: aggregate ID, Keycloak user ID

**Events** (`user/event/`):
- `UserRegisteredEvent` - User aggregate created and associated with Keycloak user ID

**No Sagas Required:**
- User registration is handled entirely by Keycloak frontend redirect flow
- Backend only creates internal User aggregate for existing Keycloak users
- No external system orchestration needed (user already exists in Keycloak)

**Authentication:**
- Users authenticate directly via Keycloak (OAuth2/OpenID Connect)
- JWT tokens contain Keycloak user ID (`sub` claim)
- Backend associates requests with User aggregates via Keycloak user ID

### Directory Structure by Bounded Context

```
src/main/java/edu/fi/muni/cz/marketplace/
├── user/              # User management context (IMPLEMENTED)
│   ├── aggregate/     # Event-sourced aggregates (@Aggregate)
│   ├── command/       # Commands
│   ├── event/         # Domain events
│   ├── query/         # Read models, repositories, event handlers (@ProcessingGroup)
│   ├── controller/    # REST endpoints
│   ├── dto/           # Request/Response objects (Java records)
│   └── exception/     # Domain exceptions
├── auction/           # Auction context (PLACEHOLDER)
├── order/             # Order context (PLACEHOLDER)
├── winner/            # Winner selection context (PLACEHOLDER)
└── config/            # Cross-cutting configuration
```

**Note:** The `saga/` and `service/` subdirectories are not present in the user context because user registration is handled entirely by Keycloak. Backend only creates internal aggregates for existing Keycloak users.

### Configuration

**application-dev.yaml** (active profile):
- Server port: 8081
- Postgres: localhost:5432/auction_marketplace (auction_user/auction_password)
- Keycloak: localhost:8089/realms/auction-marketplace
- AxonServer: localhost:8124 (standalone mode, devmode enabled)
- JPA: ddl-auto=create-drop, show-sql=true
- Telemetry: disabled in dev

### Security

OAuth2 Resource Server with JWT validation:
- Public endpoints: `/actuator/**`, `/health/**`, `/api/users/**`
- All other endpoints require JWT from Keycloak
- Stateless sessions (no JSESSIONID)
- CSRF disabled (REST API)
- JWT `sub` claim contains Keycloak user ID for backend association

## Important Patterns

### Dispatch Interceptors for Validation
Dispatch interceptors validate commands BEFORE execution by querying read models. This prevents invalid commands from reaching aggregates.

**Pattern:** Use `@Component` implementing `MessageDispatchInterceptor<CommandMessage<?>>` to intercept commands before they reach aggregates. Query read models to validate business rules (e.g., uniqueness constraints).

**Current usage:** Not used in user context (no validation needed for simple Keycloak ID association).

### Sagas (Process Managers)
Sagas coordinate business processes that involve external systems or span multiple aggregates. They listen to events and dispatch commands to orchestrate workflows.

**When to use Sagas:**
- Coordinating workflows across multiple aggregates
- Integrating with external systems (payment gateways, email services, etc.)
- Managing long-running business processes
- Handling compensating transactions

**Key characteristics:**
- Use @Saga annotation on the class
- @SagaEventHandler methods with associationProperty to track the saga instance
- Inject dependencies with @Autowired (marked transient to avoid serialization)
- @StartSaga begins a saga instance
- @EndSaga completes and removes the saga instance
- Sagas dispatch commands via CommandGateway to update aggregates
- Keep aggregates pure by moving side effects (external API calls, third-party integrations) to Sagas

**Current usage:** Not used in user context (Keycloak registration handled by frontend). Will be useful for auction/payment workflows.

### Subscription Queries for Async Response
Subscription queries convert async event-driven flows into synchronous HTTP responses with timeouts.

**Pattern:** Controller uses `QueryGateway.subscriptionQuery()` to:
1. Send command via CommandGateway
2. Subscribe to query updates
3. Block until specific event occurs or timeout
4. Return result to client

**Current usage:** Not used in user context (simple synchronous registration).

### Processing Groups with Subscribing Mode
Processing groups with subscribing mode (@ProcessingGroup + axon.eventhandling.processors.<name>.mode=subscribing) are used when you need **transactionality and consistency** between command handling and event processing.

Subscribing processors create and maintain projections immediately after an event has been applied in the **same thread**, ensuring direct consistency. These projections are **lookup tables owned by the command side only** - they should NOT be exposed via Query API.

**Use case:** Validating uniqueness constraints (e.g., nickname availability checks before command execution via dispatch interceptor).

**Default mode:** Tracking event processors are sufficient for query-side projections that don't require immediate consistency.

**Current usage:** Not used in user context. Use when implementing unique constraints in other contexts.

### Value Objects for Domain Logic
Value objects encapsulate domain logic and validation within immutable objects.

**Pattern:** Create immutable classes (using `@Value` or records) that validate input and provide domain-specific behavior. Use in aggregates and commands.

**Current usage:** Not used in user context (simple string-based Keycloak ID).

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
    @TargetAggregateIdentifier
    UUID id;
    String keycloakUserId;
}
```

**Example Event:**
```java
@Value
public class UserRegisteredEvent {
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
    String keycloakUserId
) {}
```

**Example Response DTO:**
```java
public record UserRegistrationResponse(
    UUID id
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

1. Create package structure: `{context}/{aggregate,command,event,query,controller,dto,exception}` (add `saga/` and `service/` only if needed)
2. Define aggregates with @Aggregate, @AggregateIdentifier, @CommandHandler, @EventSourcingHandler
3. Create commands (use Java classes with @Value - see Coding Policies)
4. Create events (use Java classes with @Value - include aggregate ID)
5. Create DTOs (use Java records for requests/responses - see Coding Policies)
6. Implement read model projections with @EventHandler
7. Add query handlers for read operations
8. Use CommandGateway and QueryGateway in controllers, never call aggregates directly

**Optional (add only when needed):**
- Create Sagas (@Saga) for workflows involving external systems or multiple aggregates (see Sagas pattern)
- Add Dispatch Interceptors for command validation
- Add @ProcessingGroup with subscribing mode ONLY if you need command-side lookup tables (e.g., uniqueness validation)
- Add service layer for external system integrations

**User Context Example:** The user context demonstrates a minimal implementation with no sagas, no dispatch interceptors, and no value objects. The aggregate simply associates an internal ID with a Keycloak user ID.
