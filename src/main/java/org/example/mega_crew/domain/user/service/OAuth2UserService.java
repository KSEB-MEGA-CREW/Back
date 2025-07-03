package org.example.mega_crew.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.mega_crew.global.security.oauth2.OAuth2UserInfo;
import org.example.mega_crew.global.security.oauth2.OAuth2UserInfoFactory;
import org.example.mega_crew.domain.user.entity.AuthProvider;
import org.example.mega_crew.domain.user.entity.CustomOAuth2User;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.domain.user.entity.UserRole;
import org.example.mega_crew.domain.user.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.AuthenticationException;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oAuth2User);
    }
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId, oAuth2User.getAttributes());

        User user = userRepository.findByProviderIdAndAuthProvider(
                oAuth2UserInfo.getId(),
                AuthProvider.valueOf(registrationId.toUpperCase())
        ).orElseGet(() -> registerNewOAuth2User(userRequest, oAuth2UserInfo));

        // User를 OAuth2User로 변환하여 반환
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private User registerNewOAuth2User(OAuth2UserRequest userRequest, OAuth2UserInfo oAuth2UserInfo){
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        User user = User.builder()
                .email(oAuth2UserInfo.getEmail())
                .username(oAuth2UserInfo.getName())
                .role(UserRole.USER)
                .authProvider(AuthProvider.valueOf(registrationId.toUpperCase()))
                .providerId(oAuth2UserInfo.getId())
                .build();

        return userRepository.save(user);
    }
}
