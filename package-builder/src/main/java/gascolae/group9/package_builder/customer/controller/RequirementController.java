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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/requirements")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequirementController {
    RequirementService requirementService;

    @PostMapping("/public/lead")
    public APIResponse<RequirementResponse> submitLandingLead(
            @Valid @RequestBody gascolae.group9.package_builder.customer.dto.request.LandingLeadRequest request
    ) {
        return APIResponse.<RequirementResponse>builder()
                .result(requirementService.submitLandingLead(request))
                .build();
    }

    @PostMapping
    public APIResponse<RequirementResponse> createRequirement(@Valid @RequestBody RequirementCreateRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return APIResponse.<RequirementResponse>builder()
                .result(requirementService.createRequirement(request, currentUsername))
                .build();
    }

    @GetMapping("/{id}")
    public APIResponse<RequirementResponse> getRequirementById(@PathVariable("id") String id) {
        return APIResponse.<RequirementResponse>builder()
                .result(requirementService.getRequirementById(id))
                .build();
    }

    @GetMapping("/code/{code}")
    public APIResponse<RequirementResponse> getRequirementByCode(@PathVariable("code") String code) {
        return APIResponse.<RequirementResponse>builder()
                .result(requirementService.getRequirementByCode(code))
                .build();
    }

    @GetMapping
    public APIResponse<Page<RequirementResponse>> searchRequirements(
            @RequestParam(value = "customerId", required = false) String customerId,
            @RequestParam(value = "serviceId", required = false) String serviceId,
            @RequestParam(value = "status", required = false) RequirementStatus status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return APIResponse.<Page<RequirementResponse>>builder()
                .result(requirementService.searchRequirements(customerId, serviceId, status, keyword, pageable))
                .build();
    }

    @GetMapping("/customer/{customerId}")
    public APIResponse<List<RequirementResponse>> getRequirementsByCustomer(@PathVariable("customerId") String customerId) {
        return APIResponse.<List<RequirementResponse>>builder()
                .result(requirementService.getRequirementsByCustomer(customerId))
                .build();
    }

    @PutMapping("/{id}")
    public APIResponse<RequirementResponse> updateRequirement(
            @PathVariable("id") String id,
            @Valid @RequestBody RequirementUpdateRequest request
    ) {
        return APIResponse.<RequirementResponse>builder()
                .result(requirementService.updateRequirement(id, request))
                .build();
    }

    @PutMapping("/{id}/confirm")
    public APIResponse<RequirementResponse> confirmRequirement(@PathVariable("id") String id) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return APIResponse.<RequirementResponse>builder()
                .result(requirementService.confirmRequirement(id, currentUsername))
                .build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteRequirement(@PathVariable("id") String id) {
        requirementService.deleteRequirement(id);
        return APIResponse.<Void>builder()
                .message("Xóa yêu cầu thành công")
                .build();
    }
}
