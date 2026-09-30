# Architecture & Standards Deep Dive

This document details the architectural conventions, security design, and coding patterns across each layer of the `Package_Builder` backend.

---

## 1. Request Lifecycle & Layer Responsibilities

```text
[HTTP Request]
       │
       ▼
[SecurityFilterChain] ── (CustomJwtDecoder & Introspect)
       │
       ▼
[Controller]           ── (Validation: @Valid -> GlobalExceptionHandler)
       │
       ▼
[Service]              ── (Business Logic, Authorization: @PreAuthorize)
       │
   ┌───┴───────────────┐
   ▼                   ▼
[Mapper]          [Repository]
(Entity <-> DTO)   (Spring Data JPA)
                       │
                       ▼
                  [PostgreSQL]
```

### Layer Rules

| Layer | Responsibility | Forbidden in this Layer |
| :--- | :--- | :--- |
| **Controller** | Parse HTTP requests, validate DTOs, delegate to service, wrap response in `APIResponse<T>`. | Business logic, direct DB calls, entity exposure. |
| **Service** | Business logic, transaction management, authorization checks (`@PreAuthorize`), data transformation orchestration. | Direct HTTP constructs (HttpServletRequest, ResponseStatus). |
| **Mapper** | Convert between DTOs and JPA Entities via MapStruct. | DB access, heavy business logic. |
| **Repository** | Data persistence and querying through Spring Data JPA. | Business validation, DTO construction. |
| **Entity** | DB schema mapping, relationships, auditing timestamps. | Business processing, UI formatting. |

---

## 2. Authentication & Authorization

### JWT Configuration
- **Algorithm**: `HS512` (HMAC with SHA-512)
- **Token Claims**:
  - `sub`: Username
  - `scope`: Space-separated roles (e.g. `ADMIN USER`)
  - `jti`: Unique UUID string for token invalidation / logout
  - `exp`: Expiration timestamp based on `jwt.valid-duration` (default: 1800s)
- **JwtAuthenticationConverter**:
  - Automatically prepends `ROLE_` to authorities extracted from the `scope` claim:
    ```java
    authoritiesConverter.setAuthoritiesClaimName("scope");
    authoritiesConverter.setAuthorityPrefix("ROLE_");
    ```

### Logout & Token Blacklist
- The application uses stateful token invalidation:
  - When `/authenticate/logout` is called, the token's `jti` is stored in the `invalidated_tokens` table.
  - `CustomJwtDecoder` verifies token existence in `invalidated_tokens` before permitting the request.

---

## 3. Exception Handling System

### ErrorCode Structure
In `ErrorCode.java`:
```java
UNCATEGORIZED_EXCEPTION(9000, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
INVALID_KEY(1001, "Khóa thông báo không hợp lệ", HttpStatus.BAD_REQUEST),
NOT_NULL(1002, "Vui lòng điền vào tất cả các trường", HttpStatus.BAD_REQUEST),
USER_NOT_EXISTED(1101, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
```

### How GlobalExceptionHandler works
1. **`AppException`**: Direct mapping to `errorCode.getCode()`, `errorCode.getMessage()`, `errorCode.getStatusCode()`.
2. **`MethodArgumentNotValidException`**: Reads `exception.getFieldError().getDefaultMessage()`. If it matches an enum name in `ErrorCode`, that specific error code is returned; otherwise defaults to `INVALID_KEY`.
3. **`AccessDeniedException`**: Automatically maps to `ErrorCode.UNAUTHORIZED` (403 Forbidden).
4. **`Exception` (fallback)**: Catches unexpected exceptions and returns `UNCATEGORIZED_EXCEPTION` (500 Internal Server Error) while logging details via `@Slf4j`.
