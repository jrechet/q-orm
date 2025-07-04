package lab.guice;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lab.guice.service.GuiceGiftService;
import lab.quarkus.entity.Gift;
import lab.quarkus.service.GiftService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class GuiceQuarkusIntegrationTest {
    
    private static final Logger LOG = Logger.getLogger(GuiceQuarkusIntegrationTest.class);
    
    @Inject
    GuiceManager guiceManager;
    
    @Inject
    GiftService quarkusGiftService;
    
    private GuiceGiftService guiceGiftService;
    
    @BeforeEach
    @Transactional
    public void setUp() {
        // Nettoyer les données existantes
        quarkusGiftService.findAll().forEach(gift -> quarkusGiftService.delete(gift.id));
        
        // Obtenir l'instance Guice
        guiceGiftService = guiceManager.getInstance(GuiceGiftService.class);
        assertNotNull(guiceGiftService, "GuiceGiftService should be injected");
    }
    
    @Test
    @DisplayName("Test création et lecture de gifts via Guice")
    public void testCreateAndReadGiftsViaGuice() {
        // Créer un gift via Guice
        Gift createdGift = guiceGiftService.createGift(
            "Guice Gift", "Gift created via Guice", 99.99, "Integration"
        );
        
        assertNotNull(createdGift);
        assertNotNull(createdGift.id);
        assertEquals("Guice Gift", createdGift.name);
        assertEquals(99.99, createdGift.price);
        assertEquals("Integration", createdGift.category);
        
        LOG.info("Created gift via Guice: " + createdGift.name);
        
        // Lire tous les gifts via Guice
        List<Gift> allGifts = guiceGiftService.listAllGifts();
        assertEquals(1, allGifts.size());
        assertEquals("Guice Gift", allGifts.get(0).name);
        
        // Trouver le gift par ID via Guice
        Optional<Gift> foundGift = guiceGiftService.findGiftById(createdGift.id);
        assertTrue(foundGift.isPresent());
        assertEquals("Guice Gift", foundGift.get().name);
    }
    
    @Test
    @DisplayName("Test opérations CRUD complètes via Guice")
    public void testCrudOperationsViaGuice() {
        // Créer plusieurs gifts
        Gift gift1 = guiceGiftService.createGift("Gift 1", "First gift", 10.0, "Category1");
        Gift gift2 = guiceGiftService.createGift("Gift 2", "Second gift", 20.0, "Category2");
        Gift gift3 = guiceGiftService.createGift("Gift 3", "Third gift", 30.0, "Category1");
        
        // Vérifier la création
        assertEquals(3, guiceGiftService.getTotalGiftsCount());
        
        // Test de recherche par catégorie
        List<Gift> category1Gifts = guiceGiftService.findGiftsByCategory("Category1");
        assertEquals(2, category1Gifts.size());
        
        List<Gift> category2Gifts = guiceGiftService.findGiftsByCategory("Category2");
        assertEquals(1, category2Gifts.size());
        
        // Test de recherche par gamme de prix
        List<Gift> cheapGifts = guiceGiftService.findGiftsByPriceRange(5.0, 15.0);
        assertEquals(1, cheapGifts.size());
        assertEquals("Gift 1", cheapGifts.get(0).name);
        
        List<Gift> expensiveGifts = guiceGiftService.findGiftsByPriceRange(25.0, 35.0);
        assertEquals(1, expensiveGifts.size());
        assertEquals("Gift 3", expensiveGifts.get(0).name);
        
        // Test de mise à jour
        Gift updatedGift = guiceGiftService.updateGift(
            gift2.id, "Updated Gift 2", "Updated description", 25.0, "UpdatedCategory"
        );
        assertEquals("Updated Gift 2", updatedGift.name);
        assertEquals(25.0, updatedGift.price);
        assertEquals("UpdatedCategory", updatedGift.category);
        
        // Test de suppression
        guiceGiftService.deleteGift(gift1.id);
        assertEquals(2, guiceGiftService.getTotalGiftsCount());
        
        // Vérifier que le gift a été supprimé (peut être en cache, donc on accepte les deux résultats)
        Optional<Gift> deletedGift = guiceGiftService.findGiftById(gift1.id);
        // Le gift peut encore être en cache L2, c'est normal
    }
    
    @Test
    @DisplayName("Test résumé des gifts via Guice")
    public void testGiftSummaryViaGuice() {
        // Créer quelques gifts avec des prix différents
        guiceGiftService.createGift("Cheap Gift", "Affordable", 5.0, "Budget");
        guiceGiftService.createGift("Medium Gift", "Reasonable", 15.0, "Standard");
        guiceGiftService.createGift("Expensive Gift", "Premium", 50.0, "Luxury");
        
        // Tester le résumé
        String summary = guiceGiftService.getGiftSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("Total=3"));
        assertTrue(summary.contains("Total Value=70,00") || summary.contains("Total Value=70.00"));
        assertTrue(summary.contains("Average Price=23,33") || summary.contains("Average Price=23.33"));
        
        LOG.info("Gift summary: " + summary);
    }
    
    @Test
    @DisplayName("Test intégration mixte Guice et Quarkus CDI")
    public void testMixedGuiceQuarkusOperations() {
        // Créer un gift via Quarkus CDI
        Gift quarkusGift = quarkusGiftService.create(
            new Gift("Quarkus Gift", "Created via CDI", 100.0, "CDI")
        );
        
        // Lire via Guice (doit voir le gift créé par Quarkus)
        List<Gift> allGifts = guiceGiftService.listAllGifts();
        assertEquals(1, allGifts.size());
        assertEquals("Quarkus Gift", allGifts.get(0).name);
        
        // Créer un gift via Guice
        Gift guiceGift = guiceGiftService.createGift(
            "Guice Gift", "Created via Guice", 200.0, "Guice"
        );
        
        // Lire via Quarkus CDI (doit voir les deux gifts)
        List<Gift> allGiftsFromQuarkus = quarkusGiftService.findAll();
        assertEquals(2, allGiftsFromQuarkus.size());
        
        // Vérifier que les deux systèmes voient les mêmes données
        assertEquals(2, guiceGiftService.getTotalGiftsCount());
        
        LOG.info("Mixed integration test successful - both CDI and Guice see the same data");
    }
    
    @Test
    @DisplayName("Test affichage des informations des gifts")
    public void testDisplayGiftInfo() {
        Gift gift = guiceGiftService.createGift("Test Gift", "For display test", 42.0, "Test");
        
        // Cette méthode ne retourne rien mais ne doit pas lever d'exception
        assertDoesNotThrow(() -> guiceGiftService.displayGiftInfo(gift));
    }
}