package org.example.mega_crew.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.mega_crew.user.entity.User;

import java.security.AuthProvider;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private AuthProvider authProvider;
    private LocalDateTime createdAt;

    public static UserResponse from (User user){
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
//                .authProvider(user.getAuthProvider())
//                .createdAt(user.getCreatedAt())
                .build();
    }
}
