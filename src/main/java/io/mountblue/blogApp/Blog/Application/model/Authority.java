package io.mountblue.blogApp.Blog.Application.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "authorities", uniqueConstraints = @UniqueConstraint(columnNames = "userName"))
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "username", referencedColumnName = "username")
    private User user;
    @Column(name = "authority")
    private String role;
    public Authority() {
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
