package org.example.mega_crew.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.mega_crew.domain.user.entity.AuthProvider; // Security와 충돌하지 않기 위해 명시
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.domain.user.entity.UserRole;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private AuthProvider authProvider;
    private UserRole role;

    public static UserResponse from(User user){
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .build();
    }
}
