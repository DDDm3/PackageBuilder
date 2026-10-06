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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Service Catalog", description = "Kho dịch vụ kỹ thuật số, dữ liệu Master Data & Taxonomy của GASCOLAE")
public class CatalogController {

    CatalogService catalogService;

    @Operation(summary = "Danh sách dịch vụ Catalog (Public)", 
               description = "Tìm kiếm và phân trang danh sách dịch vụ. Hỗ trợ lọc theo từ khóa, danh mục (category) hoặc mã tag.")
    @GetMapping("/services")
    public APIResponse<Page<ServiceSummaryResponse>> getServices(
            @Parameter(description = "Từ khóa tìm kiếm theo tên hoặc mã dịch vụ") @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo danh mục dịch vụ") @RequestParam(required = false) String category,
            @Parameter(description = "Lọc theo mã tag (VD: TAG-GHG, TAG-FOREST)") @RequestParam(required = false) String tagCode,
            @ParameterObject @PageableDefault(size = 20, sort = "serviceCode", direction = Sort.Direction.ASC) Pageable pageable) {
        return APIResponse.<Page<ServiceSummaryResponse>>builder()
                .result(catalogService.getServices(keyword, category, tagCode, pageable))
                .build();
    }

    @Operation(summary = "Chi tiết dịch vụ Catalog (Public)", 
               description = "Xem chi tiết dịch vụ bao gồm: inputs, outputs, deliverables bàn giao, các cấp độ level 1-2-3, tags và quan hệ liên kết.")
    @GetMapping("/services/{idOrCode}")
    public APIResponse<ServiceDetailResponse> getServiceDetail(
            @Parameter(description = "ID hoặc Mã dịch vụ (VD: S0274)") @PathVariable String idOrCode) {
        return APIResponse.<ServiceDetailResponse>builder()
                .result(catalogService.getServiceDetail(idOrCode))
                .build();
    }

    @Operation(summary = "Tạo mới dịch vụ (Admin)", description = "Thêm một dịch vụ mới vào danh mục Master Data.")
    @PostMapping("/services")
    public APIResponse<ServiceDetailResponse> createService(@Valid @RequestBody ServiceCreateRequest request) {
        return APIResponse.<ServiceDetailResponse>builder()
                .result(catalogService.createService(request))
                .message("Tạo dịch vụ thành công")
                .build();
    }

    @Operation(summary = "Cập nhật thông tin dịch vụ (Admin)")
    @PutMapping("/services/{id}")
    public APIResponse<ServiceDetailResponse> updateService(
            @Parameter(description = "ID dịch vụ cần cập nhật") @PathVariable String id,
            @Valid @RequestBody ServiceUpdateRequest request) {
        return APIResponse.<ServiceDetailResponse>builder()
                .result(catalogService.updateService(id, request))
                .message("Cập nhật thông tin dịch vụ thành công")
                .build();
    }

    @Operation(summary = "Bật/Tắt trạng thái hoạt động dịch vụ (Admin)")
    @PutMapping("/services/{id}/status")
    public APIResponse<Void> updateServiceStatus(
            @Parameter(description = "ID dịch vụ") @PathVariable String id,
            @Parameter(description = "Trạng thái hoạt động (true/false)") @RequestParam boolean active) {
        catalogService.updateServiceStatus(id, active);
        return APIResponse.<Void>builder()
                .message("Cập nhật trạng thái dịch vụ thành công")
                .build();
    }

    @Operation(summary = "Tạm dừng / Xóa mềm dịch vụ (Admin)")
    @DeleteMapping("/services/{id}")
    public APIResponse<Void> deleteService(
            @Parameter(description = "ID dịch vụ") @PathVariable String id) {
        catalogService.deleteService(id);
        return APIResponse.<Void>builder()
                .message("Đã tạm dừng / xóa dịch vụ")
                .build();
    }

    @Operation(summary = "Danh sách danh mục dịch vụ (Categories)", description = "Lấy tất cả các nhóm/ngành danh mục có trong hệ thống.")
    @GetMapping("/categories")
    public APIResponse<List<String>> getCategories() {
        return APIResponse.<List<String>>builder()
                .result(catalogService.getCategories())
                .build();
    }

    @Operation(summary = "Danh sách từ điển dữ liệu (Taxonomy Data Items)", 
               description = "Tra cứu 91 loại dữ liệu đầu vào/đầu ra chuẩn hóa trong hệ thống.")
    @GetMapping("/data-items")
    public APIResponse<Page<DataItemResponse>> getDataItems(
            @Parameter(description = "Từ khóa tìm kiếm theo tên hoặc mã data item") @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(size = 50, sort = "dataCode", direction = Sort.Direction.ASC) Pageable pageable) {
        return APIResponse.<Page<DataItemResponse>>builder()
                .result(catalogService.getDataItems(keyword, pageable))
                .build();
    }

    @Operation(summary = "Danh sách thẻ phân loại (Tags)", description = "Tra cứu danh mục thẻ phân loại theo loại tag (OBJECTIVE, INDUSTRY, ENVIRONMENT, TOPIC).")
    @GetMapping("/tags")
    public APIResponse<List<TagResponse>> getTags(
            @Parameter(description = "Loại tag cần lọc") @RequestParam(required = false) TagType type) {
        return APIResponse.<List<TagResponse>>builder()
                .result(catalogService.getTags(type))
                .build();
    }
}
