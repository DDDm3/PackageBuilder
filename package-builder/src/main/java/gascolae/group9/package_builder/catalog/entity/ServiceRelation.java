package gascolae.group9.package_builder.catalog.entity;

import gascolae.group9.package_builder.catalog.enums.RelationType;
import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "service_relations",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_service_relations_source_target_type_item",
        columnNames = {"source_service_id", "target_service_id", "relation_type", "via_data_item_id"}
    )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ServiceRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String relationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_service_id", nullable = false)
    Service sourceService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_service_id", nullable = false)
    Service targetService;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    RelationType relationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "via_data_item_id")
    DataItem viaDataItem;

    @Column(nullable = false)
    @Builder.Default
    Boolean isRequired = false;

    @Column(columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    VerificationStatus verificationStatus;

    @Column(columnDefinition = "TEXT")
    String sourceRef;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;
}
