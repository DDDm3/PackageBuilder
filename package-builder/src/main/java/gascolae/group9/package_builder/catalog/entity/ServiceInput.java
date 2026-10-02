package gascolae.group9.package_builder.catalog.entity;

import gascolae.group9.package_builder.catalog.enums.ProvidedBy;
import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
    name = "service_inputs",
    uniqueConstraints = @UniqueConstraint(name = "uk_service_inputs_service_data", columnNames = {"service_id", "data_item_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceInput {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String serviceInputId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_item_id", nullable = false)
    DataItem dataItem;

    @Column(nullable = false)
    @Builder.Default
    Boolean required = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    ProvidedBy providedBy = ProvidedBy.CUSTOMER;

    @Column(columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    VerificationStatus verificationStatus;

    @Column(columnDefinition = "TEXT")
    String sourceRef;
}
