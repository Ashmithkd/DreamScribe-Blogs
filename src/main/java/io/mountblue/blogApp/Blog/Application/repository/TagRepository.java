package io.mountblue.blogApp.Blog.Application.repository;

import io.mountblue.blogApp.Blog.Application.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag,Long> {
    public boolean existsByName(String name);
    Optional<Tag> findByName(String name);
    @Query("SELECT t FROM Tag t INNER JOIN t.posts p WHERE p.id = :postId")
    List<Tag> findTagsByPostId(Long postId);
}
