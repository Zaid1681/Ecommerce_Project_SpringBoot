# System Design Improvements and Facade Refactor

## Goal
This document explains the design changes introduced to improve the project structure, reduce responsibility coupling, and make the order workflow easier to understand, test, and extend.

The focus areas are:
- service facade design
- SOLID-friendly decomposition
- constructor injection
- preserving the existing order placement flow

## What was refactored?
Previously, `OrderServiceImpl` did many responsibilities in one method:
- loaded user cart
- built order entities
- computed totals
- reserved inventory
- transitioned order state
- saved the order
- published Kafka events

That made the class harder to maintain and harder to unit test.

## New design structure
The `OrderServiceImpl` now delegates internal work to smaller domain services.
The public contract of `OrderService` remains unchanged.

### New services introduced
- `OrderCreationService`
  - builds the `Order` and `OrderItem` entities from the cart
  - computes the total amount
  - keeps order creation logic in one place

- `InventoryReservationService`
  - reserves inventory for all order items
  - isolates inventory reservation behavior

- `OrderStateTransitionService`
  - handles lifecycle transitions using `OrderStateMachine`
  - keeps state transition rules separate from order creation

- `OrderEventPublisher`
  - publishes domain events to Kafka
  - encapsulates event payload construction and topic selection

## Why this design is better
### 1. Single Responsibility Principle (SRP)
Each new service has one clear reason to change:
- `OrderCreationService` changes when order construction changes
- `InventoryReservationService` changes when inventory reservation rules change
- `OrderStateTransitionService` changes when order state logic changes
- `OrderEventPublisher` changes when event behavior changes

### 2. Easier testing
Smaller services are easier to test in isolation.
For example, you can unit test inventory reservation without also testing order building or event publishing.

### 3. Better maintainability
The order flow is now easier to read at a glance.
The facade method in `OrderServiceImpl` becomes a high-level workflow orchestrator instead of a large business-method block.

### 4. Better future extensibility
If new order steps are added later (for example, discount calculation, order validation, or async approval), they can be added as new services or composed cleanly.

## Constructor injection and why it matters
The refactor also converted service dependency wiring from field `@Autowired` into constructor injection.

### Benefits of constructor injection
- dependencies are explicit in the constructor signature
- the objects are easier to test with mocks
- classes become immutable after construction
- there are no hidden dependencies injected through fields
- Spring can still wire everything automatically without `@Autowired` on fields

### Example
Before:
```java
@Autowired
CartRepository cartRepo;
```
After:
```java
private final CartRepository cartRepo;

public OrderServiceImpl(CartRepository cartRepo, ...) {
    this.cartRepo = cartRepo;
}
```

## How this supports the current use case
The existing `placeOrder()` API behavior remains unchanged for users.
The same end-to-end order placement flow still works, but the internal design is now cleaner.

That means:
- no API or route changes
- no visible functional changes for customers
- only internal architecture improvements

## Summary
This refactor is a design-level improvement for the project.
It makes the order workflow more modular, compliant with SOLID principles, and better prepared for future enhancements.

If you return to the repo later, these docs explain:
- why the order service was split into domain services
- why constructor injection was used
- how the facade pattern helps preserve behavior while improving design
