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

    @GetMapping("/login")
    public String login(){
        return "login";
    }
    @GetMapping("/post")
    public String getPostById(Model model,@RequestParam Long postId){
        Post post=postService.getPostById(postId);
        String author=post.getAuthor();
        User user=userService.getUserByUsername(author);
        Long userId=user.getId();
        List<Comment> comments=commentService.getCommentByPostId(postId);
        if(post==null){
            System.out.println("empty");
            return null;
        }
        model.addAttribute("listOfComments",comments);
        model.addAttribute("post", post);
        model.addAttribute("userId",userId);
        return "Post";
    }
    @GetMapping("/createPost")
    public String createPost(Model model){
        model.addAttribute("post",new Post());
        return "createPost";
    }
    @PostMapping("/processPost")
    public String processPost(@ModelAttribute("post") Post post, @RequestParam String tagsEntered, Principal principal) {
        String[] tagSplitted = tagsEntered.split(",");
        String username=principal.getName();
        post.setAuthor(username);
        User user=userService.getUserByUsername(username);
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
        return "success";
    }
    @GetMapping("/Home")
    public String Home(Model model,@RequestParam (defaultValue = "0") Integer pageNumber,
                       @RequestParam(defaultValue = "createdAt") String sortBy,
                       @RequestParam(defaultValue = "asc") String orderBy){
        Sort sort=Sort.by(Sort.Direction.fromString(orderBy),sortBy);
        int pageSize=6;
        PageRequest pageRequest=PageRequest.of(pageNumber,pageSize,sort);
        Page<Post>posts=postService.getAllPost(pageRequest);
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String username=null;
        if(authentication!=null){
            Object principal=authentication.getPrincipal();
            if(principal instanceof UserDetails){
                UserDetails userDetails=(UserDetails) principal;
                String userName=userDetails.getUsername();
                model.addAttribute("userName",userName);
            }
        }
        model.addAttribute("listOfPost",posts.getContent());
        model.addAttribute("currentPage",pageNumber);
        model.addAttribute("totalPage",posts.getTotalPages());
        return "Home";
    }
    @PostMapping("/deletePost")
    public String deletePost(@RequestParam Long postId,Principal principal){
        User user=userService.getUserByUsername(principal.getName());
        Post post=postService.getPostById(postId);
        User loggedUser=userService.getUserByUsername(principal.getName());
        Authority authority=authorService.getAuthorityByUser(user);
        if(user.getId().equals(post.getUserId().getId())|| authority.getRole().equals("admin")) {
           postService.deletePost(postId);
          return "redirect:/Home";
       }
       else{
            return "accessDenied";
       }
    }
    @PostMapping("/addComment")
    public String addComment(@RequestParam Post postId, @RequestParam String name,@RequestParam String commentText,@RequestParam String email){
        Comment comment=new Comment(name,email,commentText,postId);
        commentService.createNewComment(comment);
        String redirectTo="redirect:/post?postId="+postId.getId();
        return redirectTo;
    }
    @GetMapping("/editPost")
    public String editPost(Model model,Long postId,Principal principal, Long userId) {
        System.out.println(userId);
        Post post = postService.getPostById(postId);
        String loggedUserName=principal.getName();
        User loggedUser = userService.getUserByUsername(loggedUserName);
        Authority authority=authorService.getAuthorityByUser(loggedUser);
        if (post.getUserId().getId().equals(loggedUser.getId()) || authority.getRole().equals("admin")){
            List<Tag> existingTags = tagService.getTagByPostId(postId);
            ArrayList<String> existingTagsList = new ArrayList<>();
            for (Tag tag : existingTags) {
                existingTagsList.add(tag.getName());
            }
            String tagsString = String.join(",", existingTagsList);
            model.addAttribute("userId",userId);
            model.addAttribute("post", post);
            model.addAttribute("existingTags", tagsString);
            return "editPost";
        }
        else{
            System.out.println(post.getUserId());
            System.out.println(loggedUser.getId());
            return "accessDenied";
        }
    }
    @PostMapping("/processEdit")
    public String processEdit(@ModelAttribute("post") Post post,@RequestParam Long id,
                              @RequestParam String updatedTags,Principal principal,Long userId)  {
     post.setUpdatedAt(new Date());
     System.out.println(userId);
     User user=userService.getUserById(userId);
     post.setUserId(user);
     String[] tagsArray=updatedTags.split(";");
        Set<Tag> tagsSet = new HashSet<>();
        for (String tagName : tagsArray) {
            tagName = tagName.trim();
            Tag existingTag = tagService.getTagByName(tagName);
            if (existingTag != null) {
                tagsSet.add(existingTag);
            } else {
                Tag newTag = new Tag();
                newTag.setName(tagName);
                tagService.saveTag(newTag);
                tagsSet.add(newTag);
            }
        }
     post.setTags(tagsSet);
     Post oldPost=postService.getPostById(id);
     post.setCreatedAt(oldPost.getCreatedAt());
     postService.createPost(post);

     return "success";
    }
    @GetMapping("/search")
    public String search(Model model,@RequestParam String search){
        List<Post>postWithSearchResult=postService.searchPost(search);
        model.addAttribute("listOfPost",postWithSearchResult);
        model.addAttribute("currentPage",0);
        model.addAttribute("totalPage",1);
        return "Home";
    }
    @GetMapping("/filter")
    public String filter(Model model,@RequestParam String filterBy,@RequestParam String filterString){
        if(filterBy.equals("author")){
            List<Post>listOfPostByAuthor=postService.listOfPostByAuthor(filterString);
            model.addAttribute("listOfPost",listOfPostByAuthor);
            model.addAttribute("currentPage",0);
            model.addAttribute("totalPage",1);
            return "Home";
        }
        else {
            Tag tag=tagService.getTagByName(filterString);
            List<Post>listOfPostByTags=postService.listOfPostByTag(tag);
            model.addAttribute("listOfPost",listOfPostByTags);
            model.addAttribute("currentPage",0);
            model.addAttribute("totalPage",1);
            return "Home";
        }
    }
    @GetMapping("/register")
    public String register(){
        return "register";
    }
    @PostMapping("/processRegister")
    public String processRegister(@RequestParam String username, @RequestParam String password,String email){
       User user=new User();
       user.setUsername(username);
       user.setEmail(email);
       String encryptedPassword=hashPassword(password);
       user.setPassword(encryptedPassword);
       userService.createUser(user);
       Authority authority=new Authority();
       authority.setUser(user);
       authority.setRole("user");
       authorService.save(authority);
       return "login";
    }
    @PostMapping("/logout")
    public String logout(){
        return "redirect:/Home";
    }
    @GetMapping("/")
    public String goToHome(){
        String redirectTo="redirect:/Home";
        return redirectTo;
    }
    private String hashPassword(String plainTextPassword) {
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(plainTextPassword, salt);
    }

}