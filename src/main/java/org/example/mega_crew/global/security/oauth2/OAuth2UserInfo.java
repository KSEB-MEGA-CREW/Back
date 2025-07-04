package org.example.mega_crew.global.security.oauth2;

import java.util.Map;

// abstract class로 정의 -> AuthProvider에 따라 다르게 구현
// => 다형성 확보
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String,Object> attributes){
        this.attributes = attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();

}
