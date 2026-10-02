package gascolae.group9.package_builder.catalog.repository;

import gascolae.group9.package_builder.catalog.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String> {
    Optional<Service> findByServiceCode(String serviceCode);
    boolean existsByServiceCode(String serviceCode);
    List<Service> findByCategory(String category);
    Page<Service> findByActiveTrue(Pageable pageable);

    @Query("SELECT s FROM Service s WHERE s.active = true AND (" +
           "LOWER(s.serviceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Service> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
