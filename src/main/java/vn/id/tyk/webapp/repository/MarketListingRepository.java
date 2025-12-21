package vn.id.tyk.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.id.tyk.webapp.entity.MarketListing;
import vn.id.tyk.webapp.entity.ItemType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MarketListingRepository extends JpaRepository<MarketListing, Long> {

    // CHÚ Ý: m.item.itemDefinition (Phải đi qua item)
    @Query("SELECT m FROM MarketListing m " +
           "WHERE m.status = 'ACTIVE' " +
           "AND (:type IS NULL OR m.itemDefinition.type = :type) " +
           "AND (:minPrice IS NULL OR m.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR m.price <= :maxPrice) " +
           "AND (:search IS NULL OR LOWER(m.itemDefinition.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<MarketListing> filterMarket(
        @Param("type") ItemType type, 
        @Param("minPrice") Double minPrice, 
        @Param("maxPrice") Double maxPrice, 
        @Param("search") String search
    );
    Page<MarketListing> findBySellerIdAndStatus(Long sellerId, String status, Pageable pageable);
    List<MarketListing> findTop10ByStatusOrderByListedAtDesc(String status);
}