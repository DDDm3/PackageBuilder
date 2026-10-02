package gascolae.group9.package_builder.catalog.repository;

import gascolae.group9.package_builder.catalog.entity.DataItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataItemRepository extends JpaRepository<DataItem, String> {
    Optional<DataItem> findByDataCode(String dataCode);
    boolean existsByDataCode(String dataCode);
    Page<DataItem> findByDataNameContainingIgnoreCaseOrDataCodeContainingIgnoreCase(String dataName, String dataCode, Pageable pageable);
}
