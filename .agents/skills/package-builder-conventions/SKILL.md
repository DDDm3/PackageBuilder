---
name: package-builder-conventions
description: >-
  Comprehensive guide and coding conventions for the Package_Builder project.
  Use this skill whenever developing, adding features, modifying, or refactoring
  code in the Package_Builder Spring Boot codebase (e.g. creating entities, DTOs,
  mappers, services, controllers, error codes, and security rules).
---

# Package_Builder Coding Conventions & Development Guide

This skill provides the architectural rules, coding standards, and end-to-end workflow for developing features within the `Package_Builder` backend application.

---

## 1. Project Overview & Architecture

- **Stack**: Java 21, Spring Boot, Spring Security (OAuth2 Resource Server / Nimbus JOSE JWT HS512), Spring Data JPA, PostgreSQL (schema `package_builder`), MapStruct 1.6.3, Lombok.
- **Base Package**: `gascolae.group9.package_builder`
- **Architecture Pattern**: **Package-by-Feature (Modular Domain)**. Every domain/feature resides in its own package (e.g., `user`, `service_package`) and contains all necessary layers.

### Module Structure
```text
gascolae.group9.package_builder
├── config/                 # Global configuration (Security, JWT Decoder, PasswordEncoder, Seed)
├── dto/
│   └── response/           # Global response envelope: APIResponse<T>
├── exception/              # Centralized error handling: AppException, ErrorCode, GlobalExceptionHandler
├── user/                   # Reference implementation module
│   ├── controller/
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── entity/
│   ├── enums/
│   ├── mapper/
│   ├── repository/
│   └── service/
│       └── serviceImpl/
└── service_package/        # Feature module following identical structure
```

For in-depth details on each layer, consult [Architecture and Standards](./references/architecture-and-standards.md).

---

## 2. Core Coding Conventions

### 2.1. Lombok & Constructor Injection
- **Controllers & Services**: Always use `@RequiredArgsConstructor` with `@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)`.
- **Never** use `@Autowired` on individual fields. All injected dependencies must be `final`.
- **Logging**: Use `@Slf4j` from Lombok.

```java
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExampleServiceImpl implements ExampleService {
    ExampleRepository exampleRepository;
    ExampleMapper exampleMapper;
    // ...
}
```

### 2.2. DTO & Standard Response Envelope
- Every controller endpoint must return `APIResponse<T>`.
- Success default code is `1000`.
- Data payloads are wrapped in `.result(...)`.
- Void/mutation endpoints return `APIResponse<Void>` with a descriptive `.message(...)`.

```java
// With data
return APIResponse.<ExampleResponse>builder()
        .result(exampleService.create(request))
        .build();

// Void action
return APIResponse.<Void>builder()
        .message("cập nhật thành công")
        .build();
```

### 2.3. Validation & Exception Handling
- **ErrorCode Enum**: All business and validation errors are declared in [ErrorCode.java](file:///d:/GASCOLAE/Package_Builder/package-builder/src/main/java/gascolae/group9/package_builder/exception/ErrorCode.java).
- **DTO Validation**: The `message` parameter of Bean Validation constraints (`@NotBlank`, `@Email`, etc.) MUST match an `ErrorCode` enum name.
  ```java
  @NotBlank(message = "NOT_NULL")
  String name;
  ```
- **Business Exceptions**: Throw `AppException` with an `ErrorCode`:
  ```java
  throw new AppException(ErrorCode.USER_EXISTED);
  ```
- [GlobalExceptionHandler](file:///d:/GASCOLAE/Package_Builder/package-builder/src/main/java/gascolae/group9/package_builder/exception/GlobalExceptionHandler.java) automatically catches and translates these to the standardized `APIResponse`.

### 2.4. JPA Entities & Auditing
- Table names must be pluralized (e.g. `users`, `roles`, `services`).
- Primary key IDs are UUID Strings:
  ```java
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;
  ```
- Auditing: Add `@EntityListeners(AuditingEntityListener.class)` to entities, and define `@CreatedDate` / `@LastModifiedDate` fields (`createdAt`, `updatedAt`).
- Enums in entities must use `@Enumerated(EnumType.STRING)`.

### 2.5. MapStruct Mappings
- Always use `@Mapper(componentModel = "spring")`.
- When converting Request DTO to Entity, explicitly ignore audit and state fields managed by the service:
  ```java
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  User toUser(UserRegisterRequest request);
  ```

### 2.6. Security & Authorization
- Method security is enabled (`@EnableMethodSecurity`).
- Pre-execution checks: `@PreAuthorize("hasRole('ADMIN')")`.
- Post-execution checks: `@PostAuthorize("returnObject.username == authentication.name or hasRole('ADMIN')")`.
- Fetch logged-in user: `SecurityContextHolder.getContext().getAuthentication().getName()`.

---

## 3. Step-by-Step Runbook for Adding a New Feature

When creating or completing a module (such as `service_package`):

1. **Entities & Enums**: Define the JPA entity in `entity/` and any required status/type enums in `enums/`.
2. **Repository**: Create a Spring Data JPA interface in `repository/` extending `JpaRepository<Entity, String>`.
3. **DTOs**:
   - `dto/request/`: Define request objects with validation annotations pointing to `ErrorCode` names.
   - `dto/response/`: Define response objects with `@Data`, `@Builder`, `@FieldDefaults(level = AccessLevel.PRIVATE)`.
4. **Mapper**: Create a MapStruct interface in `mapper/` with `@Mapper(componentModel = "spring")`.
5. **Service**:
   - Create interface in `service/`.
   - Implement in `service/serviceImpl/` using `@Service`, `@RequiredArgsConstructor`, and `@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)`.
6. **Controller**:
   - Create `@RestController` in `controller/` with `@RequestMapping("/...")`.
   - Wrap all outputs in `APIResponse<T>`.
   - Use `@Valid @RequestBody` for payload validation.
7. **Error Codes**: Add any new domain-specific error codes to `ErrorCode.java`.

For complete copy-pasteable boilerplate, refer to [Feature Boilerplate Template](./references/feature-template.md).

---

## 4. Known Pitfalls & Anti-Patterns to Avoid

- ⚠️ **DO NOT** put `@EntityListeners(AuditingEntityListener.class)` on Controllers (this belongs ONLY on JPA Entities).
- ⚠️ **DO NOT** use `@Autowired` on private non-final fields in services or controllers.
- ⚠️ **DO NOT** return raw domain entities (`User`, `ServicePackage`) directly from Controller endpoints. Always map to Response DTOs.
- ⚠️ **DO NOT** forget to add `componentModel = "spring"` on MapStruct mappers.
- ⚠️ When adding public endpoints in `SecurityConfig`, ensure HTTP methods match (e.g. GET for Swagger documentation, POST for auth endpoints).
