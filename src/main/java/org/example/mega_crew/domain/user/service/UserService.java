package org.example.mega_crew.domain.user.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.history.repository.TranslationHistoryRepository;
import org.example.mega_crew.domain.question.repository.SupportTicketRepository;
import org.example.mega_crew.domain.quiz.repository.QuizCategoryRecordRepository;
import org.example.mega_crew.domain.quiz.repository.QuizRecordRepository;
import org.example.mega_crew.domain.user.dto.request.AdminSignupRequest;
import org.example.mega_crew.domain.user.dto.request.LoginRequest;
import org.example.mega_crew.domain.user.dto.request.UserSignupRequest;
import org.example.mega_crew.domain.user.dto.request.UserUpdateRequest;
import org.example.mega_crew.domain.user.dto.response.UserResponse;
import org.example.mega_crew.domain.user.entity.HearingStatus;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.domain.user.entity.AuthProvider;
import org.example.mega_crew.domain.user.entity.UserRole;
import org.example.mega_crew.domain.user.repository.UserRepository;
import org.example.mega_crew.global.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService { // 모든 타입의 User의 토큰 생성 담당, OAuth2UserService를 간소화 => 단일책임원칙 지향
   private final UserRepository userRepository;
   private final PasswordEncoder passwordEncoder;
   private final JwtUtil jwtUtil;

   private final TranslationHistoryRepository translationHistoryRepository;
   private final SupportTicketRepository supportTicketRepository;
   private final QuizRecordRepository quizRecordRepository;
   private final QuizCategoryRecordRepository quizCategoryRecordRepository;

   // email, username 중복 처리 후 user entity build
   public UserResponse signup(UserSignupRequest request) {
      if (userRepository.existsByEmail(request.getEmail())) {
         throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
      }

      if (userRepository.existsByUsername(request.getUsername())) {
         throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
      }

      // HearingStatus 처리 추가
      HearingStatus hearingStatus = HearingStatus.NORMAL;
      if ("deaf".equalsIgnoreCase(request.getHearing())) {
         hearingStatus = HearingStatus.DEAF;
      }

      // 일반회원 회원가입
      User user = User.builder()
          .email(request.getEmail())
          .password(passwordEncoder.encode(request.getPassword()))
          .username(request.getUsername())
          .hearingStatus(hearingStatus)
          .role(UserRole.USER)
          .authProvider(AuthProvider.LOCAL)
          .build();

      User savedUser = userRepository.save(user);
      return UserResponse.from(savedUser);
   }

   // 관리자 계정 생성 (최초 설정용 또는 기존 관리자가 생성)
   public UserResponse createAdmin(AdminSignupRequest request, Long currentUserId) {
      // 기존 관리자가 새 관리자를 생성하는 경우 권한 검증
      if (currentUserId != null) {
         User currentUser = userRepository.findById(currentUserId)
             .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));

         if (currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("관리자만 새로운 관리자 계정을 생성할 수 있습니다.");
         }
      }

      if (userRepository.existsByEmail(request.getEmail())) {
         throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
      }

      if (userRepository.existsByUsername(request.getUsername())) {
         throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
      }

      User admin = User.builder()
          .email(request.getEmail())
          .password(passwordEncoder.encode(request.getPassword()))
          .username(request.getUsername())
          .hearingStatus(HearingStatus.NORMAL) // 관리자는 기본적으로 NORMAL
          .role(UserRole.ADMIN) // 관리자 권한
          .authProvider(AuthProvider.LOCAL)
          .build();

      User savedAdmin = userRepository.save(admin);
      log.info("관리자 계정 생성 완료 - Email: {}, Username: {}", savedAdmin.getEmail(), savedAdmin.getUsername());
      return UserResponse.from(savedAdmin);
   }

   // 로그인 (일반, 관리자)
   public String login(LoginRequest request) {
      User user = userRepository.findByEmail(request.getEmail())
          .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다."));

      if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
         throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
      }

      String token = jwtUtil.createToken(user.getEmail(), user.getId());

      // 로그인 로그 (역할 구분)
      if (user.getRole() == UserRole.ADMIN) {
         log.info("관리자 로그인 - Email: {}, Username: {}", user.getEmail(), user.getUsername());
      } else {
         log.info("일반 사용자 로그인 - Email: {}, Username: {}", user.getEmail(), user.getUsername());
      }

      return token;
   }

   @Transactional(readOnly = true)
   public UserResponse getUserInfo(String email) {
      User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));
      return UserResponse.from(user);
   }

   @Override
   public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
      return userRepository.findByEmail(email)
          .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));
   }

   /**
    * OAuth2 로그인 처리 => SuccessHandler에서 호출
    * 실질적 OAuth2User의 처리는 SuccessHandler에서 담당
    * DB 및 토큰은 UserService가 담당
    */
   public String processOAuth2Login(String email, String username, String providerId, AuthProvider authProvider) {
      try {
         log.info("OAuth2 login process starts - Email : {}, Provider : {}", email, authProvider);

         // providerId -> string으로 변환
         String providerIdStr = String.valueOf(providerId);

         // 기존 사용자 확인 by providerId, authProvider
         Optional<User> existingUser = userRepository.findByAuthProviderAndProviderId(authProvider, providerId);

         User user;
         if (existingUser.isPresent()) {
            user = existingUser.get();
            // 사용자 정보 update
            user.updateOAuth2Info(username);
            userRepository.save(user);
            log.info("OAuth2 login process ends - Email : {}", email);
         } else {
            // email로 기존 사용자 확인 - 다른 방식으로 가입한 적이 있는지 확인
            Optional<User> existingEmailUser = userRepository.findByEmail(email);
            if (existingEmailUser.isPresent()) { // LOCAL user로 가입한 경우
               user = existingEmailUser.get();
               // 기존 LOCAL 사용자를 OAuth2로 연동
               user.linkOAuth2Account(authProvider, providerIdStr, username);
               userRepository.save(user);
               log.info("기존 사용자 OAuth2 연동 - Email: {}", email);
            } else { // 완전히 새로운 사용자
               // 새 사용자 생성
               user = createOAuth2User(email, username, providerIdStr, authProvider);
               log.info("new OAuth2 user created - Email: {}", email);
            }
         }

         // create JWT token
         String token = jwtUtil.createToken(user.getEmail(), user.getId());
         log.info("OAuth2 login process ends - Email : {}", email);

         return token;
      } catch (Exception e) {
         log.error(e.getMessage());
         throw new RuntimeException(e);
      }
   }

   private User createOAuth2User(String email, String username, String providerId, AuthProvider authProvider) {
      String uniqueUsername = generateUniqueUsername(extractUsernameFromEmail(email));

      User newUser = User.builder()
          .email(email)
          .username(uniqueUsername)
          .password(null) // OAuth2User는 password 없음
          .role(UserRole.USER)
          .authProvider(authProvider)
          .providerId(providerId)
          .build();
      return userRepository.save(newUser);
   }

   // 기존 OAuth2UserService의 헬퍼 메서드들을 UserService에 정의
   private String extractUsernameFromEmail(String email) {
      return email.split("@")[0].replaceAll("[^a-zA-Z0-9]]", ""); // email에서 username 추출
   }

   // email 앞부분이 동일할 경우 username이 중복되는 상황을 방지하기 위한 메서드
   private String generateUniqueUsername(String baseUsername) {
      String username = baseUsername;
      int counter = 1;

      while (userRepository.existsByUsername(username)) {
         username = baseUsername + counter;
         counter++;
      }
      return username;
   }

   // 프로필 편집
   public UserResponse updateUserProfile(Long userId, UserUpdateRequest request) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));

      // username 중복 검사 (현재 사용자 제외)
      if (!user.getUsername().equals(request.getUsername()) &&
          userRepository.existsByUsername(request.getUsername())) {
         throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
      }

      // HearingStatus 변환
      HearingStatus hearingStatus = HearingStatus.NORMAL;
      if ("deaf".equalsIgnoreCase(request.getHearing())) {
         hearingStatus = HearingStatus.DEAF;
      }

      // 사용자 정보 업데이트
      user.updateProfile(request.getUsername(), hearingStatus);
      User updatedUser = userRepository.save(user);

      return UserResponse.from(updatedUser);
   }

   @Transactional
   public void deleteUserAccount(Long userId) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));

      log.info("사용자 계정 삭제 시작 - User ID: {}, Email: {}", userId, user.getEmail());

      try {
         // 관련 데이터 먼저 삭제 (FK 제약조건 해결)
         deleteUserRelatedData(userId);

         // 사용자 삭제
         userRepository.delete(user);

         log.info("사용자 계정 삭제 완료 - User ID: {}", userId);
      } catch (Exception e) {
         log.error("사용자 계정 삭제 중 오류 발생 - User ID: {}, Error: {}", userId, e.getMessage());
         throw new RuntimeException("계정 삭제 중 오류가 발생했습니다.", e);
      }
   }

   /**
    * 사용자와 관련된 모든 데이터를 삭제하는 메서드
    * FK 제약조건을 위반하지 않도록 순서에 주의
    */
   private void deleteUserRelatedData(Long userId) {
      log.info("사용자 관련 데이터 삭제 시작 - User ID: {}", userId);

      try {
         // 번역 기록 삭제
         translationHistoryRepository.deleteByUserId(userId);
         log.debug("번역 기록 삭제 완료 - User ID: {}", userId);

         // 지원 티켓 삭제
         supportTicketRepository.deleteByUserId(userId);
         log.debug("지원 티켓 삭제 완료 - User ID: {}", userId);

         // 퀴즈 기록 삭제
         quizRecordRepository.deleteByUserId(userId);
         log.debug("퀴즈 기록 삭제 완료 - User ID: {}", userId);

         // 퀴즈 카테고리 기록 삭제
         quizCategoryRecordRepository.deleteByUserId(userId);
         log.debug("퀴즈 카테고리 기록 삭제 완료 - User ID: {}", userId);

         log.info("사용자 관련 데이터 삭제 완료 - User ID: {}", userId);
      } catch (Exception e) {
         log.error("사용자 관련 데이터 삭제 중 오류 발생 - User ID: {}, Error: {}", userId, e.getMessage());
         throw new RuntimeException("관련 데이터 삭제 중 오류가 발생했습니다.", e);
      }
   }
}
