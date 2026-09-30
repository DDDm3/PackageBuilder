package gascolae.group9.package_builder.customer.service.serviceImpl;

import gascolae.group9.package_builder.customer.dto.request.CustomerCreateRequest;
import gascolae.group9.package_builder.customer.dto.request.CustomerUpdateRequest;
import gascolae.group9.package_builder.customer.dto.response.CustomerResponse;
import gascolae.group9.package_builder.customer.entity.Customer;
import gascolae.group9.package_builder.customer.enums.CustomerStatus;
import gascolae.group9.package_builder.customer.mapper.CustomerMapper;
import gascolae.group9.package_builder.customer.repository.CustomerRepository;
import gascolae.group9.package_builder.customer.service.CustomerService;
import gascolae.group9.package_builder.exception.AppException;
import gascolae.group9.package_builder.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    CustomerRepository customerRepository;
    CustomerMapper customerMapper;

    @Override
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        if (customerRepository.existsByCustomerCode(request.getCustomerCode())) {
            throw new AppException(ErrorCode.CUSTOMER_CODE_EXISTED);
        }

        if (StringUtils.hasText(request.getContactEmail()) &&
                customerRepository.existsByContactEmail(request.getContactEmail())) {
            throw new AppException(ErrorCode.CUSTOMER_EMAIL_EXISTED);
        }

        Customer customer = customerMapper.toCustomer(request);
        customer.setStatus(CustomerStatus.ACTIVE);

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Created customer with code: {}", savedCustomer.getCustomerCode());
        return customerMapper.toCustomerResponse(savedCustomer);
    }

    @Override
    public CustomerResponse updateCustomer(String customerId, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        if (StringUtils.hasText(request.getContactEmail()) &&
                !request.getContactEmail().equalsIgnoreCase(customer.getContactEmail()) &&
                customerRepository.existsByContactEmail(request.getContactEmail())) {
            throw new AppException(ErrorCode.CUSTOMER_EMAIL_EXISTED);
        }

        customerMapper.updateCustomerFromRequest(request, customer);
        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Updated customer: {}", customerId);
        return customerMapper.toCustomerResponse(updatedCustomer);
    }

    @Override
    public CustomerResponse getCustomerById(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        return customerMapper.toCustomerResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByCode(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        return customerMapper.toCustomerResponse(customer);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        return customerMapper.toCustomerResponseList(customerRepository.findAll());
    }

    @Override
    public Page<CustomerResponse> searchCustomers(String keyword, CustomerStatus status, Pageable pageable) {
        String trimmedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        Page<Customer> customers = customerRepository.searchCustomers(trimmedKeyword, status, pageable);
        return customers.map(customerMapper::toCustomerResponse);
    }

    @Override
    public void updateCustomerStatus(String customerId, CustomerStatus status) {
        if (status == null) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        customer.setStatus(status);
        customerRepository.save(customer);
        log.info("Updated customer {} status to {}", customerId, status);
    }
}
