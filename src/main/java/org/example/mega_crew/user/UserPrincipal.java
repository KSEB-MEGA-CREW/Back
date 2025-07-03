package org.example.mega_crew.user;

import lombok.Getter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class UserPrincipal implements UserDetails, OAuth2User {
    private final Long id;
    private final String email;

}
