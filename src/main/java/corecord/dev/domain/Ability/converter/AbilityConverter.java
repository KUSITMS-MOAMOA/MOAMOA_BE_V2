package corecord.dev.domain.Ability.converter;

import corecord.dev.domain.Ability.dto.response.AbilityResponse;
import corecord.dev.domain.Ability.entity.Ability;
import corecord.dev.domain.Ability.entity.Keyword;
import corecord.dev.domain.analysis.entity.Analysis;
import corecord.dev.domain.user.entity.User;

import java.util.List;

public class AbilityConverter {

    public static Ability toAbility(Keyword keyword, String content, Analysis analysis, User user) {
        return Ability.builder()
                .keyword(keyword)
                .content(content)
                .analysis(analysis)
                .user(user)
                .build();
    }

    public static AbilityResponse.AbilityDto toAbilityDto(Ability ability) {
        return AbilityResponse.AbilityDto.builder()
                .keyword(ability.getKeyword().getValue())
                .content(ability.getContent())
                .build();
    }

    public static AbilityResponse.KeywordListDto toKeywordListDto(List<String> keywordList) {
        return AbilityResponse.KeywordListDto.builder()
                .keywordList(keywordList)
                .build();
    }

    public static AbilityResponse.GraphDto toGraphDto(List<AbilityResponse.KeywordStateDto> keywordStateDtoList) {
        return AbilityResponse.GraphDto.builder()
                .keywordGraph(keywordStateDtoList)
                .build();
    }
}
