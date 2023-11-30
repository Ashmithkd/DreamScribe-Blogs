package io.mountblue.blogApp.Blog.Application.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.engine.internal.Cascade;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="title")
    private String title;
    @Column(name="author")
    private String author;
    @Column(name="content")
    private String content;
    @Column(name="excerpt")
    private String excerpt;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdAt")
    @CreationTimestamp
    private Date createdAt;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="publishedAt")
    private Date publishedAt;
    @Column(name="isPublished")
    private boolean isPublished;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="updatedAt")
    private Date updatedAt;
    @ManyToMany(cascade = { CascadeType.PERSIST,CascadeType.MERGE})
    @JsonIgnore
    @JoinTable( name = "post_tags",joinColumns=@JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;
    @JsonIgnore
    @OneToMany(mappedBy="post", cascade = CascadeType.ALL) //any change on post-same change to comments
    private List<Comment>comments;
    @ManyToOne
    @JoinColumn(name ="userId")
    private User userId;

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getExcerpt() {
    return excerpt;
  }

  public void setExcerpt(String excerpt) {
    this.excerpt = excerpt;
  }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    //prePersist and preUpdate automatically updates the dates
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
    public Date getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getPublishedAt() {
    return publishedAt;
  }

  public void setPublishedAt(Date publishedAt) {
    this.publishedAt = publishedAt;
  }

  public boolean isPublished() {
    return isPublished;
  }

  public void setPublished(boolean published) {
    isPublished = published;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Set<Tag> getTags() {
    return tags;
  }

  public void setTags(Set<Tag> tags) {
    this.tags = tags;
  }

  public List<Comment> getComments() {
    return comments;
  }

  public void setComments(List<Comment> comments) {
    this.comments = comments;
  }
}
