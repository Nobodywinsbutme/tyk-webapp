package vn.id.tyk.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import vn.id.tyk.webapp.entity.DesignSubmission;
import vn.id.tyk.webapp.entity.User;
import vn.id.tyk.webapp.repository.DesignRepository;
import vn.id.tyk.webapp.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/designs")
public class DesignController {

    @Autowired
    private DesignRepository designRepository;

    @Autowired
    private UserRepository userRepository;

    // Đường dẫn thư mục upload (Nên đưa vào file properties, nhưng để đây cho gọn)
    private final Path UPLOAD_DIR = Paths.get("uploads");

    // --- HELPER: Hàm lưu file dùng chung ---
    private String saveFile(MultipartFile file) throws IOException {
        if (!Files.exists(UPLOAD_DIR)) {
            Files.createDirectories(UPLOAD_DIR);
        }
        // Lấy đuôi file (ví dụ .jpg, .png) để an toàn hơn
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // Tạo tên file ngẫu nhiên để tránh trùng tên và lỗi ký tự đặc biệt
        String newFileName = UUID.randomUUID().toString() + extension;
        
        Path filePath = UPLOAD_DIR.resolve(newFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return "/uploads/" + newFileName; // Trả về đường dẫn web
    }

    // --- HELPER: Hàm xóa file vật lý ---
    private void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
                String fileName = fileUrl.replace("/uploads/", ""); // Lấy tên file gốc
                Path filePath = UPLOAD_DIR.resolve(fileName);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Log lỗi nhưng không chặn luồng chính
        }
    }

    // 1. Gửi bài mới
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDesign(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).body("Vui lòng đăng nhập!");
        
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        try {
            DesignSubmission design = new DesignSubmission();
            design.setTitle(title);
            design.setDescription(description);
            design.setCreator(user);
            design.setStatus(DesignSubmission.SubmissionStatus.PENDING);

            try {
                design.setCategory(DesignSubmission.DesignCategory.valueOf(category.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Category không hợp lệ (MAP, SKIN, WEAPON)");
            }

            // Xử lý ảnh upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String savedPath = saveFile(imageFile);
                design.setImageUrl(savedPath);
            }

            designRepository.save(design);
            return ResponseEntity.ok("Đã gửi bản thiết kế thành công!");

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Lỗi khi lưu ảnh: " + e.getMessage());
        }
    }

    // 2. Lấy danh sách bài của TÔI
    @GetMapping("/my-designs")
    public ResponseEntity<?> getMyDesigns(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body("Chưa đăng nhập");
        
        User user = userRepository.findByUsername(userDetails.getUsername())
                 .orElseThrow(() -> new RuntimeException("User not found"));
                 
        List<DesignSubmission> myList = designRepository.findByCreatorOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(myList);
    }

    // 3. Xóa bài (Kèm xóa file ảnh)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDesign(@PathVariable Long id, 
                                          @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<DesignSubmission> designOpt = designRepository.findByIdAndCreator(id, user);
        
        if (designOpt.isPresent()) {
            DesignSubmission design = designOpt.get();
            
            // 1. Xóa file ảnh cũ trên ổ cứng để dọn rác
            deleteFile(design.getImageUrl());

            // 2. Xóa record trong DB
            designRepository.delete(design);
            return ResponseEntity.ok("Đã xóa bài viết và ảnh đính kèm.");
        } else {
            return ResponseEntity.badRequest().body("Không tìm thấy bài viết hoặc bạn không có quyền xóa!");
        }
    }

    // 4. Sửa bài (Có hỗ trợ đổi ảnh mới)
    // Lưu ý: Không dùng @RequestBody Class, mà dùng @RequestParam để nhận file
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateDesign(@PathVariable Long id,
                                          @RequestParam("title") String title,
                                          @RequestParam("description") String description,
                                          @RequestParam(value = "image", required = false) MultipartFile newImageFile, // Ảnh mới (nếu có)
                                          @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<DesignSubmission> designOpt = designRepository.findByIdAndCreator(id, user);

        if (designOpt.isPresent()) {
            DesignSubmission design = designOpt.get();
            
            try {
                // Cập nhật thông tin text
                design.setTitle(title);
                design.setDescription(description);
                
                // Nếu người dùng upload ảnh mới
                if (newImageFile != null && !newImageFile.isEmpty()) {
                    // 1. Xóa ảnh cũ đi cho đỡ rác
                    deleteFile(design.getImageUrl());
                    
                    // 2. Lưu ảnh mới
                    String newPath = saveFile(newImageFile);
                    design.setImageUrl(newPath);
                }

                // Reset trạng thái về PENDING
                design.setStatus(DesignSubmission.SubmissionStatus.PENDING);
                
                designRepository.save(design);
                return ResponseEntity.ok("Đã cập nhật! Bài viết chuyển về trạng thái chờ duyệt.");
                
            } catch (IOException e) {
                return ResponseEntity.internalServerError().body("Lỗi khi lưu ảnh mới: " + e.getMessage());
            }
        } else {
            return ResponseEntity.badRequest().body("Lỗi cập nhật! Không tìm thấy bài.");
        }
    }

    // API CÔNG KHAI: Lấy danh sách bài đã duyệt (Cho khách xem)
    @GetMapping("/public")
    public ResponseEntity<?> getPublicDesigns() {
        List<DesignSubmission> list = designRepository.findByStatusOrderByCreatedAtDesc(DesignSubmission.SubmissionStatus.APPROVED);
        return ResponseEntity.ok(list);
    }
}