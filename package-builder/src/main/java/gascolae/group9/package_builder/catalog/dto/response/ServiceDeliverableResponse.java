package gascolae.group9.package_builder.catalog.dto.response;

import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceDeliverableResponse {
    String serviceDeliverableId;
    String deliverableCode;
    String deliverableName;
    String description;
    Integer minLevelNo;
    VerificationStatus verificationStatus;
    List<DataItemResponse> dataItems;
}
