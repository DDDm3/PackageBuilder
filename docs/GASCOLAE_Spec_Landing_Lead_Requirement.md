# ĐẶC TẢ NGHIỆP VỤ & ÁNH XẠ CƠ SỞ DỮ LIỆU
## CHỨC NĂNG: TIẾP NHẬN YÊU CẦU DỰ ÁN TỪ LANDING PAGE (HYBRID LEAD SUBMISSION)

---

## 1. TỔNG QUAN NGHIỆP VỤ

### 1.1 Mục tiêu chức năng
Cung cấp cổng tiếp nhận thông tin trực tuyến (Public Portal / Landing Form) tại trang chủ GASCOLAE, cho phép khách hàng tiềm năng gửi đề bài khảo sát (UAV, viễn thám, trí tuệ nhân tạo) một cách thuận tiện mà **không cần đăng nhập tài khoản**. 

Hệ thống chuẩn hóa biểu mẫu tiếp nhận trên Landing Page gồm **đúng 9 trường thông tin cốt lõi**:
1. **`serviceID`**: Dịch vụ kỹ thuật số mà khách hàng đang quan tâm hoặc lựa chọn (VD: `S0269` hoặc ID dịch vụ).
2. **`họ và tên`**: Họ tên người đại diện liên hệ phía khách hàng.
3. **`đơn vị/ doanh nghiệp`**: Tên công ty, nông trường, hợp tác xã hoặc tổ chức.
4. **`sđt`**: Số điện thoại liên lạc trực tiếp.
5. **`level`**: Cấp độ kỹ thuật mong muốn (Level 1: Cơ bản, Level 2: Tiêu chuẩn, Level 3: AI nâng cao).
6. **`email`**: Thư điện tử để nhận báo giá và thông tin trao đổi.
7. **`diện tích`**: Quy mô diện tích khu vực cần bay chụp / đo đạc (kèm đơn vị ha/m2).
8. **`địa bàn`**: Vị trí, địa danh khảo sát thực địa (tỉnh, huyện, xã hoặc tọa độ).
9. **`mô tả chung`**: Đoạn văn tự do mô tả bối cảnh, bài toán cụ thể và mục tiêu riêng của dự án.

### 1.2 Vai trò và tác nhân (Actors)
* **Khách hàng tiềm năng (Guest / Potential Client):** Người truy cập website, duyệt danh mục dịch vụ/level, tick chọn các đầu ra mong đợi và nhập mô tả mục tiêu bài toán.
* **Hệ thống (System Core):** Tự động xác minh thông tin, gom gộp dữ liệu khách hàng (tránh trùng lặp), lưu vết đề bài sơ bộ và các đầu ra tương ứng.
* **Nhân viên Sales / Pre-Sales:** Người tiếp nhận trọn vẹn hồ sơ (gồm cả lựa chọn chuẩn hóa lẫn ghi chú tự nhiên) trên giao diện quản trị để tư vấn, tinh chỉnh danh sách đầu ra và chạy thuật toán gợi ý dịch vụ.

---

## 2. QUY TRÌNH NGHIỆP VỤ (BUSINESS WORKFLOW)

### 2.1 Sơ đồ dòng nghiệp vụ

```mermaid
flowchart TD
    Start([Khách truy cập Landing Page]) --> Browse[Duyệt danh mục 12 Dịch vụ cốt lõi]
    
    subgraph UI_Intake["Giao diện Biểu mẫu Tiếp nhận (Hybrid Intake)"]
        Browse --> PickLevel[Xem các Cấp độ Level 1 / 2 / 3<br/>& Tick chọn các Sản phẩm đầu ra Deliverables có sẵn]
        Browse --> FreeText[Nhập Mô tả mục tiêu bài toán<br/>bằng Ngôn ngữ tự nhiên vào ô ghi chú]
        Browse --> ContactInfo[Điền thông tin liên hệ & Quy mô dự án<br/>Họ tên, SĐT, Email, Công ty, Diện tích...]
    end

    PickLevel --> Submit[Khách bấm nút 'Gửi yêu cầu khảo sát']
    FreeText --> Submit
    ContactInfo --> Submit

    Submit --> CheckContact{Có ít nhất Email<br/>hoặc Số điện thoại?}
    CheckContact -- Không --> ErrContact[Báo lỗi: Yêu cầu cung cấp thông tin liên hệ]
    ErrContact --> ContactInfo

    CheckContact -- Có --> CheckCustomer{Khách hàng đã có<br/>trong hệ thống chưa?<br/>(Check Email / SĐT)}

    subgraph Customer_Handling["Xử lý Định danh Khách hàng"]
        CheckCustomer -- Khách mới --> CreateCust[Tạo mới Khách hàng<br/>- Sinh mã: CUST-YYYYMMDD-XXX<br/>- Trạng thái: ACTIVE]
        CheckCustomer -- Khách cũ --> UpdateCust[Cập nhật bổ sung<br/>thông tin công ty/SĐT nếu còn thiếu]
    end

    subgraph Requirement_Handling["Xử lý Đề bài Kỹ thuật"]
        CreateCust --> InitReq[Khởi tạo Đề bài dự án<br/>- Sinh mã: REQ-YYYYMMDD-XXX<br/>- Trạng thái: DRAFT<br/>- Người tạo: LANDING_PAGE_GUEST<br/>- Lưu đoạn văn mô tả mục tiêu tự nhiên]
        UpdateCust --> InitReq

        InitReq --> SaveOutputs[Lưu danh sách Đầu ra mong đợi<br/>- Các Deliverable được tick từ Level<br/>- Các yêu cầu đầu ra gõ thêm tự do<br/>- Mức ưu tiên: Bắt buộc REQUIRED]
    end

    SaveOutputs --> NotifySuccess([Thông báo gửi thành công cho khách])
    SaveOutputs --> Handover[Chuyển hồ sơ sang Sales Dashboard<br/>Hiển thị đầy đủ cả Level/Output đã chọn<br/>lẫn ngữ cảnh tự nhiên để Sales liên hệ]

    subgraph Sales_Action["Thao tác Tinh chỉnh của Sales (Admin Screen)"]
        Handover --> SalesReview[Sales đọc bối cảnh & liên hệ xác minh]
        SalesReview --> SalesRefine[Sales Thêm / Sửa Sản phẩm đầu ra<br/>bằng Select Box thông minh hoặc Nhập Text]
        SalesRefine --> RunRec[Bấm Chạy Động cơ Gợi ý Dịch vụ<br/>Recommendation Engine]
    end
```

### 2.2 Các bước nghiệp vụ chi tiết

#### Bước 1: Trải nghiệm người dùng trên Landing Page
Khách hàng tương tác với 3 khối thông tin:
1. **Duyệt Cấp độ & Sản phẩm bàn giao chuẩn (Service Levels & Deliverables):**
   * Khách xem các dịch vụ phù hợp với bài toán của mình.
   * Với mỗi dịch vụ, khách thấy rõ 3 cấp độ:
     * *Level 1 (Cơ bản):* Khảo sát hiện trạng, ảnh trực giao, độ phân giải tiêu chuẩn.
     * *Level 2 (Tiêu chuẩn):* Giám sát định kỳ, bản đồ số độ nét cao, dữ liệu GIS.
     * *Level 3 (Nâng cao):* Ứng dụng AI phân tích chuyên sâu, dự báo xu hướng, cảnh báo thời gian thực.
   * Mỗi Level hiển thị chi tiết các sản phẩm bàn giao (`Deliverables`). Khách hàng có thể tick chọn trực tiếp các sản phẩm mong muốn nhận được.
2. **Nhập Mục tiêu bằng Ngôn ngữ tự nhiên (Natural Language Objectives):**
   * Khách hàng có ô văn bản tự do (Textarea) để diễn đạt bối cảnh thực tế:  
     *Ví dụ: "Chúng tôi là nông trường cao su 3.000 ha ở Bình Phước, cần khảo sát tình trạng rụng lá mùa mưa và chuẩn bị hồ sơ cấp chứng chỉ carbon, ngân sách khoảng 350 triệu."*
3. **Cung cấp Thông tin liên hệ & Địa bàn khảo sát:**
   * Tên người liên hệ, đơn vị/công ty, số điện thoại, email, vị trí khu vực khảo sát, diện tích ước tính, dữ liệu đã có sẵn.

#### Bước 2: Xác thực tính hợp lệ cơ bản
* Khách bắt buộc phải nhập họ tên.
* Bắt buộc có **ít nhất một kênh liên lạc**: Email hoặc Số điện thoại. Nếu không có, hệ thống yêu cầu bổ sung trước khi tiếp nhận.

#### Bước 3: Định danh & Gom gộp khách hàng (Lead Deduplication)
Hệ thống đối soát trong cơ sở dữ liệu để tối ưu dữ liệu:
* **Khách hàng mới:** Tự động tạo hồ sơ khách hàng mới, sinh mã định danh duy nhất (`CUST-YYYYMMDD-XXX`) với trạng thái hoạt động (`ACTIVE`).
* **Khách hàng cũ (trùng email hoặc SĐT):** Tận dụng lại hồ sơ cũ, tự động bổ sung tên công ty hoặc số điện thoại mới nếu trước đây còn khuyết.

#### Bước 4: Khởi tạo Đề bài kỹ thuật sơ bộ
* Hệ thống tạo hồ sơ yêu cầu dự án mới với mã duy nhất (`REQ-YYYYMMDD-XXX`).
* Tên dự án nếu trống sẽ tự động đặt là: `"Khảo sát nhu cầu - [Tên Công ty / Tên Khách]"`.
* Toàn bộ đoạn văn bản tự do của khách được lưu nguyên vẹn vào `raw_requirement_text` và `objective_raw` để phục vụ làm ngữ cảnh cho Sales và AI ở bước sau.
* Đề bài được gán trạng thái **Bản thảo sơ bộ (`DRAFT`)** và đánh dấu người tạo là `"LANDING_PAGE_GUEST"`.

#### Bước 5: Bóc tách danh mục Sản phẩm đầu ra mong đợi
* Hệ thống lưu trữ toàn bộ các sản phẩm đầu ra vào danh sách nghiệm thu của đề bài:
  * Các sản phẩm chuẩn được tick chọn từ các Level của dịch vụ trên giao diện.
  * Các sản phẩm đặc thù khách tự gõ thêm bằng tay.
* Tất cả được gán mức ưu tiên mặc định là **Bắt buộc (`REQUIRED`)**.

#### Bước 6: Hoàn tất & Bàn giao sang Sales Admin
* Giao diện gửi thông báo xác nhận thành công cho khách hàng.
* Hồ sơ xuất hiện ngay lập tức trên màn hình **Sales Admin Dashboard** ở trạng thái `DRAFT`.

---

## 3. ÁNH XẠ THÔNG TIN TỪ LANDING PAGE SANG CƠ SỞ DỮ LIỆU

### 3.0 Bảng Tổng hợp Ánh xạ 9 Trường Dữ liệu Form Landing Page

| STT | Tên trường trên Landing Page | Tên biến DTO / Request | Bảng đích Database | Cột đích Database | Ý nghĩa nghiệp vụ |
|:---:|:---|:---|:---|:---|:---|
| **1** | **`serviceID`** | `serviceId` / `serviceID` | `customer_requirements` | `service_id` | Mã hoặc ID dịch vụ kỹ thuật số khách chọn từ trang chủ (VD: `S0269`) |
| **2** | **`họ và tên`** | `customerName` | `customers` | `customer_name` | Họ tên người đại diện liên hệ phía khách hàng |
| **3** | **`đơn vị/ doanh nghiệp`** | `companyName` | `customers` | `company_name` | Tên tổ chức, doanh nghiệp, lâm trường, HTX... |
| **4** | **`sđt`** | `contactPhone` | `customers` | `contact_phone` | Số điện thoại liên lạc trực tiếp |
| **5** | **`level`** | `level` | `customer_requirements` | `target_level` | Cấp độ kỹ thuật khách mong muốn: Level 1, Level 2, hoặc Level 3 |
| **6** | **`email`** | `contactEmail` | `customers` | `contact_email` | Email tiếp nhận hồ sơ, báo giá và trao đổi phương án |
| **7** | **`diện tích`** | `areaValue` (kèm `areaUnit`) | `customer_requirements` | `area_value`, `area_unit` | Quy mô diện tích khu vực khảo sát (VD: 3000 ha) |
| **8** | **`địa bàn`** | `locationDescription` | `customer_requirements` | `location_description` | Vị trí thực địa (Xã, huyện, tỉnh hoặc tọa độ sơ bộ) |
| **9** | **`mô tả chung`** | `rawRequirementText` | `customer_requirements` | `raw_requirement_text` | Đoạn văn mô tả bài toán, bối cảnh và mục tiêu riêng của khách |

---

Dữ liệu tiếp nhận từ Landing Page được phân rã và lưu trữ vào **3 bảng nghiệp vụ liên kết**:

```
[ Giao diện Landing Page (9 Trường Chuẩn) ]
          │
          ├── (1) Khối liên hệ: họ tên, đơn vị, sđt, email ────────► Bảng: customers
          ├── (2) Khối bài toán: serviceID, level, diện tích, ────► Bảng: customer_requirements
          │       địa bàn, mô tả chung
          └── (3) Khối sản phẩm đầu ra (từ Level & gõ thêm) ──────► Bảng: requirement_expected_outputs
```

---

### 3.1 Bảng 1: `customers` (Thông tin Khách hàng / Doanh nghiệp)

Bảng quản lý danh bạ khách hàng, phục vụ định danh đối tác và liên lạc kinh doanh.

| Thông tin trên Giao diện Landing Page | Cột Database (`customers`) | Kiểu dữ liệu | Quy tắc nghiệp vụ & Giá trị mặc định |
| :--- | :--- | :--- | :--- |
| *(Hệ thống tự sinh)* | `customer_id` | UUID | Khóa chính tự sinh (UUID v4) |
| *(Hệ thống tự sinh)* | `customer_code` | VARCHAR(50) | Mã duy nhất theo cú pháp: `CUST-YYYYMMDD-XXX` |
| **Họ và tên người liên hệ** | `customer_name` | VARCHAR(255) | Tên người gửi. Nếu bỏ trống: *"Khách hàng mới"* |
| **Tên đơn vị / Doanh nghiệp** | `company_name` | VARCHAR(255) | Tên doanh nghiệp, lâm trường, hợp tác xã... |
| **Lĩnh vực hoạt động** | `industry` | VARCHAR(100) | Ngành nghề hoạt động của đơn vị (Lâm nghiệp, Nông nghiệp...) |
| **Email liên hệ** | `contact_email` | VARCHAR(255) | Khóa tra cứu đối soát khách hàng cũ |
| **Số điện thoại liên hệ** | `contact_phone` | VARCHAR(50) | Khóa tra cứu đối soát khách hàng cũ |
| *(Hệ thống mặc định)* | `status` | VARCHAR(20) | Trạng thái: Mặc định gán `ACTIVE` |
| *(Hệ thống tự ghi nhận)* | `created_at`, `updated_at` | TIMESTAMP | Mốc thời gian tiếp nhận và cập nhật lần cuối |

---

### 3.2 Bảng 2: `customer_requirements` (Nội dung Bài toán / Yêu cầu dự án)

Bảng quản lý bài toán kỹ thuật, tiếp nhận cả thông số đo đạc lẫn mô tả mục tiêu bằng ngôn ngữ tự nhiên.

| Thông tin trên Giao diện Landing Page | Cột Database (`customer_requirements`) | Kiểu dữ liệu | Quy tắc nghiệp vụ & Giá trị mặc định |
| :--- | :--- | :--- | :--- |
| *(Hệ thống tự sinh)* | `requirement_id` | UUID | Khóa chính tự sinh (UUID v4) |
| *(Hệ thống tự sinh)* | `requirement_code` | VARCHAR(50) | Mã duy nhất theo cú pháp: `REQ-YYYYMMDD-XXX` |
| *(Khóa ngoại liên kết)* | `customer_id` | UUID | Khóa ngoại trỏ đến bản ghi tại bảng `customers` |
| **serviceID** | `service_id` | VARCHAR(100) | **Mã hoặc ID dịch vụ khách chọn từ Landing Page (VD: `S0269`)** |
| **level** | `target_level` | INT | **Cấp độ dịch vụ khách mong muốn (Level 1, Level 2, Level 3)** |
| **Tên dự án** | `project_name` | VARCHAR(255) | Nếu trống: `"Khảo sát nhu cầu - [Tên Công ty/Tên Khách]"` |
| **mô tả chung** | `raw_requirement_text` | TEXT | **Lưu toàn bộ đoạn văn bản tự nhiên của khách gửi lên** |
| **Mục tiêu dự án** | `objective_raw` | TEXT | Lưu mục tiêu chính (trích từ mô tả chung hoặc ô mục tiêu) |
| **diện tích** | `area_value` | DECIMAL(14,2)| Quy mô diện tích khu vực khảo sát (VD: 3000) |
| **Đơn vị diện tích** | `area_unit` | VARCHAR(20) | Đơn vị đo lường: Mặc định là `"ha"` (héc-ta) |
| **địa bàn** | `location_description` | TEXT | Tỉnh, huyện, xã hoặc tọa độ địa lý sơ bộ |
| **Tần suất theo dõi mong muốn** | `monitoring_frequency_raw` | VARCHAR(100)| 1 lần, định kỳ tuần, tháng, theo quý... |
| **Dữ liệu / Vật tư sẵn có** | `provided_inputs_raw` | TEXT | Những gì khách đã có sẵn (bản đồ ranh giới, giấy phép...) |
| *(Hệ thống mặc định)* | `status` | VARCHAR(20) | Trạng thái: Mặc định gán `DRAFT` (Bản thảo chờ duyệt) |
| *(Hệ thống mặc định)* | `created_by` | VARCHAR(255) | Nguồn tạo: Gán cứng `"LANDING_PAGE_GUEST"` |
| *(Chờ Sales xử lý)* | `confirmed_by`, `confirmed_at` | VARCHAR, TIMESTAMP | Để trống (Chờ Sales thẩm định và xác nhận) |
| *(Hệ thống tự ghi nhận)* | `created_at`, `updated_at` | TIMESTAMP | Mốc thời gian tạo và cập nhật |

---

### 3.3 Bảng 3: `requirement_expected_outputs` (Sản phẩm đầu ra mong đợi)

Bảng quản lý danh sách các sản phẩm/kết quả nghiệm thu mà khách hàng kỳ vọng nhận được. Nguồn dữ liệu đến từ **hai kênh**: các sản phẩm được tick chọn sẵn theo từng Level của dịch vụ và các sản phẩm do khách gõ thêm.

| Thông tin trên Giao diện Landing Page | Cột Database (`requirement_expected_outputs`) | Kiểu dữ liệu | Quy tắc nghiệp vụ & Nguồn gốc dữ liệu |
| :--- | :--- | :--- | :--- |
| *(Hệ thống tự sinh)* | `requirement_expected_output_id` | UUID | Khóa chính tự sinh (UUID v4) |
| *(Khóa ngoại liên kết)* | `requirement_id` | UUID | Khóa ngoại trỏ đến đề bài tại bảng `customer_requirements` |
| **1. Sản phẩm tick chọn từ các Level dịch vụ** | `raw_expected_output` | VARCHAR(255) | Lấy tên của Deliverable gắn với Level mà khách tick chọn (VD: *"Bản đồ trực giao 3D độ nét 5cm"*, *"Báo cáo dự báo hạn hán AI"*) |
| **2. Sản phẩm đặc thù khách tự gõ thêm** | `raw_expected_output` | VARCHAR(255) | Lưu nguyên văn chữ thô do khách tự gõ vào danh sách mong muốn |
| *(Mã từ điển kỹ thuật chuẩn)* | `data_item_id` | UUID | Ban đầu để `NULL` (Sẽ được Sales hoặc AI ánh xạ sang mã chuẩn sau) |
| *(Mức độ ưu tiên)* | `priority` | VARCHAR(20) | Mặc định gán: `REQUIRED` (Bắt buộc) |
| *(Hệ thống tự ghi nhận)* | `created_at` | TIMESTAMP | Mốc thời gian tạo bản ghi |

---

## 4. CƠ CHẾ SALES TINH CHỈNH & THÊM MỚI ĐẦU RA (SALES ADMIN OUTPUT REFINEMENT)

Sau khi Lead từ Landing Page đổ về hệ thống, nhân viên Sales mở chi tiết đề bài trên màn hình quản trị để rà soát. Tại đây, Sales có toàn quyền **Thêm mới, Chỉnh sửa hoặc Xóa bớt** các sản phẩm đầu ra mong đợi trước khi bấm nút chạy gợi ý giải pháp.

```
                    [ MÀN HÌNH QUẢN TRỊ SALES ADMIN ]
                                   │
      ┌────────────────────────────┴────────────────────────────┐
      ▼                                                         ▼
[ CÁCH 1: CHỌN TỪ SELECT BOX ]               [ CÁCH 2: NHẬP VĂN BẢN TỰ DO (TEXT) ]
• Gõ tìm kiếm từ danh mục chuẩn:             • Dành cho yêu cầu cá biệt của khách hàng
  - Từ điển 91 Data Items chuẩn                mà từ điển công ty chưa từng có.
  - Deliverables của 12 Dịch vụ / 36 Level   • Sales gõ chữ trực tiếp & nhấn Enter.
• Kết quả lưu trữ:                           • Kết quả lưu trữ:
  - raw_expected_output = Tên hiển thị         - raw_expected_output = Chữ khách yêu cầu
  - data_item_id = Mã chuẩn (DI_xxxx)          - data_item_id = NULL
  ──► Khớp 100% với Recommendation             ──► Tránh nghẽn quy trình, lưu trọn vẹn ý muốn
```

### 4.1 Chi tiết 2 phương thức nhập liệu của Sales

#### Phương thức 1: Chọn từ Select Box thông minh (Searchable Dropdown) - Khuyên dùng
* **Bản chất:** Hệ thống đã nạp sẵn danh mục dữ liệu chuẩn hóa gồm **91 loại `data_items`** và toàn bộ sản phẩm bàn giao cam kết (`Deliverables`) của 12 dịch vụ.
* **Cách thao tác:** Sales chỉ cần gõ 2-3 ký tự (ví dụ: *"sinh khối"*, *"albedo"*, *"ortho"*...), Select Box sẽ xổ xuống các gợi ý chuẩn xác để click chọn.
* **Cơ chế lưu dữ liệu:**
  * `raw_expected_output`: Lưu tên tiếng Việt dễ đọc (ví dụ: *"Bản đồ ảnh trực giao độ nét cao"*).
  * `data_item_id`: **Lưu trực tiếp mã khóa ngoại trỏ tới bảng `data_items`** (ví dụ: `DI_0012`).
* **Giá trị cốt lõi:** Khi có `data_item_id`, thuật toán **Recommendation Engine** sẽ so khớp chính xác 100% với các dịch vụ có `service_outputs` tương ứng, giúp đề xuất đúng dịch vụ và đúng cấp độ (Level) ngay tức thì.

#### Phương thức 2: Nhập Text tự do (Creatable Text Input) - Xử lý ngoại lệ
* **Bản chất:** Khách hàng có những bài toán đặc thù, đòi hỏi những đầu ra phi quy chuẩn mà từ điển công ty chưa bao giờ định nghĩa (ví dụ: *"Báo cáo đối chiếu sai số theo Quy chế an toàn số 18 của Tập đoàn X"*).
* **Cách thao tác:** Sales không tìm thấy trong danh mục thả xuống $\rightarrow$ Gõ toàn bộ cụm từ vào ô và bấm **`[ Thêm mới ]`** hoặc phím **`Enter`**.
* **Cơ chế lưu dữ liệu:**
  * `raw_expected_output`: Lưu trọn vẹn văn bản thô do Sales nhập.
  * `data_item_id`: Lưu giá trị `NULL`.
* **Giá trị cốt lõi:** Không bao giờ làm tắc nghẽn quy trình tư vấn của Sales. Mọi nhu cầu của khách đều được ghi nhận đầy đủ vào biên bản dự án.

---

### 4.2 Thiết lập Mức độ Ưu tiên cho từng Đầu ra (`priority`)

Bên cạnh tên sản phẩm, Sales có thể gắn cờ mức độ quan trọng đối với khách hàng:

| Mức độ ưu tiên (`priority`) | Ý nghĩa nghiệp vụ | Tác động đến Thuật toán Gợi ý |
| :--- | :--- | :--- |
| **`REQUIRED`** (Bắt buộc) | Sản phẩm sống còn, thiếu sản phẩm này khách không nghiệm thu hợp đồng. | Trọng số nhân đôi ($w \times 2.0$), dịch vụ nào không đáp ứng được sẽ bị trừ điểm nặng. |
| **`HIGH`** (Rất quan trọng) | Sản phẩm phục vụ báo cáo chính của dự án. | Trọng số cao ($w \times 1.5$), ưu tiên dịch vụ có cấp độ phù hợp. |
| **`NORMAL`** (Bình thường) | Sản phẩm tiêu chuẩn kèm theo gói. | Trọng số tiêu chuẩn ($w \times 1.0$). |
| **`LOW`** (Tùy chọn thêm) | Sản phẩm gia tăng giá trị, có thì tốt, không có cũng không ảnh hưởng hợp đồng. | Trọng số thấp ($w \times 0.5$). |

---

## 5. CÁC QUY TẮC NGHIỆP VỤ BỔ TRỢ (BUSINESS RULES)

1. **Quy tắc Tiếp nhận Song hành (Dual Intake Paradigm):**  
   Giao diện Landing Page không ép buộc khách hàng phải hiểu sâu kỹ thuật, nhưng cũng không để khách hàng "bơ vơ" trước một ô textarea trống. Việc hiển thị sẵn các Cấp độ (Level 1, 2, 3) và Sản phẩm bàn giao (`Deliverables`) giúp khách hàng có cơ sở định hình, đồng thời ô văn bản tự do giúp khách truyền tải trọn vẹn "tiếng nói bài toán" của mình.
2. **Quy tắc Chuẩn hóa Linh hoạt (Flexible Standardization):**  
   Hệ thống không đóng cứng danh mục đầu ra vào một dropdown duy nhất. Khách hàng và Sales luôn có quyền gõ thêm văn bản tự do nếu danh mục chuẩn chưa bao quát hết bài toán thực tế.
3. **Quy tắc Tiếp cận Không rào cản (Zero-friction Access):**  
   Khách vãng lai gửi yêu cầu không cần tài khoản hay mật khẩu. Tính định danh dựa trên tính xác thực của Số điện thoại và Email.
4. **Quy tắc Định mã tự động (Unique Identifiers):**  
   * Mã khách hàng: `CUST-YYYYMMDD-XXX`.
   * Mã yêu cầu dự án: `REQ-YYYYMMDD-XXX`.
5. **Quy tắc Kiểm duyệt Con người (Human-in-the-Loop):**  
   Toàn bộ thông tin từ Landing Page được lưu ở trạng thái sơ bộ `DRAFT`. Nhân viên Sales sẽ đọc đoạn văn tự nhiên của khách để nắm bối cảnh, xem các Level/Deliverables mà khách đã tick chọn, tinh chỉnh lại danh sách đầu ra (bằng Select Box hoặc Text) trước khi bấm nút chạy thuật toán gợi ý giải pháp.

---

## 6. BƯỚC TIẾP THEO TRONG HỆ THỐNG
Sau khi hoàn tất bước tiếp nhận và chuẩn hóa đầu ra, hệ thống sẽ chuyển sang bước:  
👉 **[Đặc Tả Động Cơ Gợi Ý Dịch Vụ (Service Recommendation Engine)](./GASCOLAE_Spec_Service_Recommendation.md)** để tự động đề xuất các dịch vụ và cấp độ phù hợp nhất cho bài toán của khách hàng.

