package org.example.mega_crew.user.repository;

import org.example.mega_crew.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository <User, Long> {

}
