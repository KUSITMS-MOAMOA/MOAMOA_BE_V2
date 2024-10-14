package corecord.dev.domain.controller;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.common.status.SuccessStatus;
import corecord.dev.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    @GetMapping("/success")
    public ResponseEntity<ApiResponse<Void>> getSuccess() {
        return ApiResponse.success(SuccessStatus.OK);
    }

    @GetMapping("/fail")
    public ResponseEntity<ApiResponse<Void>> getFail() {
        return ApiResponse.error(ErrorStatus.BAD_REQUEST);
    }
}
