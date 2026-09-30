# GASCOLAE Service Package Builder — Database Design V2 (UUID)

## 1. Quy ước chung

- Database: PostgreSQL
- Primary Key mặc định: `UUID DEFAULT gen_random_uuid()`
- Foreign Key: `UUID`
- Timestamp: `TIMESTAMPTZ`
- Các mã nghiệp vụ như `service_code`, `customer_code`, `requirement_code`, `package_code`, `data_code`, `tag_code` phải là `UNIQUE`
- Các bảng junction thuần dùng composite PK nếu phù hợp
- Các dữ liệu Service Master quan trọng nên có `source_ref`
- Dữ liệu chưa xác minh dùng `NEED_VERIFY`
- Không dùng các cột `x_` trong schema chính thức

---

# 2. Danh sách bảng

## User / Access
1. `users`

## Customer / Requirement
2. `customers`
3. `customer_requirements`
4. `requirement_expected_outputs`
5. `requirement_tags`

## Service Master
6. `services`
7. `data_items`
8. `service_inputs`
9. `service_outputs`
10. `service_deliverables`
11. `service_deliverable_items`
12. `tags`
13. `service_tags`
14. `service_relations`
15. `service_levels`

## Recommendation
16. `service_recommendations`

## Package
17. `solution_packages`
18. `package_services`
19. `package_open_questions`

## Validation
20. `validation_runs`
21. `validation_results`

> `service_levels` là bảng mở rộng theo dữ liệu thực tế. Nếu Level không được duyệt cho MVP, có thể bỏ bảng 15.

---

# 3. users

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| user_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| email | VARCHAR(255) | | NOT NULL | Yes | | |
| full_name | VARCHAR(255) | | NOT NULL | No | | non-empty |
| role | VARCHAR(30) | | NOT NULL | No | | `SALES_PRE_SALES`, `OPERATION`, `ADMIN` |
| active | BOOLEAN | | NOT NULL | No | TRUE | |
| created_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |
| updated_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |

---

# 4. customers

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| customer_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| customer_code | VARCHAR(50) | | NOT NULL | Yes | | |
| customer_name | VARCHAR(255) | | NOT NULL | No | | non-empty |
| company_name | VARCHAR(255) | | NULL | No | | |
| industry | VARCHAR(100) | | NULL | No | | raw/display value |
| contact_email | VARCHAR(255) | | NULL | No | | |
| contact_phone | VARCHAR(50) | | NULL | No | | |
| status | VARCHAR(20) | | NOT NULL | No | `ACTIVE` | `ACTIVE`, `INACTIVE` |
| created_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |
| updated_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |

---

# 5. customer_requirements

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| requirement_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| requirement_code | VARCHAR(50) | | NOT NULL | Yes | | |
| customer_id | UUID | FK → customers.customer_id | NOT NULL | No | | |
| project_name | VARCHAR(255) | | NULL | No | | |
| raw_requirement_text | TEXT | | NULL | No | | original user/Sales input |
| industry_raw | VARCHAR(100) | | NULL | No | | optional raw value |
| environment_raw | VARCHAR(100) | | NULL | No | | optional raw value |
| area_value | NUMERIC(14,2) | | NULL | No | | `>= 0` |
| area_unit | VARCHAR(20) | | NULL | No | | |
| objective_raw | TEXT | | NULL | No | | optional original objective text |
| extraction_method | VARCHAR(20) | | NOT NULL | No | `MANUAL` | `MANUAL`, `AI` |
| extraction_confidence | NUMERIC(5,4) | | NULL | No | | `0 <= value <= 1` |
| status | VARCHAR(20) | | NOT NULL | No | `DRAFT` | `DRAFT`, `CONFIRMED`, `ARCHIVED` |
| created_by | UUID | FK → users.user_id | NULL | No | | |
| confirmed_by | UUID | FK → users.user_id | NULL | No | | |
| confirmed_at | TIMESTAMPTZ | | NULL | No | | |
| created_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |
| updated_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |

---

# 6. requirement_expected_outputs

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| requirement_expected_output_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| requirement_id | UUID | FK → customer_requirements.requirement_id | NOT NULL | No | | |
| data_item_id | UUID | FK → data_items.data_item_id | NULL | No | | normalized expected output |
| raw_expected_output | VARCHAR(255) | | NULL | No | | original extracted/manual text |
| priority | VARCHAR(20) | | NOT NULL | No | `NORMAL` | `LOW`, `NORMAL`, `HIGH`, `REQUIRED` |
| created_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |

Constraints:
- At least one of `data_item_id` or `raw_expected_output` must be present.
- Recommended unique: `UNIQUE(requirement_id, data_item_id)` where `data_item_id IS NOT NULL`.

---

# 7. requirement_tags

Dùng để chuẩn hóa Objective / Industry / Environment / Topic của Requirement.

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| requirement_id | UUID | PK, FK → customer_requirements.requirement_id | NOT NULL | Composite | | |
| tag_id | UUID | PK, FK → tags.tag_id | NOT NULL | Composite | | |

Primary Key:
- `PRIMARY KEY(requirement_id, tag_id)`

---

# 8. services

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| service_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| service_code | VARCHAR(50) | | NOT NULL | Yes | | business code, ví dụ `S0286` |
| service_name | VARCHAR(255) | | NOT NULL | No | | |
| service_name_en | VARCHAR(255) | | NULL | No | | |
| category | VARCHAR(100) | | NOT NULL | No | | |
| sector_code | VARCHAR(100) | | NULL | No | | |
| subsector_code | VARCHAR(100) | | NULL | No | | |
| short_description | TEXT | | NULL | No | | |
| target_customer | TEXT | | NULL | No | | |
| customer_problems | TEXT | | NULL | No | | tách khỏi use_cases |
| use_cases | TEXT | | NULL | No | | phục vụ Use Case Match |
| technologies | TEXT | | NULL | No | | MVP có thể lưu text |
| lifecycle_status | VARCHAR(30) | | NOT NULL | No | `DRAFT` | `DRAFT`, `APPROVED`, `ARCHIVED` |
| verification_status | VARCHAR(30) | | NOT NULL | No | `NEED_VERIFY` | `VERIFIED`, `NEED_VERIFY`, `REJECTED` |
| data_classification | VARCHAR(30) | | NOT NULL | No | `INTERNAL` | `PUBLIC`, `INTERNAL`, `CONFIDENTIAL`, `DEMO` |
| source_ref | TEXT | | NULL | No | | |
| active | BOOLEAN | | NOT NULL | No | TRUE | |
| created_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |
| updated_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |

---

# 9. data_items

Dùng làm taxonomy chuẩn cho Input / Output / Deliverable / Expected Output.

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| data_item_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| data_code | VARCHAR(100) | | NOT NULL | Yes | | |
| data_name | VARCHAR(255) | | NOT NULL | No | | |
| description | TEXT | | NULL | No | | |
| classification | VARCHAR(30) | | NOT NULL | No | `INTERNAL` | `PUBLIC`, `INTERNAL`, `CONFIDENTIAL`, `DEMO` |
| verification_status | VARCHAR(30) | | NOT NULL | No | `NEED_VERIFY` | `VERIFIED`, `NEED_VERIFY` |
| source_ref | TEXT | | NULL | No | | |
| created_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |

---

# 10. service_inputs

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| service_input_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| service_id | UUID | FK → services.service_id | NOT NULL | No | | |
| data_item_id | UUID | FK → data_items.data_item_id | NOT NULL | No | | |
| required | BOOLEAN | | NOT NULL | No | TRUE | |
| provided_by | VARCHAR(30) | | NOT NULL | No | `CUSTOMER` | `CUSTOMER`, `GASCOLAE`, `AUTHORITY`, `THIRD_PARTY` |
| description | TEXT | | NULL | No | | |
| verification_status | VARCHAR(30) | | NOT NULL | No | `NEED_VERIFY` | `VERIFIED`, `NEED_VERIFY` |
| source_ref | TEXT | | NULL | No | | |

Constraint:
- `UNIQUE(service_id, data_item_id)`

---

# 11. service_outputs

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| service_output_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| service_id | UUID | FK → services.service_id | NOT NULL | No | | |
| data_item_id | UUID | FK → data_items.data_item_id | NOT NULL | No | | |
| description | TEXT | | NULL | No | | |
| verification_status | VARCHAR(30) | | NOT NULL | No | `NEED_VERIFY` | `VERIFIED`, `NEED_VERIFY` |
| source_ref | TEXT | | NULL | No | | |

Constraint:
- `UNIQUE(service_id, data_item_id)`

---

# 12. service_deliverables

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| service_deliverable_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| service_id | UUID | FK → services.service_id | NOT NULL | No | | |
| deliverable_code | VARCHAR(100) | | NULL | No | | |
| deliverable_name | VARCHAR(255) | | NOT NULL | No | | |
| description | TEXT | | NULL | No | | |
| min_level_no | SMALLINT | | NULL | No | | `> 0` nếu dùng Level |
| verification_status | VARCHAR(30) | | NOT NULL | No | `NEED_VERIFY` | `VERIFIED`, `NEED_VERIFY` |
| source_ref | TEXT | | NULL | No | | |

Constraint:
- `UNIQUE(service_id, deliverable_name)`

---

# 13. service_deliverable_items

Chuẩn hóa Deliverable ↔ Data Item để phát hiện Duplicate Deliverable.

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| service_deliverable_id | UUID | PK, FK → service_deliverables.service_deliverable_id | NOT NULL | Composite | | |
| data_item_id | UUID | PK, FK → data_items.data_item_id | NOT NULL | Composite | | |

Primary Key:
- `PRIMARY KEY(service_deliverable_id, data_item_id)`

---

# 14. tags

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| tag_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| tag_code | VARCHAR(100) | | NOT NULL | Yes | | |
| tag_name | VARCHAR(150) | | NOT NULL | Yes | | |
| tag_type | VARCHAR(30) | | NOT NULL | No | | `OBJECTIVE`, `INDUSTRY`, `ENVIRONMENT`, `TOPIC`, `KEYWORD` |
| parent_tag_id | UUID | FK → tags.tag_id | NULL | No | | self-reference |
| description | TEXT | | NULL | No | | |
| source_ref | TEXT | | NULL | No | | |
| active | BOOLEAN | | NOT NULL | No | TRUE | |

Constraint:
- `CHECK(parent_tag_id IS NULL OR parent_tag_id <> tag_id)`

---

# 15. service_tags

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| service_id | UUID | PK, FK → services.service_id | NOT NULL | Composite | | |
| tag_id | UUID | PK, FK → tags.tag_id | NOT NULL | Composite | | |

Primary Key:
- `PRIMARY KEY(service_id, tag_id)`

---

# 16. service_relations

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| relation_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| source_service_id | UUID | FK → services.service_id | NOT NULL | No | | |
| target_service_id | UUID | FK → services.service_id | NOT NULL | No | | |
| relation_type | VARCHAR(40) | | NOT NULL | No | | `PROVIDES_INPUT_FOR`, `RECOMMENDED_WITH`, `ALTERNATIVE_TO`, `OVERLAPS_WITH` |
| via_data_item_id | UUID | FK → data_items.data_item_id | NULL | No | | data item liên quan |
| is_required | BOOLEAN | | NOT NULL | No | FALSE | |
| description | TEXT | | NULL | No | | |
| verification_status | VARCHAR(30) | | NOT NULL | No | `NEED_VERIFY` | `VERIFIED`, `NEED_VERIFY` |
| source_ref | TEXT | | NULL | No | | |
| created_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |

Constraints:
- `CHECK(source_service_id <> target_service_id)`
- `UNIQUE(source_service_id, target_service_id, relation_type, via_data_item_id)`

---

# 17. service_levels

Bảng này chỉ dùng nếu Level 1/2/3 là business scope chính thức.

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| service_level_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| service_id | UUID | FK → services.service_id | NOT NULL | No | | |
| level_no | SMALLINT | | NOT NULL | No | | `> 0` |
| level_name | VARCHAR(255) | | NOT NULL | No | | |
| level_description | TEXT | | NULL | No | | |
| verification_status | VARCHAR(30) | | NOT NULL | No | `NEED_VERIFY` | `VERIFIED`, `NEED_VERIFY` |
| source_ref | TEXT | | NULL | No | | |

Constraint:
- `UNIQUE(service_id, level_no)`

---

# 18. service_recommendations

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| recommendation_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| requirement_id | UUID | FK → customer_requirements.requirement_id | NOT NULL | No | | |
| service_id | UUID | FK → services.service_id | NOT NULL | No | | |
| match_score | NUMERIC(5,2) | | NOT NULL | No | 0 | `0..100` |
| objective_score | NUMERIC(5,2) | | NOT NULL | No | 0 | `0..100` |
| use_case_score | NUMERIC(5,2) | | NOT NULL | No | 0 | `0..100` |
| industry_score | NUMERIC(5,2) | | NOT NULL | No | 0 | `0..100` |
| output_score | NUMERIC(5,2) | | NOT NULL | No | 0 | `0..100` |
| tag_score | NUMERIC(5,2) | | NOT NULL | No | 0 | `0..100` |
| recommendation_reason | TEXT | | NOT NULL | No | | |
| rank_order | INTEGER | | NULL | No | | `> 0` |
| accepted | BOOLEAN | | NULL | No | | |
| created_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |

Constraint MVP:
- `UNIQUE(requirement_id, service_id)`

> Nếu cần lưu lịch sử nhiều lần recommendation, nên thêm `recommendation_runs` ở phiên bản sau.

---

# 19. solution_packages

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| package_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| package_code | VARCHAR(50) | | NOT NULL | Yes | | |
| requirement_id | UUID | FK → customer_requirements.requirement_id | NOT NULL | No | | |
| package_name | VARCHAR(255) | | NOT NULL | No | | |
| package_status | VARCHAR(30) | | NOT NULL | No | `DRAFT` | `DRAFT`, `READY`, `CONDITIONALLY_READY`, `GAP`, `ARCHIVED` |
| current_version | INTEGER | | NOT NULL | No | 1 | `> 0` |
| created_by | UUID | FK → users.user_id | NULL | No | | |
| created_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |
| updated_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |

---

# 20. package_services

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| package_service_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| package_id | UUID | FK → solution_packages.package_id | NOT NULL | No | | |
| service_id | UUID | FK → services.service_id | NOT NULL | No | | |
| recommendation_id | UUID | FK → service_recommendations.recommendation_id | NULL | No | | |
| selected_level_id | UUID | FK → service_levels.service_level_id | NULL | No | | nếu Level được dùng |
| service_role | VARCHAR(20) | | NOT NULL | No | `CORE` | `CORE`, `SUPPORTING`, `OPTIONAL` |
| phase_no | INTEGER | | NOT NULL | No | 1 | `> 0` |
| sort_order | INTEGER | | NOT NULL | No | 1 | `> 0` |
| is_manual_add | BOOLEAN | | NOT NULL | No | FALSE | |
| notes | TEXT | | NULL | No | | |
| created_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |

Constraint:
- `UNIQUE(package_id, service_id)`

---

# 21. package_open_questions

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| question_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| package_id | UUID | FK → solution_packages.package_id | NOT NULL | No | | |
| question_text | TEXT | | NOT NULL | No | | |
| owner_role | VARCHAR(30) | | NULL | No | | `SALES_PRE_SALES`, `OPERATION`, `CUSTOMER`, `ADMIN` |
| status | VARCHAR(20) | | NOT NULL | No | `OPEN` | `OPEN`, `ANSWERED`, `RESOLVED` |
| answer_text | TEXT | | NULL | No | | |
| created_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |
| resolved_at | TIMESTAMPTZ | | NULL | No | | |

---

# 22. validation_runs

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| validation_run_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| package_id | UUID | FK → solution_packages.package_id | NOT NULL | No | | |
| run_number | INTEGER | | NOT NULL | No | | `> 0` |
| validation_status | VARCHAR(20) | | NOT NULL | No | `RUNNING` | `RUNNING`, `COMPLETED`, `FAILED` |
| calculated_package_status | VARCHAR(30) | | NULL | No | | `READY`, `CONDITIONALLY_READY`, `GAP` |
| total_errors | INTEGER | | NOT NULL | No | 0 | `>= 0` |
| total_warnings | INTEGER | | NOT NULL | No | 0 | `>= 0` |
| triggered_by | UUID | FK → users.user_id | NULL | No | | |
| started_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |
| completed_at | TIMESTAMPTZ | | NULL | No | | |

Constraint:
- `UNIQUE(package_id, run_number)`

---

# 23. validation_results

`package_id` được bỏ khỏi bảng này để tránh lặp dữ liệu; package truy ra qua `validation_run_id`.

| Field | Type | Key | Null | Unique | Default | Check / Business Rule |
|---|---|---|---|---|---|---|
| validation_result_id | UUID | PK | NOT NULL | Yes | `gen_random_uuid()` | |
| validation_run_id | UUID | FK → validation_runs.validation_run_id | NOT NULL | No | | |
| rule_code | VARCHAR(50) | | NOT NULL | No | | |
| result_type | VARCHAR(50) | | NOT NULL | No | | xem enum dưới |
| severity | VARCHAR(20) | | NOT NULL | No | `WARNING` | `INFO`, `WARNING`, `ERROR` |
| service_id | UUID | FK → services.service_id | NULL | No | | |
| related_service_id | UUID | FK → services.service_id | NULL | No | | |
| data_item_id | UUID | FK → data_items.data_item_id | NULL | No | | |
| message | TEXT | | NOT NULL | No | | |
| resolved | BOOLEAN | | NOT NULL | No | FALSE | |
| resolved_at | TIMESTAMPTZ | | NULL | No | | |
| created_at | TIMESTAMPTZ | | NOT NULL | No | CURRENT_TIMESTAMP | |

`result_type`:
- `MISSING_REQUIRED_INPUT`
- `MISSING_DEPENDENCY`
- `DUPLICATE_DELIVERABLE`
- `NEED_MANUAL_VERIFICATION`
- `OUTSIDE_REQUIREMENT`

---

# 24. Quan hệ ERD chính

```text
USERS
  ├──< CUSTOMER_REQUIREMENTS.created_by
  ├──< CUSTOMER_REQUIREMENTS.confirmed_by
  ├──< SOLUTION_PACKAGES.created_by
  └──< VALIDATION_RUNS.triggered_by

CUSTOMERS
  └──< CUSTOMER_REQUIREMENTS
        ├──< REQUIREMENT_EXPECTED_OUTPUTS >── DATA_ITEMS
        ├──< REQUIREMENT_TAGS >── TAGS
        ├──< SERVICE_RECOMMENDATIONS >── SERVICES
        └──< SOLUTION_PACKAGES
              ├──< PACKAGE_SERVICES >── SERVICES
              │                          └── SERVICE_LEVELS
              ├──< PACKAGE_OPEN_QUESTIONS
              └──< VALIDATION_RUNS
                    └──< VALIDATION_RESULTS

SERVICES
  ├──< SERVICE_INPUTS >── DATA_ITEMS
  ├──< SERVICE_OUTPUTS >── DATA_ITEMS
  ├──< SERVICE_DELIVERABLES
  │      └──< SERVICE_DELIVERABLE_ITEMS >── DATA_ITEMS
  ├──< SERVICE_TAGS >── TAGS
  ├──< SERVICE_LEVELS
  └──< SERVICE_RELATIONS >── SERVICES
                     └──────> DATA_ITEMS (via_data_item_id)
```

---

# 25. Enum / Check chính

## User Role
- `SALES_PRE_SALES`
- `OPERATION`
- `ADMIN`

## Verification Status
- `VERIFIED`
- `NEED_VERIFY`
- `REJECTED` (services)

## Lifecycle Status
- `DRAFT`
- `APPROVED`
- `ARCHIVED`

## Data Classification
- `PUBLIC`
- `INTERNAL`
- `CONFIDENTIAL`
- `DEMO`

## Requirement Status
- `DRAFT`
- `CONFIRMED`
- `ARCHIVED`

## Extraction Method
- `MANUAL`
- `AI`

## Tag Type
- `OBJECTIVE`
- `INDUSTRY`
- `ENVIRONMENT`
- `TOPIC`
- `KEYWORD`

## Input Provided By
- `CUSTOMER`
- `GASCOLAE`
- `AUTHORITY`
- `THIRD_PARTY`

## Service Relation Type
- `PROVIDES_INPUT_FOR`
- `RECOMMENDED_WITH`
- `ALTERNATIVE_TO`
- `OVERLAPS_WITH`

## Service Role
- `CORE`
- `SUPPORTING`
- `OPTIONAL`

## Package Status
- `DRAFT`
- `READY`
- `CONDITIONALLY_READY`
- `GAP`
- `ARCHIVED`

## Validation Status
- `RUNNING`
- `COMPLETED`
- `FAILED`

## Validation Result Type
- `MISSING_REQUIRED_INPUT`
- `MISSING_DEPENDENCY`
- `DUPLICATE_DELIVERABLE`
- `NEED_MANUAL_VERIFICATION`
- `OUTSIDE_REQUIREMENT`

## Severity
- `INFO`
- `WARNING`
- `ERROR`

---

# 26. ON DELETE đề xuất

| Parent → Child | ON DELETE |
|---|---|
| users → business records | SET NULL |
| customers → customer_requirements | RESTRICT |
| customer_requirements → requirement_expected_outputs | CASCADE |
| customer_requirements → requirement_tags | CASCADE |
| customer_requirements → service_recommendations | CASCADE |
| customer_requirements → solution_packages | RESTRICT |
| services → service_inputs | CASCADE |
| services → service_outputs | CASCADE |
| services → service_deliverables | CASCADE |
| services → service_tags | CASCADE |
| services → service_levels | CASCADE |
| services → service_recommendations | RESTRICT |
| solution_packages → package_services | CASCADE |
| solution_packages → package_open_questions | CASCADE |
| solution_packages → validation_runs | CASCADE |
| validation_runs → validation_results | CASCADE |
| service_deliverables → service_deliverable_items | CASCADE |
| tags → requirement_tags | RESTRICT |
| tags → service_tags | CASCADE |

---

# 27. Ghi chú thiết kế cuối

1. `UUID` là technical PK/FK.
2. Business code luôn tách khỏi UUID technical PK.
3. `data_items` là taxonomy trung tâm cho input/output/deliverable/expected output.
4. `tags` chuẩn hóa Objective / Industry / Environment / Topic / Keyword.
5. `requirement_tags` hỗ trợ một Requirement có nhiều objective, industry, environment.
6. `service_deliverable_items` cho phép validator phát hiện Duplicate Deliverable theo mã dữ liệu thay vì so text.
7. `service_inputs.provided_by` giúp `MISSING_REQUIRED_INPUT` không báo sai.
8. `service_relations.via_data_item_id` thay thế thiết kế tạm `x_via_data_code`.
9. `services.customer_problems` và `services.use_cases` được tách để phục vụ Recommendation Engine.
10. `validation_results` không lưu `package_id` vì đã truy được qua `validation_runs`.
11. `service_levels` chỉ giữ nếu Level là business scope chính thức.
12. Nếu sau MVP cần lưu lịch sử Recommendation nhiều lần, thêm bảng `recommendation_runs`.