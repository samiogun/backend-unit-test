package com.example.backend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name" , nullable = false , length = 50 , unique = true)
    private String userName;

    @Column(name = "first_name", nullable = false , length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false , length = 50)
    private String lastName;

    public User(String userName , String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
    }
}