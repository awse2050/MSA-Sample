package com.example.userservice.jpa;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "users")
@Entity
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;
    private String name;
    @Column(unique = true)
    private String userId;
    @Column(unique = true)
    private String encryptedPwed;
}
