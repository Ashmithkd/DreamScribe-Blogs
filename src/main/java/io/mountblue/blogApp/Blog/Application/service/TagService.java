package io.mountblue.blogApp.Blog.Application.service;

import io.mountblue.blogApp.Blog.Application.model.Tag;
import io.mountblue.blogApp.Blog.Application.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
public class TagService {
    @Autowired
    TagRepository tagRepository;
    public boolean checkIfTagExists(String tag){
        return tagRepository.existsByName(tag);
    }
    public Tag getTagByName(String tagName) {
        return tagRepository.findByName(tagName).orElse(null);
    }

    public void saveTag(Tag newTag) {
        tagRepository.save(newTag);
    }
    public List<Tag> getTagByPostId(Long postId) {
        return tagRepository.findTagsByPostId(postId);
    }
}
