package corecord.dev.domain.ability.exception.model;

import corecord.dev.domain.ability.exception.enums.AbilityErrorStatus;
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
