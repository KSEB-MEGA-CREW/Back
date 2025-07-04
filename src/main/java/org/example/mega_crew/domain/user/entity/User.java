package org.example.mega_crew.domain.user.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.mega_crew.global.common.BaseEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntity implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String email; // 우선 email nullable = false

  private String password;

  @Column(nullable = false)
  private String username;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role; // 일반 사용자와 관리자 구별

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AuthProvider authProvider;

  private String providerId;

  @Builder
  public User(String email, String password, String username, UserRole role,
              AuthProvider authProvider, String providerId){
    this.email = email;
    this.password = password;
    this.username = username;
    this.role = (role != null ? role : UserRole.USER);
    this.authProvider = (authProvider != null ? authProvider : AuthProvider.LOCAL);
    this.providerId = providerId;
  }

  public void updateUsername(String username){
    this.username = username;
  }


  // OAuth2 사용자 정보 업데이트
  public void updateOAuth2Info(String username) {
    this.username = username;
  }

  // UserDetails 구현
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities(){
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
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
  public boolean isEnabled() { // 일단 true return -> 나중에 user의 활동 상태를 확인 가능한 로직 추가하기
    return true;
  }
}
