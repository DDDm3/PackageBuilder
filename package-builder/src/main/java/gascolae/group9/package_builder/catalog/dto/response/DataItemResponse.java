package gascolae.group9.package_builder.catalog.dto.response;

import gascolae.group9.package_builder.catalog.enums.DataClassification;
import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataItemResponse {
    String dataItemId;
    String dataCode;
    String dataName;
    String description;
    DataClassification classification;
    VerificationStatus verificationStatus;
    String sourceRef;
    LocalDateTime createdAt;
}
