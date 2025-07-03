package org.example.mega_crew.global.security.oauth2;

import java.util.Map;

// 이후 확장할 경우 naver, kakao, naver 각각 추가하기
public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) throws IllegalAccessException {
        if (registrationId == null) {
            throw new IllegalArgumentException("Registration ID cannot be null");
        }
        switch (registrationId.toLowerCase()){
            case "google" :
                return new GoogleOAuth2UserInfo(attributes);
            case "kakao ":
                throw new IllegalArgumentException();
            default :
                throw new IllegalAccessException();
        }
    }
}
