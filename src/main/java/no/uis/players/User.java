package no.uis.players;

import javax.persistence.*;

@Entity
@Table(name = "Players")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "score")
    private int score;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User() {  }

    public String getUsername() {
        return username;
    }

    public long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "User [name=" + username + ", id=" + id + "]";
    }
}
