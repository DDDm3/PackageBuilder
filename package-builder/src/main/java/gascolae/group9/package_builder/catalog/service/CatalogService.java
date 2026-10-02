package gascolae.group9.package_builder.catalog.service;

import gascolae.group9.package_builder.catalog.dto.request.ServiceCreateRequest;
import gascolae.group9.package_builder.catalog.dto.request.ServiceUpdateRequest;
import gascolae.group9.package_builder.catalog.dto.response.*;
import gascolae.group9.package_builder.catalog.enums.TagType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CatalogService {
    Page<ServiceSummaryResponse> getServices(String keyword, String category, String tagCode, Pageable pageable);
    ServiceDetailResponse getServiceDetail(String idOrCode);
    List<String> getCategories();
    Page<DataItemResponse> getDataItems(String keyword, Pageable pageable);
    List<TagResponse> getTags(TagType type);

    ServiceDetailResponse createService(ServiceCreateRequest request);
    ServiceDetailResponse updateService(String id, ServiceUpdateRequest request);
    void updateServiceStatus(String id, boolean active);
    void deleteService(String id);
}
