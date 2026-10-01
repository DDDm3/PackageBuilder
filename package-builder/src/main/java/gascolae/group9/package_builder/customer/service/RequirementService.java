package gascolae.group9.package_builder.customer.service;

import gascolae.group9.package_builder.customer.dto.request.RequirementCreateRequest;
import gascolae.group9.package_builder.customer.dto.request.RequirementUpdateRequest;
import gascolae.group9.package_builder.customer.dto.response.RequirementResponse;
import gascolae.group9.package_builder.customer.enums.RequirementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RequirementService {
    RequirementResponse submitLandingLead(gascolae.group9.package_builder.customer.dto.request.LandingLeadRequest request);
    RequirementResponse createRequirement(RequirementCreateRequest request, String currentUsername);
    RequirementResponse updateRequirement(String requirementId, RequirementUpdateRequest request);
    RequirementResponse getRequirementById(String requirementId);
    RequirementResponse getRequirementByCode(String requirementCode);
    List<RequirementResponse> getRequirementsByCustomer(String customerId);
    Page<RequirementResponse> searchRequirements(String customerId, RequirementStatus status, String keyword, Pageable pageable);
    RequirementResponse confirmRequirement(String requirementId, String currentUsername);
    void deleteRequirement(String requirementId);
}
