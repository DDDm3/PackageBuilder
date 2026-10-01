package gascolae.group9.package_builder.customer.repository;

import gascolae.group9.package_builder.customer.entity.RequirementExpectedOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequirementExpectedOutputRepository extends JpaRepository<RequirementExpectedOutput, String> {
    List<RequirementExpectedOutput> findByRequirement_RequirementId(String requirementId);
}
