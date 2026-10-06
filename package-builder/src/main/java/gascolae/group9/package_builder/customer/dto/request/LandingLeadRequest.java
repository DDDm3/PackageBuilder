package gascolae.group9.package_builder.customer.dto.request;

import gascolae.group9.package_builder.customer.enums.OutputPriority;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin đề bài gửi từ Form tư vấn Landing Page")
public class LandingLeadRequest {
    // 0. Dịch vụ & Cấp độ khách chọn trên Landing Page
    @Schema(description = "Mã dịch vụ khách chọn trên Landing Page (VD: S0274)", example = "S0274")
    String serviceId;

    @Schema(description = "Cấp độ dịch vụ mong muốn (nhận số 1-3, hoặc chuỗi 'Level 2')", example = "2")
    Object level;

    // 1. Thông tin liên hệ khách hàng
    @Schema(description = "Họ và tên người liên hệ", example = "Nguyễn Văn An", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "NOT_NULL")
    String customerName;

    @Schema(description = "Tên cơ quan / đơn vị / doanh nghiệp", example = "Công ty CP Môi trường Đô thị Xanh")
    String companyName;

    @Schema(description = "Địa chỉ email liên hệ", example = "nguyenvanan@moitruongxanh.vn")
    String contactEmail;

    @Schema(description = "Số điện thoại liên hệ", example = "0987654321")
    String contactPhone;

    // 2. Thông tin nhu cầu dự án
    @Schema(description = "Tên dự án (nếu để trống hệ thống sẽ tự sinh theo tên khách/công ty)", example = "Khảo sát phát thải KNK Quận 7")
    String projectName;

    @Schema(description = "Địa bàn / Khu vực khảo sát (AOI)", example = "Quận 7, TP. Hồ Chí Minh")
    String locationDescription;

    @Schema(description = "Diện tích khảo sát (nhận số 25.5, chuỗi '25.5 ha', '1000 m2' hoặc object)", example = "25.5")
    Object areaValue;

    @Schema(description = "Đơn vị diện tích (mặc định: ha)", example = "ha")
    @Builder.Default
    String areaUnit = "ha";

    @Schema(description = "Tần suất quan trắc / theo dõi mong muốn", example = "Định kỳ theo quý")
    String monitoringFrequencyRaw;

    @Schema(description = "Mục tiêu bài toán", example = "Kiểm kê phát thải và phát hiện điểm nóng rò rỉ khí CH4")
    String objectiveRaw;

    @Schema(description = "Dữ liệu / tài liệu khách hàng đã có sẵn", example = "Bản đồ ranh giới khu đất tỷ lệ 1:2000 dạng CAD")
    String providedInputsRaw;

    @Schema(description = "Mô tả chung nhu cầu dự án", example = "Cần bay UAV đo nồng độ CO2 và CH4 tại các trục đường chính và bãi trung chuyển rác.")
    String rawRequirementText;

    @Schema(description = "Ngành nghề / lĩnh vực hoạt động", example = "Môi trường & Quản lý chất thải")
    String industryRaw;

    @Schema(description = "Môi trường triển khai (Đô thị, Rừng, Nông nghiệp, Công nghiệp)", example = "Đô thị")
    String environmentRaw;

    // 3. Đầu ra / Sản phẩm mong đợi (hỗ trợ text list, string đơn, chuỗi rỗng "", mảng string hoặc mảng object)
    @Schema(description = "Danh sách sản phẩm đầu ra mong đợi (dạng chuỗi)")
    List<String> rawExpectedOutputs;

    @Schema(description = "Đầu ra kỳ vọng linh hoạt (nhận text-area đa dòng, mảng chuỗi hoặc mảng object)")
    Object expectedOutputs;

    public void setServiceID(String serviceID) {
        this.serviceId = serviceID;
    }

    public Integer getLevelAsInteger() {
        if (this.level == null) return null;
        if (this.level instanceof Number num) return num.intValue();
        String str = this.level.toString().trim().replaceAll("[^0-9]", "");
        if (str.isEmpty()) return null;
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return null;
        }
    }

    public void setArea(Object input) {
        this.areaValue = input;
    }

    public BigDecimal getAreaValueAsBigDecimal() {
        return parseAreaValue(this.areaValue);
    }

    public String getAreaUnit() {
        if (this.areaValue instanceof Map<?, ?> map) {
            Object u = map.get("unit");
            if (u != null && !u.toString().trim().isEmpty()) {
                return u.toString().trim();
            }
        }
        return this.areaUnit != null ? this.areaUnit : "ha";
    }

    private BigDecimal parseAreaValue(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof BigDecimal bd) {
            return bd;
        }
        if (input instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        if (input instanceof String str) {
            String trimmed = str.trim();
            if (trimmed.isEmpty()) return null;

            // Tự động bóc tách số và đơn vị nếu người dùng nhập "20 ha", "20ha", "20.5 ha", "20,5 ha", "1000 m2"
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("^([0-9]+(?:[.,][0-9]+)?)\\s*([a-zA-Z0-9²³]+)?$").matcher(trimmed);
            if (matcher.find()) {
                String numberPart = matcher.group(1).replace(",", ".");
                String unitPart = matcher.group(2);
                if (unitPart != null && !unitPart.trim().isEmpty()) {
                    this.areaUnit = unitPart.trim().toLowerCase();
                }
                try {
                    return new BigDecimal(numberPart);
                } catch (Exception ignored) {}
            }

            try {
                return new BigDecimal(trimmed);
            } catch (Exception e) {
                return null;
            }
        }
        if (input instanceof Map<?, ?> map) {
            Object val = map.get("value") != null ? map.get("value") : map.get("areaValue");
            if (val == null) {
                val = map.get("area");
            }
            return parseAreaValue(val);
        }
        return null;
    }

    public List<ExpectedOutputRequest> getExpectedOutputsAsList() {
        return parseExpectedOutputs(this.expectedOutputs);
    }

    private List<ExpectedOutputRequest> parseExpectedOutputs(Object input) {
        List<ExpectedOutputRequest> results = new ArrayList<>();
        if (input == null) {
            if (this.rawExpectedOutputs != null) {
                for (String s : this.rawExpectedOutputs) {
                    if (s != null && !s.trim().isEmpty()) {
                        results.add(new ExpectedOutputRequest(s.trim()));
                    }
                }
            }
            return results;
        }

        if (input instanceof String str) {
            String trimmed = str.trim();
            if (!trimmed.isEmpty()) {
                String[] parts = trimmed.split("[,\n\r]+");
                for (String part : parts) {
                    if (!part.trim().isEmpty()) {
                        results.add(new ExpectedOutputRequest(part.trim()));
                    }
                }
            }
            return results;
        }

        if (input instanceof List<?> list) {
            for (Object item : list) {
                if (item == null) continue;
                if (item instanceof ExpectedOutputRequest eor) {
                    results.add(eor);
                } else if (item instanceof String s) {
                    if (!s.trim().isEmpty()) {
                        results.add(new ExpectedOutputRequest(s.trim()));
                    }
                } else if (item instanceof Map<?, ?> map) {
                    Object text = map.get("rawExpectedOutput") != null ? map.get("rawExpectedOutput") : map.get("name");
                    if (text == null) text = map.get("output");
                    if (text != null && !text.toString().trim().isEmpty()) {
                        OutputPriority p = OutputPriority.REQUIRED;
                        Object priorityObj = map.get("priority");
                        if (priorityObj != null) {
                            try {
                                p = OutputPriority.valueOf(priorityObj.toString().trim().toUpperCase());
                            } catch (Exception ignored) {}
                        }
                        results.add(ExpectedOutputRequest.builder()
                                .rawExpectedOutput(text.toString().trim())
                                .priority(p)
                                .build());
                    }
                }
            }
        }
        return results;
    }
}
