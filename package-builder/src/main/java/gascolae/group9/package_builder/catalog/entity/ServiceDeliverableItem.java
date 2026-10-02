package gascolae.group9.package_builder.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "service_deliverable_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceDeliverableItem {
    @EmbeddedId
    ServiceDeliverableItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("serviceDeliverableId")
    @JoinColumn(name = "service_deliverable_id", nullable = false)
    ServiceDeliverable deliverable;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("dataItemId")
    @JoinColumn(name = "data_item_id", nullable = false)
    DataItem dataItem;
}
