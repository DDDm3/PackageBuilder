# Feature Boilerplate Template

Use the following reference code templates when creating a new module or feature in `Package_Builder`. Replace `Item` with your domain entity name (e.g., `ServicePackage`).

---

## 1. Entity: `entity/Item.java`
```java
package gascolae.group9.package_builder.item.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true)
    String code;

    @Column(nullable = false)
    String name;

    String description;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    LocalDateTime updatedAt;
}
```

---

## 2. Repository: `repository/ItemRepository.java`
```java
package gascolae.group9.package_builder.item.repository;

import gascolae.group9.package_builder.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, String> {
    boolean existsByCode(String code);
    Optional<Item> findByCode(String code);
}
```

---

## 3. Request DTO: `dto/request/ItemCreateRequest.java`
```java
package gascolae.group9.package_builder.item.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemCreateRequest {
    @NotBlank(message = "NOT_NULL")
    String code;

    @NotBlank(message = "NOT_NULL")
    String name;

    String description;
}
```

---

## 4. Response DTO: `dto/response/ItemResponse.java`
```java
package gascolae.group9.package_builder.item.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemResponse {
    String id;
    String code;
    String name;
    String description;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

---

## 5. Mapper: `mapper/ItemMapper.java`
```java
package gascolae.group9.package_builder.item.mapper;

import gascolae.group9.package_builder.item.dto.request.ItemCreateRequest;
import gascolae.group9.package_builder.item.dto.response.ItemResponse;
import gascolae.group9.package_builder.item.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Item toItem(ItemCreateRequest request);

    ItemResponse toItemResponse(Item item);
    List<ItemResponse> toItemResponseList(List<Item> items);
}
```

---

## 6. Service Interface: `service/ItemService.java`
```java
package gascolae.group9.package_builder.item.service;

import gascolae.group9.package_builder.item.dto.request.ItemCreateRequest;
import gascolae.group9.package_builder.item.dto.response.ItemResponse;

import java.util.List;

public interface ItemService {
    ItemResponse createItem(ItemCreateRequest request);
    ItemResponse getItemById(String id);
    List<ItemResponse> getAllItems();
    void deleteItem(String id);
}
```

---

## 7. Service Implementation: `service/serviceImpl/ItemServiceImpl.java`
```java
package gascolae.group9.package_builder.item.service.serviceImpl;

import gascolae.group9.package_builder.exception.AppException;
import gascolae.group9.package_builder.exception.ErrorCode;
import gascolae.group9.package_builder.item.dto.request.ItemCreateRequest;
import gascolae.group9.package_builder.item.dto.response.ItemResponse;
import gascolae.group9.package_builder.item.entity.Item;
import gascolae.group9.package_builder.item.mapper.ItemMapper;
import gascolae.group9.package_builder.item.repository.ItemRepository;
import gascolae.group9.package_builder.item.service.ItemService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ItemServiceImpl implements ItemService {
    ItemRepository itemRepository;
    ItemMapper itemMapper;

    @Override
    public ItemResponse createItem(ItemCreateRequest request) {
        if (itemRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Item item = itemMapper.toItem(request);
        return itemMapper.toItemResponse(itemRepository.save(item));
    }

    @Override
    public ItemResponse getItemById(String id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_NULL));
        return itemMapper.toItemResponse(item);
    }

    @Override
    public List<ItemResponse> getAllItems() {
        return itemMapper.toItemResponseList(itemRepository.findAll());
    }

    @Override
    public void deleteItem(String id) {
        if (!itemRepository.existsById(id)) {
            throw new AppException(ErrorCode.NOT_NULL);
        }
        itemRepository.deleteById(id);
    }
}
```

---

## 8. Controller: `controller/ItemController.java`
```java
package gascolae.group9.package_builder.item.controller;

import gascolae.group9.package_builder.dto.response.APIResponse;
import gascolae.group9.package_builder.item.dto.request.ItemCreateRequest;
import gascolae.group9.package_builder.item.dto.response.ItemResponse;
import gascolae.group9.package_builder.item.service.ItemService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemController {
    ItemService itemService;

    @PostMapping
    public APIResponse<ItemResponse> createItem(@RequestBody @Valid ItemCreateRequest request) {
        return APIResponse.<ItemResponse>builder()
                .result(itemService.createItem(request))
                .build();
    }

    @GetMapping("/{id}")
    public APIResponse<ItemResponse> getItemById(@PathVariable("id") String id) {
        return APIResponse.<ItemResponse>builder()
                .result(itemService.getItemById(id))
                .build();
    }

    @GetMapping
    public APIResponse<List<ItemResponse>> getAllItems() {
        return APIResponse.<List<ItemResponse>>builder()
                .result(itemService.getAllItems())
                .build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteItem(@PathVariable("id") String id) {
        itemService.deleteItem(id);
        return APIResponse.<Void>builder()
                .message("Xóa thành công")
                .build();
    }
}
```
