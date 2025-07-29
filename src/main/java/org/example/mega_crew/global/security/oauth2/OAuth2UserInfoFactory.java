package org.example.mega_crew.global.security.oauth2;

import org.example.mega_crew.domain.user.entity.AuthProvider;

import java.util.Map;


public class OAuth2UserInfoFactory {

    // OAuthProvider를 활용해 UserInfo를 get한 다음 각기 다른 AuthProvider에 따라 객체 생성
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        // AuthProvider == GOOGLE인 경우
        if(registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.toString())){
            return new GoogleOAuth2UserInfo(attributes);
        }
        else if (registrationId.equalsIgnoreCase(AuthProvider.NAVER.toString())) {
            return new NaverOAuth2UserInfo(attributes);
        }
        else{
            throw new IllegalArgumentException(registrationId + "을(를) 이용한 로그인은 지원되지 않습니다.");
        }
    }
}
