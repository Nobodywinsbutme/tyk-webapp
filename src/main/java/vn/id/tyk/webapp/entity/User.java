package vn.id.tyk.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity 
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    @Column(name = "coin_balance")
    private Long coinBalance = 0L;
    
    private String role = "USER"; 

    @Column(columnDefinition = "boolean default false")
    private boolean isBanned = false; 

    private String banReason;

    private String avatarUrl;
}