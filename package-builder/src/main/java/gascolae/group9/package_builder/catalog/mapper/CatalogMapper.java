package gascolae.group9.package_builder.catalog.mapper;

import gascolae.group9.package_builder.catalog.dto.request.ServiceCreateRequest;
import gascolae.group9.package_builder.catalog.dto.request.ServiceUpdateRequest;
import gascolae.group9.package_builder.catalog.dto.response.*;
import gascolae.group9.package_builder.catalog.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", constant = "true")
    Service toService(ServiceCreateRequest request);

    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "serviceCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateServiceFromRequest(@MappingTarget Service service, ServiceUpdateRequest request);

    DataItemResponse toDataItemResponse(DataItem dataItem);

    @Mapping(target = "parentTagId", source = "parentTag.tagId")
    @Mapping(target = "parentTagCode", source = "parentTag.tagCode")
    TagResponse toTagResponse(Tag tag);

    ServiceLevelResponse toServiceLevelResponse(ServiceLevel level);

    @Mapping(target = "dataItemId", source = "dataItem.dataItemId")
    @Mapping(target = "dataCode", source = "dataItem.dataCode")
    @Mapping(target = "dataName", source = "dataItem.dataName")
    ServiceInputResponse toServiceInputResponse(ServiceInput input);

    @Mapping(target = "dataItemId", source = "dataItem.dataItemId")
    @Mapping(target = "dataCode", source = "dataItem.dataCode")
    @Mapping(target = "dataName", source = "dataItem.dataName")
    ServiceOutputResponse toServiceOutputResponse(ServiceOutput output);

    @Mapping(target = "dataItems", ignore = true)
    ServiceDeliverableResponse toServiceDeliverableResponse(ServiceDeliverable deliverable);

    @Mapping(target = "sourceServiceId", source = "sourceService.serviceId")
    @Mapping(target = "sourceServiceCode", source = "sourceService.serviceCode")
    @Mapping(target = "sourceServiceName", source = "sourceService.serviceName")
    @Mapping(target = "targetServiceId", source = "targetService.serviceId")
    @Mapping(target = "targetServiceCode", source = "targetService.serviceCode")
    @Mapping(target = "targetServiceName", source = "targetService.serviceName")
    @Mapping(target = "viaDataCode", source = "viaDataItem.dataCode")
    @Mapping(target = "viaDataName", source = "viaDataItem.dataName")
    ServiceRelationResponse toServiceRelationResponse(ServiceRelation relation);

    @Mapping(target = "tags", ignore = true)
    ServiceSummaryResponse toServiceSummaryResponse(Service service);

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "levels", ignore = true)
    @Mapping(target = "inputs", ignore = true)
    @Mapping(target = "outputs", ignore = true)
    @Mapping(target = "deliverables", ignore = true)
    @Mapping(target = "relations", ignore = true)
    ServiceDetailResponse toServiceDetailResponse(Service service);
}
