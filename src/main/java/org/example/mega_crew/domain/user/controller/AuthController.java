package org.example.mega_crew.domain.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.user.dto.request.LoginRequest;
import org.example.mega_crew.domain.user.dto.request.UserSignupRequest;
import org.example.mega_crew.domain.user.dto.response.UserResponse;
import org.example.mega_crew.domain.user.entity.AuthProvider;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.domain.user.repository.UserRepository;
import org.example.mega_crew.domain.user.service.UserService;
import org.example.mega_crew.global.common.ApiResponse;
import org.example.mega_crew.global.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody UserSignupRequest request){
        UserResponse userResponse = userService.signup(request);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String,Object>>> login(@Valid @RequestBody LoginRequest request){
        // UserResponse userInfo = userService.getUserInfo(request.getEmail());
        // 위의 로직이 db를 불필요하게 조회하는 문제의 원인 => 수정
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.createToken(user.getEmail(), user.getId());

        UserResponse userInfo = UserResponse.from(user);

        Map<String,Object> response = Map.of(
                "token", token,
                "userInfo", userInfo
        ); // (k1,k2) : (token,userInfo)
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me") // LOCAL User login successed
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(HttpServletRequest request){
        String token = jwtUtil.extractTokenFromRequest(request);
        Long userId = jwtUtil.extractUserId(token);
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));

        UserResponse userInfo = UserResponse.builder()
                .id(userId)
                .email(email)
                .username(user.getUsername())
                .build();

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @GetMapping("/oauth2/success") // OAUTH2User login successed
    public ResponseEntity<ApiResponse<Map<String,Object>>> oauth2Success(Authentication authentication){
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        if(email == null){
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("이메일 정보를 가져올 수 없습니다."));
        }

        String username = oauth2User.getAttribute("name");
        String providerId = String.valueOf(oauth2User.getAttribute("sub")); // Google의 경우

        String token = userService.processOAuth2Login(email, username, providerId, AuthProvider.GOOGLE);
        UserResponse userInfo = userService.getUserInfo(email);

        Map<String,Object> response = Map.of(
                "token", token,
                "userInfo", userInfo
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/oauth2/failure")
    public ResponseEntity<ApiResponse<Map<String,Object>>> oauth2Failure(){
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("소셜 로그인에 실패했습니다."));
    }
}
