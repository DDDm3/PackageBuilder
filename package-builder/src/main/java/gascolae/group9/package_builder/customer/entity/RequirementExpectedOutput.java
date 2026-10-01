package gascolae.group9.package_builder.customer.entity;

import gascolae.group9.package_builder.customer.enums.OutputPriority;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "requirement_expected_outputs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class RequirementExpectedOutput {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String requirementExpectedOutputId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_id", nullable = false)
    CustomerRequirement requirement;

    String dataItemId;

    @Column(nullable = false)
    String rawExpectedOutput;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    OutputPriority priority;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;
}
