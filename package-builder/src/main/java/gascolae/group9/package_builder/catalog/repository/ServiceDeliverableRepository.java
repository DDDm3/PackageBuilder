package gascolae.group9.package_builder.catalog.repository;

import gascolae.group9.package_builder.catalog.entity.ServiceDeliverable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceDeliverableRepository extends JpaRepository<ServiceDeliverable, String> {
    List<ServiceDeliverable> findByServiceServiceId(String serviceId);
    Optional<ServiceDeliverable> findByDeliverableCode(String deliverableCode);
}
