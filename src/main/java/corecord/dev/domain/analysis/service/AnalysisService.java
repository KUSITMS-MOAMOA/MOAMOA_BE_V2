package corecord.dev.domain.analysis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.domain.analysis.constant.Keyword;
import corecord.dev.domain.analysis.converter.AnalysisConverter;
import corecord.dev.domain.analysis.dto.request.AnalysisRequest;
import corecord.dev.domain.analysis.dto.response.AnalysisAiResponse;
import corecord.dev.domain.analysis.dto.response.AnalysisResponse;
import corecord.dev.domain.analysis.entity.Ability;
import corecord.dev.domain.analysis.entity.Analysis;
import corecord.dev.domain.analysis.exception.enums.AnalysisErrorStatus;
import corecord.dev.domain.analysis.exception.model.AnalysisException;
import corecord.dev.domain.analysis.repository.AbilityRepository;
import corecord.dev.domain.analysis.repository.AnalysisRepository;
import corecord.dev.common.util.ClovaRequest;
import corecord.dev.common.util.ClovaService;
import corecord.dev.domain.record.entity.Record;
import corecord.dev.domain.record.exception.enums.RecordErrorStatus;
import corecord.dev.domain.record.exception.model.RecordException;
import corecord.dev.domain.user.entity.User;
import corecord.dev.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final AnalysisRepository analysisRepository;
    private final AbilityRepository abilityRepository;
    private final UserRepository userRepository;
    private final ClovaService clovaService;


    @Transactional
    public void createAnalysis(Record record, User user) {
        // TODO: MEMO라면 CLOVA STUDIO를 이용해 content 요약

        // CLOVA STUDIO API 호출
        String apiResponse = generateAbilityAnalysis(record.getContent());
        AnalysisAiResponse response = parseAnalysisAiResponse(apiResponse);

        // Analysis 객체 생성 및 저장
        Analysis analysis = AnalysisConverter.toAnalysis(record.getContent(), response.getComment(), record);
        analysisRepository.save(analysis);

        // Ability 객체 생성 및 저장
        int abilityCount = 0;
        for (Map.Entry<String, String> entry : response.getKeywordList().entrySet()) {
            Keyword keyword = Keyword.getName(entry.getKey());

            if (keyword == null) continue;

            Ability ability = AnalysisConverter.toAbility(keyword, entry.getValue(), analysis, user);
            abilityRepository.save(ability);
            abilityCount++;
        }

        if (abilityCount < 1 || abilityCount > 3) {
            throw new AnalysisException(AnalysisErrorStatus.INVALID_ABILITY_ANALYSIS);
        }

    }

    /*
     * analysisId를 받아 경험 분석 상세 정보를 반환
     * @param userId, analysisId
     * @return
     */
    @Transactional(readOnly = true)
    public AnalysisResponse.AnalysisDto getAnalysis(Long userId, Long analysisId) {
        User user = findUserById(userId);
        Analysis analysis = findAnalysisById(analysisId);

        // User-Analysis 권한 유효성 검증
        validIsUserAuthorizedForAnalysis(user, analysis);

        return AnalysisConverter.toAnalysisDto(analysis);
    }

    /*
     * 역량 분석의 기록 내용 혹은 각 키워드에 대한 내용을 수정한 후 수정된 역량 분석 정보를 반환
     * @param userId, analysisUpdateDto
     * @return
     */
    @Transactional
    public AnalysisResponse.AnalysisDto updateAnalysis(Long userId, AnalysisRequest.AnalysisUpdateDto analysisUpdateDto) {
        User user = findUserById(userId);
        Analysis analysis = findAnalysisById(analysisUpdateDto.getAnalysisId());

        // User-Analysis 권한 유효성 검증
        validIsUserAuthorizedForAnalysis(user, analysis);

        // 경험 기록 제목 수정
        String title = analysisUpdateDto.getTitle();
        analysis.getRecord().updateTitle(title);

        // 경험 역량 분석 요약 내용 수정
        String content = analysisUpdateDto.getContent();
        analysis.updateContent(content);

        // 키워드 경험 내용 수정
        Map<String, String> abilityMap = analysisUpdateDto.getAbilityMap();
        updateAbilityContents(analysis, abilityMap);

        return AnalysisConverter.toAnalysisDto(analysis);
    }

    /*
     * analysisId를 받아 역량 분석, 경험 기록 데이터를 제거
     * @param userId, analysisId
     */
    @Transactional
    public void deleteAnalysis(Long userId, Long analysisId) {
        User user = findUserById(userId);
        Analysis analysis = findAnalysisById(analysisId);

        // User-Analysis 권한 유효성 검증
        validIsUserAuthorizedForAnalysis(user, analysis);

        analysisRepository.delete(analysis);
    }

    /*
     * user의 역량 키워드 리스트를 반환
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public AnalysisResponse.KeywordListDto getKeywordList(Long userId) {
        User user = findUserById(userId);
        List<String> keywordList = findKeywordList(user);

        return AnalysisConverter.toKeywordListDto(keywordList);
    }

    /*
     * user의 각 역량 키워드에 대한 개수, 퍼센티지 정보를 반환
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public AnalysisResponse.GraphDto getKeywordGraph(Long userId) {
        User user = findUserById(userId);

        // keyword graph 정보 조회
        List<AnalysisResponse.KeywordStateDto> keywordGraph = findKeywordGraph(user);

        return AnalysisConverter.toGraphDto(keywordGraph);
    }
  
  private String generateAbilityAnalysis(String content) {
        ClovaRequest clovaRequest = ClovaRequest.createAnalysisRequest(content);
        return clovaService.generateAiResponse(clovaRequest);
    }

    private void validIsUserAuthorizedForAnalysis(User user, Analysis analysis) {
        if (!analysis.getRecord().getUser().equals(user))
            throw new RecordException(RecordErrorStatus.USER_RECORD_UNAUTHORIZED);
    }

    private void updateAbilityContents(Analysis analysis, Map<String, String> abilityMap) {
        abilityMap.forEach((keyword, content) -> {
            Keyword key = Keyword.getName(keyword);
            Ability ability = findAbilityByKeyword(analysis, key);
            ability.updateContent(content);
        });
    }

    private Ability findAbilityByKeyword(Analysis analysis, Keyword key) {
        // keyword가 기존 역량 분석에 존재했는지 확인
        return analysis.getAbilityList().stream()
                .filter(ability -> ability.getKeyword().equals(key))
                .findFirst()
                .orElseThrow(() -> new AnalysisException(AnalysisErrorStatus.INVALID_KEYWORD));
    }

    private AnalysisAiResponse parseAnalysisAiResponse(String aiResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(aiResponse, AnalysisAiResponse.class);
        } catch (JsonProcessingException e) {
            throw new AnalysisException(AnalysisErrorStatus.INVALID_ABILITY_ANALYSIS);
        }
    }

    private List<String> findKeywordList(User user) {
        return analysisRepository.getKeywordList(user).stream()
                .map(Keyword::getValue)
                .toList();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.UNAUTHORIZED));
    }

    private Analysis findAnalysisById(Long analysisId) {
        return analysisRepository.findAnalysisById(analysisId)
                .orElseThrow(() -> new AnalysisException(AnalysisErrorStatus.ANALYSIS_NOT_FOUND));
    }

    private List<AnalysisResponse.KeywordStateDto> findKeywordGraph(User user) {
        return abilityRepository.findKeywordStateDtoList(user);
    }
}
