package corecord.dev.domain.converter;

import corecord.dev.domain.entity.User;

public class UserConverter {

    public static User toUser(String name) {
        return User.builder()
                .name(name)
                .build();
    }
}
