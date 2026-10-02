package gascolae.group9.package_builder.catalog.dto.response;

import gascolae.group9.package_builder.catalog.enums.ProvidedBy;
import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceInputResponse {
    String serviceInputId;
    String dataItemId;
    String dataCode;
    String dataName;
    Boolean required;
    ProvidedBy providedBy;
    String description;
    VerificationStatus verificationStatus;
}
