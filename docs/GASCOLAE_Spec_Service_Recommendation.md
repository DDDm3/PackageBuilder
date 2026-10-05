# ĐẶC TẢ NGHIỆP VỤ & ÁNH XẠ CƠ SỞ DỮ LIỆU
## CHỨC NĂNG: ĐỘNG CƠ GỢI Ý DỊCH VỤ (SERVICE RECOMMENDATION ENGINE)

---

## 1. TỔNG QUAN NGHIỆP VỤ

### 1.1 Mục tiêu chức năng
Tự động hóa quá trình phân tích đề bài của khách hàng ([customer_requirements](file:///d:/GASCOLAE/Package_Builder/docs/GASCOLAE_Spec_Landing_Lead_Requirement.md#32-b%E1%BA%A3ng-2-customer_requirements-n%E1%BB%99i-dung-b%C3%A0i-to%C3%A1n--y%C3%AAu-c%E1%BA%A7u-d%E1%BB%B1-%C3%A1n)) để đề xuất danh sách các dịch vụ kỹ thuật số (UAV, viễn thám, AI) phù hợp nhất từ kho 12 dịch vụ của GASCOLAE.

Hệ thống vận hành theo nguyên tắc **Chấm điểm đa tiêu chí có giải thích minh bạch (Explainable Multi-Criteria Scoring)**:
* Không sử dụng "hộp đen" AI để tự bịa kết quả.
* Thuật toán chạy trên Backend dựa trên các quy tắc toán học có trọng số, đảm bảo kết quả **nhất quán 100%**, có thể kiểm toán và giải trình rõ ràng cho khách hàng.

### 1.2 Vai trò và tác nhân (Actors)
* **Nhân viên Sales / Pre-Sales:** Người bấm nút kích hoạt gợi ý sau khi đã thẩm định đề bài của khách, xem bảng xếp hạng và bấm chấp thuận (`accepted`) để đưa dịch vụ vào Gói giải pháp.
* **Hệ thống Recommendation Engine (Backend Core):** Quét kho dữ liệu Catalog, tính toán 5 thang điểm thành phần, tổng hợp điểm số, sinh câu giải trình lý do và xếp hạng (Ranking).

---

## 2. QUY TRÌNH NGHIỆP VỤ (BUSINESS WORKFLOW)

### 2.1 Sơ đồ dòng nghiệp vụ

```mermaid
flowchart TD
    Start([Sales mở Đề bài đã thẩm định]) --> Step1[Sales bấm nút 'Chạy Gợi ý Dịch vụ']
    
    subgraph Engine["Động cơ Gợi ý (Recommendation Engine - Backend)"]
        Step1 --> FetchReq[1. Lấy dữ liệu Đề bài & Danh sách Đầu ra mong đợi<br/>Mục tiêu, Ngành, Môi trường, Deliverables, Priority]
        FetchReq --> ScanCatalog[2. Quét toàn bộ 12 Dịch vụ cốt lõi trong Catalog]
        
        ScanCatalog --> CalcScores[3. Tính toán 5 Điểm thành phần cho từng Dịch vụ:
        - Output Score (Khớp sản phẩm đầu ra mong đợi)
        - Objective Score (Khớp mục tiêu bài toán)
        - Use Case Score (Khớp tình huống ứng dụng thực tế)
        - Industry Score (Khớp ngành nghề & môi trường)
        - Tag Score (Khớp từ khóa kỹ thuật)]
        
        CalcScores --> WeightedSum[4. Tính Tổng điểm có trọng số:
        Total Match Score = Σ Wi × Si]
        
        WeightedSum --> FilterCutoff{Điểm tổng >= Ngưỡng tối thiểu?<br/>(Score >= 30đ)}
        FilterCutoff -- Không --> DropService[Loại bỏ khỏi gợi ý]
        FilterCutoff -- Có --> GenReason[5. Tự động sinh câu Giải thích lý do gợi ý<br/>Liệt kê rõ các điểm cộng theo từng tiêu chí]
        
        GenReason --> RankServices[6. Sắp xếp thứ hạng ưu tiên: Top 1, Top 2, Top 3...]
    end

    RankServices --> SaveDB[7. Lưu toàn bộ bảng điểm vào Database<br/>Bảng: service_recommendations]
    
    subgraph UI_Sales["Giao diện Sales Admin Dashboard"]
        SaveDB --> DisplayCards[Hiển thị danh sách Thẻ dịch vụ gợi ý<br/>Kèm Huy hiệu % phù hợp & Lý do đề xuất]
        DisplayCards --> SalesAction{Sales duyệt dịch vụ nào?}
        SalesAction -- Chọn dịch vụ --> AcceptService[Bấm 'Thêm vào Gói' -> accepted = TRUE<br/>Chuyển sang bước Đóng gói Package Builder]
        SalesAction -- Không phù hợp --> IgnoreService[Bỏ qua hoặc chỉnh sửa lại Đề bài]
    end
```

### 2.2 Các bước nghiệp vụ chi tiết

#### Bước 1: Kích hoạt từ màn hình quản trị Sales Admin
Sau khi nhân viên Sales gọi điện xác minh thông tin với khách và chốt danh sách sản phẩm đầu ra ([như đã đặc tả ở Mục 4](file:///d:/GASCOLAE/Package_Builder/docs/GASCOLAE_Spec_Landing_Lead_Requirement.md#4-c%C6%A1-ch%E1%BA%BF-sales-tinh-ch%E1%BB%89nh--th%C3%AAm-m%E1%BB%9Bi-%C4%91%E1%BA%A7u-ra-sales-admin-output-refinement)), Sales bấm nút:  
👉 **`[ Phân Tích & Gợi Ý Dịch Vụ ]`**.

#### Bước 2: Thu thập thông số đề bài (Input Profiles)
Hệ thống tải toàn bộ thông số từ đề bài của khách hàng:
* `objective_raw`: Mục tiêu (ví dụ: *Đo tín chỉ carbon, phòng chống hạn hán...*).
* `industry_raw` & `environment_raw`: Ngành nghề và môi trường khảo sát (ví dụ: *Lâm nghiệp / Rừng trồng*).
* `expectedOutputs`: Danh sách đầu ra mong đợi kèm mức độ ưu tiên (`REQUIRED`, `HIGH`, `NORMAL`, `LOW`) và mã dữ liệu chuẩn `data_item_id` (nếu có).

#### Bước 3: Thuật toán chấm điểm 5 tiêu chí (Scoring Mechanism)
Hệ thống duyệt qua 12 dịch vụ trong cơ sở dữ liệu và tính điểm theo thang điểm 100:

| Tiêu chí | Trọng số ($W_i$) | Cách tính điểm nghiệp vụ |
| :--- | :---: | :--- |
| **1. Khớp Sản phẩm đầu ra (`output_score`)** | **30%** | So khớp danh sách đầu ra của khách với bảng `service_outputs` / `service_deliverables` của dịch vụ.<br>• Khớp mã `data_item_id`: 100% điểm thành phần.<br>• Khớp từ khóa tên thô: 60-80% điểm.<br>• Nhân hệ số theo độ ưu tiên: `REQUIRED` ($\times 2.0$), `HIGH` ($\times 1.5$), `NORMAL` ($\times 1.0$). |
| **2. Khớp Mục tiêu dự án (`objective_score`)** | **25%** | So khớp mục tiêu của khách với trường `customer_problems` (nỗi đau khách hàng giải quyết được) và các Tag mục tiêu (`OBJECTIVE`) của dịch vụ. |
| **3. Khớp Tình huống sử dụng (`use_case_score`)** | **20%** | So khớp ngữ cảnh của khách với trường `use_cases` của dịch vụ (ví dụ: *"khảo sát biến động rừng sau mùa mưa"*). |
| **4. Khớp Ngành & Môi trường (`industry_score`)** | **15%** | Dịch vụ có gắn Tag ngành nghề (`INDUSTRY`) và địa hình (`ENVIRONMENT`) trùng khớp với đề bài không? |
| **5. Khớp Thẻ nhãn kỹ thuật (`tag_score`)** | **10%** | Mức độ tương đồng về công nghệ: UAV, LiDAR, Multispectral, AI... |

$$\text{Tổng Điểm (Match Score)} = 0.30 \cdot S_{\text{output}} + 0.25 \cdot S_{\text{obj}} + 0.20 \cdot S_{\text{usecase}} + 0.15 \cdot S_{\text{ind}} + 0.10 \cdot S_{\text{tag}}$$

#### Bước 4: Lọc ngưỡng & Sinh câu giải trình lý do (Reason Generator)
* **Ngưỡng sàng lọc (Cut-off Threshold):** Các dịch vụ có tổng điểm $< 30$ điểm sẽ bị loại bỏ để tránh gây nhiễu cho Sales.
* **Tự động sinh câu giải thích tường minh (`recommendation_reason`):**  
  Hệ thống ghép nối các luận điểm điểm cao thành câu văn tự nhiên:  
  > 💡 *Ví dụ thực tế:*  
  > *"Khớp 100% sản phẩm đầu ra: Cung cấp Bản đồ sinh khối & Báo cáo MRV (+30đ); Khớp mục tiêu: Nghiên cứu tín chỉ carbon rừng (+25đ); Phù hợp ngành Lâm nghiệp / Rừng trồng (+15đ)."*

#### Bước 5: Xếp hạng (Ranking) & Lưu trữ Database
* Các dịch vụ vượt qua vòng lọc được sắp xếp theo thứ hạng từ cao xuống thấp: Top 1, Top 2, Top 3...
* Hệ thống lưu toàn bộ kết quả chi tiết vào bảng `service_recommendations`.

#### Bước 6: Phản hồi trên màn hình Sales Admin
Giao diện hiển thị các Card dịch vụ trực quan:
* Huy hiệu % phù hợp (Match Badge): ví dụ `92% Match`, `85% Match`...
* Đoạn văn giải thích lý do rõ ràng.
* Cấp độ (Level) khuyến nghị tương ứng.
* Nút bấm hành động: **`[ + Thêm vào Gói Giải Pháp ]`** (chuyển cờ `accepted = TRUE` để đưa dịch vụ vào Gói ở bước tiếp theo).

---

## 3. ÁNH XẠ CƠ SỞ DỮ LIỆU: BẢNG `service_recommendations`

Bảng `service_recommendations` đóng vai trò là bảng lưu vết kết quả phân tích của Động cơ gợi ý cho từng đề bài cụ thể.

```
[ customer_requirements ] ──(1-N)──► [ service_recommendations ] ◄──(N-1)── [ services ]
                                                │
                                    Lưu bảng điểm 5 tiêu chí
                                    + Lý do giải trình tường minh
                                    + Thứ hạng ưu tiên (Rank)
                                    + Cờ chấp thuận (accepted)
```

| Cột Database (`service_recommendations`) | Kiểu dữ liệu | Ý nghĩa nghiệp vụ | Quy tắc & Nguồn gốc giá trị |
| :--- | :--- | :--- | :--- |
| `recommendation_id` | UUID | Khóa chính tự sinh (UUID v4) | Định danh duy nhất cho một dòng gợi ý |
| `requirement_id` | UUID (FK) | Mã đề bài khảo sát của khách | Khóa ngoại liên kết bảng `customer_requirements` |
| `service_id` | UUID (FK) | Dịch vụ được đề xuất | Khóa ngoại liên kết bảng `services` |
| `match_score` | DECIMAL(5,2) | Điểm tổng hợp có trọng số (0 - 100) | Kết quả của công thức $\sum W_i \times S_i$ |
| `output_score` | DECIMAL(5,2) | Điểm thành phần: Khớp đầu ra | Đối soát với `service_outputs` / `data_items` |
| `objective_score` | DECIMAL(5,2) | Điểm thành phần: Khớp mục tiêu | Đối soát với `customer_problems` & Tag `OBJECTIVE` |
| `use_case_score` | DECIMAL(5,2) | Điểm thành phần: Khớp tình huống | Đối soát với trường `use_cases` |
| `industry_score` | DECIMAL(5,2) | Điểm thành phần: Khớp ngành/địa hình | Đối soát với Tag `INDUSTRY` & `ENVIRONMENT` |
| `tag_score` | DECIMAL(5,2) | Điểm thành phần: Khớp từ khóa | Đối soát thẻ công nghệ `TOPIC` & `KEYWORD` |
| `recommendation_reason` | TEXT | Đoạn văn giải thích lý do gợi ý | Câu văn tiếng Việt sinh tự động từ các điểm thành phần |
| `rank_order` | INT | Thứ hạng ưu tiên hiển thị | `1` (Top 1 cao nhất), `2`, `3`... |
| `accepted` | BOOLEAN | Sales có chọn dịch vụ này vào gói? | Mặc định `FALSE`; chuyển `TRUE` khi Sales bấm chọn |
| `created_at` | TIMESTAMP | Mốc thời gian chạy thuật toán | Tự động ghi nhận thời điểm phân tích |

---

## 4. MA TRẬN LIÊN KẾT GIỮA CÁC BẢNG THAM CHIẾU NGUỒN

Để đưa ra được kết quả trên bảng `service_recommendations`, thuật toán đọc dữ liệu từ **2 nguồn chính**:

```
[ NGUỒN 1: ĐỀ BÀI CỦA KHÁCH ]                       [ NGUỒN 2: CATALOG DỊCH VỤ CÔNG TY ]
• customer_requirements (Mục tiêu, Ngành)            • services (Tên, use_cases, customer_problems)
• requirement_expected_outputs (Sản phẩm muốn)       • service_levels (Level 1, 2, 3)
                                                     • service_outputs & data_items (Sản phẩm làm ra)
                                                     • service_tags & tags (Nhãn phân loại)
                                    │
                                    ▼
                 [ BẢNG KẾT QUẢ: service_recommendations ]
```

1. **Đối soát Đầu ra (`S_output`):**  
   So sánh các dòng trong [requirement_expected_outputs](file:///d:/GASCOLAE/Package_Builder/docs/GASCOLAE_Spec_Landing_Lead_Requirement.md#33-b%E1%BA%A3ng-3-requirement_expected_outputs-s%E1%BA%A3n-ph%E1%BA%A9m-%C4%91%E1%BA%A7u-ra-mong-%C4%91%E1%BB%A3i) với bảng `service_outputs` thông qua trường liên kết trung tâm `data_item_id`.
2. **Đối soát Mục tiêu & Ngành nghề (`S_obj`, `S_ind`):**  
   So sánh trường `objective_raw` và `industry_raw` với các nhãn trong bảng `service_tags` nối với bảng `tags`.

---

## 5. CÁC QUY TẮC NGHIỆP VỤ BỔ TRỢ (BUSINESS RULES)

1. **Quy tắc Tính toán Nhất quán (Deterministic Rule):**  
   Cùng một đề bài và cùng một catalog dữ liệu, thuật toán gợi ý **phải luôn luôn trả về cùng một kết quả điểm số và thứ hạng 100% giống nhau** (không bị biến thiên như AI Chatbot).
2. **Quy tắc Minh bạch Giải trình (Explainability Rule):**  
   Mọi gợi ý bắt buộc phải đi kèm câu giải thích `recommendation_reason`. Không chấp nhận gợi ý một dịch vụ mà không nêu được căn cứ vì sao dịch vụ đó giải quyết được bài toán của khách.
3. **Quy tắc Ngưỡng chất lượng tối thiểu (Quality Cut-off):**  
   Chỉ đề xuất các dịch vụ có độ tương thích từ **30% trở lên** (`match_score >= 30`). Tránh tình trạng đề xuất tràn lan cả 12 dịch vụ gây rối mắt cho Sales.
4. **Quy tắc Quyền quyết định cuối cùng (Sales Authority):**  
   Thuật toán gợi ý chỉ đóng vai trò **Trợ lý tham mưu**. Nhân viên Sales là người duy nhất có quyền quyết định đưa dịch vụ nào vào gói giải pháp (thông qua thao tác chuyển cờ `accepted = TRUE`).

---

## 6. BƯỚC TIẾP THEO TRONG HỆ THỐNG
Sau khi Sales chấp thuận các dịch vụ được gợi ý, hệ thống sẽ chuyển sang bước:  
👉 **[Đặc Tả Đóng Gói Giải Pháp (Solution Package Builder)](./GASCOLAE_Spec_Solution_Package_Builder.md)** để phân chia giai đoạn (Phase), định rõ vai trò (Core/Supporting) và chọn cấp độ dịch vụ (Level 1, 2, 3).

