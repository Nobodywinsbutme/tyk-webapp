package vn.id.tyk.webapp.repository;

import vn.id.tyk.webapp.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findAllByOrderByCreatedAtDesc();
}
