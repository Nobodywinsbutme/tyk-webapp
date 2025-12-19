package vn.id.tyk.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Data
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT") // Để lưu được nội dung dài
    private String description;

    private LocalDateTime createdAt;

    // Lưu người tạo (Admin nào viết bài này)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author; 
}
