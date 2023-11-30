package io.mountblue.blogApp.Blog.Application.repository;

import io.mountblue.blogApp.Blog.Application.model.Authority;
import io.mountblue.blogApp.Blog.Application.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority,Long> {
    Authority getAuthorityByUser(User loggedUser);
}