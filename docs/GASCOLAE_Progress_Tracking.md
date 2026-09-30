# GASCOLAE Intelligent Service Package Builder
## Bảng Theo Dõi Tiến Độ Triển Khai (Progress Tracker)

- **Dự án**: GASCOLAE Service Package Builder (MVP 4 Tuần)
- **Tài liệu kế hoạch**: [GASCOLAE_Implementation_Plan_4Weeks.md](./GASCOLAE_Implementation_Plan_4Weeks.md)
- **Quy chuẩn lập trình**: [.agents/skills/package-builder-conventions/SKILL.md](../.agents/skills/package-builder-conventions/SKILL.md)
- **Cập nhật lần cuối**: 2026-09-30 (Bắt đầu Giai đoạn 0)

---

## 1. Dashboard Tổng Quan Tiến Độ

| Hạng mục | Trạng thái | Tỷ lệ hoàn thành | Ghi chú |
| :--- | :---: | :---: | :--- |
| **0. Khởi tạo & Quy chuẩn nền tảng** | 🟢 **ĐÃ XONG** | **100%** | Đã đọc hiểu MVP, DB V2, lập skill conventions, kế hoạch 4 tuần |
| **Giai đoạn 0: Dọn dẹp & Đồng bộ nền tảng** | 🟢 **ĐÃ XONG** | **100%** | Sửa Swagger GET, gỡ annotation thừa Controller, update RoleName |
| **Tuần 1: Customer & Requirement** | 🟡 **ĐANG LÀM** | **50%** | **Đã xong Module Customer (100%)**, tiếp tục Module Requirement |
| **Tuần 2: Service Catalog & Recommendation** | ⚪ CHƯA BẮT ĐẦU | **0%** | Catalog 12 dịch vụ, Data Items, Tags, Matching Engine |
| **Tuần 3: Package Builder & Validation Engine** | ⚪ CHƯA BẮT ĐẦU | **0%** | Gói dịch vụ, phân phase, Rule Engine quét Gap/Dependency |
| **Tuần 4: Summary, E2E Testing & Demo** | ⚪ CHƯA BẮT ĐẦU | **0%** | Báo cáo bàn giao Sales-to-Ops, Test 3 kịch bản, Docker deploy |
| **TỔNG THỂ MVP BACKEND** | 🟡 **ĐANG TIẾN HÀNH** | **~30%** | Xong Auth + Customer CRUD, tiếp tục với Requirement & AI Extraction |

---

## 2. Chi Tiết Tiến Độ Từng Hạng Mục

### 0. Khởi Tạo & Chuẩn Bị (Foundation & Architecture) - [ĐÃ HOÀN THÀNH 100%]
- [x] Đọc và phân tích toàn bộ codebase hiện tại ([package-builder](../package-builder)).
- [x] Đọc hiểu tài liệu nghiệp vụ MVP ([GASCOLAE_Service_Package_Builder_MVP.md](./GASCOLAE_Service_Package_Builder_MVP.md)).
- [x] Đọc hiểu và thẩm định thiết kế cơ sở dữ liệu ([GASCOLAE_Database_Design_V2_UUID_All_Tables.md](./GASCOLAE_Database_Design_V2_UUID_All_Tables.md)).
- [x] Đóng gói toàn bộ kiến trúc & quy chuẩn lập trình thành Workspace Skill ([package-builder-conventions](../.agents/skills/package-builder-conventions/SKILL.md)).
- [x] Lập kế hoạch triển khai chi tiết 4 tuần theo chiến lược Customer First ([GASCOLAE_Implementation_Plan_4Weeks.md](./GASCOLAE_Implementation_Plan_4Weeks.md)).
- [x] Hoàn thiện module xác thực & phân quyền nền tảng:
  - [x] Entity `User`, `Role`, `InvalidatedToken`.
  - [x] Spring Security + OAuth2 Resource Server (Nimbus JOSE JWT HS512).
  - [x] Đăng ký, đăng nhập, đổi mật khẩu, đăng xuất (Token Blacklist).
  - [x] Chuẩn hóa `APIResponse<T>`, `ErrorCode`, `GlobalExceptionHandler`.

---

### Giai đoạn 0: Dọn Dẹp & Đồng Bộ Nền Tảng (Ngày 1) - [ĐÃ HOÀN THÀNH 100%]
- [x] **Sửa cấu hình Security**: Cập nhật `SecurityConfig.java`, mở quyền `GET` cho `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`.
- [x] **Gỡ Annotation thừa trên Controller**: Xóa `@EntityListeners(AuditingEntityListener.class)` trên `UserController`, `AuthenticateController`, `RoleController`.
- [x] **Đồng bộ Role Enum**: Bổ sung `SALES_PRE_SALES`, `OPERATION`, `CUSTOMER` vào `RoleName.java`.
- [x] **Xóa file nháp**: Xóa `service_package/entity/ServicePackage.java`.

---

### Tuần 1: Customer & Customer Requirement (Ngày 2 - Ngày 7) - [0%]

#### 1. Module Customer (`customer/`) - [ĐÃ HOÀN THÀNH 100%]:
- [x] Enum `CustomerStatus` (`ACTIVE`, `INACTIVE`).
- [x] Entity `Customer` (UUID PK, customerCode Unique, name, company, industry, contact, auditing timestamps).
- [x] `CustomerRepository` (kế thừa `JpaRepository`, tìm kiếm theo code, email, tên công ty, phân trang).
- [x] DTOs: `CustomerCreateRequest`, `CustomerUpdateRequest`, `CustomerResponse`.
- [x] Mapper: `CustomerMapper` (MapStruct `componentModel = "spring"`).
- [x] Service: `CustomerService` & `CustomerServiceImpl`.
- [x] Controller: `CustomerController` (`POST`, `GET` list/detail/code, `PUT` update, `PUT` update-status).
- [x] Bổ sung mã lỗi: `CUSTOMER_NOT_FOUND`, `CUSTOMER_CODE_EXISTED`, `CUSTOMER_EMAIL_EXISTED` vào `ErrorCode.java`.

#### 2. Module Requirement & AI Extraction (`requirement/`):
- [ ] Enums: `RequirementStatus` (`DRAFT`, `CONFIRMED`, `ARCHIVED`), `ExtractionMethod` (`MANUAL`, `AI`).
- [ ] Entity `CustomerRequirement` (trỏ `customerId`, diện tích, môi trường, mục tiêu, extraction confidence).
- [ ] Entity `RequirementExpectedOutput` (đầu ra kỳ vọng).
- [ ] Repositories: `CustomerRequirementRepository`, `RequirementExpectedOutputRepository`.
- [ ] DTOs & Mapper: `RequirementCreateRequest`, `RequirementResponse`, `RequirementExtractRequest`, `RequirementMapper`.
- [ ] Service AI Extraction: Tích hợp Gemini LLM API trích xuất thông tin tự nhiên thành JSON cấu trúc.
- [ ] Controller: `RequirementController` (`POST /extract`, `POST /create`, `GET /{id}`, `GET /by-customer/{customerId}`).
- [ ] Bổ sung mã lỗi: `REQUIREMENT_NOT_FOUND`, `EXTRACTION_FAILED`.

---

### Tuần 2: Service Catalog, Taxonomy & Recommendation (Ngày 8 - Ngày 14) - [0%]

#### 1. Service Catalog & Taxonomy Master Data (`catalog/`):
- [ ] Entity & Repository `Tag`, `ServiceTag` (phân loại `OBJECTIVE`, `INDUSTRY`, `ENVIRONMENT`, `TOPIC`).
- [ ] Entity & Repository `DataItem` (taxonomy trung tâm cho inputs, outputs, deliverables).
- [ ] Entity & Repository `Service` (thông tin 12+ dịch vụ GASCOLAE, category, use cases).
- [ ] Entity & Repository `ServiceInput` (đầu vào kèm cờ `required` và `provided_by`).
- [ ] Entity & Repository `ServiceOutput` (đầu ra sinh ra bởi dịch vụ).
- [ ] Entity & Repository `ServiceDeliverable`, `ServiceDeliverableItem` (sản phẩm bàn giao).
- [ ] Entity & Repository `ServiceRelation` (quan hệ `PROVIDES_INPUT_FOR`, `RECOMMENDED_WITH`, `ALTERNATIVE_TO`, `OVERLAPS_WITH` kèm `via_data_item_id`).
- [ ] DTOs, Mappers, Services, Controllers quản lý Catalog.
- [ ] `CatalogDataInitializer`: Nạp Seed Data 12 dịch vụ mẫu và quan hệ dependency.

#### 2. Recommendation Engine (`recommendation/`):
- [ ] Entity & Repository `ServiceRecommendation`.
- [ ] Thuật toán tính điểm khớp nhu cầu có trọng số (Weighted Matching Engine):
  $$\text{Score} = w_{\text{obj}} \cdot S_{\text{obj}} + w_{\text{usecase}} \cdot S_{\text{usecase}} + w_{\text{ind}} \cdot S_{\text{ind}} + w_{\text{output}} \cdot S_{\text{output}} + w_{\text{tag}} \cdot S_{\text{tag}}$$
- [ ] Tự động sinh `recommendation_reason` tường minh.
- [ ] API: `GET /api/requirements/{id}/recommendations`.

---

### Tuần 3: Solution Package Builder & Validation Engine (Ngày 15 - Ngày 21) - [0%]

#### 1. Package Builder (`solution_package/`):
- [ ] Entity & Repository `SolutionPackage`, `PackageService`, `PackageOpenQuestion`.
- [ ] DTOs, Mapper, Service, Controller: tạo gói giải pháp, thêm/gỡ dịch vụ, gán vai trò (`CORE`, `SUPPORTING`, `OPTIONAL`), xếp thứ tự Phase.
- [ ] Quản lý câu hỏi mở `package_open_questions` giữa Sales, Operation và Customer.

#### 2. Dependency & Gap Validation Engine (`validation/`):
- [ ] Entity & Repository `ValidationRun`, `ValidationResult`.
- [ ] Lập trình Rule Engine kiểm tra 4 quy tắc:
  - [ ] Rule 1: `MISSING_REQUIRED_INPUT` (quét thiếu đầu vào bắt buộc).
  - [ ] Rule 2: `MISSING_DEPENDENCY` (quét quan hệ `PROVIDES_INPUT_FOR`).
  - [ ] Rule 3: `DUPLICATE_DELIVERABLE` (phát hiện trùng lặp sản phẩm bàn giao).
  - [ ] Rule 4: `NEED_MANUAL_VERIFICATION` (cảnh báo dịch vụ chưa kiểm chứng).
- [ ] Tự động tính toán trạng thái gói: `READY`, `CONDITIONALLY_READY`, `GAP`.
- [ ] API: `POST /api/packages/{id}/validate`.

---

### Tuần 4: Solution Summary, E2E Integration & Demo Handover (Ngày 22 - Ngày 28) - [0%]
- [ ] API Summary Aggregator: `GET /api/packages/{id}/summary` (tổng hợp dịch vụ theo phase, inputs cần chuẩn bị, deliverables bàn giao, trạng thái sẵn sàng).
- [ ] Viết Integration Test kiểm thử 3 kịch bản demo:
  - [ ] Kịch bản 1: Forest Carbon Project (Dự án Rừng 2.000 ha).
  - [ ] Kịch bản 2: Missing Dependency Gap (Cố tình thiếu dịch vụ hỗ trợ $\rightarrow$ Validator cảnh báo $\rightarrow$ Khắc phục).
  - [ ] Kịch bản 3: GHG Monitoring (Quan trắc phát thải nhà kính).
- [ ] Đóng gói Docker Compose (`docker compose up -d`) chạy ổn định.
- [ ] Tinh chỉnh tài liệu OpenAPI / Swagger UI bàn giao hoàn chỉnh cho Frontend.

---

## 3. Nhật Ký Hoạt Động (Activity Log)

| Ngày | Người thực hiện | Nội dung thực hiện | Kết quả / Ghi chú |
| :--- | :--- | :--- | :--- |
| **2026-09-30** | Antigravity & User | - Đọc và phân tích toàn bộ dự án `Package_Builder`<br>- Đọc hiểu `GASCOLAE_Service_Package_Builder_MVP.md`<br>- Đọc hiểu & thẩm định `GASCOLAE_Database_Design_V2_UUID_All_Tables.md`<br>- Xây dựng Skill `package-builder-conventions`<br>- Lập kế hoạch 4 tuần `GASCOLAE_Implementation_Plan_4Weeks.md`<br>- Thống nhất chiến lược Customer First & tạo file theo dõi tiến độ | Hoàn tất giai đoạn phân tích & kiến trúc. |
| **2026-09-30** | Antigravity | **Hoàn thành Giai đoạn 0**: <br>1. Sửa lỗi `SecurityConfig.java` (mở quyền GET Swagger UI, docs).<br>2. Gỡ bỏ `@EntityListeners` trên 3 Controllers.<br>3. Bổ sung `SALES_PRE_SALES`, `OPERATION`, `CUSTOMER` vào `RoleName.java`.<br>4. Xóa file nháp `ServicePackage.java`.<br>5. Chạy `mvn test-compile` thành công 100%. | Đạt mốc **20%** tiến độ tổng thể. |
| **2026-09-30** | Antigravity | **Hoàn thành Module Customer**: <br>1. Enum `CustomerStatus` (`ACTIVE`, `INACTIVE`).<br>2. Entity `Customer` (UUID, customerCode unique, JPA Auditing).<br>3. `CustomerRepository` (tìm kiếm, lọc status, phân trang).<br>4. DTOs: `CustomerCreateRequest`, `CustomerUpdateRequest`, `CustomerResponse`.<br>5. `CustomerMapper` (MapStruct ignore auto fields).<br>6. `CustomerService` & `CustomerServiceImpl`.<br>7. `CustomerController` (REST APIs: POST, GET search/detail/code, PUT update/status).<br>8. Bổ sung `CUSTOMER_NOT_FOUND`, `CUSTOMER_CODE_EXISTED`, `CUSTOMER_EMAIL_EXISTED` vào `ErrorCode.java`.<br>9. Biên dịch `./mvnw test-compile` thành công 100% (49 source files). | Đạt mốc **~30%** tiến độ tổng thể. Sẵn sàng cho Module Requirement. |
