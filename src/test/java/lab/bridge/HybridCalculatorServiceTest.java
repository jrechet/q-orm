package lab.bridge;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lab.quarkus.entity.Gift;
import lab.quarkus.service.GiftService;
import lab.quarkus.service.HybridCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jboss.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class HybridCalculatorServiceTest {
    
    private static final Logger LOG = Logger.getLogger(HybridCalculatorServiceTest.class);
    
    @Inject
    HybridCalculatorService hybridCalculatorService;
    
    @Inject
    GiftService giftService;
    
    @BeforeEach
    @Transactional
    public void setUp() {
        // Nettoyer les données existantes
        giftService.findAll().forEach(gift -> giftService.delete(gift.id));
        
        // Créer des gifts de test
        giftService.create(new Gift("Gift 1", "Test gift 1", 10.0, "Electronics"));
        giftService.create(new Gift("Gift 2", "Test gift 2", 25.0, "Electronics"));
        giftService.create(new Gift("Gift 3", "Test gift 3", 15.0, "Books"));
        giftService.create(new Gift("Gift 4", "Test gift 4", 30.0, "Books"));
    }
    
    @Test
    @DisplayName("Test calcul du total des gifts utilisant Guice")
    public void testCalculateTotalGiftValue() {
        String result = hybridCalculatorService.calculateTotalGiftValue();
        
        assertNotNull(result);
        assertTrue(result.contains("Total Gift Value: 80,00") || result.contains("Total Gift Value: 80.00"));
        assertTrue(result.contains("GUICE-"));
        
        LOG.info("Total gift value result: " + result);
    }
    
    @Test
    @DisplayName("Test calcul du prix moyen utilisant Guice")
    public void testCalculateAverageGiftPrice() {
        String result = hybridCalculatorService.calculateAverageGiftPrice();
        
        assertNotNull(result);
        assertTrue(result.contains("Average Gift Price: 20,00") || result.contains("Average Gift Price: 20.00"));
        assertTrue(result.contains("from 4 gifts"));
        
        LOG.info("Average gift price result: " + result);
    }
    
    @Test
    @DisplayName("Test application de remise utilisant Guice")
    public void testApplyDiscountToGift() {
        // Trouver un gift existant
        Gift gift = giftService.findAll().get(0);
        
        String result = hybridCalculatorService.applyDiscountToGift(gift.id, 20.0);
        
        assertNotNull(result);
        assertTrue(result.contains(gift.name));
        assertTrue(result.contains("20,0%") || result.contains("20.0%"));
        
        LOG.info("Discount result: " + result);
    }
    
    @Test
    @DisplayName("Test statistiques des prix utilisant Guice")
    public void testGetGiftPriceStatistics() {
        String result = hybridCalculatorService.getGiftPriceStatistics();
        
        assertNotNull(result);
        assertTrue(result.contains("Stats[GUICE-"));
        assertTrue(result.contains("Count=4"));
        assertTrue(result.contains("Sum=80,00") || result.contains("Sum=80.00"));
        assertTrue(result.contains("Avg=20,00") || result.contains("Avg=20.00"));
        assertTrue(result.contains("Min=10,00") || result.contains("Min=10.00"));
        assertTrue(result.contains("Max=30,00") || result.contains("Max=30.00"));
        
        LOG.info("Gift price statistics: " + result);
    }
    
    @Test
    @DisplayName("Test validation des prix utilisant Guice")
    public void testValidateGiftPrices() {
        String result = hybridCalculatorService.validateGiftPrices();
        
        assertNotNull(result);
        assertTrue(result.contains("All gift prices are valid"));
        assertTrue(result.contains("4 gifts checked"));
        
        LOG.info("Gift price validation result: " + result);
    }
    
    @Test
    @DisplayName("Test logique métier complexe par catégorie")
    public void testProcessGiftBusinessLogic() {
        String result = hybridCalculatorService.processGiftBusinessLogic("Electronics", 1.5);
        
        assertNotNull(result);
        assertTrue(result.contains("Business Logic Results for category 'Electronics'"));
        assertTrue(result.contains("Gift 1"));
        assertTrue(result.contains("Gift 2"));
        assertTrue(result.contains("Processed[GUICE-"));
        
        LOG.info("Business logic result: " + result);
    }
    
    @Test
    @DisplayName("Test informations du service Guice")
    public void testGetGuiceServiceInfo() {
        String result = hybridCalculatorService.getGuiceServiceInfo();
        
        assertNotNull(result);
        assertTrue(result.contains("Using Guice Service: GUICE-"));
        
        LOG.info("Guice service info: " + result);
    }
    
    @Test
    @DisplayName("Test avec catégorie inexistante")
    public void testProcessGiftBusinessLogicWithInvalidCategory() {
        String result = hybridCalculatorService.processGiftBusinessLogic("NonExistent", 2.0);
        
        assertNotNull(result);
        assertEquals("No gifts found in category: NonExistent", result);
        
        LOG.info("Invalid category result: " + result);
    }
    
    @Test
    @DisplayName("Test avec gift ID inexistant")
    public void testApplyDiscountToInvalidGift() {
        String result = hybridCalculatorService.applyDiscountToGift(999L, 10.0);
        
        assertNotNull(result);
        assertEquals("Gift not found: 999", result);
        
        LOG.info("Invalid gift ID result: " + result);
    }
}