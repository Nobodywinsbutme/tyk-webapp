package vn.id.tyk.webapp.dto;

import lombok.Builder;
import lombok.Data;
import vn.id.tyk.webapp.entity.User;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private Long coinBalance;

    // Hàm tiện ích để chuyển từ Entity -> DTO
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .coinBalance(user.getCoinBalance())
                .build();
    }
}