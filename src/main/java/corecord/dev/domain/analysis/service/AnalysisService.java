package corecord.dev.domain.analysis.service;

import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.domain.analysis.constant.Keyword;
import corecord.dev.domain.analysis.converter.AnalysisConverter;
import corecord.dev.domain.analysis.dto.request.AnalysisRequest;
import corecord.dev.domain.analysis.dto.response.AnalysisResponse;
import corecord.dev.domain.analysis.entity.Ability;
import corecord.dev.domain.analysis.entity.Analysis;
import corecord.dev.domain.analysis.exception.enums.AnalysisErrorStatus;
import corecord.dev.domain.analysis.exception.model.AnalysisException;
import corecord.dev.domain.analysis.repository.AbilityRepository;
import corecord.dev.domain.analysis.repository.AnalysisRepository;
import corecord.dev.domain.record.entity.Record;
import corecord.dev.domain.record.exception.enums.RecordErrorStatus;
import corecord.dev.domain.record.exception.model.RecordException;
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
    private final AbilityRepository abilityRepository;
    private final UserRepository userRepository;


    @Transactional
    public void createAnalysis(Record record, User user) {
        // TODO: CLOVA STUDIO API 호출

        // Analysis 객체 생성 및 저장
        // TMP Analysis
        Analysis analysis = AnalysisConverter.toAnalysis("Comment", record);
        analysisRepository.save(analysis);

        // Ability 객체 생성 및 저장
        // TMP Ability
        Ability ability1 = AnalysisConverter.toAbility(Keyword.COMMUNICATION,"Communication Skill", analysis, user);
        Ability ability2 = AnalysisConverter.toAbility(Keyword.ADAPTABILITY,"Adaptability", analysis, user);
        Ability ability3 = AnalysisConverter.toAbility(Keyword.JUDGEMENT_SKILL,"Judgement Skill", analysis, user);
        abilityRepository.save(ability1);
        abilityRepository.save(ability2);
        abilityRepository.save(ability3);
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

        // 경험 기록 내용 수정
        String content = analysisUpdateDto.getRecordContent();
        analysis.getRecord().updateContent(content);

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

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.UNAUTHORIZED));
    }

    private Analysis findAnalysisById(Long analysisId) {
        return analysisRepository.findById(analysisId)
                .orElseThrow(() -> new AnalysisException(AnalysisErrorStatus.ANALYSIS_NOT_FOUND));
    }
}
