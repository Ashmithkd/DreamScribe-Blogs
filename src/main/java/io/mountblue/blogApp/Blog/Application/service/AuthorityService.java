package io.mountblue.blogApp.Blog.Application.service;

import io.mountblue.blogApp.Blog.Application.model.Authority;
import io.mountblue.blogApp.Blog.Application.model.User;
import io.mountblue.blogApp.Blog.Application.repository.AuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorityService {
    @Autowired
    AuthorityRepository authorityRepository;
    public Authority save(Authority authority){
        return authorityRepository.save(authority);
    }

    public Authority getAuthorityByUser(User loggedUser) {
        return authorityRepository.getAuthorityByUser(loggedUser);
    }
}
