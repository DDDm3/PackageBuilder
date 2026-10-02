package gascolae.group9.package_builder.catalog.entity;

import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
    name = "service_deliverables",
    uniqueConstraints = @UniqueConstraint(name = "uk_service_deliverables_service_name", columnNames = {"service_id", "deliverable_name"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceDeliverable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String serviceDeliverableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    Service service;

    @Column(length = 100)
    String deliverableCode;

    @Column(nullable = false)
    String deliverableName;

    @Column(columnDefinition = "TEXT")
    String description;

    Integer minLevelNo;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    VerificationStatus verificationStatus;

    @Column(columnDefinition = "TEXT")
    String sourceRef;
}
