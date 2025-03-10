package app.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(
            name = "user_smartphone",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "smartphone_id")
    )
    @JsonIgnore
    private List<Smartphone> smartphones = new ArrayList<>();

    public User() {}

    public User(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Smartphone> getSmartphones() {
        return smartphones;
    }

    public void setSmartphones(List<Smartphone> smartphones) {
        this.smartphones = smartphones;
    }
}
