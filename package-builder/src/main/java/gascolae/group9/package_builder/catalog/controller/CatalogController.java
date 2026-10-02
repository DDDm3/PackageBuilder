package gascolae.group9.package_builder.catalog.controller;

import gascolae.group9.package_builder.catalog.dto.request.ServiceCreateRequest;
import gascolae.group9.package_builder.catalog.dto.request.ServiceUpdateRequest;
import gascolae.group9.package_builder.catalog.dto.response.*;
import gascolae.group9.package_builder.catalog.enums.TagType;
import gascolae.group9.package_builder.catalog.service.CatalogService;
import gascolae.group9.package_builder.dto.response.APIResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CatalogController {

    CatalogService catalogService;

    @GetMapping("/services")
    public APIResponse<Page<ServiceSummaryResponse>> getServices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tagCode,
            @ParameterObject @PageableDefault(size = 20, sort = "serviceCode", direction = Sort.Direction.ASC) Pageable pageable) {
        return APIResponse.<Page<ServiceSummaryResponse>>builder()
                .result(catalogService.getServices(keyword, category, tagCode, pageable))
                .build();
    }

    @GetMapping("/services/{idOrCode}")
    public APIResponse<ServiceDetailResponse> getServiceDetail(@PathVariable String idOrCode) {
        return APIResponse.<ServiceDetailResponse>builder()
                .result(catalogService.getServiceDetail(idOrCode))
                .build();
    }

    @PostMapping("/services")
    public APIResponse<ServiceDetailResponse> createService(@Valid @RequestBody ServiceCreateRequest request) {
        return APIResponse.<ServiceDetailResponse>builder()
                .result(catalogService.createService(request))
                .message("Tạo dịch vụ thành công")
                .build();
    }

    @PutMapping("/services/{id}")
    public APIResponse<ServiceDetailResponse> updateService(
            @PathVariable String id,
            @Valid @RequestBody ServiceUpdateRequest request) {
        return APIResponse.<ServiceDetailResponse>builder()
                .result(catalogService.updateService(id, request))
                .message("Cập nhật thông tin dịch vụ thành công")
                .build();
    }

    @PutMapping("/services/{id}/status")
    public APIResponse<Void> updateServiceStatus(
            @PathVariable String id,
            @RequestParam boolean active) {
        catalogService.updateServiceStatus(id, active);
        return APIResponse.<Void>builder()
                .message("Cập nhật trạng thái dịch vụ thành công")
                .build();
    }

    @DeleteMapping("/services/{id}")
    public APIResponse<Void> deleteService(@PathVariable String id) {
        catalogService.deleteService(id);
        return APIResponse.<Void>builder()
                .message("Đã tạm dừng / xóa dịch vụ")
                .build();
    }

    @GetMapping("/categories")
    public APIResponse<List<String>> getCategories() {
        return APIResponse.<List<String>>builder()
                .result(catalogService.getCategories())
                .build();
    }

    @GetMapping("/data-items")
    public APIResponse<Page<DataItemResponse>> getDataItems(
            @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(size = 50, sort = "dataCode", direction = Sort.Direction.ASC) Pageable pageable) {
        return APIResponse.<Page<DataItemResponse>>builder()
                .result(catalogService.getDataItems(keyword, pageable))
                .build();
    }

    @GetMapping("/tags")
    public APIResponse<List<TagResponse>> getTags(
            @RequestParam(required = false) TagType type) {
        return APIResponse.<List<TagResponse>>builder()
                .result(catalogService.getTags(type))
                .build();
    }
}
