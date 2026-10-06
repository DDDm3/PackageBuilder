package gascolae.group9.package_builder.customer.controller;

import gascolae.group9.package_builder.customer.dto.request.RequirementCreateRequest;
import gascolae.group9.package_builder.customer.dto.request.RequirementUpdateRequest;
import gascolae.group9.package_builder.customer.dto.response.RequirementResponse;
import gascolae.group9.package_builder.customer.enums.RequirementStatus;
import gascolae.group9.package_builder.customer.service.RequirementService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/requirements")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Customer Requirement", description = "Quản lý đề bài & nhu cầu dịch vụ của khách hàng")
public class RequirementController {
    RequirementService requirementService;

    @Operation(summary = "Tiếp nhận form tư vấn từ Landing Page (Public)", 
               description = "Khách vãng lai gửi form 9 trường trên Landing Page mà không cần đăng nhập. Tự động liên kết/tạo khách hàng và sinh mã REQ.")
    @PostMapping("/public/lead")
    public APIResponse<RequirementResponse> submitLandingLead(
            @Valid @RequestBody gascolae.group9.package_builder.customer.dto.request.LandingLeadRequest request
    ) {
        return APIResponse.<RequirementResponse>builder()
                .result(requirementService.submitLandingLead(request))
                .build();
    }

    @Operation(summary = "Tạo mới yêu cầu dịch vụ (Sales Admin)", 
               description = "Nhân viên Sales tạo trực tiếp hồ sơ yêu cầu cho khách hàng.")
    @PostMapping
    public APIResponse<RequirementResponse> createRequirement(@Valid @RequestBody RequirementCreateRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return APIResponse.<RequirementResponse>builder()
                .result(requirementService.createRequirement(request, currentUsername))
                .build();
    }

    @Operation(summary = "Xem chi tiết yêu cầu theo ID")
    @GetMapping("/{id}")
    public APIResponse<RequirementResponse> getRequirementById(
            @Parameter(description = "ID của yêu cầu") @PathVariable("id") String id
    ) {
        return APIResponse.<RequirementResponse>builder()
                .result(requirementService.getRequirementById(id))
                .build();
    }

    @Operation(summary = "Xem chi tiết yêu cầu theo Mã Code (VD: REQ-20261005-0001)")
    @GetMapping("/code/{code}")
    public APIResponse<RequirementResponse> getRequirementByCode(
            @Parameter(description = "Mã yêu cầu (Code)") @PathVariable("code") String code
    ) {
        return APIResponse.<RequirementResponse>builder()
                .result(requirementService.getRequirementByCode(code))
                .build();
    }

    @Operation(summary = "Tìm kiếm & lọc danh sách yêu cầu", 
               description = "Tìm kiếm theo từ khóa đa trường (mã yêu cầu, mã dịch vụ S0274, mô tả, khách hàng...), lọc theo serviceId, status, customerId và phân trang.")
    @GetMapping
    public APIResponse<Page<RequirementResponse>> searchRequirements(
            @Parameter(description = "Lọc theo ID khách hàng") @RequestParam(value = "customerId", required = false) String customerId,
            @Parameter(description = "Lọc theo mã dịch vụ (VD: S0274)") @RequestParam(value = "serviceId", required = false) String serviceId,
            @Parameter(description = "Lọc theo trạng thái (DRAFT, CONFIRMED, ARCHIVED)") @RequestParam(value = "status", required = false) RequirementStatus status,
            @Parameter(description = "Từ khóa tìm kiếm (mã, tên dự án, dịch vụ, sđt, email, địa bàn, mô tả...)") @RequestParam(value = "keyword", required = false) String keyword,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return APIResponse.<Page<RequirementResponse>>builder()
                .result(requirementService.searchRequirements(customerId, serviceId, status, keyword, pageable))
                .build();
    }

    @Operation(summary = "Lấy danh sách yêu cầu của một khách hàng")
    @GetMapping("/customer/{customerId}")
    public APIResponse<List<RequirementResponse>> getRequirementsByCustomer(
            @Parameter(description = "ID khách hàng") @PathVariable("customerId") String customerId
    ) {
        return APIResponse.<List<RequirementResponse>>builder()
                .result(requirementService.getRequirementsByCustomer(customerId))
                .build();
    }

    @Operation(summary = "Cập nhật yêu cầu dịch vụ (Sales Admin)", 
               description = "Chỉnh sửa thông tin đề bài, cập nhật cấp độ level, diện tích, danh sách deliverables mong đợi... Không ghi đè null lên các trường chưa thay đổi.")
    @PutMapping("/{id}")
    public APIResponse<RequirementResponse> updateRequirement(
            @Parameter(description = "ID yêu cầu cần sửa") @PathVariable("id") String id,
            @Valid @RequestBody RequirementUpdateRequest request
    ) {
        return APIResponse.<RequirementResponse>builder()
                .result(requirementService.updateRequirement(id, request))
                .build();
    }

    @Operation(summary = "Xác nhận chốt yêu cầu (DRAFT -> CONFIRMED)", 
               description = "Chốt đề bài để chuyển sang bước chạy Động cơ gợi ý & Xây dựng gói giải pháp.")
    @PutMapping("/{id}/confirm")
    public APIResponse<RequirementResponse> confirmRequirement(
            @Parameter(description = "ID yêu cầu cần chốt") @PathVariable("id") String id
    ) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return APIResponse.<RequirementResponse>builder()
                .result(requirementService.confirmRequirement(id, currentUsername))
                .build();
    }

    @Operation(summary = "Xóa yêu cầu dịch vụ")
    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteRequirement(
            @Parameter(description = "ID yêu cầu cần xóa") @PathVariable("id") String id
    ) {
        requirementService.deleteRequirement(id);
        return APIResponse.<Void>builder()
                .message("Xóa yêu cầu thành công")
                .build();
    }
}
