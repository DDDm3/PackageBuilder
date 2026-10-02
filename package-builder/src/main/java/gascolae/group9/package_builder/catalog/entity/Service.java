package gascolae.group9.package_builder.catalog.entity;

import gascolae.group9.package_builder.catalog.enums.DataClassification;
import gascolae.group9.package_builder.catalog.enums.LifecycleStatus;
import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "services")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String serviceId;

    @Column(nullable = false, unique = true, length = 50)
    String serviceCode;

    @Column(nullable = false)
    String serviceName;

    String serviceNameEn;

    @Column(nullable = false, length = 100)
    String category;

    @Column(length = 100)
    String sectorCode;

    @Column(length = 100)
    String subsectorCode;

    @Column(columnDefinition = "TEXT")
    String shortDescription;

    @Column(columnDefinition = "TEXT")
    String targetCustomer;

    @Column(columnDefinition = "TEXT")
    String customerProblems;

    @Column(columnDefinition = "TEXT")
    String useCases;

    @Column(columnDefinition = "TEXT")
    String technologies;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    LifecycleStatus lifecycleStatus = LifecycleStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    VerificationStatus verificationStatus = VerificationStatus.NEED_VERIFY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    DataClassification dataClassification = DataClassification.INTERNAL;

    @Column(columnDefinition = "TEXT")
    String sourceRef;

    @Column(nullable = false)
    @Builder.Default
    Boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    LocalDateTime updatedAt;
}
