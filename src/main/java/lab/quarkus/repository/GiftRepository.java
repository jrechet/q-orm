package lab.quarkus.repository;

import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import lab.quarkus.entity.Gift;
import java.util.List;

@ApplicationScoped
public class GiftRepository implements PanacheRepository<Gift> {
    
    public List<Gift> findByCategory(String category) {
        return find("category", category).list();
    }
    
    public List<Gift> findByPriceRange(Double minPrice, Double maxPrice) {
        return find("price >= ?1 and price <= ?2", minPrice, maxPrice).list();
    }
    
    public List<Gift> findByNameContaining(String name) {
        return find("name like ?1", "%" + name + "%").list();
    }
}