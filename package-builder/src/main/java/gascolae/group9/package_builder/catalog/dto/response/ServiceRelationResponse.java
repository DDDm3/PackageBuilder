package gascolae.group9.package_builder.catalog.dto.response;

import gascolae.group9.package_builder.catalog.enums.RelationType;
import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceRelationResponse {
    String relationId;
    String sourceServiceId;
    String sourceServiceCode;
    String sourceServiceName;
    String targetServiceId;
    String targetServiceCode;
    String targetServiceName;
    RelationType relationType;
    String viaDataCode;
    String viaDataName;
    Boolean isRequired;
    String description;
    VerificationStatus verificationStatus;
}
