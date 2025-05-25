package corecord.dev.domain.auth.domain.dto;

public interface OAuth2UserInfo {
    String getProviderId();
    String getName();
    String getProvider();
}
