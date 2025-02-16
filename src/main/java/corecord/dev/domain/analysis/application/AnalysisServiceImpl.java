package corecord.dev.domain.analysis.application;

import corecord.dev.domain.ability.application.AbilityService;
import corecord.dev.domain.analysis.domain.converter.AnalysisConverter;
import corecord.dev.domain.analysis.domain.dto.request.AnalysisRequest;
import corecord.dev.domain.analysis.domain.dto.response.AnalysisResponse;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.analysis.exception.AnalysisException;
import corecord.dev.domain.analysis.infra.openai.dto.response.AnalysisAiResponse;
import corecord.dev.domain.analysis.status.AnalysisErrorStatus;
import corecord.dev.domain.record.application.RecordDbService;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.record.exception.RecordException;
import corecord.dev.domain.record.status.RecordErrorStatus;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {
    private final AnalysisAIService analysisAIService;
    private final AbilityService abilityService;
    private final AnalysisDbService analysisDbService;
    private final UserDbService userDbService;
    private final RecordDbService recordDbService;

    /**
     * recordId를 받아, 해당 경험 기록에 대한 역량 분석을 수행 후 생성된 역량 분석 상세 정보를 반환합니다.
     *
     * @param userId
     * @param recordId
     * @return 분석된 역량 키워드, 코멘트, 기록, 기록 수단 정보를 반환
     */
    @Override
    public AnalysisResponse.AnalysisDto postAnalysis(Long userId, Long recordId) {
        User user = userDbService.findUserById(userId);
        Record record = recordDbService.findRecordById(recordId);

        // User-Record 권한 유효성 검증
        validIsUserAuthorizedForRecord(user, record);

        // 역량 분석 API 호출
        Analysis analysis = createAnalysis(record, user);

        return AnalysisConverter.toAnalysisDto(analysis);
    }

    private void validIsUserAuthorizedForRecord(User user, Record record) {
        if (!record.getUser().equals(user))
            throw new RecordException(RecordErrorStatus.USER_RECORD_UNAUTHORIZED);
    }

    @Override
    public Analysis createAnalysis(Record record, User user) {

        // MEMO 경험 기록이라면, AI를 이용해 요약 진행
        String content = getRecordContent(record);

        // AI API 호출
        AnalysisAiResponse response = generateAbilityAnalysis(content);

        Analysis analysis = record.getAnalysis() == null ?
                createAndSaveNewAnalysis(record, content, response) : // 역량 분석
                updateAnalysisAndDeleteAbility(record, content, response); // 역량 분석 업데이트

        // Ability 객체 생성 및 저장
        abilityService.parseAndSaveAbilities(response.getKeywordList(), analysis, user);

        return analysis;
    }

    private String getRecordContent(Record record) {
        String content = record.isMemoType()
                ? generateMemoSummary(record.getContent())
                : record.getContent();

        validAnalysisContentLength(content);
        return content;
    }

    private String generateMemoSummary(String content) {
        String response = analysisAIService.generateMemoSummary(content);

        validIsRecordEnough(response);
        return response;
    }

    private void validAnalysisContentLength(String content) {
        if (content.isEmpty() || content.length() > 500)
            throw new AnalysisException(AnalysisErrorStatus.OVERFLOW_ANALYSIS_CONTENT);
    }

    private void validIsRecordEnough(String response) {
        if (response.contains("NO_RECORD"))
            throw new RecordException(RecordErrorStatus.NO_RECORD);
    }

    private AnalysisAiResponse generateAbilityAnalysis(String content) {
        AnalysisAiResponse response = analysisAIService.generateAbilityAnalysis(content);

        validAnalysisCommentLength(response.getComment());
        validAnalysisKeywordContentLength(response.getKeywordList());

        return response;
    }

    private void validAnalysisCommentLength(String comment) {
        if (comment.isEmpty() || comment.length() > 300)
            throw new AnalysisException(AnalysisErrorStatus.OVERFLOW_ANALYSIS_COMMENT);
    }

    private void validAnalysisKeywordContentLength(Map<String, String> keywordList) {
        for (Map.Entry<String, String> entry : keywordList.entrySet()) {
            String keyContent = entry.getValue();

            if (keyContent.isEmpty() || keyContent.length() > 300)
                throw new AnalysisException(AnalysisErrorStatus.OVERFLOW_ANALYSIS_KEYWORD_CONTENT);
        }
    }

    private Analysis createAndSaveNewAnalysis(Record record, String content, AnalysisAiResponse response) {
        Analysis analysis = AnalysisConverter.toAnalysis(content, response.getComment(), record);
        analysisDbService.saveAnalysis(analysis);
        return analysis;
    }

    private Analysis updateAnalysisAndDeleteAbility(Record record, String content, AnalysisAiResponse response) {
        // analysis content 내용 변경
        Analysis analysis = record.getAnalysis();
        analysisDbService.updateAnalysisContent(analysis, content);
        analysisDbService.updateAnalysisComment(analysis, response.getComment());

        // 기존 Ability 객체 삭제
        abilityService.deleteOriginAbilityList(analysis);
        return analysis;
    }

    /**
     * analysisId를 받아 경험 분석 상세 정보를 반환합니다.
     *
     * @param userId
     * @param analysisId
     * @return 분석된 역량 키워드, 코멘트, 기록, 기록 수단 정보를 반환
     */
    @Override
    public AnalysisResponse.AnalysisDto getAnalysis(Long userId, Long analysisId) {
        Analysis analysis = analysisDbService.findAnalysisById(analysisId);

        // User-Analysis 권한 유효성 검증
        validIsUserAuthorizedForAnalysis(userId, analysis);

        return AnalysisConverter.toAnalysisDto(analysis);
    }

    /**
     * 역량 분석의 기록 내용 혹은 각 키워드에 대한 내용을 수정한 후 수정된 역량 분석 정보를 반환합니다.
     *
     * @param userId, analysisUpdateDto
     * @return 분석된 역량 키워드, 코멘트, 기록, 기록 수단 정보를 반환
     */
    @Override
    @Transactional
    public AnalysisResponse.AnalysisDto updateAnalysis(Long userId, AnalysisRequest.AnalysisUpdateDto analysisUpdateDto) {
        Analysis analysis = analysisDbService.findAnalysisById(analysisUpdateDto.getAnalysisId());

        // User-Analysis 권한 유효성 검증
        validIsUserAuthorizedForAnalysis(userId, analysis);

        // 경험 기록 제목 수정
        String title = analysisUpdateDto.getTitle();
        recordDbService.updateRecordTitle(analysis.getRecord(), title);

        // 경험 역량 분석 요약 내용 수정
        String content = analysisUpdateDto.getContent();
        analysisDbService.updateAnalysisContent(analysis, content);

        // 키워드 경험 내용 수정
        Map<String, String> abilityMap = analysisUpdateDto.getAbilityMap();
        abilityService.updateAbilityContents(analysis, abilityMap);

        return AnalysisConverter.toAnalysisDto(analysis);
    }

    /**
     * analysisId를 받아 역량 분석, 경험 기록 데이터를 제거합니다.
     *
     * @param userId, analysisId
     */
    @Override
    @Transactional
    public void deleteAnalysis(Long userId, Long analysisId) {
        Analysis analysis = analysisDbService.findAnalysisById(analysisId);

        // User-Analysis 권한 유효성 검증
        validIsUserAuthorizedForAnalysis(userId, analysis);

        analysisDbService.deleteAnalysis(analysis);
    }

    private void validIsUserAuthorizedForAnalysis(Long userId, Analysis analysis) {
        if (!analysis.getRecord().getUser().getUserId().equals(userId))
            throw new RecordException(RecordErrorStatus.USER_RECORD_UNAUTHORIZED);
    }
}
