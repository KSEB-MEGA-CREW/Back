package org.example.mega_crew.user.entity;


import jakarta.persistence.*;
import lombok.*;
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
public class User implements UserDetails {
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
  private UserRole role; // role을 부여함으로써 일반, 소셜 연동 회원 구분

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AuthProvider authProvider;

  private String providerId;

  @Column(nullable = false)
  private boolean isActive = true;

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
  public boolean isAccountNonLocked() {
    return isActive;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return isActive;
  }

  public void updateUsername(String username){
    this.username = username;
  }

}
