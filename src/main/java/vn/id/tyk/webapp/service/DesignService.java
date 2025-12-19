package vn.id.tyk.webapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.id.tyk.webapp.dto.DesignResponse;
import vn.id.tyk.webapp.entity.DesignSubmission;
import vn.id.tyk.webapp.entity.User;
import vn.id.tyk.webapp.repository.DesignRepository;
import vn.id.tyk.webapp.repository.UserRepository;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DesignService {

    @Autowired
    private DesignRepository designRepository;

    @Autowired
    private UserRepository userRepository;

    // Đường dẫn lưu ảnh
    private final Path UPLOAD_DIR = Paths.get("uploads");

    // --- LOGIC FILE (Private) ---
    private String saveFile(MultipartFile file) throws IOException {
        if (!Files.exists(UPLOAD_DIR)) Files.createDirectories(UPLOAD_DIR);
        
        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".") 
                           ? originalName.substring(originalName.lastIndexOf(".")) 
                           : ".jpg";
        
        String newName = UUID.randomUUID().toString() + extension;
        Path path = UPLOAD_DIR.resolve(newName);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        
        return "/uploads/" + newName;
    }

    private void deleteFile(String fileUrl) {
        if (fileUrl == null) return;
        try {
            String fileName = fileUrl.replace("/uploads/", "");
            Files.deleteIfExists(UPLOAD_DIR.resolve(fileName));
        } catch (IOException e) {
            e.printStackTrace(); // Log lỗi nhưng không crash app
        }
    }

    // --- LOGIC NGHIỆP VỤ ---

    // 1. Tạo mới bài viết
    public DesignResponse createDesign(String title, String desc, String category, MultipartFile image, String username) throws IOException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        DesignSubmission design = new DesignSubmission();
        design.setTitle(title);
        design.setDescription(desc);
        design.setCreator(user);
        
        // Nếu là ADMIN đăng thì Approved luôn, User thì Pending
        if ("ADMIN".equals(user.getRole())) {
            design.setStatus(DesignSubmission.SubmissionStatus.APPROVED);
            design.setApprovedBy(user);
        } else {
            design.setStatus(DesignSubmission.SubmissionStatus.PENDING);
        }

        try {
            design.setCategory(DesignSubmission.DesignCategory.valueOf(category.toUpperCase()));
        } catch (Exception e) {
            throw new RuntimeException("Category not valid");
        }

        if (image != null && !image.isEmpty()) {
            design.setImageUrl(saveFile(image));
        } else {
            throw new RuntimeException("Must have an image!");
        }

        DesignSubmission saved = designRepository.save(design);
        return DesignResponse.fromEntity(saved);
    }

    // 2. Lấy danh sách Public (Approved)
    public List<DesignResponse> getPublicDesigns() {
        return designRepository.findByStatusOrderByCreatedAtDesc(DesignSubmission.SubmissionStatus.APPROVED)
                .stream().map(DesignResponse::fromEntity).collect(Collectors.toList());
    }

    // 3. Lấy danh sách cá nhân
    public List<DesignResponse> getMyDesigns(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return designRepository.findByCreatorOrderByCreatedAtDesc(user)
                .stream().map(DesignResponse::fromEntity).collect(Collectors.toList());
    }

    // 4. Lấy danh sách chờ duyệt (Cho Admin)
    public List<DesignResponse> getPendingDesigns() {
        return designRepository.findByStatus(DesignSubmission.SubmissionStatus.PENDING)
                .stream().map(DesignResponse::fromEntity).collect(Collectors.toList());
    }

    // 5. Xóa bài viết
    public void deleteDesign(Long id, String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        DesignSubmission design;

        if ("ADMIN".equals(user.getRole())) {
            design = designRepository.findById(id).orElseThrow(() -> new RuntimeException("Cannot find design"));
        } else {
            design = designRepository.findByIdAndCreator(id, user)
                    .orElseThrow(() -> new RuntimeException("You do not have permission to delete this design"));
        }

        deleteFile(design.getImageUrl());
        designRepository.delete(design);
    }

    // 6. Cập nhật bài viết
    public DesignResponse updateDesign(Long id, String title, String desc, MultipartFile newImage, String username) throws IOException {
        User user = userRepository.findByUsername(username).orElseThrow();
        DesignSubmission design = designRepository.findByIdAndCreator(id, user)
                .orElseThrow(() -> new RuntimeException("Cannot find design or you do not have permission to edit"));

        design.setTitle(title);
        design.setDescription(desc);

        if (newImage != null && !newImage.isEmpty()) {
            deleteFile(design.getImageUrl()); // Xóa ảnh cũ
            design.setImageUrl(saveFile(newImage)); // Lưu ảnh mới
        }
        
        // Sửa xong phải duyệt lại
        design.setStatus(DesignSubmission.SubmissionStatus.PENDING);
        
        return DesignResponse.fromEntity(designRepository.save(design));
    }

    // 7. Duyệt / Từ chối bài (Admin)
    public void changeStatus(Long id, String statusStr, String adminUsername) {
        User admin = userRepository.findByUsername(adminUsername).orElseThrow();
        DesignSubmission design = designRepository.findById(id).orElseThrow(() -> new RuntimeException("Design not exist"));

        try {
            DesignSubmission.SubmissionStatus status = DesignSubmission.SubmissionStatus.valueOf(statusStr.toUpperCase());
            design.setStatus(status);
            design.setApprovedBy(admin);
            designRepository.save(design);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Condition not valid");
        }
    }
}