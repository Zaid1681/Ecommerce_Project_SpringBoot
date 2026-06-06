# Optimistic Locking and Version Control

## What is optimistic locking?
Optimistic locking is a concurrency control strategy used to prevent conflicting updates when multiple transactions try to modify the same database row at the same time.

Instead of locking the row when it is read, optimistic locking allows multiple transactions to read the row and only checks for conflicts at save time.
A version field is stored with the row, and each update verifies that the stored version has not changed since the row was read.
If the version has changed, the update fails and typically the application retries the operation.

## Why we use it
In ecommerce systems, inventory reservation is a high-concurrency activity.
Multiple users may place orders at the same time for the same product.
If two requests read the same stock value and both attempt to reserve it, one of them can inadvertently oversell the product.

Optimistic locking prevents this by ensuring that only the first write succeeds with the current row version.
If a second request tries to save based on stale data, the save fails and the operation can retry with fresh data.

## How it works in this project
### 1. Added a version field to `Product`
The `Product` entity now includes a `@Version Long version` field.
This is the version counter that JPA uses to detect concurrent updates.

```java
@Version
private Long version;
```

### 2. Reserve stock in a retry-safe way
The `InventoryService.reserveStock(...)` method now:
- reads the product row
- computes available stock using `stock - reservedQuantity`
- updates `reservedQuantity`
- saves the product
- retries up to 3 times if a version conflict occurs

The key point is this: the retry loop only repeats when a save fails because the row version changed.
If the first attempt succeeds, the method returns immediately and no extra fetch or save happens.

### 3. What happens during a conflict
If two concurrent requests try to reserve stock for the same product:
- the first request commits successfully and increments the version
- the second request attempts to save using the older version
- JPA detects the version mismatch and throws `ObjectOptimisticLockingFailureException`
- the second request catches the exception, reloads the product, recomputes availability, and retries

## Why this solves the problem
This implementation solves overselling and race conditions without using pessimistic database locks.

Advantages:
- preserves existing request flow and latency for the normal case
- avoids long locks that block other transactions
- ensures data consistency when inventory is updated concurrently
- makes the operation idempotent in the sense that a stale save is rejected rather than silently overwriting a newer update

## Practical effect for this repo
In the current order placement flow:
- the project still creates the order normally
- it still reserves stock for each item
- but now the reservation uses a version-controlled `Product` row
- concurrent orders will not silently corrupt inventory values
- the system will retry only when a real concurrent modification conflict occurs

This is the concurrency improvement for the inventory domain in this project.

## Retries when reserving stock

Why retry: optimistic locking detects concurrent updates by comparing a `@Version` value at save/flush time. When a concurrent request has already modified the same `Product` row, the update in the current transaction will fail with an optimistic-lock exception. A retry loop lets the service handle that transient conflict by reloading the latest row, recomputing availability, and attempting the update again.

Key points:
- `save()` stages changes in the persistence context; `flush()` forces the SQL update immediately so optimistic-lock exceptions occur inside the retry block rather than at transaction commit.
- Limit retries (this project uses 3 attempts) to avoid infinite loops under sustained contention.
- Add a short backoff between attempts (e.g., a few milliseconds, optionally exponential) to reduce livelock when many concurrent writers contend for the same row.

When to use alternatives:
- If contention on a SKU is very high, consider pessimistic locking (`SELECT ... FOR UPDATE`) or an atomic DB update (single `UPDATE` statement with a `WHERE available >= ?` check) to reduce retry complexity.
- For extreme scale, a reservation queue or a separate write-serializing service can be used to serialize updates for hot SKUs.

User-facing behavior:
- The retry strategy keeps most user requests fast (no retries when there is no contention) while gracefully handling transient conflicts.
- On repeated failures the service returns a clear error (failure to reserve due to concurrent updates), allowing callers to surface a friendly message (e.g., "Item went out of stock; please try again").
