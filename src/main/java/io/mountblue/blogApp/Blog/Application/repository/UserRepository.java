package io.mountblue.blogApp.Blog.Application.repository;

import io.mountblue.blogApp.Blog.Application.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findUsernameByUsername(String username);
    User findUserById(Long userId);
}

