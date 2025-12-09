package vn.id.tyk.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.tyk.webapp.entity.User;
import java.util.Optional;

// JpaRepository<User, Long> nghĩa là: Quản lý bảng User, khóa chính kiểu Long
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Boot tự động hiểu hàm này: "SELECT * FROM users WHERE username = ?"
    Optional<User> findByUsername(String username);
    
    // Kiểm tra xem user tồn tại chưa
    boolean existsByUsername(String username);
}