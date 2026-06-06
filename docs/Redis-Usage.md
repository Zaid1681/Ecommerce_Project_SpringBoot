# Redis Usage in the E-Commerce Backend

This project uses Redis for three primary purposes:

1. **Rate Limiting**
2. **Session Validation**
3. **Caching User Data**

---

## 1. Rate Limiting

Redis is used to limit the number of requests a user can make within a fixed time window.

### Flow

1. `RateLimitFilter` executes before authentication for every incoming request.
2. If the request path is **not** part of `PUBLIC_ENDPOINTS`, the filter extracts the username from the JWT token.
3. The filter calls:

```java
rateLimiterService.isAllowed(username);
```

4. Inside `RateLimiterService`:

```java
String key = "rate:" + username;
redisTemplate.opsForValue().increment(key);
```

* When the key is created for the first time, a TTL of **30 minutes** is applied.
* A request counter is maintained for each user.
* If the counter exceeds the configured limit (**100 requests**), the request is rejected.

### Response Behavior

| Condition      | Result                       |
| -------------- | ---------------------------- |
| Within limit   | Request proceeds normally    |
| Limit exceeded | HTTP `429 Too Many Requests` |

### Important Note

The following endpoints are intentionally excluded from rate limiting:

```java
/api/order
/api/order/*
/api/payment/webhook
```

As a result, several order-related operations bypass the rate limiter by design.

### Relevant Files

* `RateLimitFilter.java`
* `RateLimiterService.java`

---

## 2. Redis-Backed Session Validation

Redis is used as a session store to validate active login sessions.

### Login Flow

Upon successful authentication:

```java
sessionService.storeSession(user.getName(), token);
```

The session is stored in Redis:

```text
Key   : session:{username}
Value : JWT Token
TTL   : 30 minutes
```

### Request Validation Flow

For every authenticated request:

1. `JwtFilter` validates the JWT token.
2. The filter calls:

```java
sessionService.isSessionValid(username, token);
```

3. The stored token is retrieved from Redis and compared against the incoming token.

### Result

| Condition                    | Response                |
| ---------------------------- | ----------------------- |
| Token matches stored session | Request allowed         |
| Token missing or different   | HTTP `401 Unauthorized` |

### Benefits

* Short-lived session management
* Token invalidation support
* Single active session enforcement (if desired)

### Relevant Files

* `UserController.java`
* `JwtFilter.java`
* `SessionService.java`

---

## 3. Redis Caching for User Data

Spring Cache is configured to use Redis as the cache provider.

### Configuration

`CacheConfig` enables:

```java
@EnableCaching
```

and configures a `RedisCacheManager`.

### Cached Operations

#### Fetch User

```java
@Cacheable(cacheNames = "users", key = "#id")
public User getUserById(Long id)
```

If the user is already cached, the database is not queried.

#### Create User

```java
@CacheEvict(cacheNames = "users", key = "#result.id")
```

#### Update User

```java
@CacheEvict(cacheNames = "users", key = "#id")
```

#### Delete User

```java
@CacheEvict(cacheNames = "users", key = "#id")
```

### Benefits

* Reduced database load
* Faster user retrieval
* Automatic cache invalidation for stale entries

### Relevant Files

* `CacheConfig.java`
* `UserServiceImpl.java`

---

# Current Redis Status

Redis integration exists in the codebase, but Redis will only function if a Redis server is available.

### Current Configuration

The Redis connection properties are currently commented out:

```properties
# spring.data.redis.host=localhost
# spring.data.redis.port=6379
```

### Implications

* Redis beans are configured (`RedisConfig`, `CacheConfig`).
* Redis-dependent features require a running Redis instance.
* If Redis is unavailable, operations using `RedisTemplate` may throw connection exceptions.

---

# Running Redis Locally

## Option 1: Homebrew (macOS)

```bash
brew install redis
brew services start redis
```

Verify:

```bash
redis-cli ping
```

Expected response:

```text
PONG
```

---

## Option 2: Docker

```bash
docker run -d \
  --name ecommerce-redis \
  -p 6379:6379 \
  redis:latest
```

Verify:

```bash
docker exec -it ecommerce-redis redis-cli ping
```

Expected response:

```text
PONG
```

---

# Enabling Redis

Either:

### Uncomment Redis Properties

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### Or

Keep them commented if Redis is running on Spring Boot's default Redis configuration (`localhost:6379`).

---

# Verifying Redis Functionality

1. Start Redis.
2. Start the Spring Boot application.
3. Call the login endpoint:

```http
POST /api/users/login
```

### Expected Behavior

If Redis is connected successfully:

* Session tokens are stored in Redis.
* Rate limiting counters are created.
* User caching becomes active.

If Redis is unavailable:

* Redis-related operations throw connection exceptions.
* Session storage and rate limiting will fail.

---

# Summary

Redis is used in this project for:

| Feature            | Purpose                                |
| ------------------ | -------------------------------------- |
| Rate Limiting      | Restrict excessive API requests        |
| Session Validation | Store and validate active JWT sessions |
| User Caching       | Cache user lookup results              |

To enable all Redis-backed functionality, ensure a Redis server is running and accessible by the application.
