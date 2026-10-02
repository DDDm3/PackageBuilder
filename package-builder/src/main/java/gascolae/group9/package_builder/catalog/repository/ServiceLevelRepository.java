package gascolae.group9.package_builder.catalog.repository;

import gascolae.group9.package_builder.catalog.entity.ServiceLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceLevelRepository extends JpaRepository<ServiceLevel, String> {
    List<ServiceLevel> findByServiceServiceIdOrderByLevelNoAsc(String serviceId);
    Optional<ServiceLevel> findByServiceServiceIdAndLevelNo(String serviceId, Integer levelNo);
}
