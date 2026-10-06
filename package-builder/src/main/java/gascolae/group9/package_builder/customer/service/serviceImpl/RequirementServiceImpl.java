package gascolae.group9.package_builder.customer.service.serviceImpl;

import gascolae.group9.package_builder.customer.dto.request.ExpectedOutputRequest;
import gascolae.group9.package_builder.customer.dto.request.LandingLeadRequest;
import gascolae.group9.package_builder.customer.dto.request.RequirementCreateRequest;
import gascolae.group9.package_builder.customer.dto.request.RequirementUpdateRequest;
import gascolae.group9.package_builder.customer.dto.response.RequirementResponse;
import gascolae.group9.package_builder.customer.entity.Customer;
import gascolae.group9.package_builder.customer.entity.CustomerRequirement;
import gascolae.group9.package_builder.customer.entity.RequirementExpectedOutput;
import gascolae.group9.package_builder.customer.enums.CustomerStatus;
import gascolae.group9.package_builder.customer.enums.OutputPriority;
import gascolae.group9.package_builder.customer.enums.RequirementStatus;
import gascolae.group9.package_builder.customer.mapper.RequirementMapper;
import gascolae.group9.package_builder.customer.repository.CustomerRepository;
import gascolae.group9.package_builder.customer.repository.CustomerRequirementRepository;
import gascolae.group9.package_builder.customer.service.RequirementService;
import gascolae.group9.package_builder.exception.AppException;
import gascolae.group9.package_builder.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RequirementServiceImpl implements RequirementService {
    CustomerRequirementRepository requirementRepository;
    CustomerRepository customerRepository;
    RequirementMapper requirementMapper;

    @Override
    @Transactional
    public RequirementResponse submitLandingLead(LandingLeadRequest request) {
        boolean hasEmail = StringUtils.hasText(request.getContactEmail());
        boolean hasPhone = StringUtils.hasText(request.getContactPhone());

        if (!hasEmail && !hasPhone) {
            throw new AppException(ErrorCode.CONTACT_INFO_REQUIRED);
        }

        // 1. Tìm hoặc tạo Customer
        Customer customer = null;
        if (hasEmail) {
            customer = customerRepository.findByContactEmail(request.getContactEmail().trim()).orElse(null);
        }
        if (customer == null && hasPhone) {
            customer = customerRepository.findByContactPhone(request.getContactPhone().trim()).orElse(null);
        }

        if (customer == null) {
            String customerCode = generateCustomerCode();
            String customerName = StringUtils.hasText(request.getCustomerName()) ? request.getCustomerName().trim() : "Khách hàng mới";
            customer = Customer.builder()
                    .customerCode(customerCode)
                    .customerName(customerName)
                    .companyName(StringUtils.hasText(request.getCompanyName()) ? request.getCompanyName().trim() : null)
                    .contactEmail(hasEmail ? request.getContactEmail().trim() : null)
                    .contactPhone(hasPhone ? request.getContactPhone().trim() : null)
                    .industry(StringUtils.hasText(request.getIndustryRaw()) ? request.getIndustryRaw().trim() : null)
                    .status(CustomerStatus.ACTIVE)
                    .build();
            customer = customerRepository.save(customer);
            log.info("Auto-created new customer from landing lead: {}", customer.getCustomerCode());
        } else {
            // Cập nhật thông tin công ty / liên hệ nếu khách hàng cũ chưa có
            if (!StringUtils.hasText(customer.getCompanyName()) && StringUtils.hasText(request.getCompanyName())) {
                customer.setCompanyName(request.getCompanyName().trim());
            }
            if (!StringUtils.hasText(customer.getContactPhone()) && hasPhone) {
                customer.setContactPhone(request.getContactPhone().trim());
            }
            if (!StringUtils.hasText(customer.getContactEmail()) && hasEmail) {
                customer.setContactEmail(request.getContactEmail().trim());
            }
            customerRepository.save(customer);
        }

        // 2. Tạo CustomerRequirement
        CustomerRequirement requirement = requirementMapper.toRequirementFromLead(request);
        requirement.setCustomer(customer);
        requirement.setRequirementCode(generateRequirementCode());

        if (!StringUtils.hasText(requirement.getProjectName())) {
            String displayOrg = StringUtils.hasText(customer.getCompanyName()) ? customer.getCompanyName() : customer.getCustomerName();
            requirement.setProjectName("Khảo sát nhu cầu - " + displayOrg);
        }

        requirement.setStatus(RequirementStatus.DRAFT);
        requirement.setCreatedBy("LANDING_PAGE_GUEST");

        // 3. Xử lý Expected Outputs từ text list (rawExpectedOutputs)
        if (!CollectionUtils.isEmpty(request.getRawExpectedOutputs())) {
            for (String rawText : request.getRawExpectedOutputs()) {
                if (StringUtils.hasText(rawText)) {
                    RequirementExpectedOutput output = RequirementExpectedOutput.builder()
                            .rawExpectedOutput(rawText.trim())
                            .priority(OutputPriority.REQUIRED)
                            .build();
                    requirement.addExpectedOutput(output);
                }
            }
        }

        // Xử lý Expected Outputs (hỗ trợ cả text list, chuỗi đa dòng, mảng object hoặc chuỗi rỗng)
        List<gascolae.group9.package_builder.customer.dto.request.ExpectedOutputRequest> expectedOutputsList = request.getExpectedOutputsAsList();
        if (!CollectionUtils.isEmpty(expectedOutputsList)) {
            for (gascolae.group9.package_builder.customer.dto.request.ExpectedOutputRequest outputReq : expectedOutputsList) {
                RequirementExpectedOutput output = requirementMapper.toExpectedOutput(outputReq);
                requirement.addExpectedOutput(output);
            }
        }

        CustomerRequirement saved = requirementRepository.save(requirement);
        log.info("Created requirement: {} from landing lead for customer: {}", saved.getRequirementCode(), customer.getCustomerCode());
        return requirementMapper.toRequirementResponse(saved);
    }

    @Override
    @Transactional
    public RequirementResponse createRequirement(RequirementCreateRequest request, String currentUsername) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        String code = request.getRequirementCode();
        if (!StringUtils.hasText(code)) {
            code = generateRequirementCode();
        } else if (requirementRepository.existsByRequirementCode(code)) {
            throw new AppException(ErrorCode.REQUIREMENT_CODE_EXISTED);
        }

        CustomerRequirement requirement = requirementMapper.toRequirement(request);
        requirement.setRequirementCode(code);
        requirement.setCustomer(customer);
        requirement.setStatus(RequirementStatus.DRAFT);
        requirement.setCreatedBy(currentUsername);

        List<ExpectedOutputRequest> createOutputsList = request.getExpectedOutputsAsList();
        if (!CollectionUtils.isEmpty(createOutputsList)) {
            createOutputsList.forEach(outputRequest -> {
                RequirementExpectedOutput output = requirementMapper.toExpectedOutput(outputRequest);
                requirement.addExpectedOutput(output);
            });
        }

        CustomerRequirement saved = requirementRepository.save(requirement);
        log.info("Created requirement: {} for customer: {}", saved.getRequirementCode(), customer.getCustomerCode());
        return requirementMapper.toRequirementResponse(saved);
    }

    @Override
    @Transactional
    public RequirementResponse updateRequirement(String requirementId, RequirementUpdateRequest request) {
        CustomerRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUIREMENT_NOT_FOUND));

        if (requirement.getStatus() == RequirementStatus.CONFIRMED) {
            throw new AppException(ErrorCode.REQUIREMENT_ALREADY_CONFIRMED);
        }

        // Cho phép Sales sửa thông tin doanh nghiệp / khách hàng ngay tại màn hình B1
        if (StringUtils.hasText(request.getCustomerName()) || StringUtils.hasText(request.getCompanyName())) {
            Customer customer = requirement.getCustomer();
            if (customer != null) {
                if (StringUtils.hasText(request.getCustomerName())) {
                    customer.setCustomerName(request.getCustomerName().trim());
                }
                if (StringUtils.hasText(request.getCompanyName())) {
                    customer.setCompanyName(request.getCompanyName().trim());
                }
                customerRepository.save(customer);
            }
        }

        requirementMapper.updateRequirementFromRequest(request, requirement);

        if (request.getExpectedOutputs() != null) {
            requirement.getExpectedOutputs().clear();
            request.getExpectedOutputsAsList().forEach(outputRequest -> {
                RequirementExpectedOutput output = requirementMapper.toExpectedOutput(outputRequest);
                requirement.addExpectedOutput(output);
            });
        }

        CustomerRequirement updated = requirementRepository.save(requirement);
        log.info("Updated requirement: {}", requirementId);
        return requirementMapper.toRequirementResponse(updated);
    }

    private String generateCustomerCode() {
        String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String code;
        do {
            int random = java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 9999);
            code = "CUST-" + datePart + "-" + random;
        } while (customerRepository.existsByCustomerCode(code));
        return code;
    }

    private String generateRequirementCode() {
        String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String code;
        do {
            int random = java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 9999);
            code = "REQ-" + datePart + "-" + random;
        } while (requirementRepository.existsByRequirementCode(code));
        return code;
    }

    @Override
    public RequirementResponse getRequirementById(String requirementId) {
        CustomerRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUIREMENT_NOT_FOUND));
        return requirementMapper.toRequirementResponse(requirement);
    }

    @Override
    public RequirementResponse getRequirementByCode(String requirementCode) {
        CustomerRequirement requirement = requirementRepository.findByRequirementCode(requirementCode)
                .orElseThrow(() -> new AppException(ErrorCode.REQUIREMENT_NOT_FOUND));
        return requirementMapper.toRequirementResponse(requirement);
    }

    @Override
    public List<RequirementResponse> getRequirementsByCustomer(String customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new AppException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        return requirementMapper.toRequirementResponseList(requirementRepository.findByCustomer_CustomerId(customerId));
    }

    @Override
    public Page<RequirementResponse> searchRequirements(String customerId, RequirementStatus status, String keyword, Pageable pageable) {
        return searchRequirements(customerId, null, status, keyword, pageable);
    }

    @Override
    public Page<RequirementResponse> searchRequirements(String customerId, String serviceId, RequirementStatus status, String keyword, Pageable pageable) {
        boolean hasCustomerId = StringUtils.hasText(customerId);
        boolean hasServiceId = StringUtils.hasText(serviceId);
        boolean hasKeyword = StringUtils.hasText(keyword);

        // Trường hợp 1: Không có bộ lọc nào -> findAll trực tiếp (nhanh nhất, tránh lỗi lower(bytea))
        if (!hasCustomerId && !hasServiceId && status == null && !hasKeyword) {
            return requirementRepository.findAll(pageable).map(requirementMapper::toRequirementResponse);
        }

        // Trường hợp 2: Chỉ lọc theo status (VD: status=DRAFT)
        if (!hasCustomerId && !hasServiceId && status != null && !hasKeyword) {
            return requirementRepository.findByStatus(status, pageable).map(requirementMapper::toRequirementResponse);
        }

        // Trường hợp 3: Chỉ lọc theo customerId
        if (hasCustomerId && !hasServiceId && status == null && !hasKeyword) {
            return requirementRepository.findByCustomer_CustomerId(customerId.trim(), pageable).map(requirementMapper::toRequirementResponse);
        }

        // Trường hợp 4: Có keyword hoặc kết hợp -> bọc % và lowercase ở tầng Java
        String pattern = hasKeyword ? "%" + keyword.trim().toLowerCase() + "%" : null;
        String trimmedCustomerId = hasCustomerId ? customerId.trim() : null;
        String trimmedServiceId = hasServiceId ? serviceId.trim() : null;

        Page<CustomerRequirement> page = requirementRepository.searchRequirements(trimmedCustomerId, trimmedServiceId, status, pattern, pageable);
        return page.map(requirementMapper::toRequirementResponse);
    }

    @Override
    @Transactional
    public RequirementResponse confirmRequirement(String requirementId, String currentUsername) {
        CustomerRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUIREMENT_NOT_FOUND));

        requirement.setStatus(RequirementStatus.CONFIRMED);
        requirement.setConfirmedBy(currentUsername);
        requirement.setConfirmedAt(LocalDateTime.now());

        CustomerRequirement saved = requirementRepository.save(requirement);
        log.info("Confirmed requirement: {} by {}", saved.getRequirementCode(), currentUsername);
        return requirementMapper.toRequirementResponse(saved);
    }

    @Override
    @Transactional
    public void deleteRequirement(String requirementId) {
        CustomerRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUIREMENT_NOT_FOUND));

        if (requirement.getStatus() == RequirementStatus.CONFIRMED) {
            throw new AppException(ErrorCode.REQUIREMENT_ALREADY_CONFIRMED);
        }

        requirementRepository.delete(requirement);
        log.info("Deleted requirement: {}", requirementId);
    }
}
