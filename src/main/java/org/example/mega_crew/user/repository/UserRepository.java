package org.example.mega_crew.user.repository;


import org.example.mega_crew.user.entity.AuthProvider;
import org.example.mega_crew.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Member;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Member> findByProviderIdAndAuthProvider(String providerId, AuthProvider authProvider);
}
