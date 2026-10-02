package gascolae.group9.package_builder.catalog.entity;

import gascolae.group9.package_builder.catalog.enums.DataClassification;
import gascolae.group9.package_builder.catalog.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class DataItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String dataItemId;

    @Column(nullable = false, unique = true, length = 100)
    String dataCode;

    @Column(nullable = false)
    String dataName;

    @Column(columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    DataClassification classification = DataClassification.INTERNAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    VerificationStatus verificationStatus = VerificationStatus.NEED_VERIFY;

    @Column(columnDefinition = "TEXT")
    String sourceRef;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;
}
