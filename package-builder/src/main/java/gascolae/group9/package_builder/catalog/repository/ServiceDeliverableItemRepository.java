package gascolae.group9.package_builder.catalog.repository;

import gascolae.group9.package_builder.catalog.entity.ServiceDeliverableItem;
import gascolae.group9.package_builder.catalog.entity.ServiceDeliverableItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceDeliverableItemRepository extends JpaRepository<ServiceDeliverableItem, ServiceDeliverableItemId> {
    List<ServiceDeliverableItem> findByDeliverableServiceDeliverableId(String serviceDeliverableId);
    List<ServiceDeliverableItem> findByDataItemDataItemId(String dataItemId);
}
