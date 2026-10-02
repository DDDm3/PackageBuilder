package gascolae.group9.package_builder.catalog.service.serviceImpl;

import gascolae.group9.package_builder.catalog.dto.response.*;
import gascolae.group9.package_builder.catalog.entity.*;
import gascolae.group9.package_builder.catalog.enums.TagType;
import gascolae.group9.package_builder.catalog.mapper.CatalogMapper;
import gascolae.group9.package_builder.catalog.repository.*;
import gascolae.group9.package_builder.catalog.service.CatalogService;
import gascolae.group9.package_builder.exception.AppException;
import gascolae.group9.package_builder.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

    ServiceRepository serviceRepository;
    TagRepository tagRepository;
    DataItemRepository dataItemRepository;
    ServiceLevelRepository serviceLevelRepository;
    ServiceInputRepository serviceInputRepository;
    ServiceOutputRepository serviceOutputRepository;
    ServiceDeliverableRepository serviceDeliverableRepository;
    ServiceDeliverableItemRepository serviceDeliverableItemRepository;
    ServiceTagRepository serviceTagRepository;
    ServiceRelationRepository serviceRelationRepository;
    CatalogMapper catalogMapper;

    @Override
    public Page<ServiceSummaryResponse> getServices(String keyword, String category, String tagCode, Pageable pageable) {
        Page<Service> servicePage;

        if (tagCode != null && !tagCode.trim().isEmpty()) {
            Optional<Tag> tagOpt = tagRepository.findByTagCode(tagCode.trim());
            if (tagOpt.isPresent()) {
                List<ServiceTag> serviceTags = serviceTagRepository.findByTagTagId(tagOpt.get().getTagId());
                List<Service> services = serviceTags.stream()
                        .map(ServiceTag::getService)
                        .filter(s -> Boolean.TRUE.equals(s.getActive()))
                        .filter(s -> category == null || category.trim().isEmpty() || s.getCategory().equalsIgnoreCase(category.trim()))
                        .filter(s -> keyword == null || keyword.trim().isEmpty() || matchesKeyword(s, keyword.trim().toLowerCase()))
                        .toList();

                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), services.size());
                List<Service> subList = (start <= end && start < services.size()) ? services.subList(start, end) : Collections.emptyList();
                servicePage = new PageImpl<>(subList, pageable, services.size());
            } else {
                servicePage = Page.empty(pageable);
            }
        } else if (category != null && !category.trim().isEmpty()) {
            List<Service> services = serviceRepository.findByCategory(category.trim()).stream()
                    .filter(s -> Boolean.TRUE.equals(s.getActive()))
                    .filter(s -> keyword == null || keyword.trim().isEmpty() || matchesKeyword(s, keyword.trim().toLowerCase()))
                    .toList();

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), services.size());
            List<Service> subList = (start <= end && start < services.size()) ? services.subList(start, end) : Collections.emptyList();
            servicePage = new PageImpl<>(subList, pageable, services.size());
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            servicePage = serviceRepository.searchByKeyword(keyword.trim(), pageable);
        } else {
            servicePage = serviceRepository.findByActiveTrue(pageable);
        }

        List<String> serviceIds = servicePage.getContent().stream()
                .map(Service::getServiceId)
                .toList();

        Map<String, List<String>> tagMap = new HashMap<>();
        if (!serviceIds.isEmpty()) {
            List<Object[]> results = serviceTagRepository.findTagCodesByServiceIds(serviceIds);
            for (Object[] row : results) {
                String sId = (String) row[0];
                String code = (String) row[1];
                tagMap.computeIfAbsent(sId, k -> new ArrayList<>()).add(code);
            }
        }

        return servicePage.map(service -> {
            ServiceSummaryResponse response = catalogMapper.toServiceSummaryResponse(service);
            response.setTags(tagMap.getOrDefault(service.getServiceId(), Collections.emptyList()));
            return response;
        });
    }

    private boolean matchesKeyword(Service s, String kw) {
        return (s.getServiceCode() != null && s.getServiceCode().toLowerCase().contains(kw)) ||
               (s.getServiceName() != null && s.getServiceName().toLowerCase().contains(kw)) ||
               (s.getCategory() != null && s.getCategory().toLowerCase().contains(kw)) ||
               (s.getShortDescription() != null && s.getShortDescription().toLowerCase().contains(kw));
    }

    @Override
    public ServiceDetailResponse getServiceDetail(String idOrCode) {
        Service service = serviceRepository.findById(idOrCode)
                .or(() -> serviceRepository.findByServiceCode(idOrCode))
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        ServiceDetailResponse response = catalogMapper.toServiceDetailResponse(service);

        // Tags
        response.setTags(serviceTagRepository.findTagCodesByServiceId(service.getServiceId()));

        // Levels
        List<ServiceLevel> levels = serviceLevelRepository.findByServiceServiceIdOrderByLevelNoAsc(service.getServiceId());
        response.setLevels(levels.stream()
                .map(catalogMapper::toServiceLevelResponse)
                .toList());

        // Inputs
        List<ServiceInput> inputs = serviceInputRepository.findByServiceServiceId(service.getServiceId());
        response.setInputs(inputs.stream()
                .map(catalogMapper::toServiceInputResponse)
                .toList());

        // Outputs
        List<ServiceOutput> outputs = serviceOutputRepository.findByServiceServiceId(service.getServiceId());
        response.setOutputs(outputs.stream()
                .map(catalogMapper::toServiceOutputResponse)
                .toList());

        // Deliverables with items
        List<ServiceDeliverable> deliverables = serviceDeliverableRepository.findByServiceServiceId(service.getServiceId());
        response.setDeliverables(deliverables.stream().map(d -> {
            ServiceDeliverableResponse dRes = catalogMapper.toServiceDeliverableResponse(d);
            List<ServiceDeliverableItem> dItems = serviceDeliverableItemRepository.findByDeliverableServiceDeliverableId(d.getServiceDeliverableId());
            dRes.setDataItems(dItems.stream()
                    .map(di -> catalogMapper.toDataItemResponse(di.getDataItem()))
                    .toList());
            return dRes;
        }).toList());

        // Relations (outgoing & incoming)
        List<ServiceRelation> outgoing = serviceRelationRepository.findBySourceServiceServiceId(service.getServiceId());
        List<ServiceRelation> incoming = serviceRelationRepository.findByTargetServiceServiceId(service.getServiceId());
        Set<ServiceRelation> allRelations = new LinkedHashSet<>(outgoing);
        allRelations.addAll(incoming);
        response.setRelations(allRelations.stream()
                .map(catalogMapper::toServiceRelationResponse)
                .toList());

        return response;
    }

    @Override
    public List<String> getCategories() {
        return serviceRepository.findAll().stream()
                .map(Service::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public Page<DataItemResponse> getDataItems(String keyword, Pageable pageable) {
        Page<DataItem> page;
        if (keyword != null && !keyword.trim().isEmpty()) {
            page = dataItemRepository.findByDataNameContainingIgnoreCaseOrDataCodeContainingIgnoreCase(
                    keyword.trim(), keyword.trim(), pageable);
        } else {
            page = dataItemRepository.findAll(pageable);
        }
        return page.map(catalogMapper::toDataItemResponse);
    }

    @Override
    public List<TagResponse> getTags(TagType type) {
        List<Tag> tags;
        if (type != null) {
            tags = tagRepository.findByTagType(type);
        } else {
            tags = tagRepository.findByActiveTrue();
        }
        return tags.stream()
                .map(catalogMapper::toTagResponse)
                .toList();
    }

    @Override
    @Transactional
    public ServiceDetailResponse createService(gascolae.group9.package_builder.catalog.dto.request.ServiceCreateRequest request) {
        if (serviceRepository.existsByServiceCode(request.getServiceCode())) {
            throw new AppException(ErrorCode.SERVICE_CODE_EXISTED);
        }

        Service service = catalogMapper.toService(request);
        if (service.getLifecycleStatus() == null) {
            service.setLifecycleStatus(gascolae.group9.package_builder.catalog.enums.LifecycleStatus.DRAFT);
        }
        if (service.getVerificationStatus() == null) {
            service.setVerificationStatus(gascolae.group9.package_builder.catalog.enums.VerificationStatus.NEED_VERIFY);
        }
        if (service.getDataClassification() == null) {
            service.setDataClassification(gascolae.group9.package_builder.catalog.enums.DataClassification.INTERNAL);
        }
        service.setActive(true);

        final Service savedService = serviceRepository.save(service);

        if (request.getTagCodes() != null && !request.getTagCodes().isEmpty()) {
            for (String tagCode : request.getTagCodes()) {
                tagRepository.findByTagCode(tagCode.trim()).ifPresent(tag -> {
                    ServiceTag st = ServiceTag.builder()
                            .id(new ServiceTagId(savedService.getServiceId(), tag.getTagId()))
                            .service(savedService)
                            .tag(tag)
                            .build();
                    serviceTagRepository.save(st);
                });
            }
        }

        log.info("Service created successfully: code={}, id={}", savedService.getServiceCode(), savedService.getServiceId());
        return getServiceDetail(savedService.getServiceId());
    }

    @Override
    @Transactional
    public ServiceDetailResponse updateService(String id, gascolae.group9.package_builder.catalog.dto.request.ServiceUpdateRequest request) {
        Service service = serviceRepository.findById(id)
                .or(() -> serviceRepository.findByServiceCode(id))
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        catalogMapper.updateServiceFromRequest(service, request);
        if (request.getActive() != null) {
            service.setActive(request.getActive());
        }

        final Service savedService = serviceRepository.save(service);

        if (request.getTagCodes() != null) {
            List<ServiceTag> existing = serviceTagRepository.findByServiceServiceId(savedService.getServiceId());
            serviceTagRepository.deleteAll(existing);

            for (String tagCode : request.getTagCodes()) {
                tagRepository.findByTagCode(tagCode.trim()).ifPresent(tag -> {
                    ServiceTag st = ServiceTag.builder()
                            .id(new ServiceTagId(savedService.getServiceId(), tag.getTagId()))
                            .service(savedService)
                            .tag(tag)
                            .build();
                    serviceTagRepository.save(st);
                });
            }
        }

        log.info("Service updated successfully: code={}, id={}", savedService.getServiceCode(), savedService.getServiceId());
        return getServiceDetail(savedService.getServiceId());
    }

    @Override
    @Transactional
    public void updateServiceStatus(String id, boolean active) {
        Service service = serviceRepository.findById(id)
                .or(() -> serviceRepository.findByServiceCode(id))
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        service.setActive(active);
        serviceRepository.save(service);
        log.info("Service status updated: code={}, active={}", service.getServiceCode(), active);
    }

    @Override
    @Transactional
    public void deleteService(String id) {
        updateServiceStatus(id, false);
    }
}
