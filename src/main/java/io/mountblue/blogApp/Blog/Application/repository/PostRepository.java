package io.mountblue.blogApp.Blog.Application.repository;

import io.mountblue.blogApp.Blog.Application.model.Post;
import io.mountblue.blogApp.Blog.Application.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
List<Post> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrContentContainingIgnoreCaseOrTagsNameContainingIgnoreCase
        (String title, String author,String content,String tags);
List<Post> findByAuthorContaining(String author);
List<Post> findByTagsContaining(Tag tag);

}
