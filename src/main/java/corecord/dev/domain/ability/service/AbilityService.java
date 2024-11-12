package corecord.dev.domain.ability.service;

import corecord.dev.common.exception.GeneralException;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.domain.ability.converter.AbilityConverter;
import corecord.dev.domain.ability.dto.response.AbilityResponse;
import corecord.dev.domain.ability.entity.Ability;
import corecord.dev.domain.ability.entity.Keyword;
import corecord.dev.domain.ability.repository.AbilityRepository;
import corecord.dev.domain.analysis.entity.Analysis;
import corecord.dev.domain.analysis.exception.enums.AnalysisErrorStatus;
import corecord.dev.domain.analysis.exception.model.AnalysisException;
import corecord.dev.domain.user.entity.User;
import corecord.dev.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AbilityService {
    private final AbilityRepository abilityRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    /*
     * user의 역량 키워드 리스트를 반환
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public AbilityResponse.KeywordListDto getKeywordList(Long userId) {
        User user = findUserById(userId);
        List<String> keywordList = findKeywordList(user);

        return AbilityConverter.toKeywordListDto(keywordList);
    }

    /*
     * user의 각 역량 키워드에 대한 개수, 퍼센티지 정보를 반환
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public AbilityResponse.GraphDto getKeywordGraph(Long userId) {
        User user = findUserById(userId);

        // keyword graph 정보 조회
        List<AbilityResponse.KeywordStateDto> keywordGraph = findKeywordGraph(user);

        return AbilityConverter.toGraphDto(keywordGraph);
    }

    // CLOVA STUDIO를 통해 얻은 키워드 정보 파싱
    @Transactional
    public void parseAndSaveAbilities(Map<String, String> keywordList, Analysis analysis, User user) {
        int abilityCount = 0;
        for (Map.Entry<String, String> entry : keywordList.entrySet()) {
            Keyword keyword = Keyword.getName(entry.getKey());

            if (keyword == null) continue;

            Ability ability = AbilityConverter.toAbility(keyword, entry.getValue(), analysis, user);
            abilityRepository.save(ability);
            if (analysis.getAbilityList() != null)
                analysis.addAbility(ability);
            abilityCount++;
        }

        if (abilityCount < 1 || abilityCount > 3) {
            throw new AnalysisException(AnalysisErrorStatus.INVALID_ABILITY_ANALYSIS);
        }
    }

    public void deleteOriginAbilityList(Analysis analysis) {
        List<Ability> abilityList = analysis.getAbilityList();

        if (!abilityList.isEmpty()) {
            // 연관된 abilities 삭제
            abilityRepository.deleteAll(abilityList);

            // Analysis에서 abilities 리스트 비우기
            analysis.getAbilityList().clear();
            entityManager.flush();
        }
    }

    private List<String> findKeywordList(User user) {
        return abilityRepository.getKeywordList(user).stream()
                .map(Keyword::getValue)
                .toList();
    }

    private List<AbilityResponse.KeywordStateDto> findKeywordGraph(User user) {
        return abilityRepository.findKeywordStateDtoList(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.UNAUTHORIZED));
    }
}
