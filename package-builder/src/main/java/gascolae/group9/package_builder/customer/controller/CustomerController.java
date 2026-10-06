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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Customer", description = "Quản lý hồ sơ khách hàng doanh nghiệp")
public class CustomerController {
    CustomerService customerService;

    @Operation(summary = "Tạo mới khách hàng")
    @PostMapping
    public APIResponse<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.createCustomer(request))
                .build();
    }

    @Operation(summary = "Xem thông tin khách hàng theo ID")
    @GetMapping("/{id}")
    public APIResponse<CustomerResponse> getCustomerById(
            @Parameter(description = "ID khách hàng") @PathVariable("id") String id
    ) {
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.getCustomerById(id))
                .build();
    }

    @Operation(summary = "Xem thông tin khách hàng theo Mã Code (VD: CUST-20261005-0001)")
    @GetMapping("/code/{code}")
    public APIResponse<CustomerResponse> getCustomerByCode(
            @Parameter(description = "Mã khách hàng") @PathVariable("code") String code
    ) {
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.getCustomerByCode(code))
                .build();
    }

    @Operation(summary = "Tìm kiếm & phân trang danh sách khách hàng", 
               description = "Tìm kiếm theo từ khóa (tên, công ty, email, phone) và lọc theo trạng thái (ACTIVE, INACTIVE).")
    @GetMapping
    public APIResponse<Page<CustomerResponse>> searchCustomers(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "Trạng thái khách hàng") @RequestParam(value = "status", required = false) CustomerStatus status,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return APIResponse.<Page<CustomerResponse>>builder()
                .result(customerService.searchCustomers(keyword, status, pageable))
                .build();
    }

    @Operation(summary = "Cập nhật thông tin khách hàng")
    @PutMapping("/{id}")
    public APIResponse<CustomerResponse> updateCustomer(
            @Parameter(description = "ID khách hàng") @PathVariable("id") String id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.updateCustomer(id, request))
                .build();
    }

    @Operation(summary = "Cập nhật trạng thái hoạt động khách hàng (ACTIVE / INACTIVE)")
    @PutMapping("/{id}/status")
    public APIResponse<Void> updateCustomerStatus(
            @Parameter(description = "ID khách hàng") @PathVariable("id") String id,
            @Parameter(description = "Trạng thái mới") @RequestParam("status") CustomerStatus status
    ) {
        customerService.updateCustomerStatus(id, status);
        return APIResponse.<Void>builder()
                .message("Cập nhật trạng thái khách hàng thành công")
                .build();
    }
}
