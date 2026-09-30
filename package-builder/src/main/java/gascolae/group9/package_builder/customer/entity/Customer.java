package gascolae.group9.package_builder.customer.entity;

import gascolae.group9.package_builder.customer.enums.CustomerStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String customerId;

    @Column(nullable = false, unique = true, length = 50)
    String customerCode;

    @Column(nullable = false)
    String customerName;

    String companyName;

    @Column(length = 100)
    String industry;

    String contactEmail;

    @Column(length = 50)
    String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    CustomerStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    LocalDateTime updatedAt;
}
