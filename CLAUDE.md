# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Build and Run
- `./gradlew build` - Build the project and run all tests
- `./gradlew bootRun` - Run the Spring Boot application locally
- `./gradlew clean` - Clean build artifacts

### Testing
- `./gradlew test` - Run all tests
- `./gradlew test --tests ClassName` - Run a single test class
- `./gradlew test --tests ClassName.methodName` - Run a single test method
- Tests use Testcontainers for PostgreSQL and Redis (will auto-start containers)

### Code Generation
- `./gradlew kaptKotlin` - Generate QueryDSL Q classes (required after modifying entities)

## Architecture Overview

### Domain-Driven Modular Structure

The codebase is organized by business domain under `com.example.shop`:
- `auth/` - JWT/OAuth2 authentication, role-based authorization
- `account/` - User account management, addresses, profiles
- `products/` - Product catalog with hierarchical categories
- `purchase/` - Purchase transactions and payment gateway integration
- `cart/` - Shopping cart management
- `refund/` - Refund request processing
- `kafka/` - Event-driven messaging system
- `redis/` - Caching layer and token state management
- `admin/` - Administrative operations
- `common/` - Shared utilities and base classes

### Layered Architecture Pattern

```
Controller → Service → Repository → Entity
```

**Service Layer Structure:**
- **DomainService**: Entity-level operations (e.g., `ProductService`)
- **ApplicationService**: Use case orchestration with transactions
- **FacadeService**: Cross-domain operations (e.g., `FacadeAccountCrudService`)

**Repository Pattern:**
- Spring Data JPA repositories with custom QueryDSL extensions
- Use QueryDSL for complex queries (Q classes auto-generated via kapt)
- Helper classes: `QuerydslPagingHelper` for pagination, `JpaBatchHelper` for batch operations

### Key Domain Models

**Authentication Domain:**
- `Account` ← → `Authority` (role-based access control)
- `AccountGroup` ← → `GroupMember` ← → `Account` (group permissions)
- `Role` - Value object enforcing `ROLE_` prefix with hierarchy support
- `AccountDomain` - Read model combining Account + group authorities

**Product Domain:**
- `Category` - Hierarchical tree structure (indexed: name, parent_id, full_path)
- `Product` - Business methods: `decrementStock()`, `incrementStock()`, `isPurchasable()`
- Indexed fields: stock, price, category_id, is_enabled

**Purchase Domain:**
- `Purchase` - Order record with status tracking (READY → COMPLETED/FAILED/STOCK_INSUFFICIENT)
- `PurchaseProduct` - Line items with stock state machine (READY → PRODUCT_STOCK_DECREMENTED → PRODUCT_STOCK_RESTORED)
- `Payment` - Payment gateway integration record
- `PurchaseDomain` - Read model aggregating Purchase + PurchaseProducts

**Cart Domain:**
- `Cart` - Shopping cart per account (indexed: account_id, is_purchased)
- `CartItem` - Line items referencing Cart and Product

**Refund Domain:**
- `Refund` - Status enum: REQUESTED, APPROVED, REJECTED, CANCELED

### Event-Driven Architecture (Kafka)

**PRODUCT_STOCK_UPDATE_TOPIC (32 partitions):**
- Purpose: Async stock decrement/restore during purchase flow
- Consumers: 32 concurrent listeners (1:1 partition mapping)
- Message: `ProductStockUpdateKafkaMessage`
- Ensures high throughput and exactly-once semantics
- Flow: `PurchaseService` → Kafka → `ProductStockUpdateKafkaMsgHandler` → `Product.decrementStock()`

**NOTIFY_TOPIC (3 partitions):**
- Purpose: Admin notifications, audit logging
- Messages: AutoRegisteredAccount, Refund notifications
- Falls back to NOTIFY_DLQ_TOPIC on failure, then database storage

### External Integrations

**Toss Payments (Korean Payment Gateway):**
- Interface: `ExternalPaymentService`
- Implementation: `TossPaymentService`
- Config: `payment.toss.secret` in application.yml
- Helper: `PurchaseApproveHelper` for payment approval workflow

**OAuth2 Authentication:**
- Google OAuth2 login support
- Custom JWT decoders for third-party tokens
- Service: `GoogleOidcUserService`

### Security Architecture

- **Stateless JWT authentication** (access + refresh tokens)
- **Filter chains**: Email/password filter → OAuth2 filter → JWT validation filter
- **Rate limiting**: Bucket4j + Redis (configured in `rate-limit` section of application.yml)
  - Heavy operations (login, token refresh) consume more tokens
- **Role hierarchy**: `RoleHierarchyHelper` processes role inheritance
- **Helpers**: `MyJwtTokenHelper`, `RefreshTokenStateHelper`, `RedisRateLimitHelper`

### Important Design Decisions

1. **Entity Equality**: Custom `BaseCompareEntity<T>` handles Hibernate proxy comparison to prevent lazy-loading issues in equals/hashCode

2. **Kafka Partitioning**: Stock updates use 32 partitions for high throughput with manual ACK mode for exactly-once semantics

3. **Purchase State Machine**: Clear status transitions with Kafka-driven async stock updates and failure handling (cart auto-restoration on stock failure)

4. **QueryDSL Q Classes**: After modifying entities, run `./gradlew kaptKotlin` to regenerate Q classes before building

5. **Transaction Management**: Service layer handles transactions; async operations via Kafka for non-critical updates

### Testing Patterns

- **Testcontainers**: PostgreSQL (`TestPostgresqlContainer`) and Redis (`TestRedisContainerConfig`) auto-start for integration tests
- **Test Factories**: `TestAuthorityFactory`, `TestAccountGroupFactory`, `AuthTestUtil` for test data setup
- **Test Configs**: `EasyAccessTokenTestConfig` for quick token generation in tests
- **Isolation**: `@Transactional` tests with automatic rollback

### Global Response Format

All API responses use:
```kotlin
GlobalResponse<T>(
    isError: Boolean,
    result: T?,
    errorMsg: String?
)
```

Custom exceptions implement HTTP status code interfaces for proper error handling.

### Database Setup

- **PostgreSQL** with Hikari connection pooling (20 max connections)
- **JPA batch operations** enabled (batch_size: 20)
- **Schema management**: `spring.jpa.hibernate.ddl-auto: update`
- **Sequence generators** for ID generation

### Redis Usage

- Token state management (refresh tokens)
- API rate limiting buckets (Bucket4j)
- Authority refresh notifications (pub/sub)

### Monitoring & Resilience

- **Circuit Breaker**: Resilience4j for external service calls
- **Actuator**: Spring Boot Actuator endpoints enabled
- **Logging**: Log4j2 (excludes default Spring Boot logging)
- **API Documentation**: Swagger UI at `/swagger-ui`
