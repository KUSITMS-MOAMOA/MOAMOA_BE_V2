package corecord.dev.domain.ability.presentation;

import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.web.UserId;
import corecord.dev.domain.ability.status.AbilitySuccessStatus;
import corecord.dev.domain.ability.domain.dto.response.AbilityResponse;
import corecord.dev.domain.ability.application.AbilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keyword")
public class AbilityController {
    private final AbilityService abilityService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<AbilityResponse.KeywordListDto>> getKeywordList(
            @UserId Long userId
    ) {
        AbilityResponse.KeywordListDto response = abilityService.getKeywordList(userId);
        return ApiResponse.success(AbilitySuccessStatus.KEYWORD_LIST_GET_SUCCESS, response);
    }

    @GetMapping("/graph")
    public ResponseEntity<ApiResponse<AbilityResponse.GraphDto>> getKeywordGraph(
            @UserId Long userId
    ) {
        AbilityResponse.GraphDto response = abilityService.getKeywordGraph(userId);
        return ApiResponse.success(AbilitySuccessStatus.KEYWORD_GRAPH_GET_SUCCESS, response);
    }

}
