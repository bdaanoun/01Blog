package com.o1blog._blog.repository;

import com.o1blog._blog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    long countByRole(User.Role role);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    @Query("select u.id from User u where u.username = :username")
    Optional<Long> findIdByUsername(@Param("username") String username);

    Optional<User> findByEmailOrUsername(String email, String username);

    boolean existsByEmail(String email);

    boolean existsByRole(User.Role role);

    boolean existsByUsername(String username);
}