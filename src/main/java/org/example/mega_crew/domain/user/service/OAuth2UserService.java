package org.example.mega_crew.domain.user.service;


import lombok.RequiredArgsConstructor;
import org.example.mega_crew.global.security.oauth2.OAuth2UserInfo;
import org.example.mega_crew.global.security.oauth2.OAuth2UserInfoFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService { // 기존 : DB 처리까지 담당 -> 수정 : 최소한의 역할만 담당하고 OAuth2User return
    // DB관련 처리는 SuccessHandler에서

    // OAuth2UserService는 Spring Security에서 요구하는 최소한의 역할만 담당
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId, oAuth2User.getAttributes()
            );

            if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
                throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info", "OAuth2 provider에서 이메일을 찾을 수 없습니다.", null)
                );
            }

            Map<String, Object> userAttributes = registrationId.equals("naver")
                ? (Map<String, Object>) oAuth2User.getAttributes().get("response")
                : oAuth2User.getAttributes();

            return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                userAttributes,
                getNameAttributeKey(registrationId)
            );

        } catch (OAuth2AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("unknown_error", "OAuth2 처리 중 알 수 없는 오류 발생", null), ex
            );
        }
    }


    private String getNameAttributeKey(String registrationId) {
        switch (registrationId) {
            case "google":
                return "sub";
            case "naver":
                return "id";
            case "kakao":
                return "id1"; // Kakao도 기본 id
            default:
                return "id2";
        }
    }
}
