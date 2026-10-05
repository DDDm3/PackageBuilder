package gascolae.group9.package_builder.customer.dto.request;

import gascolae.group9.package_builder.customer.enums.OutputPriority;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LandingLeadRequest {
    // 0. Dịch vụ & Cấp độ khách chọn trên Landing Page
    String serviceId;

    Object level;

    // 1. Thông tin liên hệ khách hàng
    @NotBlank(message = "NOT_NULL")
    String customerName;

    String companyName;

    String contactEmail;

    String contactPhone;

    // 2. Thông tin nhu cầu dự án
    String projectName;

    String locationDescription;

    Object areaValue;

    @Builder.Default
    String areaUnit = "ha";

    String monitoringFrequencyRaw;

    String objectiveRaw;

    String providedInputsRaw;

    String rawRequirementText;

    String industryRaw;

    String environmentRaw;

    // 3. Đầu ra / Sản phẩm mong đợi (hỗ trợ text list, string đơn, chuỗi rỗng "", mảng string hoặc mảng object)
    List<String> rawExpectedOutputs;

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
