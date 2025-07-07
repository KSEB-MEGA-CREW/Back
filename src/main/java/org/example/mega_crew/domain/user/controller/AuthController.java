package org.example.mega_crew.domain.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.mega_crew.domain.user.dto.request.LoginRequest;
import org.example.mega_crew.domain.user.dto.request.UserSignupRequest;
import org.example.mega_crew.domain.user.dto.response.UserResponse;
import org.example.mega_crew.domain.user.service.UserService;
import org.example.mega_crew.global.common.ApiResponse;
import org.example.mega_crew.global.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody UserSignupRequest request){
        UserResponse userResponse = userService.signup(request);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String,Object>>> login(@Valid @RequestBody LoginRequest request){
        String token = userService.login(request);
        UserResponse userInfo = userService.getUserInfo(request.getEmail());

        Map<String,Object> response = Map.of(
                "token", token,
                "userInfo", userInfo
        ); // (k1,k2) : (token,userInfo)
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me") // LOCAL User login successed
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(HttpServletRequest request){
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractEmail(token);
        UserResponse userInfo = userService.getUserInfo(email);
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

        String token = jwtUtil.createToken(email);
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
