package corecord.dev.domain.auth.domain.dto;

import corecord.dev.domain.user.domain.enums.Provider;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class NaverUserInfo implements OAuth2UserInfo {
    private Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getProvider() {
        return Provider.NAVER.name();
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

}
