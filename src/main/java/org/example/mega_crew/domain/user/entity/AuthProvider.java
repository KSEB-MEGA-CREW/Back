package org.example.mega_crew.domain.user.entity;

// LOCAL : 일반 회원가입 user
// GOOGLE : google 소셜 연동 로그인 user
// 추후 필요할 경우, 카카오, 네이버 등 확장 가능
public enum AuthProvider {
    LOCAL, GOOGLE
}
