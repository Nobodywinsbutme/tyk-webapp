package vn.id.tyk.webapp.dto;
import lombok.Data;

@Data
public class LoginRequestDTO {
    private String username;
    private String password;
}