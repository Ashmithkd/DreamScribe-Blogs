package io.mountblue.blogApp.Blog.Application.controller;
import io.mountblue.blogApp.Blog.Application.model.*;
import io.mountblue.blogApp.Blog.Application.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;

@Controller
public class BlogController {
    @Autowired PostService postService;
    @Autowired CommentService commentService;
    @Autowired TagService tagService;
    @Autowired UserService userService;
    @Autowired AuthorityService authorService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // FIX 1: template was "Post" (capital P) — on Linux this 404s because file is post.html
    // FIX 2: null-check post BEFORE calling post.getAuthor() to prevent NPE
    @GetMapping("/post")
    public String getPostById(Model model, @RequestParam Long postId) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return "redirect:/Home";
        }
        String author = post.getAuthor();
        User user = userService.getUserByUsername(author);
        Long userId = (user != null) ? user.getId() : null;
        List<Comment> comments = commentService.getCommentByPostId(postId);
        model.addAttribute("listOfComments", comments);
        model.addAttribute("post", post);
        model.addAttribute("userId", userId);
        return "post"; // FIX: was "Post" — must match filename exactly on case-sensitive FS
    }

    @GetMapping("/createPost")
    public String createPost(Model model) {
        model.addAttribute("post", new Post());
        return "createPost";
    }

    @PostMapping("/processPost")
    public String processPost(@ModelAttribute("post") Post post, @RequestParam String tagsEntered, Principal principal) {
        String username = principal.getName();
        post.setAuthor(username);
        User user = userService.getUserByUsername(username);
        post.setUserId(user);
        Set<Tag> tags = buildTagSet(tagsEntered, ",");
        post.setTags(tags);
        postService.createPost(post);
        return "success";
    }

    @GetMapping("/Home")
    public String Home(Model model,
                       @RequestParam(defaultValue = "0") Integer pageNumber,
                       @RequestParam(defaultValue = "createdAt") String sortBy,
                       @RequestParam(defaultValue = "desc") String orderBy) {
        Sort sort = Sort.by(Sort.Direction.fromString(orderBy), sortBy);
        int pageSize = 6;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        Page<Post> posts = postService.getAllPost(pageRequest);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                model.addAttribute("userName", userDetails.getUsername());
            }
        }
        model.addAttribute("listOfPost", posts.getContent());
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPage", posts.getTotalPages());
        return "home";
    }

    @PostMapping("/deletePost")
    public String deletePost(@RequestParam Long postId, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        Post post = postService.getPostById(postId);
        if (post == null) return "redirect:/Home";
        Authority authority = authorService.getAuthorityByUser(user);
        if (user.getId().equals(post.getUserId().getId()) || authority.getRole().equals("admin")) {
            postService.deletePost(postId);
            return "redirect:/Home";
        } else {
            return "accessDenied";
        }
    }

    @PostMapping("/addComment")
    public String addComment(@RequestParam Post postId, @RequestParam String name,
                             @RequestParam String commentText, @RequestParam String email) {
        Comment comment = new Comment(name, email, commentText, postId);
        commentService.createNewComment(comment);
        return "redirect:/post?postId=" + postId.getId();
    }

    @GetMapping("/editPost")
    public String editPost(Model model, Long postId, Principal principal, Long userId) {
        Post post = postService.getPostById(postId);
        if (post == null) return "redirect:/Home";
        String loggedUserName = principal.getName();
        User loggedUser = userService.getUserByUsername(loggedUserName);
        Authority authority = authorService.getAuthorityByUser(loggedUser);
        if (post.getUserId().getId().equals(loggedUser.getId()) || authority.getRole().equals("admin")) {
            List<Tag> existingTags = tagService.getTagByPostId(postId);
            ArrayList<String> existingTagsList = new ArrayList<>();
            for (Tag tag : existingTags) existingTagsList.add(tag.getName());
            String tagsString = String.join(",", existingTagsList);
            model.addAttribute("userId", userId);
            model.addAttribute("post", post);
            model.addAttribute("existingTags", tagsString);
            return "editPost";
        } else {
            return "accessDenied";
        }
    }

    // FIX 3: was splitting updatedTags by ";" but tags are entered/stored comma-separated
    @PostMapping("/processEdit")
    public String processEdit(@ModelAttribute("post") Post post, @RequestParam Long id,
                              @RequestParam String updatedTags, Principal principal, Long userId) {
        post.setUpdatedAt(new Date());
        User user = userService.getUserById(userId);
        post.setUserId(user);
        // FIX: was split(";") — changed to split(",") to match how tags are entered
        Set<Tag> tagsSet = buildTagSet(updatedTags, ",");
        post.setTags(tagsSet);
        Post oldPost = postService.getPostById(id);
        if (oldPost != null) post.setCreatedAt(oldPost.getCreatedAt());
        postService.createPost(post);
        return "success";
    }

    @GetMapping("/search")
    public String search(Model model, @RequestParam String search) {
        List<Post> postWithSearchResult = postService.searchPost(search);
        model.addAttribute("listOfPost", postWithSearchResult);
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPage", 1);
        return "Home";
    }

    @GetMapping("/filter")
    public String filter(Model model, @RequestParam String filterBy, @RequestParam String filterString) {
        if (filterBy.equals("author")) {
            List<Post> listOfPostByAuthor = postService.listOfPostByAuthor(filterString);
            model.addAttribute("listOfPost", listOfPostByAuthor);
        } else {
            Tag tag = tagService.getTagByName(filterString);
            List<Post> listOfPostByTags = (tag != null) ? postService.listOfPostByTag(tag) : List.of();
            model.addAttribute("listOfPost", listOfPostByTags);
        }
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPage", 1);
        return "Home";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/processRegister")
    public String processRegister(@RequestParam String username, @RequestParam String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(hashPassword(password));
        userService.createUser(user);
        Authority authority = new Authority();
        authority.setUser(user);
        authority.setRole("user");
        authorService.save(authority);
        return "login";
    }

    @PostMapping("/logout")
    public String logout() {
        return "redirect:/Home";
    }

    @GetMapping("/")
    public String goToHome() {
        return "redirect:/Home";
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    /** Shared tag-building logic: splits by delimiter, trims, reuses existing tags. */
    private Set<Tag> buildTagSet(String rawTags, String delimiter) {
        Set<Tag> tags = new HashSet<>();
        if (rawTags == null || rawTags.isBlank()) return tags;
        for (String tagName : rawTags.split(delimiter)) {
            tagName = tagName.trim();
            if (tagName.isEmpty()) continue;
            Tag existing = tagService.getTagByName(tagName);
            if (existing != null) {
                tags.add(existing);
            } else {
                Tag newTag = new Tag();
                newTag.setName(tagName);
                tagService.saveTag(newTag);
                tags.add(newTag);
            }
        }
        return tags;
    }
}
