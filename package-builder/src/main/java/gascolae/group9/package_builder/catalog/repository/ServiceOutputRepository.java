package gascolae.group9.package_builder.catalog.repository;

import gascolae.group9.package_builder.catalog.entity.ServiceOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOutputRepository extends JpaRepository<ServiceOutput, String> {
    List<ServiceOutput> findByServiceServiceId(String serviceId);
}
