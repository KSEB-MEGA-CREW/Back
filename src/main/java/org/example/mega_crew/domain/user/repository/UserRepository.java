package org.example.mega_crew.domain.user.repository;


import org.example.mega_crew.domain.user.entity.AuthProvider;
import org.example.mega_crew.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByProviderIdAndAuthProvider(String providerId, AuthProvider authProvider);
}
