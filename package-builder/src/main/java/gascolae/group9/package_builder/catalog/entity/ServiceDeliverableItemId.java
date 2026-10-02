package gascolae.group9.package_builder.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceDeliverableItemId implements Serializable {
    @Column(name = "service_deliverable_id")
    String serviceDeliverableId;

    @Column(name = "data_item_id")
    String dataItemId;
}
