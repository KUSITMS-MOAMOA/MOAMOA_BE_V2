package corecord.dev.domain.analysis.service;

import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.domain.ability.entity.Keyword;
import corecord.dev.domain.ability.exception.enums.AbilityErrorStatus;
import corecord.dev.domain.ability.exception.model.AbilityException;
import corecord.dev.domain.ability.service.AbilityService;
import corecord.dev.domain.analysis.converter.AnalysisConverter;
import corecord.dev.domain.analysis.dto.request.AnalysisRequest;
import corecord.dev.domain.analysis.dto.response.AnalysisAiResponse;
import corecord.dev.domain.analysis.dto.response.AnalysisResponse;
import corecord.dev.domain.ability.entity.Ability;
import corecord.dev.domain.analysis.entity.Analysis;
import corecord.dev.domain.analysis.exception.enums.AnalysisErrorStatus;
import corecord.dev.domain.analysis.exception.model.AnalysisException;
import corecord.dev.domain.analysis.repository.AnalysisRepository;
import corecord.dev.domain.record.entity.Record;
import corecord.dev.domain.record.exception.enums.RecordErrorStatus;
import corecord.dev.domain.record.exception.model.RecordException;
import corecord.dev.domain.record.repository.RecordRepository;
import corecord.dev.domain.user.entity.User;
import corecord.dev.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final RecordRepository recordRepository;
    private final OpenAiService openAiService;
    private final AbilityService abilityService;


    /*
     * CLOVA STUDIO룰 활용해 역량 분석 객체를 생성 후 반환
     * @param record
     * @param user
     * @return
     */
    @Transactional
    public Analysis createAnalysis(Record record, User user) {

        // MEMO 경험 기록이라면, CLOVA STUDIO를 이용해 요약 진행
        String content = getRecordContent(record);

        // Open-ai API 호출
        AnalysisAiResponse response = generateAbilityAnalysis(content);

        // Analysis 객체 생성 및 저장
        Analysis analysis = AnalysisConverter.toAnalysis(content, response.getComment(), record);
        analysisRepository.save(analysis);

        // Ability 객체 생성 및 저장
        abilityService.parseAndSaveAbilities(response.getKeywordList(), analysis, user);

        return analysis;
    }

    /*
     * CLOVA STUDIO를 활용해 역량 분석을 재수행함 Analysis 객체 데이터 교체 후 반환
     * @param record
     * @param user
     * @return
     */
    @Transactional
    public Analysis recreateAnalysis(Record record, User user) {
        Analysis analysis = record.getAnalysis();

        // MEMO 경험 기록이라면, CLOVA STUDIO를 이용해 요약 진행
        String content = getRecordContent(record);

        // Open-ai API 호출
        AnalysisAiResponse response = generateAbilityAnalysis(content);

        // Analysis 객체 수정
        analysis.updateContent(content);
        analysis.updateComment(response.getComment());

        // 기존 Ability 객체 삭제
        abilityService.deleteOriginAbilityList(analysis);

        // Ability 객체 생성 및 저장
        abilityService.parseAndSaveAbilities(response.getKeywordList(), analysis, user);

        return analysis;
    }

    /*
     * recordId를 받아, 해당 경험 기록에 대한 역량 분석을 수행 후 생성된 역량 분석 상세 정보를 반환
     * @param userId
     * @param recordId
     * @return
     */
    public AnalysisResponse.AnalysisDto postAnalysis(Long userId, Long recordId) {
        User user = findUserById(userId);
        Record record = findRecordById(recordId);

        // User-Record 권한 유효성 검증
        validIsUserAuthorizedForRecord(user, record);

        // 역량 분석 API 호출
        Analysis analysis = record.getAnalysis() == null ?
                createAnalysis(record, user) :
                recreateAnalysis(record, user);       // 기존 Analysis 객체가 있을 경우 교체

        return AnalysisConverter.toAnalysisDto(analysis);
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

    private AnalysisAiResponse generateAbilityAnalysis(String content) {
        AnalysisAiResponse response = openAiService.generateAbilityAnalysis(content);

        // 글자 수 validation
        validAnalysisCommentLength(response.getComment());
        validAnalysisKeywordContentLength(response.getKeywordList());

        return response;
    }

    private String generateMemoSummary(String content) {
        String response = openAiService.generateMemoSummary(content);
        validAnalysisContentLength(response);

        return response;
    }

    private String getRecordContent(Record record) {
        String content = record.isMemoType()
                ? generateMemoSummary(record.getContent())
                : record.getContent();

        validAnalysisContentLength(content);

        return content;
    }

    private void validIsUserAuthorizedForAnalysis(User user, Analysis analysis) {
        if (!analysis.getRecord().getUser().equals(user))
            throw new RecordException(RecordErrorStatus.USER_RECORD_UNAUTHORIZED);
    }

    private void validIsUserAuthorizedForRecord(User user, Record record) {
        if (!record.getUser().equals(user))
            throw new RecordException(RecordErrorStatus.USER_RECORD_UNAUTHORIZED);
    }

    private void validAnalysisContentLength(String content) {
        if (content.isEmpty() || content.length() > 500)
            throw new AnalysisException(AnalysisErrorStatus.OVERFLOW_ANALYSIS_CONTENT);
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
                .orElseThrow(() -> new AbilityException(AbilityErrorStatus.INVALID_KEYWORD));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.UNAUTHORIZED));
    }

    private Analysis findAnalysisById(Long analysisId) {
        return analysisRepository.findAnalysisById(analysisId)
                .orElseThrow(() -> new AnalysisException(AnalysisErrorStatus.ANALYSIS_NOT_FOUND));
    }

    private Record findRecordById(Long recordId) {
        return recordRepository.findRecordById(recordId)
                .orElseThrow(() -> new RecordException(RecordErrorStatus.RECORD_NOT_FOUND));
    }
}
