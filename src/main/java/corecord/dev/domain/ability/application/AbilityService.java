package corecord.dev.domain.ability.application;

import corecord.dev.domain.ability.domain.converter.AbilityConverter;
import corecord.dev.domain.ability.domain.dto.response.AbilityResponse;
import corecord.dev.domain.ability.domain.entity.Ability;
import corecord.dev.domain.ability.domain.entity.Keyword;
import corecord.dev.domain.ability.status.AbilityErrorStatus;
import corecord.dev.domain.ability.exception.AbilityException;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.user.application.UserDbService;
import corecord.dev.domain.user.domain.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AbilityService {
    private final EntityManager entityManager;
    private final UserDbService userDbService;
    private final AbilityDbService abilityDbService;

    /*
     * user의 역량 키워드 리스트를 반환
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public AbilityResponse.KeywordListDto getKeywordList(Long userId) {
        User user = userDbService.findUserById(userId);
        List<String> keywordList = abilityDbService.findKeywordList(user);

        return AbilityConverter.toKeywordListDto(keywordList);
    }

    /*
     * user의 각 역량 키워드에 대한 개수, 퍼센티지 정보를 반환
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public AbilityResponse.GraphDto getKeywordGraph(Long userId) {
        User user = userDbService.findUserById(userId);

        // keyword graph 정보 조회
        List<AbilityResponse.KeywordStateDto> keywordGraph = abilityDbService.findKeywordGraph(user);

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
            abilityDbService.saveAbility(ability);

            if (analysis.getAbilityList() != null)
                analysis.addAbility(ability);
            abilityCount++;
        }

        validAbilityCount(abilityCount);
    }

    private void validAbilityCount(int abilityCount) {
        if (abilityCount < 1 || abilityCount > 3)
            throw new AbilityException(AbilityErrorStatus.INVALID_ABILITY_KEYWORD);
    }

    @Transactional
    public void deleteOriginAbilityList(Analysis analysis) {
        List<Ability> abilityList = analysis.getAbilityList();

        if (!abilityList.isEmpty()) {
            // 연관된 abilities 삭제
            abilityDbService.deleteAbilityList(abilityList);

            // Analysis에서 abilities 리스트 비우기
            analysis.getAbilityList().clear();
            entityManager.flush();
        }
    }

    @Transactional
    public void updateAbilityContents(Analysis analysis, Map<String, String> abilityMap) {
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
}
