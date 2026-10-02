package gascolae.group9.package_builder.catalog.dto.response;

import gascolae.group9.package_builder.catalog.enums.LifecycleStatus;
import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceSummaryResponse {
    String serviceId;
    String serviceCode;
    String serviceName;
    String serviceNameEn;
    String category;
    String sectorCode;
    String subsectorCode;
    String shortDescription;
    LifecycleStatus lifecycleStatus;
    VerificationStatus verificationStatus;
    Boolean active;
    List<String> tags;
}
