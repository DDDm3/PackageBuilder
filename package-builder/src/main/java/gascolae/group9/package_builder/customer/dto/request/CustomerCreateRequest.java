package gascolae.group9.package_builder.customer.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerCreateRequest {
    @NotBlank(message = "NOT_NULL")
    String customerCode;

    @NotBlank(message = "NOT_NULL")
    String customerName;

    String companyName;

    String industry;

    @Email(message = "EMAIL_INVALID")
    String contactEmail;

    String contactPhone;
}
