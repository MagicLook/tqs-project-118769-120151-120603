package com.MagicLook.data;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    private String telephone;

    @Column(nullable = false, length = 60)
    private String password;


    // Constructors
    public User() {

    }

    public User(String firstName, String lastName, String email, String telephone, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.telephone = telephone;
        this.password = password;
    }

}
