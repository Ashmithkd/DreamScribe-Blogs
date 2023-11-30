package io.mountblue.blogApp.Blog.Application.service;

import io.mountblue.blogApp.Blog.Application.model.Post;
import io.mountblue.blogApp.Blog.Application.model.Tag;
import io.mountblue.blogApp.Blog.Application.repository.PostRepository;
import org.hibernate.annotations.Comments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PostService {
    @Autowired
    private  PostRepository postRepository;

    public Page<Post> getALlPosts(PageRequest pageRequest) {
        return postRepository.findAll(pageRequest);
    }
    public  Post getPostById(Long postId) {
        return postRepository.findById(postId).orElse(null);
    }
    public  Post createPost(Post post) {
        return postRepository.save(post);
    }

    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }
    public Page<Post>getAllPost(Pageable page){
        return postRepository.findAll(page);
    }
    public List<Post> searchPost(String search){
        return postRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrContentContainingIgnoreCaseOrTagsNameContainingIgnoreCase
                (search,search,search,search );
    }
    public List<Post> listOfPostByAuthor(String author){
        return postRepository.findByAuthorContaining(author);
    }
    public List<Post> listOfPostByTag(Tag tag){
        return postRepository.findByTagsContaining(tag);
    }

}

