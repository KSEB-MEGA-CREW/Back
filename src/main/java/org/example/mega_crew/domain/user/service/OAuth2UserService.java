package org.example.mega_crew.domain.user.service;


import lombok.RequiredArgsConstructor;
import org.example.mega_crew.domain.user.entity.AuthProvider;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.domain.user.entity.UserRole;
import org.example.mega_crew.domain.user.repository.UserRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo, AuthProvider authProvider){
        User user = User.builder()
                .email(oAuth2UserInfo.getEmail())
                .username(oAuth2UserInfo.getName())
                .role(UserRole.USER)
                .authProvider(authProvider)
                .providerId(oAuth2UserInfo.getId())
                .build();

        return userRepository.save(user);
    }

    private String getNameAttributeKey(String registrationId){
        if("google".equals(registrationId)){
            return "sub";
        }
        return "id";
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User){
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId,oAuth2User.getAttributes());

        if(oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()){
            throw new OAuth2AuthenticationException("OAuth2 provider에서 이메일을 찾을 수 없습니다.");
        }

        AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());
        Optional<User> userOptional = userRepository.findByProviderAndProviderId(authProvider, oAuth2UserInfo.getId());

        User user;
        if(userOptional.isPresent()){
            user = userOptional.get();
            user.updateOAuth2Info(oAuth2UserInfo.getName());
        } else{
            user = registerNewUser(oAuth2UserInfo, authProvider);
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oAuth2User.getAttributes(),
                getNameAttributeKey(registrationId)
        );
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try{
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex){
            throw new OAuth2AuthenticationException("OAuth2 인증에 실패하였습니다.");
        }
    }
}
