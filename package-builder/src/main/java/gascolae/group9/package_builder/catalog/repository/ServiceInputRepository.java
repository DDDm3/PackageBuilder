package gascolae.group9.package_builder.catalog.repository;

import gascolae.group9.package_builder.catalog.entity.ServiceInput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceInputRepository extends JpaRepository<ServiceInput, String> {
    List<ServiceInput> findByServiceServiceId(String serviceId);
    List<ServiceInput> findByServiceServiceIdAndRequiredTrue(String serviceId);
}
