# Orovia Payment System

Orovia Payment System is a Spring Boot service that powers online payment flows for an OTA-style hotel platform. It demonstrates clean architecture, DDD-inspired layering, and production-grade concerns like idempotency, reconciliation, and settlements.

## Architecture Overview

- **Layered packages**: `controller`, `service`, `domain`, `repository`, `integration`, `mapper`, `dto`, `config`.
- **Domain aggregates**: Booking, PaymentOrder, PaymentTransaction, Refund, LedgerEntry, Settlement, HotelAccount.
- **Payment gateway abstraction**: `PaymentGatewayClient` interface with a mock `OroviaMockGatewayClient` implementation and `PaymentGatewayRouter` for routing by payment method.
- **Jobs**: `PaymentReconciliationJob` and `SettlementJob` scheduled to keep records aligned and payouts generated.

## Tech Stack
- Java 17, Spring Boot 3.2
- Spring Web, Data JPA, Validation, Security, Scheduling, Caching
- PostgreSQL with Flyway migrations (H2 for tests)
- Redis-ready cache abstraction
- JUnit 4 + Mockito for testing

## Domain Concepts
- **PaymentOrder**: tracks gateway order and status (CREATED, PENDING, SUCCESS, FAILED, REFUNDED).
- **PaymentTransaction**: captures each payment attempt or webhook event.
- **Refund**: supports full/partial refunds with status lifecycle.
- **LedgerEntry**: double-entry accounting (customer, platform, hotel).
- **Settlement**: payout batches to hotels, built from accrued ledger balances.
- **Booking**: simplified booking aggregate with pay-at-hotel and prepaid support.

## Key Flows

### Create Payment Order
`POST /api/v1/payments/orders`
```json
{
  "bookingId": 1,
  "amount": 120.00,
  "currency": "USD",
  "paymentMethod": "CARD",
  "customerId": 10,
  "hotelId": 22,
  "returnUrl": "https://example.com/return"
}
```
- Validates booking and idempotency (booking + method).
- Creates internal `PaymentOrder` (CREATED -> PENDING).
- Calls payment gateway to create external order and payment URL/token.

### Payment Success/Failure Callback
`POST /api/v1/payments/webhook/{provider}`
- Verifies HMAC signature (stubbed in mock client).
- Persists `PaymentTransaction`.
- Updates `PaymentOrder` status, Booking status, and ledger entries.
- Emits `PaymentCompletedEvent`.

### Refunds
`POST /api/v1/payments/{paymentOrderId}/refunds`
- Supports partial refunds; validates amounts and payment state.
- Calls gateway refund API and records ledger reversal entries.
`GET /api/v1/payments/{paymentOrderId}/refunds` lists history.

### Reconciliation
- `PaymentReconciliationJob` (02:00 daily) compares internal orders with gateway status via `PaymentStatusService`/`PaymentGatewayClient`.
- `GET /api/v1/reconciliation/reports` returns mismatches.

### Settlements
- `SettlementJob` (02:30 daily) aggregates hotel payable balances and marks settlements complete (mock payout).
- `GET /api/v1/settlements` filters by status/date; `GET /api/v1/settlements/hotels/{hotelId}` lists hotel-specific payouts.

### Pay at Hotel
- Represented via `PaymentMethod.PAY_AT_HOTEL`; orders are created without gateway capture and marked `CONFIRMED_PENDING_PAYMENT` on bookings.

## Running Locally
```bash
mvn clean install
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```
Database defaults to PostgreSQL (`jdbc:postgresql://localhost:5432/orovia`) with credentials `orovia/secret`. Use H2 by activating the `test` profile.

Flyway migrations are located in `src/main/resources/db/migration` and will bootstrap tables on startup.

## Configuration Highlights
- `spring.datasource.*`: PostgreSQL connection
- `spring.redis.*`: Redis host/port for cache
- `payment.gateway.mock.hmacSecret`: mock signature secret
- Logging tuned via `logging.level` with verbose debug for `com.orovia.payment`.

## API Reference (selected)
- `POST /api/v1/payments/orders` – create payment order
- `GET /api/v1/payments/orders/{id}` – get order
- `POST /api/v1/payments/webhook/{provider}` – gateway webhook
- `POST /api/v1/payments/{paymentOrderId}/refunds` – create refund
- `GET /api/v1/payments/{paymentOrderId}/refunds` – list refunds
- `GET /api/v1/ledger/entries?referenceType=PAYMENT&referenceId=1` – ledger view
- `GET /api/v1/settlements` and `/settlements/hotels/{hotelId}` – settlements
- `GET /api/v1/reconciliation/reports` – reconciliation report

## Extensibility
- Add a gateway by implementing `PaymentGatewayClient` and registering in `PaymentGatewayRouter`.
- Adjust commission logic inside `SettlementService.applySettlementSplit`.
- Plug authentication/authorization by extending `SecurityConfig`.

## Planet-Scale Architecture

- **Service contexts**: payment-orchestrator (order creation), payment-callback (webhooks), refund, ledger, settlement, and reconciliation layers can be deployed independently while reusing shared contracts.
- **Event transport**: domain events (`payments.created`, `payments.authorized`) flow over Kafka with an in-memory bus fallback for local development. Consumers update ledger and settlement state without coupling to the synchronous payment API.
- **Sharding & IDs**: entities accept externally generated IDs (Snowflake/ULID-ready) to avoid database hot spots; shard routing can fan out booking or hotel scoped data to per-region datasources.
- **Caches & read models**: Redis-backed idempotency and rate limiting guard high-RPS paths; read replicas can be configured per shard for GET-heavy endpoints.
- **Resiliency & limits**: token-bucket rate limits and circuit breakers (Resilience4j) should wrap gateway/settlement clients with backoff and bulkheads. Configuration toggles live under `eventing.*`, `idempotency.*`, and `ratelimit.*` namespaces.
- **Mock vs. real infra**: set `eventing.kafka.enabled=true` to publish through Kafka; keep `false` to rely on the in-memory bus. Redis settings under `spring.redis.*` power idempotency keys.

### Text flow diagrams

```
Client -> payment-orchestrator -> PaymentOrderCreatedEvent -> payment-callback -> PaymentAuthorizedEvent -> ledger -> settlement
```

```
Gateway webhook -> payment-callback (idempotent) -> PaymentAuthorizedEvent -> ledger updates -> settlement accrual -> payout jobs
```

### Key configuration

| Property | Purpose |
| --- | --- |
| `eventing.kafka.enabled` | Switch Kafka publisher on/off |
| `eventing.kafka.bootstrap-servers` | Kafka brokers for domain events |
| `spring.redis.*` | Backing store for idempotency keys |
| `ratelimit.*` | Token-bucket sizing per endpoint |
| `shards.*` | Future shard datasource mapping for hotel/booking routing |

## Security & Compliance Notes
- No raw card data stored; relies on gateway tokens/URLs.
- HMAC verification stubs show where to secure webhooks.
- PCI-DSS considerations documented for future hardening.
