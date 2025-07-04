package lab.guice.service;

import lab.quarkus.entity.Gift;
import lab.quarkus.service.GiftService;
import lab.quarkus.repository.GiftRepository;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Service Guice qui utilise les composants Quarkus (GiftService et GiftRepository).
 * Démontre l'intégration entre Guice et Quarkus CDI.
 * Note: Pas d'annotations Guice ici pour éviter les conflits avec Quarkus CDI
 */
public class GuiceGiftService {
    
    private static final Logger LOG = Logger.getLogger(GuiceGiftService.class);
    
    private final GiftService quarkusGiftService;
    private final GiftRepository quarkusGiftRepository;
    
    public GuiceGiftService(GiftService quarkusGiftService, GiftRepository quarkusGiftRepository) {
        this.quarkusGiftService = quarkusGiftService;
        this.quarkusGiftRepository = quarkusGiftRepository;
        LOG.info("GuiceGiftService initialized with Quarkus components");
    }
    
    /**
     * Liste tous les gifts via le service Quarkus
     */
    public List<Gift> listAllGifts() {
        LOG.info("Listing all gifts via Quarkus service");
        return quarkusGiftService.findAll();
    }
    
    /**
     * Trouve un gift par ID via le repository Quarkus
     */
    public Optional<Gift> findGiftById(Long id) {
        LOG.info("Finding gift by ID: " + id + " via Quarkus repository");
        return quarkusGiftRepository.findByIdOptional(id);
    }
    
    /**
     * Crée un nouveau gift via le service Quarkus
     */
    public Gift createGift(String name, String description, double price, String category) {
        LOG.info("Creating new gift: " + name + " via Quarkus service");
        Gift gift = new Gift(name, description, price, category);
        return quarkusGiftService.create(gift);
    }
    
    /**
     * Met à jour un gift existant via le service Quarkus
     */
    public Gift updateGift(Long id, String name, String description, double price, String category) {
        LOG.info("Updating gift ID: " + id + " via Quarkus service");
        Gift updatedGift = new Gift(name, description, price, category);
        return quarkusGiftService.update(id, updatedGift);
    }
    
    /**
     * Supprime un gift par ID via le service Quarkus
     */
    public void deleteGift(Long id) {
        LOG.info("Deleting gift ID: " + id + " via Quarkus service");
        quarkusGiftService.delete(id);
    }
    
    /**
     * Trouve des gifts par catégorie via le repository Quarkus
     */
    public List<Gift> findGiftsByCategory(String category) {
        LOG.info("Finding gifts by category: " + category + " via Quarkus repository");
        return quarkusGiftRepository.findByCategory(category);
    }
    
    /**
     * Trouve des gifts dans une gamme de prix via le repository Quarkus
     */
    public List<Gift> findGiftsByPriceRange(double minPrice, double maxPrice) {
        LOG.info("Finding gifts by price range: " + minPrice + "-" + maxPrice + " via Quarkus repository");
        return quarkusGiftRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    /**
     * Retourne le nombre total de gifts via le repository Quarkus
     */
    public long getTotalGiftsCount() {
        LOG.info("Getting total gifts count via Quarkus repository");
        return quarkusGiftRepository.count();
    }
    
    /**
     * Méthode utilitaire pour afficher les informations des gifts
     */
    public void displayGiftInfo(Gift gift) {
        LOG.info("Gift info: ID=" + gift.id + ", Name=" + gift.name + 
                ", Price=" + gift.price + ", Category=" + gift.category);
    }
    
    /**
     * Exemple d'opération business qui combine plusieurs appels
     */
    public String getGiftSummary() {
        long totalCount = getTotalGiftsCount();
        List<Gift> allGifts = listAllGifts();
        
        double totalValue = allGifts.stream()
            .mapToDouble(gift -> gift.price)
            .sum();
            
        double averagePrice = totalCount > 0 ? totalValue / totalCount : 0.0;
        
        String summary = String.format(
            "Gift Summary: Total=%d, Total Value=%.2f, Average Price=%.2f", 
            totalCount, totalValue, averagePrice
        );
        
        LOG.info(summary);
        return summary;
    }
}