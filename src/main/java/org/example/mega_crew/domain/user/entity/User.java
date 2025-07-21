package org.example.mega_crew.domain.user.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.mega_crew.global.common.BaseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends BaseEntity implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  private String password;

  @Column(unique = true, nullable = false)
  private String username;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private HearingStatus hearingStatus = HearingStatus.NORMAL;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private UserRole role = UserRole.USER; // 기본값 설정

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private AuthProvider authProvider = AuthProvider.LOCAL; // 기본값 설정

  private String providerId;

  public void updateUsername(String username){
    this.username = username;
  }

  // OAuth2 사용자 정보 업데이트
  public void updateOAuth2Info(String username) {
    this.username = username;
  }

  public void linkOAuth2Account(AuthProvider authProvider, String providerId, String username) {
    this.authProvider = authProvider;
    this.providerId = providerId;
    this.username = username;
  }

  // UserDetails 구현 메서드들...
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities(){
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword(){
    return password;
  }

  @Override
  public String getUsername(){
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
