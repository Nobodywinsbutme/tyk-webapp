package vn.id.tyk.webapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.tyk.webapp.entity.InventoryItem;
import vn.id.tyk.webapp.entity.ItemType;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    @Query("SELECT i FROM InventoryItem i " +
           "JOIN i.itemDefinition d " +
           "WHERE i.user.id = :userId " +
           "AND (:type IS NULL OR d.type = :type) " +
           "AND (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<InventoryItem> findByUserAndFilter(
            @Param("userId") Long userId,
            @Param("type") ItemType type,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}