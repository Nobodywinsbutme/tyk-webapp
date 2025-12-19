package vn.id.tyk.webapp.service;

import vn.id.tyk.webapp.dto.UpdateProfileDTO;
import vn.id.tyk.webapp.entity.User;
import vn.id.tyk.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 1. Hàm tìm User (Logic xử lý việc mất session)
    // Controller chỉ việc gọi, không cần biết bên trong tìm bằng cách nào
    public User getAuthenticatedUser(HttpSession session, Principal principal) {
        User sessionUser = (User) session.getAttribute("tyk_user");
        if (sessionUser != null) return sessionUser;

        if (principal != null) {
            return userRepository.findByUsername(principal.getName()).orElse(null);
        }
        return null;
    }

    // 2. Hàm update Profile (Logic nghiệp vụ)
    public void updateProfile(User user, UpdateProfileDTO dto) {
        // Chỉ cập nhật nếu có dữ liệu gửi lên
        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            // user.setFullName(dto.getFullName()); // Bỏ comment nếu User có field này
        }
        
        if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        
        // Lưu xuống DB
        userRepository.save(user);
    }
}