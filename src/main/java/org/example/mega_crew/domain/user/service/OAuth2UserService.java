package org.example.mega_crew.domain.user.service;


import lombok.RequiredArgsConstructor;
import org.example.mega_crew.global.security.oauth2.OAuth2UserInfo;
import org.example.mega_crew.global.security.oauth2.OAuth2UserInfoFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

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
            OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

            if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
                throw new OAuth2AuthenticationException("OAuth2 provider에서 이메일을 찾을 수 없습니다.");
            }

            // 단순히 OAuth2User 객체만 반환
            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    oAuth2User.getAttributes(),
                    getNameAttributeKey(registrationId)
            );

        } catch (Exception ex) {
            throw new OAuth2AuthenticationException("OAuth2 인증에 실패하였습니다.");
        }
    }

    private String getNameAttributeKey(String registrationId) {
        switch (registrationId) {
            case "google":
                return "sub";
            case "naver":
                return "sub";
            case "kakao":
                return "id"; // Kakao도 기본 id
            default:
                return "id";
        }
    }
}
