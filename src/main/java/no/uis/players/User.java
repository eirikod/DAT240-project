package no.uis.players;

import javax.persistence.*;
import java.util.Random;

@Entity
@Table(name = "Players")
public class User {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id = "" + Math.abs(new Random().nextLong());

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

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public int getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "User [name=" + username + ", id=" + id + "]";
    }
}
