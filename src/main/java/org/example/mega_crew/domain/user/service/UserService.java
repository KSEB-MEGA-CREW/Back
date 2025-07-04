package org.example.mega_crew.domain.user.service;


import lombok.RequiredArgsConstructor;
import org.example.mega_crew.domain.user.dto.request.LoginRequest;
import org.example.mega_crew.domain.user.dto.request.UserSignupRequest;
import org.example.mega_crew.domain.user.dto.response.UserResponse;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.domain.user.entity.AuthProvider;
import org.example.mega_crew.domain.user.entity.UserRole;
import org.example.mega_crew.domain.user.repository.UserRepository;
import org.example.mega_crew.global.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // email, username 중복 처리 후 user entity build
    public UserResponse signup(UserSignupRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if(userRepository.existsByUsername(request.getUsername())){
            throw new IllegalArgumentException("이미 존재하는 username입니다.");
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

    public String login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        return jwtUtil.createToken(user.getEmail());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserInfo(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));
        return UserResponse.from(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));
    }
}
