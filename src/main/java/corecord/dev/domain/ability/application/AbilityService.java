package corecord.dev.domain.ability.application;


import corecord.dev.domain.ability.domain.dto.response.AbilityResponse;
import corecord.dev.domain.analysis.domain.entity.Analysis;
import corecord.dev.domain.user.domain.entity.User;

import java.util.Map;

public interface AbilityService {

    AbilityResponse.KeywordListDto getKeywordList(Long userId);
    AbilityResponse.GraphDto getKeywordGraph(Long userId);

    void parseAndSaveAbilities(Map<String, String> keywordList, Analysis analysis, User user);
    void deleteOriginAbilityList(Analysis analysis);
    void updateAbilityContents(Analysis analysis, Map<String, String> abilityMap);

    void createExampleAbility(User user, Analysis analysis);
}
