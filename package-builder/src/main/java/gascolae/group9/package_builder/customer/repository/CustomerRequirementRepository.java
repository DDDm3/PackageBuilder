package gascolae.group9.package_builder.customer.repository;

import gascolae.group9.package_builder.customer.entity.CustomerRequirement;
import gascolae.group9.package_builder.customer.enums.RequirementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRequirementRepository extends JpaRepository<CustomerRequirement, String> {
    boolean existsByRequirementCode(String requirementCode);
    Optional<CustomerRequirement> findByRequirementCode(String requirementCode);
    List<CustomerRequirement> findByCustomer_CustomerId(String customerId);
    Page<CustomerRequirement> findByStatus(RequirementStatus status, Pageable pageable);
    Page<CustomerRequirement> findByCustomer_CustomerId(String customerId, Pageable pageable);

    @Query("SELECT r FROM CustomerRequirement r WHERE " +
            "(:customerId IS NULL OR r.customer.customerId = :customerId) AND " +
            "(:serviceId IS NULL OR LOWER(r.serviceId) = LOWER(:serviceId)) AND " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:keyword IS NULL OR (" +
            "LOWER(r.requirementCode) LIKE :keyword OR " +
            "LOWER(r.serviceId) LIKE :keyword OR " +
            "LOWER(r.projectName) LIKE :keyword OR " +
            "LOWER(r.customer.customerName) LIKE :keyword OR " +
            "LOWER(r.customer.companyName) LIKE :keyword OR " +
            "LOWER(r.customer.contactEmail) LIKE :keyword OR " +
            "LOWER(r.customer.contactPhone) LIKE :keyword OR " +
            "LOWER(r.locationDescription) LIKE :keyword OR " +
            "LOWER(r.rawRequirementText) LIKE :keyword OR " +
            "LOWER(r.industryRaw) LIKE :keyword OR " +
            "LOWER(r.objectiveRaw) LIKE :keyword))")
    Page<CustomerRequirement> searchRequirements(
            @Param("customerId") String customerId,
            @Param("serviceId") String serviceId,
            @Param("status") RequirementStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
