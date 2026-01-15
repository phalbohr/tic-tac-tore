# Coding Style Guide

## Table of Contents

1.  [Variable Declarations](#1-variable-declarations)
2.  [Technical text language](#2-technical-text-language)
3.  [Annotations](#3-annotations)
4.  [Optimistic Locking](#4-optimistic-locking)
5.  [Guiding Principles for @Transactional, @Retryable, and @Idempotent](#5-guiding-principles-for-transactional-retryable-and-idempotent)
6.  [Fail-Fast Principle](#6-fail-fast-principle)
7.  [Tell, Don't Ask Principle](#7-tell-dont-ask-principle)
8.  [Read Everywhere, Change from the Service](#8-read-everywhere-change-from-the-service)
9.  [Pass Primitive Fields, Not Whole Objects](#9-pass-primitive-fields-not-whole-objects)
10. [SRP is King! (Single Responsibility Principle)](#10-srp-is-king-single-responsibility-principle)
11. [JPA Persistence Best Practices](#11-jpa-persistence-best-practices)
12. [The Single Level of Abstraction Principle](#12-the-single-level-of-abstraction-principle)
13. [Controller vs. Service Responsibilities](#13-controller-vs-service-responsibilities)
14. [Class Purpose and Usage Documentation (CPUD)](#14-class-purpose-and-usage-documentation-cpud)
15. [Layer Separation and Dependency Rules](#15-layer-separation-and-dependency-rules)

## 1. Variable Declarations

### Rule: Explicit Type Declarations

- Do not use `var` for declaring or initializing variables
- Always specify the explicit class name instead of using type inference

#### Examples:

**✅ Correct:**

```java
PhysicalUniqueItemResponse response = new PhysicalUniqueItemResponse();
List<string> items = new List<string>();
Dictionary<int, string> mapping = new Dictionary<int, string>();
```

**❌ Incorrect:**

```java
var response = new PhysicalUniqueItemResponse();
var items = new List<string>();
var mapping = new Dictionary<int, string>();
```

#### Rationale:

- Explicit type declarations make code more readable
- Easier to understand the intended data types at a glance
- Reduces ambiguity in complex codebases

## 2. Technical text language

### Rule: In comments, exceptions, log messages, and all developer-facing text use only English language

#### Scope:

- Comments, exceptions, log messages
- Variable/method names, commit messages
- API documentation, unit tests
- TODO

#### Examples:

**✅ Correct:**

```java
// Assumption: Method exists
throw new IllegalArgumentException("Serial numbers are required for serializable products.");
log.

info("Received request to create warehouse: {}",createRequest.name());
```

**❌ Incorrect:**

```java
// Annahme: Methode existiert
throw new IllegalArgumentException("Seriennummer ist für serialisierbare Produkte erforderlich.");
log.

info("Anfrage zum Anlegen eines Lagers erhalten: {}",createRequest.name());
```

## 3. Annotations

### Rule: Mark controllers with OpenAPI annotations for automatic documentation generation

#### Required Annotations:

@Operation - method description and metadata
@ApiResponses - response codes and content types
@Schema - data model definitions
@Parameter - request parameters documentation

#### Examples:

**✅ Correct:**

```java

@Operation(
        summary = "Create a new unique physical item",
        operationId = "addUniqueItem",
        description = "Creates a new unique, serialized physical item. Requires ADMIN or MANAGER role."
)
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Unique item created successfully",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = PhysicalUniqueItemResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
})
@PostMapping("/items")
public ResponseEntity<PhysicalUniqueItemResponse> createUniqueItem(
        @Parameter(description = "Item creation data") @Valid @RequestBody CreateItemRequest request) {
// Implementation
}
```

**❌ Incorrect:**

```java

@PostMapping("/items")
public ResponseEntity<PhysicalUniqueItemResponse> createUniqueItem(@RequestBody CreateItemRequest request) {
// No OpenAPI annotations - documentation not generated
}
```

#### Rationale:

- Auto-documentation: Generates comprehensive API documentation without manual maintenance
- Client SDK Generation: Enables automatic generation of client libraries
- API Testing: Provides interactive Swagger UI for testing endpoints
- Team Communication: Clear API contracts for frontend and external teams
- Validation: Ensures consistent API design and documentation standards

## 4. Optimistic Locking

### Rule: Use @Version for all mutable entities

- All mutable JPA entities (`@Entity`) that can be updated must include a field for optimistic locking to prevent lost
  updates in concurrent scenarios.
- This field must be annotated with `@Version`.

#### Style Guide:

- The field should be declared as `private long version;`.
- Do not use the wrapper type `Long`.
- Do not add the redundant `@Column(name = "version")` annotation.

#### Examples:

**✅ Correct:**

```java

@Entity
public class Warehouse {

    @Version
    private long version;

    // ... other fields
}
```

**❌ Incorrect:**

```java

@Entity
public class Warehouse {

    @Version
    @Column(name = "version")
    private Long version; // Uses wrapper type and redundant @Column

    // ... other fields
}
```

#### Rationale:

- **Prevents Lost Updates:** Optimistic locking is a crucial mechanism to ensure data integrity in systems with
  concurrent write operations.
- **Consistency:** Using `long` is semantically correct as the version cannot be `null`. Omitting the redundant
  `@Column` annotation keeps the code clean and relies on JPA's default naming strategy.

## 5. Guiding Principles for @Transactional, @Retryable, and @Idempotent

To ensure data consistency, resilience, and safe API design, it is crucial to correctly apply `@Transactional`,
`@Retryable`, and our custom `@Idempotent` annotations. These annotations should be layered according to the principles
of atomicity and separation of concerns.

### Rule: Layer annotations based on their function

#### 1. `@Transactional` — Ensuring Atomicity

- **Core Principle:** A transaction should be as short as possible, wrapping only the set of database operations that
  must be atomic (all succeed or all fail together).
- **Where to Apply:**
    - On the method that directly encapsulates the "read-modify-write" logic. This is typically a method within a
      component that represents a single, self-contained business operation.
    - Avoid placing it on high-level methods that also perform non-transactional side effects (e.g., calling external
      services, sending messages).
- **Read-Only Operations:** For methods that only read data, use `@Transactional(readOnly = true)` to provide
  optimization hints to the JPA provider and database.

#### 2. `@Retryable` — Building Resilience

- **Core Principle:** Use this to automatically re-run an operation that failed due to a temporary, recoverable (
  transient) error, such as a database deadlock or optimistic locking conflict.
- **Interaction with `@Transactional`:** The `@Retryable` annotation must be placed on a method that **calls** the
  `@Transactional` method. It must be "outside" the transactional boundary. When a transactional method fails, the
  transaction is rolled back and closed. The retry mechanism must be able to start a *new* transaction for the next
  attempt. Placing both annotations on the same method will not work as expected.

#### 3. `@Idempotent` — Ensuring Safe Retries

- **Core Principle:** This custom annotation ensures that a state-changing operation can be executed multiple times with
  the same input, but the system's state will only change on the first successful execution. Subsequent calls with the
  same idempotency key will return the original cached result.
- **Where to Apply:** On the method that contains the core, state-changing business logic. This is typically the same
  method that is annotated with `@Transactional`. The idempotency check should happen just before the transaction begins
  to prevent unnecessary database work.

#### 4. Deriving Idempotency Keys for Nested Operations

- **Core Principle:** When an idempotent operation calls another operation that requires an idempotency key, the inner
  operation's idempotency key should be deterministically derived from the outer operation's idempotency key. This
  ensures that the entire chain of operations remains idempotent and consistent, even if retried.

- **Rationale:**
    - **Consistency:** Guarantees that retrying the outer operation will result in the same idempotency keys being used
      for all nested operations, preventing partial execution or inconsistent states.
    - **Traceability:** Allows for easier debugging and tracking of related idempotent operations.
    - **Correctness:** Prevents issues where a retry of the outer operation might generate new, different idempotency
      keys for inner operations, leading to unintended side effects.

- **Example:**
  In the `TransferMassStockReservationOperation`, the `executeTransfer` method calls `unreserveStockOperation` and
  `reserveStockOperation`, both of which are idempotent and require an idempotency key. The `idempotencyKey` for these
  inner operations is derived from the main `request.idempotencyKey()` and the specific operation name.

  **✅ Correct:**

  ```java
  // In TransferMassStockReservationOperation.java
  private void executeTransfer(@NotNull @Valid PhysicalMassStockTransferOfReservationRequest request,
                               @NotNull UUID performedByUserId, @NotNull UUID productId,
                               @NotNull @Positive Integer quantityToTransfer) {
      UnreserveStockRequest unreserveStockRequest = new UnreserveStockRequest(
              request.orderItemId(), performedByUserId, getDerivedUuid(
              request.idempotencyKey(), unreserveStockOperation.getOperationName()));
      ReserveStockRequest reserveStockRequest = new ReserveStockRequest(
              request.orderItemId(), productId, request.toWarehouseId(), quantityToTransfer, null,
              performedByUserId, getDerivedUuid(request.idempotencyKey(), reserveStockOperation.getOperationName()));
      unreserveStockOperation.execute(unreserveStockRequest, performedByUserId);
      reserveStockOperation.execute(reserveStockRequest, performedByUserId);
  }

  private UUID getDerivedUuid(@NotNull UUID idempotencyKey, @NotNull String operationName) {
      // A common pattern is to combine the parent idempotency key with a unique identifier for the nested operation.
      return UUID.nameUUIDFromBytes((idempotencyKey.toString() + "-" + operationName).getBytes());
  }
  ```

### Example: Correct Layering

This example shows the correct separation of concerns. A `Service` class orchestrates the call and handles retries,
while a dedicated `Operation` class handles the atomic, transactional, and idempotent business logic.

**✅ Correct:**

- The Orchestration Layer (e.g., a `Service`)
- The Business Logic Layer (e.g., an `Operation`)

**❌ Incorrect:**

- Placing @Transactional on the orchestrating method that also has @Retryable, or placing all three annotations on a
  single method, violates these principles and can lead to incorrect behavior.

## 6. Fail-Fast Principle

### Rule: Validate preconditions at the beginning of methods

Apply the "Fail-Fast" principle by checking all preconditions and validating arguments at the very beginning of a
method. If any check fails, throw an exception immediately. This is often implemented using "Guard Clauses".

#### Core Principle:

- A method should validate its state and input parameters before executing its core logic.
- Group all validation checks (guard clauses) at the top of the method.
- Avoid mixing validation logic with the main business logic.

#### Rationale:

- **Robustness:** Prevents the system from operating on invalid data, which could lead to an inconsistent state.
- **Readability:** The main business logic becomes clean and free from nested `if` statements for validation, making it
  easier to understand.
- **Debugging:** Errors are detected and reported at the source, making it much easier to find and fix bugs. The stack
  trace points directly to the problem's origin.

#### Examples:

Let's look at a method in `WarehouseService`.

**❌ Incorrect (Validation logic is mixed or delayed):**

```java
public WarehouseResponse deactivateWarehouse(UUID warehouseId, WarehouseDeactivateRequest request,
                                             UUID performedByUserId) {
    // Core logic starts
    log.info("Attempting to process deactivate warehouse request...");
    if (warehouseId.equals(request.warehouseId())) {
        // More logic...
        return deactivateWarehouseOperation.execute(request, performedByUserId);
    } else {
        // Validation happens late, after some operations may have already occurred.
        throw new IllegalArgumentException("The warehouse ID in the path does not match the ID in the request body.");
    }
}
```

**✅ Correct (Fail-Fast with a Guard Clause):**

```java
public WarehouseResponse deactivateWarehouse(@NotNull UUID warehouseId,
                                             @NotNull @Valid WarehouseDeactivateRequest request,
                                             @NotNull UUID performedByUserId) {
    // --- Guard Clause ---
    if (!warehouseId.equals(request.warehouseId())) {
        throw new IllegalArgumentException("The warehouse ID in the path does not match the ID in the request body.");
    }

    // --- Core Logic ---
    // This code only runs if all preconditions are met.
    log.info("Processing deactivate warehouse request for ID: {}", request.warehouseId());
    return deactivateWarehouseOperation.execute(request, performedByUserId);
}
```

## 7. Tell, Don't Ask Principle

### Rule: Encapsulate business logic within domain objects

Instead of asking an object for its state to make decisions in the calling code (e.g., in a service), you should **tell
** the object what to do. The object itself should contain the business logic required to perform the action based on
its own state.

#### Core Principle:

- An object should be responsible for its own state and the rules that govern it.
- Methods on domain objects should represent meaningful business operations, not just simple getters and setters.

#### Rationale:

- **Stronger Encapsulation:** The internal state and the rules that govern it are hidden from the outside world. Callers
  don't need to know the implementation details.
- **Higher Cohesion:** Logic and the data it operates on are kept together in one place (the domain object).
- **Reduced Coupling:** The calling code (e.g., a service) is not tightly coupled to the internal structure of the
  domain object.
- **Improved Maintainability:** When business rules change, you only need to modify the domain object, not every service
  that uses it. This reduces the risk of introducing bugs.

#### Examples:

Let's consider changing the quantity of a `PhysicalMassStock` item.

**❌ Incorrect ("Ask" style):** The service asks for data, performs logic, and then sets the new state.

```java
// In a service class...
PhysicalMassStock stock = stockRepository.findById(...);
int quantityChange = -10;

// Asking for state and performing logic outside the domain object
if(stock.

getQuantity() +quantityChange <stock.

getReservedQuantity()){
        throw new

InsufficientStockException(...);
}
        stock.

setQuantity(stock.getQuantity() +quantityChange); // Directly manipulating state
```

**✅ Correct ("Tell" style):** The service tells the domain object what to do. The object handles its own state changes
and validation.

```java
// In a service class...
PhysicalMassStock stock = stockRepository.findById(...);
int quantityChange = -10;

// Telling the object to perform an operation
stock.

adjustQuantity(quantityChange); // The logic is encapsulated within the adjustQuantity method
```

The corresponding method in the `PhysicalMassStock` entity encapsulates all the rules:

```java
// In PhysicalMassStock.java
public void adjustQuantity(@NotZero Integer quantityChange) {
    int newQuantity = this.quantity + quantityChange;
    if (newQuantity < 0) {
        throw new InsufficientStockException(...);
    }
    if (newQuantity < this.reservedQuantity) {
        throw new InsufficientStockException(...);
    }
    this.setQuantity(newQuantity);
}
```

## 8. Read Everywhere, Change from the Service

### Rule: Centralize all write operations in a dedicated service, while allowing read operations from multiple places.

This principle establishes a clear pattern for data access: read operations can be performed from various parts of the
project to avoid unnecessary complexity, but all write operations must be strictly channeled through a single, dedicated
service for that entity.

#### Core Principle:

- **Read Operations:** Any component (e.g., another service, an operation class) is permitted to use a repository (
  `JpaRepository`) directly to read data. This avoids creating redundant "pass-through" methods in services just to
  fetch data.
- **Write Operations:** All create, update, and delete operations for a specific entity must be encapsulated within the
  single service responsible for that entity (e.g., all modifications to `Warehouse` entities must go through
  `WarehouseService`). No other component should directly call `repository.save()` or `repository.delete()` for that
  entity.

#### Rationale:

- **Single Point of Change:** Centralizing write operations provides a single, predictable entry point for all state
  modifications of an entity. This is crucial for debugging, applying business rules consistently, and ensuring data
  integrity.
- **Architectural Simplicity:** Allowing direct repository access for reads simplifies the architecture by avoiding
  unnecessary service layers or methods that add no value.
- **Clear Ownership:** It establishes clear ownership. For example, `WarehouseService` *owns* the lifecycle and
  consistency of the `Warehouse` entity.
- **Encapsulation of Business Logic:** The dedicated service acts as a gatekeeper, ensuring that all business rules,
  validation, and auditing logic are applied before any data is changed.

#### Examples:

**❌ Incorrect ("Change from anywhere"):** An external service directly modifies a `Warehouse` entity, bypassing its
dedicated service.

 ```java
 // In some other service, e.g., OrderService.java
@Autowired
private WarehouseRepository warehouseRepository;

public void processOrder(...) {
    Warehouse warehouse = warehouseRepository.findById(...).get();
    warehouse.setLocationDetails("Updated by OrderService"); // Bypasses WarehouseService rules
    warehouseRepository.save(warehouse); // VIOLATION: Another service is directly changing a Warehouse.
}
 ```

**✅ Correct:** The external service delegates the change to the responsible service. Reading, however, can be direct.

 ```java
 // In some other service, e.g., OrderService.java
@Autowired
private WarehouseService warehouseService;
@Autowired
private WarehouseRepository warehouseRepository; // Direct read access is okay

public void processOrder(...) {
    // CORRECT (Read): Reading directly from the repository is allowed.
    Warehouse warehouse = warehouseRepository.findById(...).get();
    log.info("Processing order for warehouse: {}", warehouse.getName());

    // CORRECT (Write): Delegating the change to the owner service.
    WarehouseUpdateRequest updateRequest = new WarehouseUpdateRequest(...);
    warehouseService.updateWarehouse(warehouse.getWarehouseId(), updateRequest, userId);
}
 ```

## 9. Pass Primitive Fields, Not Whole Objects

### Rule: Prefer passing primitive data types between components instead of entire objects.

When a method in one component (e.g., a service) calls a method in another, or interacts with a domain entity, it should
pass only the necessary primitive data (like IDs, names, quantities) rather than complex DTOs or entire entity objects.

#### Core Principle:

- **Data, not Containers:** Pass the specific pieces of data a method needs, not the object that happens to contain that
  data.
- **Decouple Components:** Avoid making one component dependent on the data structure of another.

#### Rationale:

- **Reduced Coupling:** The receiving method doesn't need to know about the structure of the calling component's DTOs or
  entities. This makes refactoring safer and easier, as changes to one object don't cascade through the system.
- **Clearer API Contracts:** Method signatures become self-documenting.
  `updateWarehouseName(UUID warehouseId, String newName)` is much clearer about its intent and requirements than
  `updateWarehouse(WarehouseUpdateRequest request)`.
- **Enhanced Testability:** Methods that accept primitive types are simpler to test. You don't need to build complex
  mock objects or DTOs.
- **Prevents Unintended Side Effects:** Passing a whole entity can tempt a developer to "ask" for more data or modify
  the object directly, violating the "Tell, Don't Ask" principle. Passing only primitives enforces a clear boundary.

#### Examples:

**❌ Incorrect:** The `StockService` accepts a whole `Warehouse` entity, creating a dependency on its structure.

```java
// In StockService.java
public void adjustStockForWarehouse(Warehouse warehouse, int quantityChange) {
    // This service now needs to know about the Warehouse object
    UUID warehouseId = warehouse.getWarehouseId();
    // ... logic
}
```

**✅ Correct:** The `StockService` only asks for the data it actually needs—the ID.

```java
// In StockService.java
public void adjustStockForWarehouse(UUID warehouseId, int quantityChange) {
    // This service is decoupled from the Warehouse object structure.
    // ... logic
}
```

## 10. SRP is King! (Single Responsibility Principle)

### Rule: Every component should have one, and only one, reason to change.

This rule applies to classes, methods, and modules. A component should have a single, well-defined responsibility. If a
component is doing more than one thing, it should be split.

#### Core Principle:

- **High Cohesion:** Group related functionalities together.
- **Low Coupling:** Minimize dependencies between unrelated components.
- A class should not be a "God Object" that knows and does everything. A method should not be a "Script" that performs a
  long sequence of unrelated steps.

#### Rationale:

- **Maintainability:** When a component has only one responsibility, changes related to that responsibility are isolated
  to that single component. This reduces the risk of unintended side effects.
- **Testability:** Components with a single responsibility are much easier to test. You can test their behavior in
  isolation without setting up a complex environment for their other, unrelated responsibilities.
- **Readability & Reusability:** Small, focused components are easier to understand, reuse, and reason about.

#### Examples:

Our architecture with `Service` and `Operation` classes is a direct application of SRP.

**✅ Correct (SRP is followed):** The `WarehouseService` orchestrates, while the `ActivateWarehouseOperation` has the
single responsibility of activating a warehouse.

```java
// In WarehouseService.java (Orchestrator)
@Service
public class WarehouseService {
    // ...
    private final ActivateWarehouseOperation activateWarehouseOperation;

    public WarehouseResponse activateWarehouse(...) {
        // Responsibility: Orchestrate the activation process.
        return activateWarehouseOperation.execute(request, performedByUserId);
    }
}

// In ActivateWarehouseOperation.java (Executor)
@Component
public class ActivateWarehouseOperation {
    // ...
    public WarehouseResponse execute(...) {
        // Single Responsibility: Find, validate, activate, and save a warehouse.
        // ...
    }
}
```

**❌ Incorrect (SRP is violated):** A single service method handles orchestration, business logic, data access, and other
concerns.

```java
// In a monolithic WarehouseService.java
@Service
public class WarehouseService {
    // ...
    public WarehouseResponse activateWarehouse(...) {
        // VIOLATION: This method has too many responsibilities:
        // 1. Fetches data from the repository
        // 2. Contains core business logic (changing the state)
        // 3. Saves data back to the repository
        // 4. Logs the operation

        Warehouse warehouse = warehouseRepository.findById(request.warehouseId()).orElseThrow(...);
        warehouse.setActive(true);
        Warehouse activatedWarehouse = warehouseRepository.save(warehouse);
        log.info("Successfully activated warehouse...");
        return warehouseMapper.toWarehouseResponse(activatedWarehouse);
    }
}
```

## 11. JPA Persistence Best Practices

### Rule: Always use the instance returned by the `save()` method

When persisting or updating an entity using a Spring Data JPA repository's `save()` method, you must always capture and
use the instance returned by the method. Do not assume the original object you passed to `save()` is the managed
instance.

#### Core Principle:

- The `repository.save(entity)` method returns the managed instance of the entity. This returned instance might be a
  different object in memory from the one you passed in.
- All subsequent operations within the same transaction should be performed on this returned, managed instance.

#### Rationale:

- **Data Integrity:** The returned instance is guaranteed to be in the current persistence context. Any changes made to
  it will be tracked and flushed to the database. Changes made to the original, potentially detached instance might be
  lost.
- **Avoids Subtle Bugs:** Relying on the original object can lead to hard-to-debug issues, including
  `DetachedEntityException` or updates that are silently ignored.
- **Consistency with JPA Specification:** This practice aligns with the behavior defined by the JPA specification, where
  `merge` (which `save` often delegates to) returns a managed copy.

#### Examples:

**❌ Incorrect:** The `save` method is called, but its return value is ignored. Subsequent code might be operating on a
detached instance.

 ```java
 // In an Operation or Service class...
public void updateStock(PhysicalMassStock stock) {
    stock.setQuantity(100);
    massStockRepository.save(stock); // VIOLATION: Return value is ignored.

    // Any further operations on 'stock' are risky.
    log.info("Updated stock with ID: {}", stock.getStockId()); // Might log data from a detached object.
}
 ```

**✅ Correct:** The managed instance returned by `save()` is captured and used for any further operations.

 ```java
 // In an Operation or Service class...
public void updateStock(PhysicalMassStock stock) {
    stock.setQuantity(100);
    PhysicalMassStock savedStock = massStockRepository.save(stock); // CORRECT: Return value is captured.

    // All further operations use the managed 'savedStock' instance.
    log.info("Updated stock with ID: {}", savedStock.getStockId());
}
```

## 12. The Single Level of Abstraction Principle

### Rule: Compose methods from well-named operations, hiding implementation details

Each method should represent a single level of abstraction. High-level methods should read like a narrative, describing
*what* they do, not *how*. The implementation details ("how") should be encapsulated in lower-level methods.

This approach allows code to be read naturally, like a book. A reader can understand from the method name what will be
done and only delve into the implementation if necessary.

#### Core Principles:

- **Readability as a Narrative:** Code should read from top to bottom like a story. Each method call should be like a
  well-named chapter or paragraph that explains part of the process.
- **Hide Details:** Do not clutter the main logic flow with complex checks, loops, or multiple sequential operations.
  Instead, group them into private methods with descriptive names.
- **One Level at a Time:** Within a single method, all lines of code should be at the same level of abstraction. Avoid
  mixing high-level business operation calls with low-level data manipulations.

#### Rationale:

- **Clarity:** Allows for a quick understanding of a method's purpose without getting bogged down in unnecessary
  details. The reader can decide whether they need to dive deeper into the implementation of a specific operation.
- **Maintainability:** Logic is easier to change. If you need to alter how an operation is performed, you only modify
  one small method without affecting the main, high-level flow.
- **Debugging:** Errors are easier to track. The stack trace will point to a specific, well-named operation (e.g.,
  `issueSerializableMassStock`), which immediately provides context for the problem, rather than pointing to a line
  within a long, monolithic method.

#### Examples:

The `execute` method in `ConfirmIssuanceOperation` is a perfect example of this principle.

**✅ Correct (Code reads like a story):**

The high-level `execute` method clearly shows the main decision: we either issue a unique item or a mass stock item.

```java
// In ConfirmIssuanceOperation.java
public IssuanceStockResponse execute(@NotNull @Valid ConfirmIssuanceRequest request,
                                     @NotNull UUID performedByUserId) {
    // 1. Attempt to find an existing unique item
    Optional<PhysicalUniqueItem> uniqueItemOptional = physicalUniqueItemRepository
            .findUniqueItemByOrderItemId(request.orderItemId());

    // 2. Make a decision based on the result
    if (uniqueItemOptional.isPresent()) {
        // 3a. Issue a unique item (details are hidden)
        return issueUniqueItem(uniqueItemOptional.get(), request, performedByUserId);
    }

    // 3b. Issue a mass stock item (details are hidden)
    return issueMassStock(request, performedByUserId);
}
```

In turn, the `issueMassStock` method also delegates logic to the next level of abstraction, separating the process into
issuing serializable and non-serializable stock.

```java
private IssuanceStockResponse issueMassStock(@NotNull ConfirmIssuanceRequest request,
                                             @NotNull UUID performedByUserId) {
    ProductDetailDto product = productDataProvider.getProductDetailDto(request.productId());
    if (product.isSerializableAtIssuance()) {
        // Details of serialization are hidden in this method
        PhysicalUniqueItem issuedItem = issueSerializableMassStock(request, product, performedByUserId);
        // ...
    } else {
        // Details of a simple issuance are hidden here
        PhysicalMassStock issuedStock = issueNonSerializableMassStock(request, performedByUserId);
        // ...
    }
}
```

**❌ Incorrect (All details at one level):**

If all the logic were in one method, it would be very difficult to read and understand.

```java
public IssuanceStockResponse execute(@NotNull @Valid ConfirmIssuanceRequest request,
                                     @NotNull UUID performedByUserId) {
    Optional<PhysicalUniqueItem> uniqueItemOptional =
            physicalUniqueItemRepository.findUniqueItemByOrderItemId(request.orderItemId());
    if (uniqueItemOptional.isPresent()) {
        // ... all logic from issueUniqueItem would be here ...
        log.info("Attempting to confirm issuance for unique item...");
        // ... many lines of code ...
    } else {
        ProductDetailDto product = productDataProvider.getProductDetailDto(request.productId());
        if (product.isSerializableAtIssuance()) {
            // ... all logic from issueSerializableMassStock would be here ...
            log.info("Attempting to serialize and confirm issuance...");
            if (request.quantity() != 1) {
                throw new InvalidOperationException(...);
            }
            // ... many more lines of code ...
        } else {
            // ... all logic from issueNonSerializableMassStock would be here ...
            log.info("Attempting to confirm issuance for mass stock...");
            // ... and so on ...
        }
    }
    // This method becomes huge and unmaintainable
}
```

## 13. Controller vs. Service Responsibilities

### Rule: Strict separation of HTTP layer and Business Logic

- **Controllers** are responsible for request reception, primary validation (`@Valid`), and constructing the HTTP
  response (status codes, headers).
- **Services** are responsible for all business logic, transaction management, and **mapping entities to response DTOs
  **.
- Services must always return a fully prepared Data Transfer Object (DTO) to the Controller. Entities (`@Entity`) must
  never leak out of the Service layer into the Controller.

#### Core Principle:

- **Controllers as Gatekeepers:** They ensure the request is syntactically correct and route it. They do *not* contain
  business rules and should not know about the internal domain model (Entities).
- **Services as Executives:** They execute the business operation and transform the result into the format defined by
  the public API contract (Response DTO).
- **One Way Data Flow:** `Request DTO` → **Controller** → **Service** → *Processing* → `Response DTO` → **Controller** →
  `JSON`.

#### Rationale:

- **Prevents LazyInitializationException:** Mapping Entities to DTOs often requires accessing lazy-loaded relationships.
  This must happen within the Service's `@Transactional` boundary. Doing this in the Controller (where the transaction
  is closed) will cause runtime errors.
- **Decoupling:** The API contract (DTOs) is separated from the database schema (Entities). You can refactor entities
  without breaking the Controller logic.
- **Testability:** Controllers become "thin" and easy to test (focusing only on HTTP status/validation). Services can be
  tested in isolation without mocking HTTP objects (`ResponseEntity`, `HttpServletRequest`).
- **Reusability:** If multiple controllers (e.g., internal API vs. external API) need the same logic, the Service
  provides the same consistent DTO to both.

#### Examples:

**❌ Incorrect:** The Controller interacts with the Repository and performs mapping.

```java

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository; // VIOLATION: Repository usage in Controller

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
        // VIOLATION: Business logic (finding entity) in Controller
        Product product = productRepository.findById(id)
                                           .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // VIOLATION: Mapping Entity -> DTO in Controller
        ProductResponse response = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getStockQuantity()
        );

        return ResponseEntity.ok(response);
    }
}
```

**✅ Correct:** The Controller validates and delegates. The Service handles logic and mapping.

```java
// ProductController.java
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        // Responsibility 1: Primary Validation (@Valid)
        // Responsibility 2: Delegate to Service
        ProductResponse response = productService.createProduct(request);

        // Responsibility 3: Wrap in HTTP Response
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

// ProductService.java
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        // Responsibility 1: Business Logic
        Product product = productMapper.toEntity(request);
        product = productRepository.save(product);

        // Responsibility 2: Mapping Entity -> Response DTO
        // The transaction is still open here, safe for lazy loading
        return productMapper.toResponse(product);
    }
}
```

## 14. Class Purpose and Usage Documentation (CPUD)

### Rule: Add a structured comment block to every class definition.

Every class must start with a special JavaDoc comment block that explains its purpose, usage context, and constraints.
This ensures that developers and AI assistants understand the intended business logic, its connection to the development documentation, and do not misuse or modify the
class in a way that violates architectural principles. The description should consider not only the code itself but also the development documentation.

#### Format:

The comment block must follow this structure. Use Markdown within the comment.

```java
/**
 * @brief [One-sentence summary of the class's responsibility].
 *
 * @purpose
 * [Detailed description of the class's role and purpose within the application's business logic.
 * Explain what this class is meant to do and why it exists.]
 *
 * @usage
 * - [Guideline 1: How to correctly use this class. Describe common scenarios.]
 * - [Guideline 2: Explain key interactions with other components.]
 * 
 * @documentation
 * - See: @docs/architecture/microservices/[service-name]/[path-to-relevant-doc].md
 *
 * @restrictions
 * - [Restriction 1: Explicitly state what this class should NOT be used for.]
 * - [Restriction 2: Mention any anti-patterns or incorrect modifications to avoid.]
 *
 * @dependencies
 * - [Dependency 1: Link to critical related classes or services, e.g., @link AnotherClass].
 * - [Dependency 2: Mention important enums or configurations that govern its behavior.]
 */
```

#### Example: `InventoryTransaction.java`

```java
/**
 * @brief Represents a log entry for a physical stock movement within the inventory.
 *
 * @purpose
 * This entity's sole purpose is to create an immutable audit trail of all physical changes to inventory levels.
 * It logs events such as receiving new stock, issuing stock for an order, transferring stock between
 * warehouses, and adjusting stock levels after a count.
 *
 * @usage
 * - Create a new {@code InventoryTransaction} instance only when a physical change in stock occurs.
 * - The type of transaction is strictly governed by the 
 *   {@link de.itemis.shop.inventory.domain.model.enums.InventoryTransactionType} enum.
 *
 * @documentation
 * - See: @docs/architecture/microservices/04-inventory-management-service/05-database-schema.dbml
 * 
 * @restrictions
 * - **DO NOT** use this entity to log logical or temporary state changes, such as stock reservations
 *   ({@code RESERVED}) or the release of reservations ({@code UNRESERVED}). Stock reservation is a separate
 *   concern managed elsewhere and does not represent a physical stock movement.
 * - This entity is designed to be append-only. Transactions should not be modified after creation.
 *
 * @dependencies
 * - The allowed transaction types are strictly defined in {@link de.itemis.shop.inventory.domain.model.enums.InventoryTransactionType}.
 */
```

## 15. Layer Separation and Dependency Rules

### Rule: Maintain strict layer independence and proper dependency direction

The application follows a layered architecture where the domain layer must remain independent of infrastructure concerns and the application layer. Dependencies must only flow in one direction: from outer layers (application, infrastructure) toward the core (domain).

#### Core Principle:

- **Domain Layer Independence:** The domain layer (`domain` package) contains core business logic and must NOT depend on:
  - Infrastructure frameworks (Spring Web, Spring MVC, etc.)
  - Application layer components (`application` package)
  - Presentation concerns (HTTP status codes, REST annotations, etc.)

- **Proper Dependency Direction:**
  ```
  Infrastructure/Presentation Layer (Controllers, Exception Handlers)
           ↓ (depends on)
      Application Layer (Services, DTOs, Operations)
           ↓ (depends on)
       Domain Layer (Entities, Domain Exceptions, Business Logic)
  ```

- **Application Layer:** Can depend on domain layer but not vice versa. Responsible for:
  - Orchestrating domain operations
  - Mapping between DTOs and domain entities
  - Handling HTTP concerns (status codes, exception mapping)
  - DTO-specific validation

#### Rationale:

- **Domain Purity:** The domain layer represents pure business logic that can be tested and reused in any context (web, CLI, batch jobs)
- **Framework Independence:** Business logic is not coupled to any particular framework, making it easier to migrate or upgrade
- **Better Testability:** Domain logic can be tested without Spring Web or other infrastructure dependencies
- **Improved Reusability:** Domain components can be used in multiple contexts without carrying framework baggage
- **Clear Separation of Concerns:** HTTP concerns, validation, and business logic are properly separated

#### Examples:

**❌ Incorrect (Domain layer depends on Spring Web):**

```java
// In domain/exception/InsufficientStockException.java
package de.itemis.shop.inventory.domain.exception;

import org.springframework.http.HttpStatus;  // VIOLATION: Spring Web in domain
import org.springframework.web.bind.annotation.ResponseStatus;  // VIOLATION

@ResponseStatus(HttpStatus.CONFLICT)  // VIOLATION: HTTP concern in domain
public class InsufficientStockException extends RuntimeException {
    // ...
}
```

**✅ Correct (Pure domain exception):**

```java
// In domain/exception/InsufficientStockException.java
package de.itemis.shop.inventory.domain.exception;

// No framework imports - pure domain exception
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
```

```java
// HTTP mapping handled in application layer
// In application/exception/handler/GlobalApiExceptionHandler.java
@RestControllerAdvice
public class GlobalApiExceptionHandler {
    
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        // Application layer maps domain exception to HTTP response
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ApiErrorResponse("INSUFFICIENT_STOCK", ex.getMessage()));
    }
}
```

**❌ Incorrect (Domain depends on application layer):**

```java
// In domain/model/PhysicalMassStock.java
package de.itemis.shop.inventory.domain.model;

import de.itemis.shop.inventory.application.validation.annotation.NotZero;  // VIOLATION

@Entity
public class PhysicalMassStock {
    public void adjustQuantity(@NotZero Integer quantityChange) {  // VIOLATION
        // ...
    }
}
```

**✅ Correct (Domain validation in domain layer):**

```java
// In domain/model/PhysicalMassStock.java
package de.itemis.shop.inventory.domain.model;

import de.itemis.shop.inventory.domain.validation.annotation.NotZero;  // CORRECT

@Entity
public class PhysicalMassStock {
    public void adjustQuantity(@NotZero Integer quantityChange) {  // CORRECT
        // ...
    }
}
```

```java
// In domain/validation/annotation/NotZero.java
package de.itemis.shop.inventory.domain.validation.annotation;

import de.itemis.shop.inventory.domain.validation.validator.NotZeroValidator;
import jakarta.validation.Constraint;  // Jakarta validation is acceptable

@Constraint(validatedBy = NotZeroValidator.class)
public @interface NotZero {
    // Domain-specific validation annotation
}
```

#### Validation Placement Guidelines:

- **Domain Validation:** Belongs in `domain.validation` package
  - Validates domain invariants and business rules
  - Can use Jakarta Validation annotations (framework-agnostic)
  - Examples: `@NotZero` for domain-specific numeric constraints

- **Application Validation:** Belongs in `application.validation` package
  - Validates DTO structure and request-specific rules
  - Examples: `@ValidReserveRequestQuantity` for cross-field DTO validation

#### Verification:

Regularly verify layer independence using grep searches:

```bash
# Domain layer should NOT import from application layer
grep -r "import de.itemis.shop.inventory.application" src/main/java/*/domain/

# Domain layer should NOT import Spring Web
grep -r "import org.springframework.web.bind.annotation" src/main/java/*/domain/
```

Both searches should return no results.
