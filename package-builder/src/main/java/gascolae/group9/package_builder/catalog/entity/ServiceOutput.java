package gascolae.group9.package_builder.catalog.entity;

import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
    name = "service_outputs",
    uniqueConstraints = @UniqueConstraint(name = "uk_service_outputs_service_data", columnNames = {"service_id", "data_item_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceOutput {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String serviceOutputId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_item_id", nullable = false)
    DataItem dataItem;

    @Column(columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    VerificationStatus verificationStatus;

    @Column(columnDefinition = "TEXT")
    String sourceRef;
}
