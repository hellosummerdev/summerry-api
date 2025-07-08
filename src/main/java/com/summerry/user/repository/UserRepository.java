package com.summerry.user.repository;

import com.summerry.user.entity.SocialType;
import com.summerry.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    List<User> findByEmail(String email);
    boolean existsByEmailAndUserIdNot(String email, Long userId);
    boolean existsByPhoneAndUserIdNot(String phone, Long userId);
}
