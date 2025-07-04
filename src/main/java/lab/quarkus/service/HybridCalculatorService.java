package lab.quarkus.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lab.guice.service.PureGuiceService;
import lab.quarkus.entity.Gift;
import lab.quarkus.repository.GiftRepository;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Quarkus CDI qui utilise un composant Guice (PureGuiceService).
 * Démontre l'injection de composants Guice dans Quarkus CDI.
 */
@ApplicationScoped
public class HybridCalculatorService {
    
    private static final Logger LOG = Logger.getLogger(HybridCalculatorService.class);
    
    @Inject
    PureGuiceService guiceCalculator; // Service Guice injecté dans CDI !
    
    @Inject
    GiftRepository giftRepository; // Service CDI natif
    
    /**
     * Calcule le prix total de tous les gifts en utilisant le service Guice
     */
    public String calculateTotalGiftValue() {
        LOG.info("Calculating total gift value using Guice calculator");
        
        List<Gift> allGifts = giftRepository.listAll();
        double total = 0.0;
        
        for (Gift gift : allGifts) {
            total = guiceCalculator.calculate(total, gift.price, "add");
        }
        
        String formatted = guiceCalculator.formatResult(total);
        return "Total Gift Value: " + formatted + " (calculated by " + guiceCalculator.getServiceId() + ")";
    }
    
    /**
     * Calcule le prix moyen des gifts en utilisant Guice
     */
    public String calculateAverageGiftPrice() {
        LOG.info("Calculating average gift price using Guice calculator");
        
        List<Gift> allGifts = giftRepository.listAll();
        if (allGifts.isEmpty()) {
            return "No gifts available for average calculation";
        }
        
        double total = 0.0;
        for (Gift gift : allGifts) {
            total = guiceCalculator.calculate(total, gift.price, "add");
        }
        
        double average = guiceCalculator.calculate(total, (double) allGifts.size(), "divide");
        String formatted = guiceCalculator.formatResult(average);
        
        return "Average Gift Price: " + formatted + " (from " + allGifts.size() + " gifts)";
    }
    
    /**
     * Applique une remise en utilisant le service Guice
     */
    public String applyDiscountToGift(Long giftId, double discountPercent) {
        LOG.info("Applying discount to gift ID: " + giftId + " using Guice calculator");
        
        Gift gift = giftRepository.findById(giftId);
        if (gift == null) {
            return "Gift not found: " + giftId;
        }
        
        // Calcul de la remise avec Guice
        double discountAmount = guiceCalculator.calculate(gift.price, discountPercent, "multiply");
        discountAmount = guiceCalculator.calculate(discountAmount, 100.0, "divide");
        
        double newPrice = guiceCalculator.calculate(gift.price, discountAmount, "subtract");
        
        String oldPriceStr = guiceCalculator.formatResult(gift.price);
        String newPriceStr = guiceCalculator.formatResult(newPrice);
        String discountStr = guiceCalculator.formatResult(discountAmount);
        
        return String.format("Gift '%s': %s -> %s (discount: %s, %.1f%%)", 
                           gift.name, oldPriceStr, newPriceStr, discountStr, discountPercent);
    }
    
    /**
     * Statistiques des prix des gifts utilisant Guice
     */
    public String getGiftPriceStatistics() {
        LOG.info("Getting gift price statistics using Guice calculator");
        
        List<Gift> allGifts = giftRepository.listAll();
        if (allGifts.isEmpty()) {
            return "No gifts available for statistics";
        }
        
        double[] prices = allGifts.stream()
            .mapToDouble(gift -> gift.price)
            .toArray();
            
        return guiceCalculator.getStats(prices);
    }
    
    /**
     * Validation des données gift utilisant Guice
     */
    public String validateGiftPrices() {
        LOG.info("Validating gift prices using Guice calculator");
        
        List<Gift> allGifts = giftRepository.listAll();
        List<Gift> invalidGifts = allGifts.stream()
            .filter(gift -> !guiceCalculator.isValidNumber(String.valueOf(gift.price)))
            .collect(Collectors.toList());
            
        if (invalidGifts.isEmpty()) {
            return "All gift prices are valid (" + allGifts.size() + " gifts checked)";
        } else {
            return "Found " + invalidGifts.size() + " gifts with invalid prices";
        }
    }
    
    /**
     * Logique métier complexe combinant CDI et Guice
     */
    public String processGiftBusinessLogic(String category, double multiplier) {
        LOG.info("Processing gift business logic for category: " + category);
        
        List<Gift> categoryGifts = giftRepository.findByCategory(category);
        if (categoryGifts.isEmpty()) {
            return "No gifts found in category: " + category;
        }
        
        StringBuilder result = new StringBuilder();
        result.append("Business Logic Results for category '").append(category).append("':\n");
        
        for (Gift gift : categoryGifts) {
            String businessResult = guiceCalculator.processBusinessLogic(
                String.valueOf(gift.price), multiplier
            );
            result.append("- ").append(gift.name).append(": ").append(businessResult).append("\n");
        }
        
        return result.toString();
    }
    
    /**
     * Retourne les informations du service Guice utilisé
     */
    public String getGuiceServiceInfo() {
        return "Using Guice Service: " + guiceCalculator.getServiceId();
    }
}