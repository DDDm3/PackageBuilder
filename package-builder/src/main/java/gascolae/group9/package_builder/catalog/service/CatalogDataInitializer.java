package gascolae.group9.package_builder.catalog.service;

import gascolae.group9.package_builder.catalog.entity.*;
import gascolae.group9.package_builder.catalog.enums.*;
import gascolae.group9.package_builder.catalog.repository.*;
import gascolae.group9.package_builder.catalog.util.SimpleCsvParser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

@Component
@Order(2)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CatalogDataInitializer implements ApplicationRunner {

    DataItemRepository dataItemRepository;
    TagRepository tagRepository;
    ServiceRepository serviceRepository;
    ServiceLevelRepository serviceLevelRepository;
    ServiceInputRepository serviceInputRepository;
    ServiceOutputRepository serviceOutputRepository;
    ServiceDeliverableRepository serviceDeliverableRepository;
    ServiceDeliverableItemRepository serviceDeliverableItemRepository;
    ServiceTagRepository serviceTagRepository;
    ServiceRelationRepository serviceRelationRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (serviceRepository.count() > 0 && dataItemRepository.count() > 0) {
            log.info("Catalog Master Data already initialized (services: {}, data_items: {}), skipping seed.",
                    serviceRepository.count(), dataItemRepository.count());
            return;
        }

        log.info("Starting Catalog Master Data seeding from seed CSV files...");
        try {
            seedAll();
            log.info("Catalog Master Data seeding completed successfully!");
        } catch (Exception e) {
            log.error("Failed to seed Catalog Master Data: {}", e.getMessage(), e);
        }
    }

    public void seedAll() throws Exception {
        // 1. data_items
        Map<String, DataItem> dataItemMap = seedDataItems();

        // 2. tags
        Map<String, Tag> tagMap = seedTags();

        // 3. services
        Map<String, Service> serviceMap = seedServices();

        // 4. service_levels
        seedServiceLevels(serviceMap);

        // 5. service_inputs
        seedServiceInputs(serviceMap, dataItemMap);

        // 6. service_outputs
        seedServiceOutputs(serviceMap, dataItemMap);

        // 7. service_deliverables
        Map<String, ServiceDeliverable> deliverableMap = seedServiceDeliverables(serviceMap);

        // 8. service_deliverable_items
        seedServiceDeliverableItems(deliverableMap, dataItemMap);

        // 9. service_tags
        seedServiceTags(serviceMap, tagMap);

        // 10. service_relations
        seedServiceRelations(serviceMap, dataItemMap);
    }

    private List<Map<String, String>> readCsv(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return SimpleCsvParser.parse(is);
        }
    }

    private Map<String, DataItem> seedDataItems() throws Exception {
        List<Map<String, String>> rows = readCsv("seed/data_items.csv");
        Map<String, DataItem> map = new HashMap<>();
        List<DataItem> entities = new ArrayList<>();

        for (Map<String, String> row : rows) {
            String code = row.get("data_code");
            DataItem item = DataItem.builder()
                    .dataCode(code)
                    .dataName(row.get("data_name"))
                    .description(clean(row.get("description")))
                    .classification(parseEnum(DataClassification.class, row.get("classification"), DataClassification.INTERNAL))
                    .verificationStatus(parseEnum(VerificationStatus.class, row.get("verification_status"), VerificationStatus.NEED_VERIFY))
                    .sourceRef(clean(row.get("source_ref")))
                    .build();
            entities.add(item);
            map.put(code, item);
        }
        dataItemRepository.saveAll(entities);
        log.info("Seeded {} data_items", entities.size());
        return map;
    }

    private Map<String, Tag> seedTags() throws Exception {
        List<Map<String, String>> rows = readCsv("seed/tags.csv");
        Map<String, Tag> map = new HashMap<>();
        List<Tag> entities = new ArrayList<>();

        // Pass 1: create all tags without parent
        for (Map<String, String> row : rows) {
            String code = row.get("tag_code");
            Tag tag = Tag.builder()
                    .tagCode(code)
                    .tagName(row.get("tag_name"))
                    .tagType(parseEnum(TagType.class, row.get("tag_type"), TagType.OBJECTIVE))
                    .description(clean(row.get("description")))
                    .sourceRef(clean(row.get("source_ref")))
                    .active("true".equalsIgnoreCase(row.get("active")))
                    .build();
            entities.add(tag);
            map.put(code, tag);
        }
        tagRepository.saveAll(entities);

        // Pass 2: wire parent tags
        List<Tag> updated = new ArrayList<>();
        for (Map<String, String> row : rows) {
            String code = row.get("tag_code");
            String parentCode = clean(row.get("parent_tag_code"));
            if (parentCode != null && map.containsKey(parentCode)) {
                Tag tag = map.get(code);
                tag.setParentTag(map.get(parentCode));
                updated.add(tag);
            }
        }
        if (!updated.isEmpty()) {
            tagRepository.saveAll(updated);
        }
        log.info("Seeded {} tags (with {} parent associations)", entities.size(), updated.size());
        return map;
    }

    private Map<String, Service> seedServices() throws Exception {
        List<Map<String, String>> rows = readCsv("seed/services.csv");
        Map<String, Service> map = new HashMap<>();
        List<Service> entities = new ArrayList<>();

        for (Map<String, String> row : rows) {
            String code = row.get("service_code");
            Service s = Service.builder()
                    .serviceCode(code)
                    .serviceName(row.get("service_name"))
                    .serviceNameEn(clean(row.get("service_name_en")))
                    .category(row.get("category"))
                    .sectorCode(clean(row.get("sector_code")))
                    .subsectorCode(clean(row.get("subsector_code")))
                    .shortDescription(clean(row.get("short_description")))
                    .targetCustomer(clean(row.get("target_customer")))
                    .customerProblems(clean(row.get("customer_problems")))
                    .useCases(clean(row.get("use_cases")))
                    .technologies(clean(row.get("technologies")))
                    .lifecycleStatus(parseEnum(LifecycleStatus.class, row.get("lifecycle_status"), LifecycleStatus.DRAFT))
                    .verificationStatus(parseEnum(VerificationStatus.class, row.get("verification_status"), VerificationStatus.NEED_VERIFY))
                    .dataClassification(parseEnum(DataClassification.class, row.get("data_classification"), DataClassification.INTERNAL))
                    .sourceRef(clean(row.get("source_ref")))
                    .active("true".equalsIgnoreCase(row.get("active")))
                    .build();
            entities.add(s);
            map.put(code, s);
        }
        serviceRepository.saveAll(entities);
        log.info("Seeded {} services", entities.size());
        return map;
    }

    private void seedServiceLevels(Map<String, Service> serviceMap) throws Exception {
        List<Map<String, String>> rows = readCsv("seed/service_levels.csv");
        List<ServiceLevel> entities = new ArrayList<>();

        for (Map<String, String> row : rows) {
            Service s = serviceMap.get(row.get("service_code"));
            if (s == null) continue;

            ServiceLevel level = ServiceLevel.builder()
                    .service(s)
                    .levelNo(Integer.parseInt(row.get("level_no")))
                    .levelName(row.get("level_name"))
                    .levelDescription(clean(row.get("level_description")))
                    .verificationStatus(parseEnum(VerificationStatus.class, row.get("verification_status"), null))
                    .sourceRef(clean(row.get("source_ref")))
                    .build();
            entities.add(level);
        }
        serviceLevelRepository.saveAll(entities);
        log.info("Seeded {} service_levels", entities.size());
    }

    private void seedServiceInputs(Map<String, Service> serviceMap, Map<String, DataItem> dataItemMap) throws Exception {
        List<Map<String, String>> rows = readCsv("seed/service_inputs.csv");
        List<ServiceInput> entities = new ArrayList<>();

        for (Map<String, String> row : rows) {
            Service s = serviceMap.get(row.get("service_code"));
            DataItem d = dataItemMap.get(row.get("data_code"));
            if (s == null || d == null) continue;

            ServiceInput input = ServiceInput.builder()
                    .service(s)
                    .dataItem(d)
                    .required("true".equalsIgnoreCase(row.get("required")))
                    .providedBy(parseEnum(ProvidedBy.class, row.get("provided_by"), ProvidedBy.CUSTOMER))
                    .description(clean(row.get("description")))
                    .verificationStatus(parseEnum(VerificationStatus.class, row.get("verification_status"), null))
                    .sourceRef(clean(row.get("source_ref")))
                    .build();
            entities.add(input);
        }
        serviceInputRepository.saveAll(entities);
        log.info("Seeded {} service_inputs", entities.size());
    }

    private void seedServiceOutputs(Map<String, Service> serviceMap, Map<String, DataItem> dataItemMap) throws Exception {
        List<Map<String, String>> rows = readCsv("seed/service_outputs.csv");
        List<ServiceOutput> entities = new ArrayList<>();

        for (Map<String, String> row : rows) {
            Service s = serviceMap.get(row.get("service_code"));
            DataItem d = dataItemMap.get(row.get("data_code"));
            if (s == null || d == null) continue;

            ServiceOutput output = ServiceOutput.builder()
                    .service(s)
                    .dataItem(d)
                    .description(clean(row.get("description")))
                    .verificationStatus(parseEnum(VerificationStatus.class, row.get("verification_status"), null))
                    .sourceRef(clean(row.get("source_ref")))
                    .build();
            entities.add(output);
        }
        serviceOutputRepository.saveAll(entities);
        log.info("Seeded {} service_outputs", entities.size());
    }

    private Map<String, ServiceDeliverable> seedServiceDeliverables(Map<String, Service> serviceMap) throws Exception {
        List<Map<String, String>> rows = readCsv("seed/service_deliverables.csv");
        Map<String, ServiceDeliverable> map = new HashMap<>();
        List<ServiceDeliverable> entities = new ArrayList<>();

        for (Map<String, String> row : rows) {
            Service s = serviceMap.get(row.get("service_code"));
            if (s == null) continue;

            String code = clean(row.get("deliverable_code"));
            String minLevelStr = clean(row.get("min_level_no"));
            Integer minLevel = minLevelStr != null ? Integer.parseInt(minLevelStr) : null;

            ServiceDeliverable d = ServiceDeliverable.builder()
                    .service(s)
                    .deliverableCode(code)
                    .deliverableName(row.get("deliverable_name"))
                    .description(clean(row.get("description")))
                    .minLevelNo(minLevel)
                    .verificationStatus(parseEnum(VerificationStatus.class, row.get("verification_status"), null))
                    .sourceRef(clean(row.get("source_ref")))
                    .build();
            entities.add(d);
            if (code != null) {
                map.put(code, d);
            }
        }
        serviceDeliverableRepository.saveAll(entities);
        log.info("Seeded {} service_deliverables", entities.size());
        return map;
    }

    private void seedServiceDeliverableItems(Map<String, ServiceDeliverable> deliverableMap, Map<String, DataItem> dataItemMap) throws Exception {
        List<Map<String, String>> rows = readCsv("seed/service_deliverable_items.csv");
        List<ServiceDeliverableItem> entities = new ArrayList<>();

        for (Map<String, String> row : rows) {
            ServiceDeliverable del = deliverableMap.get(row.get("deliverable_code"));
            DataItem data = dataItemMap.get(row.get("data_code"));
            if (del == null || data == null) continue;

            ServiceDeliverableItem item = ServiceDeliverableItem.builder()
                    .id(new ServiceDeliverableItemId(del.getServiceDeliverableId(), data.getDataItemId()))
                    .deliverable(del)
                    .dataItem(data)
                    .build();
            entities.add(item);
        }
        serviceDeliverableItemRepository.saveAll(entities);
        log.info("Seeded {} service_deliverable_items", entities.size());
    }

    private void seedServiceTags(Map<String, Service> serviceMap, Map<String, Tag> tagMap) throws Exception {
        List<Map<String, String>> rows = readCsv("seed/service_tags.csv");
        List<ServiceTag> entities = new ArrayList<>();

        for (Map<String, String> row : rows) {
            Service s = serviceMap.get(row.get("service_code"));
            Tag t = tagMap.get(row.get("tag_code"));
            if (s == null || t == null) continue;

            ServiceTag st = ServiceTag.builder()
                    .id(new ServiceTagId(s.getServiceId(), t.getTagId()))
                    .service(s)
                    .tag(t)
                    .build();
            entities.add(st);
        }
        serviceTagRepository.saveAll(entities);
        log.info("Seeded {} service_tags", entities.size());
    }

    private void seedServiceRelations(Map<String, Service> serviceMap, Map<String, DataItem> dataItemMap) throws Exception {
        List<Map<String, String>> rows = readCsv("seed/service_relations.csv");
        List<ServiceRelation> entities = new ArrayList<>();

        for (Map<String, String> row : rows) {
            Service src = serviceMap.get(row.get("source_service_code"));
            Service tgt = serviceMap.get(row.get("target_service_code"));
            if (src == null || tgt == null) continue;

            String viaCode = clean(row.get("via_data_code"));
            DataItem viaItem = viaCode != null ? dataItemMap.get(viaCode) : null;

            ServiceRelation rel = ServiceRelation.builder()
                    .sourceService(src)
                    .targetService(tgt)
                    .relationType(parseEnum(RelationType.class, row.get("relation_type"), RelationType.RECOMMENDED_WITH))
                    .viaDataItem(viaItem)
                    .isRequired("true".equalsIgnoreCase(row.get("is_required")))
                    .description(clean(row.get("description")))
                    .verificationStatus(parseEnum(VerificationStatus.class, row.get("verification_status"), null))
                    .sourceRef(clean(row.get("source_ref")))
                    .build();
            entities.add(rel);
        }
        serviceRelationRepository.saveAll(entities);
        log.info("Seeded {} service_relations", entities.size());
    }

    private String clean(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        return str.trim();
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String val, E defaultVal) {
        if (val == null || val.trim().isEmpty()) {
            return defaultVal;
        }
        try {
            return Enum.valueOf(enumClass, val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown enum value '{}' for {}, using default {}", val, enumClass.getSimpleName(), defaultVal);
            return defaultVal;
        }
    }
}
