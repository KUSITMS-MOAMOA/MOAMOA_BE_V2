package corecord.dev.domain.ability.application;

import corecord.dev.domain.ability.domain.converter.AbilityConverter;
import corecord.dev.domain.ability.domain.dto.response.AbilityResponse;
import corecord.dev.domain.ability.domain.entity.Ability;
import corecord.dev.domain.ability.domain.enums.Keyword;
import corecord.dev.domain.ability.exception.AbilityException;
import corecord.dev.domain.ability.status.AbilityErrorStatus;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.user.domain.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AbilityServiceImpl implements AbilityService {

    private final EntityManager entityManager;
    private final AbilityDbService abilityDbService;

    /**
     * user의 역량 키워드 리스트를 반환합니다.
     * 정렬 기준: 개수 내림차순, 최근 생성 순
     *
     * @param userId
     * @return String type 키워드 명칭 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public AbilityResponse.KeywordListDto getKeywordList(Long userId) {
        List<String> keywordList = abilityDbService.findKeywordList(userId);
        return AbilityConverter.toKeywordListDto(keywordList);
    }

    /**
     * user의 각 역량 키워드에 대한 개수, 퍼센티지 정보를 반환합니다.
     * 정렬 기준: 퍼센티지 내림차순
     *
     * @param userId
     * @return 각 키워드의 명칭, 개수, 전체 키워드 중 퍼센트 정보를 담은 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public AbilityResponse.GraphDto getKeywordGraph(Long userId) {
        List<AbilityResponse.KeywordStateDto> keywordGraph = abilityDbService.findKeywordGraph(userId);
        return AbilityConverter.toGraphDto(keywordGraph);
    }

    /**
     * AI를 통해 얻은 키워드 정보를 파싱해 저장합니다.
     * keywordList를 순회하며 최대 3개의 Keyword를 파싱합니다.
     * 파싱된 데이터를 기반으로 Ability entity를 생성 및 저장합니다.
     *
     * @param keywordList AI로부터 받은 {키워드: 키워드 코멘트} 데이터
     * @param analysis
     * @param user
     */
    @Override
    @Transactional
    public void parseAndSaveAbilities(Map<String, String> keywordList, Analysis analysis, User user) {
        int abilityCount = 0;
        for (Map.Entry<String, String> entry : keywordList.entrySet()) {
            Keyword keyword = Keyword.getName(entry.getKey());

            if (keyword == null) continue;

            Ability ability = AbilityConverter.toAbility(keyword, entry.getValue(), analysis, user);
            abilityDbService.saveAbility(ability);
            analysis.addAbility(ability);

            abilityCount++;
        }
        validAbilityCount(abilityCount);
    }

    private void validAbilityCount(int abilityCount) {
        if (abilityCount < 1 || abilityCount > 3)
            throw new AbilityException(AbilityErrorStatus.INVALID_ABILITY_KEYWORD);
    }

    /**
     * Analysis와 연관된 모든 Ability entity를 제거합니다.
     *
     * @param analysis
     */
    @Override
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

    /**
     * 기존 Analysis의 역량 분석 데이터를 변경합니다.
     * 기존 keyword의 content를 새로운 응답값으로 변경합니다.
     *
     * @param analysis
     * @param abilityMap 새로운 AI 역량 분석 응답값
     */
    @Override
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
