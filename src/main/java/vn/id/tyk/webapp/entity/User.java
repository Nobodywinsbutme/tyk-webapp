package vn.id.tyk.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data // Tự động sinh Getter, Setter, toString (nhờ Lombok)
@Entity // Báo cho Spring biết đây là một bảng trong DB
@Table(name = "users") // Đặt tên bảng trong SQL là 'users' (tránh trùng từ khóa 'user')
public class User {

    @Id // Đây là Khóa chính (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự động tăng (1, 2, 3...)
    private Long id;

    @Column(unique = true, nullable = false) // Không được trùng, không được để trống
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    // Số dư Coin (Mặc định là 0)
    @Column(name = "coin_balance")
    private Long coinBalance = 0L;
    
    // Role: USER hoặc ADMIN (để sau này phân quyền)
    private String role = "USER"; 

    @Column(columnDefinition = "boolean default false")
    private boolean isBanned = false; // Mặc định là không bị ban

    // Lý do bị ban (Admin ghi chú)
    private String banReason;
}