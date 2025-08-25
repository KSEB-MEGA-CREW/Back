package org.example.mega_crew.global.security.oauth2;

import java.util.Map;

public class KakaoOauth2UserInfo extends OAuth2UserInfo{

   public KakaoOauth2UserInfo(Map<String, Object> attributes) {
      super(attributes);
   }

   @Override
   public String getId() {
      return String.valueOf(attributes.get("id"));
   }

   @Override
   public String getName() {
      Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
      if (properties == null) return null;

      return (String) properties.get("nickname");
   }

   @Override
   public String getEmail() {
      Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
      if (kakaoAccount == null) return null;

      return (String) kakaoAccount.get("email");
   }
}
