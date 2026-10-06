package gascolae.group9.package_builder.user.controller;

import gascolae.group9.package_builder.dto.response.APIResponse;
import gascolae.group9.package_builder.user.dto.request.RoleCreateRequest;
import gascolae.group9.package_builder.user.dto.response.RoleResponse;
import gascolae.group9.package_builder.user.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Role Management", description = "Quản lý vai trò & quyền hạn người dùng")
public class RoleController {
    RoleService roleService;

    @Operation(summary = "Tạo mới vai trò (Role)")
    @PostMapping("/create")
    public APIResponse<RoleResponse> createRole(@RequestBody @Valid RoleCreateRequest request){
        return APIResponse.<RoleResponse>builder()
                .result(roleService.createRole(request))
                .build();
    }

    @Operation(summary = "Danh sách vai trò trong hệ thống")
    @GetMapping
    public APIResponse<List<RoleResponse>> getAllRole(){
        return APIResponse.<List<RoleResponse>>builder()
                .result(roleService.getAllRole())
                .build();
    }
}
