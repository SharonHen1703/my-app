package com.myapp.server.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u where lower(u.email) = lower(:email)")
    Optional<User> findByEmailNormalized(@Param("email") String email);
}
