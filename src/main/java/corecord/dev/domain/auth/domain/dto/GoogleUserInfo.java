package corecord.dev.domain.auth.domain.dto;

import lombok.AllArgsConstructor;
import corecord.dev.domain.user.domain.enums.Provider;

import java.util.Map;

@AllArgsConstructor
public class GoogleUserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getProvider() {
        return Provider.GOOGLE.name();
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

}