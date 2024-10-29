package corecord.dev.domain.analysis.service;

import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.domain.analysis.converter.AnalysisConverter;
import corecord.dev.domain.analysis.dto.response.AnalysisResponse;
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

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final AnalysisRepository analysisRepository;
    private final AbilityRepository abilityRepository;
    private final UserRepository userRepository;


    @Transactional
    public void createAnalysis(Record record) {

        // CLOVA STUDIO API 호출

        // Analysis 객체 생성 및 저장

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

    private void validIsUserAuthorizedForAnalysis(User user, Analysis analysis) {
        if (!analysis.getRecord().getUser().equals(user))
            throw new RecordException(RecordErrorStatus.USER_RECORD_UNAUTHORIZED);
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
