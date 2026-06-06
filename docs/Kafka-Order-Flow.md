# Kafka Order & Notification Flow Documentation

## 1. Overview

This document explains how the ecommerce app handles order placement, payment, Kafka events, and customer notifications.

The current implementation uses:
- Spring Boot backend
- Kafka for event delivery
- Stripe for payment processing
- Java mail for email notifications

The Kafka feature is used to publish and consume domain events after order and payment actions.

## 2. Main feature goals

- Create an order and reserve stock
- Publish an `order-confirmed` event after the order is confirmed
- Initiate payment and publish `payment-initiated`
- Receive Stripe webhook callbacks and publish either `payment-succeeded` or `payment-failed`
- Consume those events and send email notifications
- Keep order state transitions explicit and controlled by a state machine

## 3. Key Kafka topics

The system uses a constants class so topic names are not hardcoded in many places.

Defined topics in `src/main/java/com/platform/ecommerce/kafka/KafkaTopics.java`:
- `KafkaTopics.ORDER_CONFIRMED = "order-confirmed"`
- `KafkaTopics.PAYMENT_INITIATED = "payment-initiated"`
- `KafkaTopics.PAYMENT_SUCCEEDED = "payment-succeeded"`
- `KafkaTopics.PAYMENT_FAILED = "payment-failed"`

Using constants avoids typos and makes refactoring safer.

## 4. Order lifecycle and state machine

### Supported order statuses

The app defines these order statuses in `src/main/java/com/platform/ecommerce/common/enums/OrderStatus.java`:
- `PLACED`
- `CONFIRMED`
- `PAYMENT_PENDING`
- `PAID`
- `PAYMENT_FAILED`
- `CANCELLED`
- `PROCESSING`
- `SHIPPED`
- `DELIVERED`

### Valid transitions

The state machine in `src/main/java/com/platform/ecommerce/order/statemachine/OrderStateMachine.java` restricts transitions:

| From | To |
|---|---|
| `PLACED` | `CONFIRMED`, `CANCELLED` |
| `CONFIRMED` | `PAYMENT_PENDING`, `CANCELLED` |
| `PAYMENT_PENDING` | `PAID`, `PAYMENT_FAILED`, `CANCELLED` |
| `PAID` | `PROCESSING`, `CANCELLED` |
| `PROCESSING` | `SHIPPED`, `CANCELLED` |
| `SHIPPED` | `DELIVERED` |
| `PAYMENT_FAILED` | `CANCELLED` |

This ensures the order only moves through valid lifecycle states.

## 5. Frontend-triggered API flow

### 5.1 Place order

Endpoint:
- `POST /api/order`

What it does:
- Retrieves the current user from session
- Loads the user’s cart
- Creates `Order` with items and total amount
- Reserves stock for each cart item
- Transitions order to `CONFIRMED`
- Saves the order
- Publishes Kafka event `order-confirmed`

Returns:
- `OrderResponseDto` with order details

### 5.2 Initiate payment

Endpoint:
- `POST /api/payment/initiate?orderId={orderId}`

What it does:
- Loads the order by `orderId`
- Creates a Stripe `PaymentIntent`
- Saves a `Payment` record with status `PENDING`
- Transitions the order to `PAYMENT_PENDING`
- Publishes Kafka event `payment-initiated`
- Returns Stripe `clientSecret` and related payment info to frontend

### 5.3 Stripe webhook callback

Endpoint:
- `POST /api/payment/webhook`

What it does:
- Reads raw request body bytes to verify Stripe signature
- Validates the Stripe webhook using `stripe.webhook.secret`
- Handles these Stripe event types:
  - `payment_intent.succeeded`
  - `payment_intent.payment_failed`
  - `payment_intent.canceled`
- On success, updates payment/order state and publishes `payment-succeeded`
- On failure/cancel, updates payment/order state and publishes `payment-failed`

### 5.4 Order status tracking

Endpoint:
- `GET /api/order/status/{orderId}`

This returns the current status of the order so the frontend can show the lifecycle progress.

## 6. Detailed lifecycle sequence

### 6.1 User places an order

1. Frontend calls `POST /api/order`
2. Backend creates order and sets state to `CONFIRMED`
3. System publishes `order-confirmed`
4. Notification consumer receives the event and sends a confirmation email

### 6.2 User initiates payment

1. Frontend calls `POST /api/payment/initiate?orderId={orderId}`
2. Backend creates Stripe `PaymentIntent`
3. Order moves to `PAYMENT_PENDING`
4. System publishes `payment-initiated`
5. Notification consumer receives the event and sends a payment initiation email

### 6.3 Stripe confirms payment

1. Stripe triggers the webhook to `POST /api/payment/webhook`
2. Backend verifies the signature and parses the event
3. If payment succeeded:
   - Payment status becomes `SUCCESS`
   - Order transitions to `PAID`
   - Stock is confirmed
   - Order fulfillment starts (`PROCESSING`)
   - Kafka event `payment-succeeded` is published
   - Notification consumer sends payment success email
   - Shipping service begins processing
4. If payment failed/canceled:
   - Payment status becomes `FAILED`
   - Order transitions to `CANCELLED`
   - Stock is restored
   - Kafka event `payment-failed` is published
   - Notification consumer sends payment failed email

> Note: the code currently cancels the order on failure and restores stock immediately.

## 7. Kafka event producer

The producer is implemented in `src/main/java/com/platform/ecommerce/kafka/producer/KafkaEventProducer.java`.

What it does:
- Serializes event objects to JSON using Jackson
- Sends JSON strings to a Kafka topic via `KafkaTemplate`

Example code path:
- `OrderServiceImpl` sends `KafkaTopics.ORDER_CONFIRMED`
- `PaymentServiceImpl` sends `KafkaTopics.PAYMENT_INITIATED`
- `PaymentServiceImpl` sends `KafkaTopics.PAYMENT_SUCCEEDED`
- `PaymentServiceImpl` sends `KafkaTopics.PAYMENT_FAILED`

## 8. Kafka consumer and notifications

### Consumer
The notification consumer is in `src/main/java/com/platform/ecommerce/kafka/consumer/NotificationConsumer.java`.

It listens for events on these topics:
- `KafkaTopics.ORDER_CONFIRMED`
- `KafkaTopics.PAYMENT_INITIATED`
- `KafkaTopics.PAYMENT_SUCCEEDED`
- `KafkaTopics.PAYMENT_FAILED`

Each listener method:
- receives the incoming JSON message as `String`
- deserializes to the correct event class
- calls `NotificationService` to send the email

### Notification service
The email implementation is in `src/main/java/com/platform/ecommerce/notification/serviceImpl/NotificationServiceImpl.java`.

It currently sends from and to fixed addresses:
- `from`: `zaidkhan1781@gmail.com`
- `to`: `zaidkhan1682@gmail.com`

The service builds a simple email with:
- subject
- body text describing the order/payment state
- sends the message via `JavaMailSender`

## 9. Key implementation files

| File | Purpose |
|---|---|
| `src/main/java/com/platform/ecommerce/config/KafkaConfig.java` | Kafka producer/consumer beans and topic serializers/deserializers |
| `src/main/java/com/platform/ecommerce/kafka/KafkaTopics.java` | Single source of truth for Kafka topic names |
| `src/main/java/com/platform/ecommerce/kafka/producer/KafkaEventProducer.java` | JSON event publishing helper |
| `src/main/java/com/platform/ecommerce/order/serviceImpl/OrderServiceImpl.java` | Place order, reserve stock, confirm order, publish order event |
| `src/main/java/com/platform/ecommerce/payment/serviceImpl/PaymentServiceImpl.java` | Create Stripe payment intent, handle webhook, publish payment events |
| `src/main/java/com/platform/ecommerce/kafka/consumer/NotificationConsumer.java` | Kafka listeners that dispatch email notifications |
| `src/main/java/com/platform/ecommerce/notification/serviceImpl/NotificationServiceImpl.java` | Builds and sends email messages |
| `src/main/java/com/platform/ecommerce/order/statemachine/OrderStateMachine.java` | Validates allowed order state transitions |

## 10. How Kafka fits into the feature

Kafka is used for asynchronous event delivery:
- order confirmation is published after the order is created and confirmed
- payment initiation is published when Stripe payment begins
- payment result events are published from webhook handling

This allows different parts of the system to react independently:
- notifications can be sent without blocking the API response
- shipping or analytics systems can subscribe later
- the backend remains loosely coupled

## 11. Beginner-friendly summary

1. User hits `POST /api/order`.
2. Order is created and confirmed.
3. `order-confirmed` event is sent to Kafka.
4. User starts payment with `POST /api/payment/initiate?orderId=...`.
5. `payment-initiated` event is sent to Kafka.
6. Stripe calls `/api/payment/webhook` when payment succeeds or fails.
7. Backend updates order/payment state and publishes `payment-succeeded` or `payment-failed`.
8. Notification consumer reads the Kafka event and sends an email.
9. Order state can be checked at `GET /api/order/status/{orderId}`.

## 12. Notes for future improvements

- Use configured `from`/`to` email values instead of hardcoded addresses.
- Add dedicated DTO validation for the webhook payload.
- Add a separate Kafka topic or consumer for shipping and analytics.
- Use a typed JSON consumer factory instead of raw `String` consumers.
- Implement dead-letter handling for failed Kafka message processing.

---

This document is intended to help a beginner understand how the order/payment/Kafka/notification feature works end to end. If you want, I can also add a simple sequence diagram or a quick how-to section for testing the flow locally with Kafka and Stripe.