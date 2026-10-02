package gascolae.group9.package_builder.catalog.dto.request;

import gascolae.group9.package_builder.catalog.enums.DataClassification;
import gascolae.group9.package_builder.catalog.enums.LifecycleStatus;
import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceCreateRequest {

    @NotBlank(message = "NOT_NULL")
    String serviceCode;

    @NotBlank(message = "NOT_NULL")
    String serviceName;

    String serviceNameEn;

    @NotBlank(message = "NOT_NULL")
    String category;

    String sectorCode;
    String subsectorCode;
    String shortDescription;
    String targetCustomer;
    String customerProblems;
    String useCases;
    String technologies;

    LifecycleStatus lifecycleStatus;
    VerificationStatus verificationStatus;
    DataClassification dataClassification;
    String sourceRef;

    List<String> tagCodes;
}
