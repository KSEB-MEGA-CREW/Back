package org.example.mega_crew.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.mega_crew.user.dto.request.UserSignupRequest;
import org.example.mega_crew.user.dto.response.UserResponse;
import org.example.mega_crew.user.entity.AuthProvider;
import org.example.mega_crew.user.entity.User;
import org.example.mega_crew.user.entity.UserRole;
import org.example.mega_crew.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse signup(UserSignupRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new DataIntegrityViolationException("이메일이 이미 존재합니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .role(UserRole.USER)
                .authProvider(AuthProvider.LOCAL)
                .build();
        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
    }

    public UserResponse getUserInfo(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        return UserResponse.from(user);
    }

}
