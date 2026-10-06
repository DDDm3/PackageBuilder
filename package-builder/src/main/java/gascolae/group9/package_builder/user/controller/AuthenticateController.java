package gascolae.group9.package_builder.user.controller;

import com.nimbusds.jose.JOSEException;
import gascolae.group9.package_builder.dto.response.APIResponse;
import gascolae.group9.package_builder.user.dto.request.IntrospectRequest;
import gascolae.group9.package_builder.user.dto.request.LoginRequest;
import gascolae.group9.package_builder.user.dto.request.LogoutRequest;
import gascolae.group9.package_builder.user.dto.response.IntrospectResponse;
import gascolae.group9.package_builder.user.dto.response.LoginResponse;
import gascolae.group9.package_builder.user.service.AuthenticateService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@Slf4j
@RestController
@RequestMapping("/authenticate")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Authentication", description = "Xác thực, đăng nhập JWT, kiểm tra và thu hồi token")
public class AuthenticateController {
    AuthenticateService authenticateService;

    @Operation(summary = "Đăng nhập hệ thống (Lấy JWT Access Token)", description = "Đăng nhập với username và password để nhận Bearer Token.")
    @PostMapping("/login")
    public APIResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request){
        return APIResponse.<LoginResponse>builder()
                .message("Login successful")
                .result(authenticateService.authenticate(request))
                .build();
    }

    @Operation(summary = "Kiểm tra tính hợp lệ của Token (Introspect)", description = "Kiểm tra xem token còn hạn và chưa bị thu hồi (blacklist) hay không.")
    @PostMapping("/introspect")
    public APIResponse<IntrospectResponse> introspect(@RequestBody @Valid IntrospectRequest request) throws ParseException, JOSEException {
        return APIResponse.<IntrospectResponse>builder()
                .message("Introspect successful")
                .result(authenticateService.introspect(request))
                .build();
    }

    @Operation(summary = "Đăng xuất hệ thống (Thu hồi Token)", description = "Đưa JWT Token vào danh sách đen (Blacklist InvalidatedToken) để vô hiệu hóa ngay lập tức.")
    @PostMapping("/logout")
    public APIResponse<Void> logout(@RequestBody @Valid LogoutRequest request) throws ParseException, JOSEException {
        authenticateService.logout(request);
        return APIResponse.<Void>builder()
                .message("Logout successful")
                .build();
    }
}
