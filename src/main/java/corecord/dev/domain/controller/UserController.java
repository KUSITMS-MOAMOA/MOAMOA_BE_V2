package corecord.dev.domain.controller;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.common.status.SuccessStatus;
import corecord.dev.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    @GetMapping()
    public ApiResponse getSuccess() {
        return ApiResponse.SuccessResponse(SuccessStatus.SUCCESS, "OK");
    }

    @GetMapping("/fail")
    public ApiResponse getFailure() {
        return ApiResponse.FailureResponse(ErrorStatus.BAD_REQUEST);
    }

}
