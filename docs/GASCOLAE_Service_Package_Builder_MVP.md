# GASCOLAE Intelligent Service Package Builder

## Tài liệu mô tả MVP, chiến lược triển khai và công nghệ

**Loại dự án:** Extra Innovation Project / Enterprise MVP  
**Đối tượng sử dụng chính:** Sales, Pre-Sales, Operation; mở rộng cho Customer Self-Service  
**Nhóm triển khai:** 03 sinh viên  
**Thời gian MVP đề xuất:** 04 tuần  
**Nguyên tắc cốt lõi:** Không phụ thuộc dữ liệu UAV thực tế; chỉ sử dụng Service Assets/Metadata đã được xác minh làm nguồn sự thật.

---

## 1. Executive Summary

GASCOLAE Service Package Builder là một web application hỗ trợ đội Sales chuyển **nhu cầu của khách hàng** thành một **gói giải pháp gồm nhiều service GASCOLAE** có cấu trúc, có giải thích, có kiểm tra dependency và có thể bàn giao cho Operation.

Thay vì Sales phải đọc từng Service Profile và tự ghép các dịch vụ, hệ thống thực hiện chuỗi:

`Customer Requirement → Requirement Extraction → Service Recommendation → Package Builder → Dependency & Gap Validation → Solution Summary`

MVP không xử lý ảnh UAV, LiDAR, telemetry hoặc dữ liệu mission thực tế. Hệ thống làm việc với dữ liệu business/service như Service ID, category, target customer, use case, inputs, outputs, deliverables, tags và relations.

> **Lưu ý về hình mockup:** Một số code dịch vụ, nhãn công nghệ, thời lượng và nội dung chi tiết trong hình minh họa là dữ liệu concept do công cụ tạo ảnh sinh ra. Chúng chỉ minh họa UX, không được xem là capability, timeline hoặc dữ liệu chính thức của GASCOLAE nếu chưa được xác minh trong Service Assets.

---

## 2. Bài toán doanh nghiệp

Khi catalog dịch vụ tăng, Sales gặp bốn vấn đề chính:

1. Khách hàng mô tả **bài toán**, không nói chính xác tên service cần mua.
2. Một nhu cầu thực tế thường cần **nhiều service kết hợp** thay vì một service riêng lẻ.
3. Các service có thể có dependency theo input/output; chọn thiếu service sẽ tạo package không triển khai được.
4. Sales và Operation có nguy cơ hiểu khác nhau về scope, required inputs, deliverables và open questions.

Service Package Builder tạo một lớp cấu hình giải pháp nằm giữa **Service Catalog** và **Proposal/Delivery workflow**.

---

## 3. Mục tiêu MVP

### 3.1 Mục tiêu kinh doanh

- Giúp Sales tìm service phù hợp nhanh hơn.
- Chuẩn hóa cách xây solution package giữa các Sales.
- Giảm bỏ sót input/dependency trước khi gửi proposal.
- Tạo một package có cấu trúc để bàn giao sang Operation.
- Tái sử dụng dữ liệu từ Service Assets thay vì tạo một kho dữ liệu mới không kiểm soát.

### 3.2 Mục tiêu kỹ thuật

- Xây một web application chạy end-to-end.
- Tách AI khỏi nguồn sự thật: LLM chỉ hỗ trợ extraction, không tự tạo capability.
- Recommendation phải giải thích được vì sao service được chọn.
- Validation phải dựa trên rule và quan hệ dữ liệu có thể test.
- Có khả năng mở rộng từ 12 services của nhóm sang catalog lớn hơn.

---

## 4. Người dùng và User Journey

### Sales / Pre-Sales

1. Tạo Customer Requirement.
2. Nhập requirement dạng form hoặc natural language.
3. Xem các field AI trích xuất và chỉnh sửa nếu cần.
4. Xem danh sách Recommended Services và lý do match.
5. Thêm/bỏ/sắp xếp service trong package.
6. Chạy Validate Package.
7. Xử lý cảnh báo missing input/dependency.
8. Xuất Solution Package Summary / Proposal Draft.

### Operation

1. Nhận package đã được Sales xác nhận.
2. Xem required inputs, expected deliverables và open questions.
3. Kiểm tra readiness trước khi triển khai.
4. Theo dõi các dependency quan trọng giữa các phase.

### Customer (Phase 2)

Khách hàng có thể dùng một phiên bản self-service đơn giản để mô tả mục tiêu và nhận solution recommendation trước khi liên hệ Sales.

---

## 5. Phạm vi MVP

### In Scope

- Service Catalog.
- Customer Requirement.
- Natural-language Requirement Extraction (optional AI nhưng nên có).
- Service Recommendation.
- Service Package Builder.
- Service Relations / Dependency Model.
- Required Input Aggregation.
- Deliverable Aggregation.
- Dependency & Gap Validation.
- Solution Package Summary.
- Export/Print solution summary.
- Demo deployment.

### Out of Scope

- Xử lý ảnh/video UAV.
- Điều khiển drone / mission planning.
- Pricing tự động cuối cùng.
- Contract/payment.
- Tích hợp CRM thật.
- Production-grade IAM đa tổ chức.
- Auto-approval không có người kiểm tra.

---

## 6. Chức năng chi tiết

### 6.1 Service Catalog

Mỗi service tối thiểu gồm:

- Service ID / Service Code
- Service Name
- Category
- Short Description
- Target Customer
- Problems / Use Cases
- Inputs
- Outputs
- Deliverables
- Tags
- Data Classification / Verification Status

Nguồn dữ liệu: Research Workbook → Knowledge Base → Service Profile → Metadata → SOP → Business Assets.

### 6.2 Requirement Mapping

Input có thể là form có cấu trúc hoặc natural language. Ví dụ:

> “Doanh nghiệp quản lý 2.000 ha rừng, muốn đánh giá tiềm năng carbon và chuẩn bị MRV.”

AI chỉ trích xuất các entity như industry, area, objective, expected outputs. Sau extraction, Sales có quyền sửa tất cả field trước khi recommendation chạy.

### 6.3 Service Recommendation

MVP ưu tiên rule-based / weighted matching:

- Objective Match
- Use Case Match
- Customer/Industry Match
- Expected Output Match
- Tag Match

Mỗi recommendation phải trả về **reason**, ví dụ “Matches objective: Carbon Project; expected output: MRV”.

### 6.4 Package Builder

Sales có thể:

- Add service
- Remove service
- Reorder service
- Đánh dấu Core / Supporting / Optional
- Tổ chức service thành phase

### 6.5 Dependency & Gap Validation

Các trạng thái MVP:

- `MISSING_REQUIRED_INPUT`
- `MISSING_DEPENDENCY`
- `DUPLICATE_DELIVERABLE`
- `NEED_MANUAL_VERIFICATION`

Relation type đề xuất:

- `PROVIDES_INPUT_FOR`
- `RECOMMENDED_WITH`
- `ALTERNATIVE_TO`
- `OVERLAPS_WITH`

Ví dụ: nếu Carbon Assessment cần Biomass Data nhưng package chưa có nguồn cung cấp, hệ thống cảnh báo và có thể gợi ý supporting service đã được catalog xác minh.

### 6.6 Solution Package Summary

Output cuối gồm:

- Customer / Project information
- Objective
- Selected Services & phases
- Required Inputs
- Outputs
- Deliverables
- Dependency status
- Open Questions
- Package Status: Ready / Conditionally Ready / Gap
- Export PDF / Print

---

## 7. Luồng nghiệp vụ end-to-end

```text
Customer Requirement
        ↓
Structured Requirement
        ↓
Service Recommendation
        ↓
Human Review by Sales
        ↓
Service Package Builder
        ↓
Dependency & Gap Validation
        ↓
Resolve Missing Inputs / Dependencies
        ↓
Solution Package Summary
        ↓
Sales-to-Ops Handover
```

Điểm quan trọng: AI **không** được bypass human review hoặc tự biến dữ liệu chưa xác minh thành cam kết bán hàng.

---

## 8. Kiến trúc kỹ thuật đề xuất

### Frontend

- Next.js
- React
- TypeScript
- Tailwind CSS hoặc component library nhẹ

### Backend

- Java Spring Boot
- REST API
- Spring Data JPA
- Bean Validation
- OpenAPI/Swagger

### Database

- PostgreSQL

Các nhóm bảng:

- `services`
- `service_inputs`
- `service_outputs`
- `service_deliverables`
- `service_tags`
- `service_relations`
- `customer_requirements`
- `solution_packages`
- `package_services`
- `validation_results`

### AI Layer

- LLM API cho natural-language extraction và hỗ trợ summary.
- Output AI phải theo JSON schema.
- Không dùng LLM làm nguồn dữ liệu dịch vụ.
- Nếu AI extraction lỗi hoặc confidence thấp, yêu cầu người dùng xác nhận.

### Deployment MVP

- Frontend: Vercel hoặc containerized deployment.
- Backend: Docker + cloud VM/PaaS.
- Database: managed PostgreSQL hoặc Docker PostgreSQL cho demo.
- Environment variables cho credentials/API keys.

---

## 9. Data Strategy và Single Source of Truth

### Data pipeline

`Verified Service Assets → Normalization → Service Dataset → PostgreSQL → API → Recommendation/Validation`

### Quy tắc dữ liệu

- Không tự bịa service, price, capability, standard, legal claim hoặc timeline.
- Missing data phải dùng `NEED_VERIFY` hoặc trạng thái tương đương.
- Mọi field quan trọng nên có `source_ref` hoặc khả năng truy ngược về Service Asset.
- Dữ liệu public và internal cần có classification nếu triển khai thực tế.
- Mock data phải được gắn nhãn `DEMO`.

---

## 10. Recommendation Strategy

### Phase 1 - Rule-based

Ưu tiên vì dễ giải thích, dễ test và phù hợp MVP.

Ví dụ logic:

```text
IF objective contains Carbon Project
  → match carbon-related services
IF expected_output contains MRV
  → tăng match cho MRV-related service
IF environment = Forest
  → tăng match cho forest-related services
```

### Phase 2 - Semantic Search

Có thể bổ sung embeddings / pgvector khi catalog lớn hơn. Semantic search chỉ chọn candidate; quyết định cuối vẫn qua rule/verification.

---

## 11. Validation Strategy

Validator không chỉ hỏi “service có được chọn chưa?” mà kiểm tra package integrity:

1. Required input có nguồn cung cấp không?
2. Có dependency cần thực hiện trước không?
3. Có deliverable trùng nhau không?
4. Có field nào chưa xác minh không?
5. Có service nào nằm ngoài requirement không?

MVP chỉ cần 3-4 rule ổn định nhưng phải có unit test và demo case rõ ràng.

---

## 12. Chiến lược triển khai 4 tuần

### Week 1 - Foundation

**SV1 Backend:** Spring Boot, PostgreSQL, schema, Service API.  
**SV2 Frontend:** Next.js, Service Catalog, Service Detail, search/filter.  
**SV3 Data/AI/QA:** chuẩn hóa 12 service, seed data, data dictionary.

**Milestone:** Service Catalog chạy từ DB → API → UI.

### Week 2 - Requirement & Recommendation

**SV1:** Requirement API, matching engine, recommendation endpoint.  
**SV2:** Requirement form, recommendation UI, match reasons.  
**SV3:** taxonomy, mapping rules, test scenarios, optional AI extraction.

**Milestone:** Customer Requirement → Recommended Services.

### Week 3 - Package Builder & Validation

**SV1:** package API, relations, validator.  
**SV2:** Package Builder, add/remove/reorder, validation panel.  
**SV3:** relation dataset, dependency mapping, QA.

**Milestone:** Package Builder phát hiện được missing dependency/input.

### Week 4 - Summary, QA, Demo

**SV1:** summary API, bugfix, deployment.  
**SV2:** summary UI, export/print, UI polish.  
**SV3:** end-to-end tests, user guide, demo script.

**Milestone:** MVP chạy end-to-end và có URL demo.

---

## 13. Phân công 3 thành viên

### Sinh viên 1 - Backend / System Lead

- Architecture
- Database schema
- REST API
- Recommendation Engine
- Validation Engine
- Deployment backend

### Sinh viên 2 - Frontend / UX

- UI/UX flow
- Service Catalog
- Requirement UI
- Recommendation UI
- Package Builder
- Solution Summary
- Export/Print

### Sinh viên 3 - Data / AI / QA

- Service data normalization
- Relation rules
- Requirement taxonomy
- Optional AI extraction
- Test cases
- Data QA
- Demo scenarios / User Guide

Cả 3 cùng chịu trách nhiệm integration review và demo.

---

## 14. Definition of Done

MVP hoàn thành khi người dùng có thể:

1. Mở catalog service từ database.
2. Tạo Customer Requirement.
3. Nhận recommendation có match reason.
4. Tạo package và add/remove/reorder service.
5. Validate package.
6. Phát hiện ít nhất missing dependency và missing required input.
7. Sửa package và chạy validation lại.
8. Xem aggregated inputs, outputs, deliverables và open questions.
9. Xuất/print Solution Package Summary.
10. Chạy 3 demo scenario ổn định.

---

## 15. Test Strategy

### Unit Test

- Recommendation rules
- Validation rules
- Package status calculation

### Integration Test

- API + PostgreSQL
- Requirement → recommendation
- Package → validation

### End-to-End Test

- Forest Carbon scenario
- GHG Monitoring scenario
- Missing Dependency scenario

### Data QA

- Service ID uniqueness
- Input/output taxonomy consistency
- `NEED_VERIFY` handling
- No unsupported public claims

---

## 16. Demo Scenario chính

**Customer:** ABC Forestry  
**Objective:** Forest Carbon Project  
**Area:** 2.000 ha  
**Expected:** Carbon Assessment + MRV

Demo steps:

1. Sales nhập natural-language request.
2. AI extract requirement.
3. Sales xác nhận/chỉnh field.
4. System trả recommended services.
5. Sales tạo package.
6. Cố tình thiếu một supporting dependency.
7. Validator cảnh báo missing input.
8. Sales thêm supporting service.
9. Validator chuyển trạng thái tốt hơn.
10. Xuất Solution Package Summary và chuyển sang Operation.

---

## 17. KPI đánh giá MVP

Không nên tuyên bố KPI thực tế trước khi pilot. KPI đề xuất để đo sau khi có người dùng:

- Thời gian từ requirement đến draft package.
- Số lần package bị thiếu input/dependency.
- Tỷ lệ recommendation được Sales chấp nhận/chỉnh sửa.
- Tỷ lệ package cần sửa sau khi Operation review.
- Thời gian onboarding Sales mới để tìm đúng service.

---

## 18. Rủi ro và biện pháp

### Dữ liệu service chưa đủ

**Biện pháp:** `NEED_VERIFY`, không auto-fill.

### AI hallucination

**Biện pháp:** AI chỉ extraction; catalog/rule engine là source of truth; structured output + human confirmation.

### Scope MVP quá lớn

**Biện pháp:** giữ 6 module core, pricing/CRM/UAV để Phase 2.

### Dependency mapping sai

**Biện pháp:** relation dataset có review, test case và source reference.

### Mockup bị hiểu là dữ liệu thật

**Biện pháp:** gắn nhãn concept/demo; không dùng code, technology hoặc timeline trong ảnh làm dữ liệu production nếu chưa xác minh.

---

## 19. Roadmap sau MVP

### Phase 2

- Proposal Draft Generator
- Quotation Configurator sử dụng verified pricing rules
- Customer Self-Service Solution Finder
- Sales-to-Ops project workspace
- CRM integration

### Phase 3

- Semantic search / embeddings
- Analytics: most-used packages, conversion, common gaps
- Role-based access
- Versioning service catalog
- Audit trail / approvals

---

## 20. Giá trị chiến lược cho GASCOLAE

Service Package Builder không thay thế Sales. Nó biến catalog dịch vụ thành một **configurable solution system**:

`Service Data → Customer Need → Solution Package → Validation → Sales/Ops Handover`

Giá trị lớn nhất của MVP là chứng minh rằng các Service Assets được chuẩn hóa có thể trở thành dữ liệu vận hành cho một ứng dụng thực tế, thay vì chỉ tồn tại dưới dạng Word/Excel/PowerPoint.

---

## 21. Kết luận

Phạm vi phù hợp nhất cho nhóm 3 sinh viên trong 4 tuần là xây một **MVP full-stack có rule engine và validation**, thay vì một chatbot thuần LLM. Sản phẩm phải demo được một flow có business value rõ ràng: từ nhu cầu khách hàng đến một package có thể kiểm tra và bàn giao cho Operation.
