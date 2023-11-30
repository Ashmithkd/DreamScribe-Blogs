package io.mountblue.blogApp.Blog.Application.service;
import io.mountblue.blogApp.Blog.Application.model.User;
import io.mountblue.blogApp.Blog.Application.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserRepository  userRepository;
    public User createUser(User user) {
       return userRepository.save(user);
    }


    public User getUserByUsername(String username) {
        return userRepository.findUsernameByUsername(username);
    }

    public User getUserById(Long userId) {
        return userRepository.findUserById(userId);
    }
}
