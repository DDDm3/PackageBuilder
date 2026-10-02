package gascolae.group9.package_builder.catalog.dto.response;

import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceOutputResponse {
    String serviceOutputId;
    String dataItemId;
    String dataCode;
    String dataName;
    String description;
    VerificationStatus verificationStatus;
}
