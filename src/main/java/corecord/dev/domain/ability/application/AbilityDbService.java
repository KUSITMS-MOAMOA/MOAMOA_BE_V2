package corecord.dev.domain.ability.application;

import corecord.dev.domain.ability.domain.dto.response.AbilityResponse;
import corecord.dev.domain.ability.domain.entity.Ability;
import corecord.dev.domain.ability.domain.entity.Keyword;
import corecord.dev.domain.ability.domain.repository.AbilityRepository;
import corecord.dev.domain.folder.domain.entity.Folder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AbilityDbService {
    private final AbilityRepository abilityRepository;

    @Transactional
    public void saveAbility(Ability ability) {
        abilityRepository.save(ability);
    }

    @Transactional
    public void deleteAbilityByUserId(Long userId) {
        abilityRepository.deleteAbilityByUserId(userId);
    }

    @Transactional
    public void deleteAbilityByFolder(Folder folder) {
        abilityRepository.deleteAbilityByFolder(folder);
    }

    @Transactional
    public void deleteAbilityList(List<Ability> abilityList) {
        abilityRepository.deleteAll(abilityList);
    }

    public List<AbilityResponse.KeywordStateDto> findKeywordGraph(Long userId) {
        return abilityRepository.findKeywordStateDtoList(userId);
    }

    public List<String> findKeywordList(Long userId) {
        return abilityRepository.getKeywordList(userId).stream()
                .map(Keyword::getValue)
                .toList();
    }
}
