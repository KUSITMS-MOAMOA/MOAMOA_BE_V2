package corecord.dev.domain.user.converter;

import corecord.dev.domain.user.entity.User;

public class UserConverter {

    public static User toUser(String name) {
        return User.builder()
                .name(name)
                .build();
    }
}
