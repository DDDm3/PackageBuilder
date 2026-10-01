package gascolae.group9.package_builder.customer.service;

import gascolae.group9.package_builder.customer.dto.request.CustomerCreateRequest;
import gascolae.group9.package_builder.customer.dto.request.CustomerUpdateRequest;
import gascolae.group9.package_builder.customer.dto.response.CustomerResponse;
import gascolae.group9.package_builder.customer.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerCreateRequest request);
    CustomerResponse updateCustomer(String customerId, CustomerUpdateRequest request);
    CustomerResponse getCustomerById(String customerId);
    CustomerResponse getCustomerByCode(String customerCode);
    List<CustomerResponse> getAllCustomers();
    Page<CustomerResponse> searchCustomers(String keyword, CustomerStatus status, Pageable pageable);
    void updateCustomerStatus(String customerId, CustomerStatus status);
}
