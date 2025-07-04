package lab.quarkus.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lab.quarkus.entity.Gift;
import lab.quarkus.repository.GiftRepository;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GiftService {
    
    @Inject
    GiftRepository giftRepository;
    
    @Transactional
    public Gift create(Gift gift) {
        giftRepository.persist(gift);
        return gift;
    }
    
    public List<Gift> findAll() {
        return giftRepository.listAll();
    }
    
    public Optional<Gift> findById(Long id) {
        return giftRepository.findByIdOptional(id);
    }
    
    @Transactional
    public Gift update(Long id, Gift updatedGift) {
        Gift gift = giftRepository.findById(id);
        if (gift != null) {
            gift.name = updatedGift.name;
            gift.description = updatedGift.description;
            gift.price = updatedGift.price;
            gift.category = updatedGift.category;
            giftRepository.persist(gift);
        }
        return gift;
    }
    
    @Transactional
    public boolean delete(Long id) {
        return giftRepository.deleteById(id);
    }
    
    public List<Gift> findByCategory(String category) {
        return giftRepository.findByCategory(category);
    }
    
    public List<Gift> findByPriceRange(Double minPrice, Double maxPrice) {
        return giftRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    public List<Gift> findByNameContaining(String name) {
        return giftRepository.findByNameContaining(name);
    }
}