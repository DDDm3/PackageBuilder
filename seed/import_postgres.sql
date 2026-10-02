-- =====================================================================
-- GASCOLAE Service Package Builder — Import seed Service Master
-- Seed: SV3 V2-final (23/09/2026) · Schema: Database Design V2 (UUID)
-- Chạy bằng psql, tại thư mục chứa các file CSV:
--     psql -d gascolae -f import_postgres.sql
-- Điều kiện: 21 bảng của V2 đã được tạo; extension pgcrypto đã bật.
-- =====================================================================

\set ON_ERROR_STOP on
BEGIN;

SET search_path TO package_builder, public;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------
-- PHẦN 1 — Bảng tạm, cột khớp đúng thứ tự cột trong file CSV
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS stg_data_items, stg_tags, stg_services, stg_service_levels,
    stg_service_inputs, stg_service_outputs, stg_service_deliverables,
    stg_service_deliverable_items, stg_service_tags, stg_service_relations;

CREATE TEMP TABLE stg_data_items (
    data_code TEXT, data_name TEXT, description TEXT,
    classification TEXT, verification_status TEXT, source_ref TEXT);

CREATE TEMP TABLE stg_tags (
    tag_code TEXT, tag_name TEXT, tag_type TEXT, parent_tag_code TEXT,
    description TEXT, source_ref TEXT, active TEXT);

CREATE TEMP TABLE stg_services (
    service_code TEXT, service_name TEXT, service_name_en TEXT, category TEXT,
    sector_code TEXT, subsector_code TEXT, short_description TEXT, target_customer TEXT,
    customer_problems TEXT, use_cases TEXT, technologies TEXT, lifecycle_status TEXT,
    verification_status TEXT, data_classification TEXT, source_ref TEXT, active TEXT);

CREATE TEMP TABLE stg_service_levels (
    service_code TEXT, level_no TEXT, level_name TEXT, level_description TEXT,
    verification_status TEXT, source_ref TEXT);

CREATE TEMP TABLE stg_service_inputs (
    service_code TEXT, data_code TEXT, required TEXT, provided_by TEXT,
    description TEXT, verification_status TEXT, source_ref TEXT);

CREATE TEMP TABLE stg_service_outputs (
    service_code TEXT, data_code TEXT, description TEXT,
    verification_status TEXT, source_ref TEXT);

CREATE TEMP TABLE stg_service_deliverables (
    service_code TEXT, deliverable_code TEXT, deliverable_name TEXT, description TEXT,
    min_level_no TEXT, verification_status TEXT, source_ref TEXT);

CREATE TEMP TABLE stg_service_deliverable_items (
    deliverable_code TEXT, data_code TEXT);

CREATE TEMP TABLE stg_service_tags (service_code TEXT, tag_code TEXT);

CREATE TEMP TABLE stg_service_relations (
    source_service_code TEXT, target_service_code TEXT, relation_type TEXT, via_data_code TEXT,
    is_required TEXT, description TEXT, verification_status TEXT, source_ref TEXT);

-- ---------------------------------------------------------------------
-- PHẦN 2 — Nạp CSV vào bảng tạm
-- File mã hóa UTF-8 có BOM; HEADER true nên dòng tiêu đề được bỏ qua.
-- ---------------------------------------------------------------------
\copy stg_data_items                FROM 'data_items.csv'                WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')
\copy stg_tags                      FROM 'tags.csv'                      WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')
\copy stg_services                  FROM 'services.csv'                  WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')
\copy stg_service_levels            FROM 'service_levels.csv'            WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')
\copy stg_service_inputs            FROM 'service_inputs.csv'            WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')
\copy stg_service_outputs           FROM 'service_outputs.csv'           WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')
\copy stg_service_deliverables      FROM 'service_deliverables.csv'      WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')
\copy stg_service_deliverable_items FROM 'service_deliverable_items.csv' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')
\copy stg_service_tags              FROM 'service_tags.csv'              WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')
\copy stg_service_relations         FROM 'service_relations.csv'         WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')

-- ---------------------------------------------------------------------
-- PHẦN 3 — Chèn sang bảng thật, tra mã nghiệp vụ ra UUID
-- Thứ tự dưới đây bắt buộc: bảng cha trước, bảng con sau.
-- ---------------------------------------------------------------------

-- 3.1 data_items
INSERT INTO data_items (data_code, data_name, description, classification, verification_status, source_ref)
SELECT data_code, data_name, NULLIF(description,''), classification, verification_status, NULLIF(source_ref,'')
FROM stg_data_items;

-- 3.2 tags — nạp trước, chưa điền parent_tag_id
INSERT INTO tags (tag_code, tag_name, tag_type, description, source_ref, active)
SELECT tag_code, tag_name, tag_type, NULLIF(description,''), NULLIF(source_ref,''), active::boolean
FROM stg_tags;

-- 3.3 tags — bước hai: điền tag cha sau khi mọi tag đã tồn tại
UPDATE tags t
SET parent_tag_id = p.tag_id
FROM stg_tags s
JOIN tags p ON p.tag_code = s.parent_tag_code
WHERE t.tag_code = s.tag_code AND NULLIF(s.parent_tag_code,'') IS NOT NULL;

-- 3.4 services
INSERT INTO services (service_code, service_name, service_name_en, category, sector_code, subsector_code,
    short_description, target_customer, customer_problems, use_cases, technologies,
    lifecycle_status, verification_status, data_classification, source_ref, active)
SELECT service_code, service_name, NULLIF(service_name_en,''), category, NULLIF(sector_code,''), NULLIF(subsector_code,''),
    NULLIF(short_description,''), NULLIF(target_customer,''), NULLIF(customer_problems,''), NULLIF(use_cases,''),
    NULLIF(technologies,''), lifecycle_status, verification_status, data_classification, NULLIF(source_ref,''), active::boolean
FROM stg_services;

-- 3.5 service_levels
INSERT INTO service_levels (service_id, level_no, level_name, level_description, verification_status, source_ref)
SELECT sv.service_id, s.level_no::smallint, s.level_name, NULLIF(s.level_description,''),
       s.verification_status, NULLIF(s.source_ref,'')
FROM stg_service_levels s
JOIN services sv ON sv.service_code = s.service_code;

-- 3.6 service_inputs
INSERT INTO service_inputs (service_id, data_item_id, required, provided_by, description, verification_status, source_ref)
SELECT sv.service_id, d.data_item_id, s.required::boolean, s.provided_by,
       NULLIF(s.description,''), s.verification_status, NULLIF(s.source_ref,'')
FROM stg_service_inputs s
JOIN services   sv ON sv.service_code = s.service_code
JOIN data_items d  ON d.data_code     = s.data_code;

-- 3.7 service_outputs
INSERT INTO service_outputs (service_id, data_item_id, description, verification_status, source_ref)
SELECT sv.service_id, d.data_item_id, NULLIF(s.description,''), s.verification_status, NULLIF(s.source_ref,'')
FROM stg_service_outputs s
JOIN services   sv ON sv.service_code = s.service_code
JOIN data_items d  ON d.data_code     = s.data_code;

-- 3.8 service_deliverables
INSERT INTO service_deliverables (service_id, deliverable_code, deliverable_name, description,
    min_level_no, verification_status, source_ref)
SELECT sv.service_id, s.deliverable_code, s.deliverable_name, NULLIF(s.description,''),
       NULLIF(s.min_level_no,'')::smallint, s.verification_status, NULLIF(s.source_ref,'')
FROM stg_service_deliverables s
JOIN services sv ON sv.service_code = s.service_code;

-- 3.9 service_deliverable_items
INSERT INTO service_deliverable_items (service_deliverable_id, data_item_id)
SELECT dl.service_deliverable_id, d.data_item_id
FROM stg_service_deliverable_items s
JOIN service_deliverables dl ON dl.deliverable_code = s.deliverable_code
JOIN data_items           d  ON d.data_code         = s.data_code;

-- 3.10 service_tags
INSERT INTO service_tags (service_id, tag_id)
SELECT sv.service_id, t.tag_id
FROM stg_service_tags s
JOIN services sv ON sv.service_code = s.service_code
JOIN tags     t  ON t.tag_code      = s.tag_code;

-- 3.11 service_relations
INSERT INTO service_relations (source_service_id, target_service_id, relation_type, via_data_item_id,
    is_required, description, verification_status, source_ref)
SELECT a.service_id, b.service_id, s.relation_type, d.data_item_id,
       s.is_required::boolean, NULLIF(s.description,''), s.verification_status, NULLIF(s.source_ref,'')
FROM stg_service_relations s
JOIN services a ON a.service_code = s.source_service_code
JOIN services b ON b.service_code = s.target_service_code
LEFT JOIN data_items d ON d.data_code = NULLIF(s.via_data_code,'');

COMMIT;

-- ---------------------------------------------------------------------
-- PHẦN 4 — Đối chiếu số dòng với bảng trong SV3_D3_Seed_12_Services.xlsx
-- ---------------------------------------------------------------------
SELECT 'data_items' AS bang, COUNT(*) AS so_dong, 91 AS mong_doi FROM data_items
UNION ALL SELECT 'tags',                      COUNT(*), 194 FROM tags
UNION ALL SELECT 'services',                  COUNT(*),  12 FROM services
UNION ALL SELECT 'service_levels',            COUNT(*),  36 FROM service_levels
UNION ALL SELECT 'service_inputs',            COUNT(*),  94 FROM service_inputs
UNION ALL SELECT 'service_outputs',           COUNT(*),  94 FROM service_outputs
UNION ALL SELECT 'service_deliverables',      COUNT(*),  86 FROM service_deliverables
UNION ALL SELECT 'service_deliverable_items', COUNT(*),  94 FROM service_deliverable_items
UNION ALL SELECT 'service_tags',              COUNT(*), 298 FROM service_tags
UNION ALL SELECT 'service_relations',         COUNT(*),   2 FROM service_relations
ORDER BY 1;

-- ---------------------------------------------------------------------
-- PHẦN 5 — Kiểm tra sau khi nạp. Mọi câu dưới đây phải trả về 0 dòng.
-- ---------------------------------------------------------------------

-- 5.1 Mã cha của tag chưa được điền
SELECT s.tag_code, s.parent_tag_code
FROM stg_tags s JOIN tags t ON t.tag_code = s.tag_code
WHERE NULLIF(s.parent_tag_code,'') IS NOT NULL AND t.parent_tag_id IS NULL;

-- 5.2 Dịch vụ không có input bắt buộc nào từ khách hàng
SELECT sv.service_code
FROM services sv
WHERE NOT EXISTS (SELECT 1 FROM service_inputs i
                  WHERE i.service_id = sv.service_id AND i.required AND i.provided_by = 'CUSTOMER');

-- 5.3 Dịch vụ không có deliverable
SELECT sv.service_code FROM services sv
WHERE NOT EXISTS (SELECT 1 FROM service_deliverables d WHERE d.service_id = sv.service_id);

-- 5.4 Dịch vụ không đủ 3 Level
SELECT sv.service_code, COUNT(l.*) AS so_level
FROM services sv LEFT JOIN service_levels l ON l.service_id = sv.service_id
GROUP BY sv.service_code HAVING COUNT(l.*) <> 3;

-- 5.5 Dịch vụ thiếu tag phân loại (OBJECTIVE, INDUSTRY, ENVIRONMENT)
SELECT sv.service_code, t.tag_type
FROM services sv
CROSS JOIN (VALUES ('OBJECTIVE'),('INDUSTRY'),('ENVIRONMENT')) AS t(tag_type)
WHERE NOT EXISTS (
    SELECT 1 FROM service_tags st JOIN tags tg ON tg.tag_id = st.tag_id
    WHERE st.service_id = sv.service_id AND tg.tag_type = t.tag_type);

-- 5.6 Deliverable chưa gắn mã dữ liệu nào
SELECT sv.service_code, d.deliverable_code
FROM service_deliverables d JOIN services sv ON sv.service_id = d.service_id
WHERE NOT EXISTS (SELECT 1 FROM service_deliverable_items i
                  WHERE i.service_deliverable_id = d.service_deliverable_id);

-- 5.7 Quan hệ PROVIDES_INPUT_FOR thiếu mã dữ liệu trung gian
SELECT r.relation_id FROM service_relations r
WHERE r.relation_type = 'PROVIDES_INPUT_FOR' AND r.via_data_item_id IS NULL;

-- ---------------------------------------------------------------------
-- PHẦN 6 — Nạp lại từ đầu (chỉ chạy khi cần làm lại)
-- Xóa theo thứ tự ngược để không vướng khóa ngoại.
-- ---------------------------------------------------------------------
-- BEGIN;
-- DELETE FROM service_relations;
-- DELETE FROM service_tags;
-- DELETE FROM service_deliverable_items;
-- DELETE FROM service_deliverables;
-- DELETE FROM service_outputs;
-- DELETE FROM service_inputs;
-- DELETE FROM service_levels;
-- UPDATE tags SET parent_tag_id = NULL;
-- DELETE FROM tags;
-- DELETE FROM services;
-- DELETE FROM data_items;
-- COMMIT;
