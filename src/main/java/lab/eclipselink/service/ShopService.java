package lab.eclipselink.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lab.eclipselink.entity.Shop;
import lab.eclipselink.repository.ShopRepository;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ShopService {
    
    @Inject
    ShopRepository shopRepository;
    
    public Shop create(Shop shop) {
        return shopRepository.save(shop);
    }
    
    public List<Shop> findAll() {
        return shopRepository.findAll();
    }
    
    public Optional<Shop> findById(Long id) {
        return shopRepository.findById(id);
    }
    
    public Shop update(Long id, Shop updatedShop) {
        Optional<Shop> existingShop = shopRepository.findById(id);
        if (existingShop.isPresent()) {
            Shop shop = existingShop.get();
            shop.setName(updatedShop.getName());
            shop.setAddress(updatedShop.getAddress());
            shop.setCity(updatedShop.getCity());
            shop.setPhone(updatedShop.getPhone());
            shop.setCategory(updatedShop.getCategory());
            return shopRepository.save(shop);
        }
        return null;
    }
    
    public boolean delete(Long id) {
        Optional<Shop> shop = shopRepository.findById(id);
        if (shop.isPresent()) {
            shopRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<Shop> findByCity(String city) {
        return shopRepository.findByCity(city);
    }
    
    public List<Shop> findByCategory(String category) {
        return shopRepository.findByCategory(category);
    }
    
    public List<Shop> findByNameContaining(String name) {
        return shopRepository.findByNameContaining(name);
    }
}