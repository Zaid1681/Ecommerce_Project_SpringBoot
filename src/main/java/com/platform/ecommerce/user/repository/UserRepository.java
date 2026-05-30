package com.platform.ecommerce.user.repository;

import com.platform.ecommerce.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    public Users findByUsername(String userName);
    public boolean existsByUsername(String userName);
}
