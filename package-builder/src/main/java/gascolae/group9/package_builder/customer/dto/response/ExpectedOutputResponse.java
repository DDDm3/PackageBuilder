package gascolae.group9.package_builder.customer.dto.response;

import gascolae.group9.package_builder.customer.enums.OutputPriority;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpectedOutputResponse {
    String requirementExpectedOutputId;
    String rawExpectedOutput;
    OutputPriority priority;
    String dataItemId;
    LocalDateTime createdAt;
}
