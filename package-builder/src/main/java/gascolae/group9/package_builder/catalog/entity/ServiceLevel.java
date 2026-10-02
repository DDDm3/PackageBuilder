package gascolae.group9.package_builder.catalog.entity;

import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
    name = "service_levels",
    uniqueConstraints = @UniqueConstraint(name = "uk_service_levels_service_level", columnNames = {"service_id", "level_no"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String serviceLevelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    Service service;

    @Column(name = "level_no", nullable = false)
    Integer levelNo;

    @Column(nullable = false)
    String levelName;

    @Column(columnDefinition = "TEXT")
    String levelDescription;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    VerificationStatus verificationStatus;

    @Column(columnDefinition = "TEXT")
    String sourceRef;
}
