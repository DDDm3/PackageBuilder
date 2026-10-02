package gascolae.group9.package_builder.catalog.repository;

import gascolae.group9.package_builder.catalog.entity.ServiceRelation;
import gascolae.group9.package_builder.catalog.enums.RelationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRelationRepository extends JpaRepository<ServiceRelation, String> {
    List<ServiceRelation> findBySourceServiceServiceId(String sourceServiceId);
    List<ServiceRelation> findByTargetServiceServiceId(String targetServiceId);
    List<ServiceRelation> findByRelationType(RelationType relationType);
    List<ServiceRelation> findBySourceServiceServiceIdAndRelationType(String sourceServiceId, RelationType relationType);
}
