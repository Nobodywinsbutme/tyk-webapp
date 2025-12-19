package vn.id.tyk.webapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình: Khi ai đó truy cập đường dẫn /uploads/** // -> thì chui vào thư mục "uploads" ở gốc dự án để lấy file
        Path uploadDir = Paths.get("./uploads");
        // Chuyển sang URI string (tự động thêm file:/// và xử lý dấu gạch chéo đúng chuẩn OS)
        String uploadPath = uploadDir.toUri().toString();
        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}