package io.mountblue.blogApp.Blog.Application.repository;

import io.mountblue.blogApp.Blog.Application.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByPostId(Long postId);  //custom method which is automatically created by jpa
}
