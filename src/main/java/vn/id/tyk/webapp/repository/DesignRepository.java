package vn.id.tyk.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.tyk.webapp.entity.DesignSubmission;
import vn.id.tyk.webapp.entity.User;
import java.util.Optional;
import java.util.List;

public interface DesignRepository extends JpaRepository<DesignSubmission, Long> {
    // Tìm tất cả bài viết của 1 user cụ thể (để hiển thị trang cá nhân)
    List<DesignSubmission> findByCreatorOrderByCreatedAtDesc(User creator);

    List<DesignSubmission> findByStatusOrderByCreatedAtDesc(DesignSubmission.SubmissionStatus status);
    
    // Tìm bài viết theo ID và User (để đảm bảo chỉ chủ bài viết mới được sửa/xóa)
    Optional<DesignSubmission> findByIdAndCreator(Long id, User creator);
}