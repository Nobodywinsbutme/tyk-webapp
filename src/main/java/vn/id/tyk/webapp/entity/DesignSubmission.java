package vn.id.tyk.webapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter // Dùng Getter/Setter thay vì @Data cho Entity để an toàn hơn
@Entity
@Table(name = "design_submissions")
public class DesignSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; 

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String imageUrl; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DesignCategory category;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "designs", "role"})
    private User creator;

    // Không gán new Date() ở đây
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "designs"})
    private User approvedBy;

    // Tự động gán ngày giờ ngay trước khi lưu vào DB
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- ENUM ---
    public enum DesignCategory {
        MAP, SKIN, WEAPON
    }

    public enum SubmissionStatus {
        PENDING, APPROVED, REJECTED
    }
}