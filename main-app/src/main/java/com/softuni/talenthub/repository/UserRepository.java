package com.softuni.talenthub.repository;

import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findAllByRole(UserRole role);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.permissions WHERE u.role <> :role")
    List<User> findAllNonAdminsWithPermissions(UserRole role);
}
