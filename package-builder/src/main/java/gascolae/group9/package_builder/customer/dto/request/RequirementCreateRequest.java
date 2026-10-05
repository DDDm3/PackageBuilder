package gascolae.group9.package_builder.customer.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequirementCreateRequest {
    @NotBlank(message = "NOT_NULL")
    String customerId;

    @NotBlank(message = "NOT_NULL")
    String requirementCode;

    @NotBlank(message = "NOT_NULL")
    String projectName;

    String rawRequirementText;
    String industryRaw;
    String environmentRaw;
    Object areaValue;
    String areaUnit;
    String locationDescription;
    String monitoringFrequencyRaw;
    String objectiveRaw;
    String providedInputsRaw;
    String serviceId;
    Object level;

    Object expectedOutputs;

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

    public List<ExpectedOutputRequest> getExpectedOutputsAsList() {
        return parseExpectedOutputs(this.expectedOutputs);
    }

    private List<ExpectedOutputRequest> parseExpectedOutputs(Object input) {
        List<ExpectedOutputRequest> results = new java.util.ArrayList<>();
        if (input == null) {
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
                } else if (item instanceof java.util.Map<?, ?> map) {
                    Object text = map.get("rawExpectedOutput") != null ? map.get("rawExpectedOutput") : map.get("name");
                    if (text == null) text = map.get("output");
                    if (text != null && !text.toString().trim().isEmpty()) {
                        gascolae.group9.package_builder.customer.enums.OutputPriority p = gascolae.group9.package_builder.customer.enums.OutputPriority.REQUIRED;
                        Object priorityObj = map.get("priority");
                        if (priorityObj != null) {
                            try {
                                p = gascolae.group9.package_builder.customer.enums.OutputPriority.valueOf(priorityObj.toString().trim().toUpperCase());
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

    public BigDecimal getAreaValueAsBigDecimal() {
        return parseAreaValue(this.areaValue);
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
            if (trimmed.isEmpty()) {
                return null;
            }
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
        if (input instanceof java.util.Map<?, ?> map) {
            Object val = map.get("value") != null ? map.get("value") : map.get("areaValue");
            if (val == null) {
                val = map.get("area");
            }
            Object unit = map.get("unit");
            if (unit != null && !unit.toString().trim().isEmpty()) {
                this.areaUnit = unit.toString().trim();
            }
            return parseAreaValue(val);
        }
        return null;
    }

    public void setArea(Object input) {
        this.areaValue = input;
    }
}
