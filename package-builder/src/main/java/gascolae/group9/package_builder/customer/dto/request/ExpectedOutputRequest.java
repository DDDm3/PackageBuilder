package gascolae.group9.package_builder.customer.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import gascolae.group9.package_builder.customer.enums.OutputPriority;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpectedOutputRequest {
    @NotBlank(message = "NOT_NULL")
    String rawExpectedOutput;

    @Builder.Default
    OutputPriority priority = OutputPriority.NORMAL;

    String dataItemId;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ExpectedOutputRequest(String rawExpectedOutput) {
        this.rawExpectedOutput = rawExpectedOutput;
        this.priority = OutputPriority.REQUIRED;
    }
}
