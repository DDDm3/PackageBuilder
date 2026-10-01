package gascolae.group9.package_builder.customer.repository;

import gascolae.group9.package_builder.customer.entity.Customer;
import gascolae.group9.package_builder.customer.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    boolean existsByCustomerCode(String customerCode);
    boolean existsByContactEmail(String contactEmail);
    boolean existsByContactPhone(String contactPhone);
    Optional<Customer> findByCustomerCode(String customerCode);
    Optional<Customer> findByContactEmail(String contactEmail);
    Optional<Customer> findByContactPhone(String contactPhone);

    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE " +
            "(:keyword IS NULL OR (" +
            "LOWER(c.customerCode) LIKE :keyword OR " +
            "LOWER(c.customerName) LIKE :keyword OR " +
            "LOWER(c.companyName) LIKE :keyword OR " +
            "LOWER(c.contactEmail) LIKE :keyword OR " +
            "LOWER(c.contactPhone) LIKE :keyword OR " +
            "LOWER(c.industry) LIKE :keyword)) AND " +
            "(:status IS NULL OR c.status = :status)")
    Page<Customer> searchCustomers(
            @Param("keyword") String keyword,
            @Param("status") CustomerStatus status,
            Pageable pageable
    );
}
