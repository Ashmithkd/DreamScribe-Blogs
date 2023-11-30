package io.mountblue.blogApp.Blog.Application.controller;
import io.mountblue.blogApp.Blog.Application.model.*;
import io.mountblue.blogApp.Blog.Application.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;
@RestController
@RequestMapping("/api/blog")
public class BlogRestController {
    @Autowired
    PostService postService;
    @Autowired
    CommentService commentService;
    @Autowired
    TagService tagService;
    @Autowired
    UserService userService;
    @Autowired
    AuthorityService authorService;

    @GetMapping("/home")
    public ResponseEntity<Page<Post>> Home(@RequestParam(defaultValue = "0") Integer pageNumber,
                                           @RequestParam(defaultValue = "createdAt") String sortBy,
                                           @RequestParam(defaultValue = "asc") String orderBy) {
        Sort sort = Sort.by(Sort.Direction.fromString(orderBy), sortBy);
        int pageSize = 6;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        Page<Post> posts = postService.getAllPost(pageRequest);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
            }
        }
        return new ResponseEntity<Page<Post>>(posts, HttpStatus.OK);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Long postId) {
        Post post = postService.getPostById(postId);
        String author = post.getAuthor();
        User user = userService.getUserByUsername(author);
        Long userId = user.getId();
        List<Comment> comments = commentService.getCommentByPostId(postId);
        if (post == null) {
            System.out.println("empty");
            return null;
        }
        return new ResponseEntity<Post>(post, HttpStatus.OK);
    }
    @GetMapping("/createPost")
    public ResponseEntity<Post> createPost() {
        Post post = new Post();
        post.setTitle("Default Title");
        post.setExcerpt("Default Excerpt");
        return new ResponseEntity<>(post, HttpStatus.OK);
    }
    @PostMapping("/processPost")
    public ResponseEntity<String> processPost(
            @RequestBody Post post,
            @RequestParam String tagsEntered,
            Principal principal) {
        try {
            String[] tagSplitted = tagsEntered.split(",");
            String username = principal.getName();
            post.setAuthor(username);
            User user = userService.getUserByUsername(username);
            post.setUserId(user);
            Set<Tag> tags = new HashSet<>();
            for (String tagName : tagSplitted) {
                tagName = tagName.trim();
                Tag existingTag = tagService.getTagByName(tagName);
                if (existingTag != null) {
                    tags.add(existingTag);
                } else {
                    Tag newTag = new Tag();
                    newTag.setName(tagName);
                    tagService.saveTag(newTag);
                    tags.add(newTag);
                }
            }
            post.setTags(tags);
            postService.createPost(post);

            return new ResponseEntity<>("Post created successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create the post", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/deletePost/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        Post post = postService.getPostById(postId);
        User loggedUser = userService.getUserByUsername(principal.getName());
        Authority authority = authorService.getAuthorityByUser(user);

        if (user.getId().equals(post.getUserId().getId()) || authority.getRole().equals("admin")) {
            postService.deletePost(postId);
            return new ResponseEntity<>("Post deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
        }
    }
    @GetMapping("/filter")
    public ResponseEntity<List<Post>> filter(@RequestParam String filterBy, @RequestParam String filterString) {
        List<Post> listOfPosts;

        if (filterBy.equals("author")) {
            listOfPosts = postService.listOfPostByAuthor(filterString);
        } else {
            Tag tag = tagService.getTagByName(filterString);
            listOfPosts = postService.listOfPostByTag(tag);
        }

        return new ResponseEntity<>(listOfPosts, HttpStatus.OK);
    }


}

