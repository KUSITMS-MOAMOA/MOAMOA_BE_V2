package corecord.dev.domain.Ability.exception.model;

import corecord.dev.domain.Ability.exception.enums.AbilityErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AbilityException extends RuntimeException {

    private final AbilityErrorStatus abilityErrorStatus;

    @Override
    public String getMessage() {
        return abilityErrorStatus.getMessage();
    }
}
