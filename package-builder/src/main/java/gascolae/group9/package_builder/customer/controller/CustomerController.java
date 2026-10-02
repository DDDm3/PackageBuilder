package gascolae.group9.package_builder.customer.controller;

import gascolae.group9.package_builder.customer.dto.request.CustomerCreateRequest;
import gascolae.group9.package_builder.customer.dto.request.CustomerUpdateRequest;
import gascolae.group9.package_builder.customer.dto.response.CustomerResponse;
import gascolae.group9.package_builder.customer.enums.CustomerStatus;
import gascolae.group9.package_builder.customer.service.CustomerService;
import gascolae.group9.package_builder.dto.response.APIResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerController {
    CustomerService customerService;

    @PostMapping
    public APIResponse<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.createCustomer(request))
                .build();
    }

    @GetMapping("/{id}")
    public APIResponse<CustomerResponse> getCustomerById(@PathVariable("id") String id) {
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.getCustomerById(id))
                .build();
    }

    @GetMapping("/code/{code}")
    public APIResponse<CustomerResponse> getCustomerByCode(@PathVariable("code") String code) {
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.getCustomerByCode(code))
                .build();
    }

    @GetMapping
    public APIResponse<Page<CustomerResponse>> searchCustomers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) CustomerStatus status,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return APIResponse.<Page<CustomerResponse>>builder()
                .result(customerService.searchCustomers(keyword, status, pageable))
                .build();
    }

    @PutMapping("/{id}")
    public APIResponse<CustomerResponse> updateCustomer(
            @PathVariable("id") String id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.updateCustomer(id, request))
                .build();
    }

    @PutMapping("/{id}/status")
    public APIResponse<Void> updateCustomerStatus(
            @PathVariable("id") String id,
            @RequestParam("status") CustomerStatus status
    ) {
        customerService.updateCustomerStatus(id, status);
        return APIResponse.<Void>builder()
                .message("Cập nhật trạng thái khách hàng thành công")
                .build();
    }
}
