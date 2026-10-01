package gascolae.group9.package_builder.customer.dto.response;

import gascolae.group9.package_builder.customer.enums.CustomerStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerResponse {
    String customerId;
    String customerCode;
    String customerName;
    String companyName;
    String industry;
    String contactEmail;
    String contactPhone;
    CustomerStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
