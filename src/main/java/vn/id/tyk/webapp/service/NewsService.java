package vn.id.tyk.webapp.service;

import vn.id.tyk.webapp.dto.NewsDTO;
import vn.id.tyk.webapp.entity.News;
import vn.id.tyk.webapp.entity.User;
import vn.id.tyk.webapp.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;

    public News createNews(NewsDTO newsDTO, User adminUser) {
        News news = new News();
        news.setTitle(newsDTO.getTitle());
        news.setDescription(newsDTO.getDescription());
        news.setCreatedAt(LocalDateTime.now());
        news.setAuthor(adminUser); // Lưu vết admin nào đã tạo

        return newsRepository.save(news);
    }

    public Iterable<News> getAllNews() {
        return newsRepository.findAllByOrderByCreatedAtDesc();
    }

    public News updateNews(Long id, NewsDTO newsDTO) throws Exception {
        News existingNews = newsRepository.findById(id)
                .orElseThrow(() -> new Exception("News not found with id: " + id));

        existingNews.setTitle(newsDTO.getTitle());
        existingNews.setDescription(newsDTO.getDescription());
        // Có thể cập nhật thêm các trường khác nếu cần

        return newsRepository.save(existingNews);
    }

    public void deleteNews(Long id) throws Exception {
        News existingNews = newsRepository.findById(id)
                .orElseThrow(() -> new Exception("News not found with id: " + id));

        newsRepository.delete(existingNews);
    }
}
