# ĐẶC TẢ NGHIỆP VỤ & ÁNH XẠ CƠ SỞ DỮ LIỆU
## CHỨC NĂNG: BÁO CÁO GIẢI PHÁP TỔNG HỢP & BÀN GIAO VẬN HÀNH (SOLUTION SUMMARY & OPERATION HANDOVER)

---

## 1. TỔNG QUAN NGHIỆP VỤ

### 1.1 Mục tiêu chức năng
Chức năng **Báo cáo giải pháp tổng hợp & Bàn giao vận hành** là chặng đích khép lại toàn bộ chu trình tư vấn giải pháp kỹ thuật của hệ thống GASCOLAE Package Builder. 

Sau khi gói giải pháp đã được lắp ghép và vượt qua kỳ kiểm định kỹ thuật ([Validation Engine](./GASCOLAE_Spec_Validation_Engine.md)), hệ thống tự động tổng hợp toàn bộ dữ liệu phân tán trong cơ sở dữ liệu để tạo ra **hai bộ tài liệu nghiệp vụ chuẩn mực**:
1. **Bản Đề xuất Giải pháp Thương mại (Commercial Proposal / Solution Proposal):**  
   Dành cho **Khách hàng** phê duyệt và ký hợp đồng. Trình bày bằng ngôn ngữ kinh doanh chuyên nghiệp: bối cảnh dự án, lộ trình các giai đoạn (Phase 1, 2, 3), danh sách sản phẩm cam kết nhận được (`Deliverables`) và dự toán chi phí.
2. **Hồ sơ Kỹ thuật Bàn giao Hiện trường (Technical Handover Dossier):**  
   Dành cho **Đội Kỹ sư Vận hành (Operation Engineers / UAV Pilots)** triển khai thực địa. Trình bày bằng ngôn ngữ kỹ thuật chuẩn xác: bảng tổng hợp vật tư dữ liệu đầu vào cần chuẩn bị (`Required Inputs`), điều kiện ranh giới bay, các yêu cầu độ chính xác tọa độ/độ phân giải và danh sách các câu hỏi vướng mắc đã được giải tỏa (`Open Questions`).

### 1.2 Vai trò và tác nhân (Actors)
* **Nhân viên Sales / Pre-Sales:** Người kích hoạt tổng hợp báo cáo, kiểm tra nội dung và ký số/xuất file gửi khách hàng.
* **Khách hàng (Client):** Nhận bản Proposal, xem xét phạm vi công việc, cam kết bàn giao và tiến hành ký kết hợp đồng.
* **Đội Kỹ sư Vận hành (Operation Team):** Tiếp nhận hồ sơ kỹ thuật, kiểm tra bảng tiêu chí sẵn sàng (Readiness Checklist), phân công phi công và thiết bị bay ra hiện trường.
* **AI Layer (LLM API - Gemini):** Trợ lý ngôn ngữ tự động viết đoạn văn Thuyết minh giải pháp (Executive Summary / Solution Narrative) thật trang trọng, súc tích dựa trên dữ liệu thật của gói.

---

## 2. QUY TRÌNH NGHIỆP VỤ (BUSINESS WORKFLOW)

### 2.1 Sơ đồ dòng nghiệp vụ

```mermaid
flowchart TD
    Start([Gói đạt trạng thái READY hoặc CONDITIONALLY_READY]) --> Step1[Sales bấm 'Xem Báo Cáo Giải Pháp & Bàn Giao']
    
    subgraph Data_Aggregation["1. Động cơ Tổng hợp Dữ liệu Đa tầng (Aggregation Engine)"]
        Step1 --> AggInputs[Tổng hợp Dữ liệu Đầu vào (Aggregated Inputs)<br/>- Nhóm 'Khách hàng tự cấp'<br/>- Nhóm 'GASCOLAE tự thu thập/sinh ra']
        AggInputs --> AggDeliverables[Tổng hợp Sản phẩm Cam kết (Aggregated Deliverables)<br/>Gom toàn bộ Deliverables theo từng Phase & Level]
        AggDeliverables --> GetValidationCert[Trích xuất Chứng chỉ Kiểm định Hợp lệ<br/>Validation Run kết quả 0 Errors]
        GetValidationCert --> GetResolvedQuestions[Trích xuất Biên bản Giải tỏa Vướng mắc<br/>100% Open Questions đã RESOLVED]
    end

    subgraph AI_Narrative["2. Trau chuốt Thuyết minh Giải pháp (AI Layer)"]
        AggDeliverables --> AIGen[Gọi Gemini API: Soạn thảo Executive Summary<br/>Dựa trên Mục tiêu đề bài + Danh sách dịch vụ trong gói]
        AIGen --> NarrativeReview[Hiển thị Đoạn văn Thuyết minh trên UI<br/>Sales có quyền đọc và chỉnh sửa câu từ]
    end

    subgraph Readiness_Gate["3. Kiểm tra Tiêu chuẩn Sẵn sàng (Readiness Checklist)"]
        NarrativeReview --> CheckReadiness{Đủ điều kiện sẵn sàng?<br/>- Gói status = READY<br/>- 100% Required Inputs rõ nguồn<br/>- 100% Open Questions xong}
        CheckReadiness -- Chưa đủ --> ExportDraft[Cho phép xuất nháp có điều kiện DRAFT PROPOSAL]
        CheckReadiness -- Hoàn tất --> UnlockOfficial[Mở khóa Xuất Hợp đồng Chính thức & Lệnh Bàn giao]
    end

    subgraph Handover_Execution["4. Xuất bản & Chuyển giao Thực địa"]
        UnlockOfficial --> ExportCustomer[1. Xuất Proposal gửi Khách hàng<br/>Định dạng: PDF / Web View / In ấn]
        UnlockOfficial --> TransferOps[2. Bấm 'Bàn giao cho Operation'<br/>Chuyển gói sang bảng theo dõi của Đội bay]
        TransferOps --> FreezePackage[3. Đóng băng Gói giải pháp: package_status = ARCHIVED<br/>Lưu trữ làm Baseline không thể chỉnh sửa]
        TransferOps --> OpsAccept([Đội bay tiếp nhận & Lập kế hoạch bay thực địa])
    end
```

### 2.2 Các bước nghiệp vụ chi tiết

#### Bước 1: Kiểm tra điều kiện tiên quyết (Prerequisite Check)
Nút bấm **`[ Báo Cáo Giải Pháp & Bàn Giao ]`** chỉ được kích hoạt khi:
* Gói giải pháp đã chạy qua [Validation Engine](./GASCOLAE_Spec_Validation_Engine.md).
* Trạng thái gói đạt **`READY`** (Sẵn sàng hoàn hảo) hoặc **`CONDITIONALLY_READY`** (Sẵn sàng có điều kiện). Nếu gói đang mang trạng thái **`GAP`**, hệ thống cương quyết khóa chức năng này.

#### Bước 2: Động cơ tổng hợp dữ liệu tự động (Data Aggregation Engine)
Hệ thống gom toàn bộ dữ liệu phân tán từ các bảng nghiệp vụ:
1. **Tổng hợp danh mục Đầu vào cần thiết (Aggregated Required Inputs):**  
   Hệ thống gom tất cả các dòng trong `service_inputs` của các dịch vụ nằm trong gói, lọc bỏ trùng lặp và phân nhóm rõ ràng thành 2 cột:
   * **Cột 1 (Trách nhiệm của Khách hàng):** Các dữ liệu khách phải bàn giao trước ngày khởi công (ví dụ: *Ranh giới khu vực Shapefile, Giấy phép tiếp cận nông trường, Danh sách loại cây trồng*).
   * **Cột 2 (Trách nhiệm của GASCOLAE):** Các dữ liệu đội bay và kỹ sư tự đo đạc (ví dụ: *Ảnh UAV thô, Dữ liệu trạm đo bức xạ mặt trời, Tọa độ mốc GPS/RTK*).
2. **Tổng hợp danh mục Sản phẩm bàn giao cam kết (Aggregated Deliverables):**  
   Hệ thống gom tất cả các dòng trong `service_deliverables` gắn với cấp độ (`selected_level_id`) của từng dịch vụ trong gói, sắp xếp theo từng Phase (Phase 1, Phase 2, Phase 3).

#### Bước 3: Soạn thảo Thuyết minh giải pháp bằng AI (Executive Summary Narrative)
* Hệ thống truyền các thông số: Tên khách hàng, Mục tiêu bài toán (`objective_raw`), Diện tích, Danh sách dịch vụ được chọn sang cho **Gemini API**.
* AI tự động tạo ra một đoạn văn Thuyết minh giải pháp ngắn gọn (khoảng 3 - 4 đoạn) bằng văn phong chuyên nghiệp:
  > 💡 *Ví dụ đoạn văn do AI sinh ra:*  
  > *"Kính gửi Ban Lãnh đạo Nông trường Cao su Bình Phước, GASCOLAE hân hạnh đề xuất Gói giải pháp Đo đạc sinh khối và Kiểm kê Carbon trọn gói cho diện tích 3.000 ha. Giải pháp được chia làm 3 giai đoạn: Phase 1 sử dụng máy bay không người lái mang camera đa phổ quét độ nét 5cm/pixel; Phase 2 xử lý bản đồ số độ cao; và Phase 3 ứng dụng trí tuệ nhân tạo ước tính trữ lượng carbon nhằm phục vụ lập hồ sơ chứng nhận tín chỉ carbon quốc tế MRV..."*
* Nhân viên Sales đọc lại đoạn văn này trên giao diện và có toàn quyền chỉnh sửa nếu muốn.

#### Bước 4: Kiểm tra Tiêu chuẩn Sẵn sàng (Readiness Checklist)
Trước khi bàn giao, hệ thống hiển thị bảng kiểm định 4 tiêu chí cốt lõi:
* [x] **Trạng thái Gói:** Đạt chuẩn `READY` (0 lỗi, 0 cảnh báo).
* [x] **Dữ liệu đầu vào:** 100% dữ liệu bắt buộc đã xác định rõ đơn vị cung cấp.
* [x] **Vướng mắc thực địa:** 100% câu hỏi mở (`package_open_questions`) đã ở trạng thái `RESOLVED`.
* [x] **Phê duyệt:** Đã có chữ ký duyệt của Trưởng bộ phận Pre-Sales.

#### Bước 5: Bàn giao sang Operation & Đóng băng phiên bản
* Sales bấm nút **`[ Xác Nhận Bàn Giao Vận Hành ]`**:
  * Đội Kỹ sư Vận hành (Operation) nhận được thông báo hồ sơ mới trên trang quản trị nội bộ.
  * Phiên bản hiện tại của gói giải pháp chuyển sang trạng thái chốt sổ cố định (`package_status = ARCHIVED` hoặc `LOCKED`), đóng vai trò là **Đường cơ sở kỹ thuật (Technical Baseline)** để đối chiếu khi nghiệm thu.

---

## 3. CẤU TRÚC HỒ SƠ BÁO CÁO GIẢI PHÁP TỔNG HỢP (SOLUTION SUMMARY DOSSIER)

Một bộ tài liệu Báo cáo Giải pháp Tổng hợp hoàn chỉnh bao gồm **7 phần chuẩn mực**:

| Phần | Tên đề mục | Nội dung chi tiết | Bên tiếp nhận |
| :---: | :--- | :--- | :---: |
| **1** | **Thông tin Dự án & Khách hàng** | Tên doanh nghiệp, người đại diện, địa bàn khảo sát, diện tích quy mô, mã hồ sơ `PKG-...`. | Khách hàng & Operation |
| **2** | **Thuyết minh Giải pháp (Executive Summary)** | Đoạn văn do AI trau chuốt, tóm tắt tổng thể phương án kỹ thuật và giá trị mang lại. | Khách hàng |
| **3** | **Lộ trình Triển khai theo Phase** | Danh sách dịch vụ được phân bổ vào Phase 1, Phase 2, Phase 3 kèm thời gian dự kiến. | Khách hàng & Operation |
| **4** | **Bảng Tổng hợp Dữ liệu Đầu vào (Required Inputs)** | Danh sách phân tách rõ: Dữ liệu Khách hàng cần chuẩn bị vs Dữ liệu GASCOLAE tự thu thập. | Khách hàng & Operation |
| **5** | **Bảng Danh mục Sản phẩm Bàn giao (Deliverables)** | Danh sách toàn bộ báo cáo, bản đồ số, file dữ liệu cam kết bàn giao khi kết thúc dự án. | Khách hàng & Operation |
| **6** | **Biên bản Giải tỏa Vướng mắc (Resolved Questions)** | Danh sách các câu hỏi mở về an toàn bay, thời tiết, pháp lý đã được các bên thống nhất phương án. | Operation |
| **7** | **Chứng chỉ Thẩm định Kỹ thuật (Validation Certificate)** | Kết quả kiểm định tự động của Validation Engine chứng minh tính toàn vẹn 0 lỗi kỹ thuật. | Lãnh đạo phê duyệt |

---

## 4. MA TRẬN ÁNH XẠ DỮ LIỆU ĐA TẦNG (DATA AGGREGATION MATRIX)

Báo cáo Solution Summary không tạo thêm bảng dữ liệu mới, mà là **bộ tổng hợp dữ liệu (View / Aggregation DTO)** trích xuất tự động từ **8 bảng nghiệp vụ trong cơ sở dữ liệu**:

```
                                  [ CƠ SỞ DỮ LIỆU POSTGRESQL ]
                                                │
    ┌───────────────────────┬───────────────────┼───────────────────┬───────────────────────┐
    ▼                       ▼                   ▼                   ▼                       ▼
Bảng `customers`    `customer_requirements`   `solution_packages`  `package_services`      `validation_runs`
(Tên KH, liên hệ)   (Diện tích, vị trí, mục tiêu) (Mã gói, version) (Danh sách dịch vụ/phase) (Chứng chỉ 0 lỗi)
    │                       │                   │                   │                       │
    └───────────────────────┴───────────────────┼───────────────────┴───────────────────────┘
                                                │
                                                ▼
                         [ BỘ BÁO CÁO TỔNG HỢP: SOLUTION SUMMARY ]
                                ├── 1. Khách hàng & Mục tiêu dự án
                                ├── 2. Thuyết minh AI Executive Summary
                                ├── 3. Lộ trình dịch vụ Phase 1 - 2 - 3
                                ├── 4. Bảng Required Inputs (Khách cấp vs GASCOLAE cấp)
                                ├── 5. Bảng Deliverables cam kết bàn giao
                                └── 6. Tiêu chuẩn sẵn sàng Readiness & Bàn giao Operation
```

### Chi tiết nguồn trích xuất cho từng chỉ tiêu trên Báo cáo:

| Chỉ tiêu hiển thị trên Báo cáo | Nguồn bảng dữ liệu gốc | Điều kiện trích xuất & Tổng hợp |
| :--- | :--- | :--- |
| **Đơn vị & Đại diện liên hệ** | `customers` | Nối qua `customer_id` của đề bài |
| **Địa bàn & Diện tích quy mô** | `customer_requirements` | Lấy từ `location_description`, `area_value`, `area_unit` |
| **Lộ trình Phase 1, Phase 2, Phase 3** | `package_services` $\rightarrow$ `services` | Sắp xếp theo `phase_no ASC, sort_order ASC` |
| **Dữ liệu do Khách hàng cung cấp** | `package_services` $\rightarrow$ `service_inputs` | Lọc theo `provided_by = 'CUSTOMER'` và `is_required = TRUE`, gom nhóm loại bỏ trùng lặp |
| **Dữ liệu do GASCOLAE tự đo đạc** | `package_services` $\rightarrow$ `service_inputs` | Lọc theo `provided_by = 'GASCOLAE'`, gom nhóm loại bỏ trùng lặp |
| **Toàn bộ Sản phẩm nghiệm thu** | `package_services` $\rightarrow$ `service_deliverables` | Lấy theo `service_id` và `selected_level_id`, hiển thị kèm định dạng file bàn giao |
| **Chứng chỉ hợp lệ kỹ thuật** | `validation_runs` | Lấy phiên kiểm định gần nhất có `calculated_package_status = 'READY'` |
| **Biên bản thống nhất thực địa** | `package_open_questions` | Lọc toàn bộ câu hỏi có `status = 'RESOLVED'` kèm nội dung `resolution_note` |

---

## 5. CÁC QUY TẮC NGHIỆP VỤ BỔ TRỢ (BUSINESS RULES)

1. **Quy tắc Kiểm soát Cửa ngõ Sẵn sàng (Readiness Gatekeeping Rule):**  
   Hệ thống nghiêm cấm thực hiện thao tác "Bàn giao cho Operation" nếu gói giải pháp chưa vượt qua bài kiểm định kỹ thuật (Status $\ne$ `READY`). Đội kỹ thuật không bao giờ tiếp nhận một hồ sơ còn tồn tại lỗ hổng dữ liệu.
2. **Quy tắc Minh bạch Trách nhiệm Dữ liệu (Input Provider Disambiguation):**  
   Bảng tổng hợp dữ liệu đầu vào (`Aggregated Inputs`) bắt buộc phải phân định rõ ràng 100% từng dòng dữ liệu: do Khách hàng chịu trách nhiệm bàn giao hay do GASCOLAE tự thu thập. Tuyệt đối không để xảy ra tranh chấp *"Ai phải chuẩn bị bản đồ ranh giới?"* sau khi đã ký hợp đồng.
3. **Quy tắc Đóng băng Đường cơ sở (Baseline Freeze Rule):**  
   Ngay khi lệnh Bàn giao Vận hành được xác nhận thành công, hồ sơ gói giải pháp sẽ bị đóng băng (`ARCHIVED` / `LOCKED`). Mọi yêu cầu thay đổi scope (phạm vi công việc) phát sinh sau thời điểm này bắt buộc phải tạo thành Phụ lục hợp đồng mới (`Version + 1`) và phải chạy lại quy trình kiểm định từ đầu.

---

## 6. TỔNG KẾT CHUỖI GIÁ TRỊ TOÀN DỰ ÁN (END-TO-END VALUE CHAIN)

Hệ thống **GASCOLAE Intelligent Service Package Builder** đã số hóa và tự động hóa trọn vẹn chuỗi giá trị từ một ý tưởng sơ khai trên website đến một kế hoạch bay không người lái khả thi ngoài thực địa:

```
[ Bước 1: Landing Page ] ──► [ Bước 2: Recommendation ] ──► [ Bước 3: Package Builder ]
Khách gửi đề bài thô        Chấm điểm 5 tiêu chí toán học     Lắp ghép Lego theo Phase
& mong muốn đầu ra          đề xuất dịch vụ & level           & định rõ vai trò Core
          │                                                               │
          └───────────────────────────────┬───────────────────────────────┘
                                          ▼
                         [ Bước 4: Validation Engine ]
                         Trọng tài quét lỗi Input/Output
                         chặn đứng rủi ro bất khả thi
                                          │
                                          ▼
                     [ Bước 5: Solution Summary & Handover ]
                     Xuất Proposal chốt hợp đồng với Khách
                     & Bàn giao kế hoạch bay cho Operation!
```
