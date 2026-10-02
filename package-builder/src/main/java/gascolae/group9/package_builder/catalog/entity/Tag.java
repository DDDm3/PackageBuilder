package gascolae.group9.package_builder.catalog.entity;

import gascolae.group9.package_builder.catalog.enums.TagType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "tags")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String tagId;

    @Column(nullable = false, unique = true, length = 100)
    String tagCode;

    @Column(nullable = false, unique = true, length = 150)
    String tagName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    TagType tagType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_tag_id")
    Tag parentTag;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(columnDefinition = "TEXT")
    String sourceRef;

    @Column(nullable = false)
    @Builder.Default
    Boolean active = true;
}
