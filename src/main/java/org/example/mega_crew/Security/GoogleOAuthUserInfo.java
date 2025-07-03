package org.example.mega_crew.Security;


import java.util.Map;

public class GoogleOAuthUserInfo implements OAuth2UserInfo {
    private Map<String, Object> attributes;

    public GoogleOAuthUserInfo(Map<String, Object> attributes){
        this.attributes = attributes;
    }

    @Override
    public String getId(){
        return (String) attributes.get("sub");
    }

    @Override
    public String getName(){
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail(){
        return (String) attributes.get("email");
    }
}
