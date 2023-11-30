package io.mountblue.blogApp.Blog.Application.service;

import io.mountblue.blogApp.Blog.Application.model.Comment;
import io.mountblue.blogApp.Blog.Application.model.Post;
import io.mountblue.blogApp.Blog.Application.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    public Comment createNewComment(Comment comment){
        return commentRepository.save(comment);
    }
    public List<Comment>getCommentByPostId(Long postId){
        return commentRepository.findByPostId(postId);
    }

}
