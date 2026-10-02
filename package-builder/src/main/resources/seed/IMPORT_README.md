# Hướng dẫn import seed vào PostgreSQL

**Seed:** SV3 V2-final, 23/09/2026 · **Schema:** GASCOLAE Database Design V2 (UUID) · **Người nhận:** SV1

## 1. Trước khi chạy

1. Đã tạo 21 bảng theo bản V2. Seed này nạp 10 bảng thuộc nhóm Service Master; các bảng còn lại là dữ liệu phát sinh khi chạy app.
2. Đã bật extension `pgcrypto` để `gen_random_uuid()` hoạt động. Câu lệnh bật nằm sẵn trong file SQL.
3. File `import_postgres.sql` phải nằm **cùng thư mục** với 10 file CSV, vì lệnh `\copy` dùng đường dẫn tương đối.

## 2. Cách chạy

```bash
cd seed
psql -d <tên_database> -f import_postgres.sql
```

Nếu dùng DBeaver hoặc pgAdmin thì `\copy` không chạy được, vì đó là lệnh của psql. Khi đó có hai cách:
- Dùng chức năng Import Data của công cụ để nạp từng CSV vào bảng `stg_*`, rồi chạy tay Phần 3 trở đi.
- Hoặc đổi `\copy` thành `COPY ... FROM '/đường/dẫn/tuyệt/đối/file.csv'`. Cách này yêu cầu file nằm trên máy chủ database và tài khoản có quyền đọc file.

## 3. File SQL gồm những gì

| Phần | Nội dung |
|---|---|
| 1 | Tạo 10 bảng tạm `stg_*`, cột khớp đúng thứ tự cột trong CSV |
| 2 | 10 lệnh `\copy` nạp CSV vào bảng tạm |
| 3 | Chèn sang bảng thật, JOIN để tra mã nghiệp vụ ra UUID; riêng `parent_tag_id` được cập nhật ở bước 3.3 |
| 4 | Đếm số dòng từng bảng kèm số mong đợi |
| 5 | 7 câu kiểm tra sau khi nạp, mỗi câu phải trả về 0 dòng |
| 6 | Câu lệnh nạp lại từ đầu, đang để dạng ghi chú |

Toàn bộ phần 1 đến 3 nằm trong một transaction. Có lỗi thì không dữ liệu nào được ghi.

## 4. Số dòng mong đợi

| Bảng | Số dòng |
|---|---|
| data_items | 91 |
| tags | 194 |
| services | 12 |
| service_levels | 36 |
| service_inputs | 94 |
| service_outputs | 94 |
| service_deliverables | 86 |
| service_deliverable_items | 94 |
| service_tags | 298 |
| service_relations | 2 |

## 5. Vì sao seed dùng mã nghiệp vụ chứ không dùng UUID

UUID do database sinh khi chèn, SV3 không biết trước. Nên seed mang `service_code`, `data_code`, `tag_code`, `deliverable_code`, và bước chèn sẽ JOIN để tra ra UUID tương ứng. Nhờ vậy seed nạp lại được nhiều lần mà không phụ thuộc UUID của lần nạp trước.

## 6. Ba lỗi hay gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| `violates foreign key constraint` | Nạp sai thứ tự, bảng cha chưa có dữ liệu | Chạy đúng thứ tự trong Phần 3; đừng chạy lẻ từng câu |
| Tiếng Việt hiển thị sai | Database hoặc client không dùng UTF-8 | Kiểm tra `SHOW server_encoding;` phải là UTF8. File CSV là UTF-8 có BOM, `HEADER true` bỏ qua dòng tiêu đề nên BOM không gây lỗi |
| `parent_tag_id` rỗng hết | Chỉ chạy bước chèn tag mà bỏ bước 3.3 | Chạy lại câu UPDATE ở bước 3.3 |

## 7. Sau khi nạp

1. Đối chiếu Phần 4 với bảng số dòng ở trên.
2. Chạy Phần 5, cả 7 câu phải trả về 0 dòng.
3. Báo lại SV3 nếu có chênh lệch, kèm tên bảng và thông báo lỗi.

## 8. Ghi chú về `service_relations`

Hiện có 2 quan hệ, đều truy được về Service Asset:

| Từ | Đến | Loại | Bắt buộc | Căn cứ |
|---|---|---|---|---|
| S0285-S0287 | S0286 | `RECOMMENDED_WITH` | Không | S0285-S0287 file 03 D003 và rule R009 của S0286 |
| S0272 | S0274 | `PROVIDES_INPUT_FOR` | Không | Danh mục nguồn phát thải là đầu ra của S0272 và đầu vào của S0274 |

Phần quan hệ còn lại thuộc D5, Chặng 3, mỗi dòng đều phải có lý do, nguồn và người duyệt trước khi bổ sung.
