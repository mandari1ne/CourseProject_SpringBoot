package org.example.kursach.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user_info")
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String surname;
    private String name;
    private String patronymic;
    private String department;
    private String position;
    private String email;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
