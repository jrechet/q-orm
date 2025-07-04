package lab.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lab.guice.service.GuiceGiftService;
import lab.guice.service.PureGuiceService;
import lab.quarkus.entity.Gift;
import lab.quarkus.service.GiftService;
import lab.quarkus.repository.GiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jboss.logging.Logger;

import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test qui démontre l'utilisation directe de Guice.createInjector() 
 * avec le QuarkusIntegrationModule dans un contexte Quarkus initialisé.
 */
@QuarkusTest
public class DirectGuiceTest {
    
    private static final Logger LOG = Logger.getLogger(DirectGuiceTest.class);
    
    @Inject
    GiftService quarkusGiftService;
    
    private Injector directGuiceInjector;
    
    @BeforeEach
    @Transactional
    public void setUp() {
        // Nettoyer les données existantes
        quarkusGiftService.findAll().forEach(gift -> quarkusGiftService.delete(gift.id));
        
        // Créer un injecteur Guice directement avec notre module
        // Quarkus/Arc est déjà initialisé grâce à @QuarkusTest
        LOG.info("Creating Guice injector directly with QuarkusIntegrationModule");
        directGuiceInjector = Guice.createInjector(new QuarkusIntegrationModule());
        
        assertNotNull(directGuiceInjector, "Direct Guice injector should be created");
        LOG.info("Direct Guice injector created successfully");
    }
    
    @Test
    @DisplayName("Test création directe d'injecteur Guice avec QuarkusIntegrationModule")
    public void testDirectGuiceInjectorCreation() {
        // Vérifier que l'injecteur Guice a bien été créé
        assertNotNull(directGuiceInjector);
        
        // Tester l'injection d'un service purement Guice
        PureGuiceService pureGuiceService = directGuiceInjector.getInstance(PureGuiceService.class);
        assertNotNull(pureGuiceService);
        
        String serviceId = pureGuiceService.getServiceId();
        assertTrue(serviceId.startsWith("GUICE-"));
        LOG.info("PureGuiceService ID: " + serviceId);
        
        // Tester un calcul simple
        double result = pureGuiceService.calculate(10.0, 5.0, "add");
        assertEquals(15.0, result, 0.001);
        LOG.info("Calculation result: 10.0 + 5.0 = " + result);
    }
    
    @Test
    @DisplayName("Test injection Quarkus→Guice via injecteur direct")
    public void testQuarkusToGuiceInjection() {
        // Obtenir le service Guice qui utilise les composants Quarkus
        GuiceGiftService guiceGiftService = directGuiceInjector.getInstance(GuiceGiftService.class);
        assertNotNull(guiceGiftService);
        
        // Vérifier qu'on peut créer un gift via le service Guice
        Gift createdGift = guiceGiftService.createGift(
            "Direct Guice Gift", "Created via direct Guice injector", 42.0, "Test"
        );
        
        assertNotNull(createdGift);
        assertNotNull(createdGift.id);
        assertEquals("Direct Guice Gift", createdGift.name);
        assertEquals(42.0, createdGift.price);
        assertEquals("Test", createdGift.category);
        
        LOG.info("Created gift via direct Guice: " + createdGift.name + " (ID: " + createdGift.id + ")");
        
        // Vérifier que le gift est visible depuis Quarkus aussi
        var foundByQuarkus = quarkusGiftService.findById(createdGift.id);
        assertTrue(foundByQuarkus.isPresent());
        assertEquals("Direct Guice Gift", foundByQuarkus.get().name);
        
        LOG.info("Gift found by Quarkus service: " + foundByQuarkus.get().name);
    }
    
    @Test
    @DisplayName("Test injection des providers Quarkus dans Guice")
    public void testQuarkusProvidersInGuice() {
        // Tester l'injection directe des composants Quarkus
        GiftService giftServiceFromGuice = directGuiceInjector.getInstance(GiftService.class);
        assertNotNull(giftServiceFromGuice);
        
        GiftRepository giftRepositoryFromGuice = directGuiceInjector.getInstance(GiftRepository.class);
        assertNotNull(giftRepositoryFromGuice);
        
        // Vérifier que ce sont les mêmes instances que celles injectées par Quarkus
        // (en termes de fonctionnalité, pas forcément de référence)
        int initialCount = quarkusGiftService.findAll().size();
        
        Gift testGift = new Gift("Test Provider", "Testing providers", 25.0, "Provider");
        Gift createdByGuiceProvider = giftServiceFromGuice.create(testGift);
        
        assertNotNull(createdByGuiceProvider);
        assertEquals("Test Provider", createdByGuiceProvider.name);
        
        // Vérifier que le gift est bien créé
        int newCount = quarkusGiftService.findAll().size();
        assertEquals(initialCount + 1, newCount);
        
        LOG.info("Successfully used Quarkus providers injected via Guice");
    }
    
    @Test
    @DisplayName("Test opérations complètes via injecteur direct")
    public void testCompleteOperationsViaDirectInjector() {
        GuiceGiftService guiceService = directGuiceInjector.getInstance(GuiceGiftService.class);
        PureGuiceService pureService = directGuiceInjector.getInstance(PureGuiceService.class);
        
        // Créer quelques gifts
        Gift gift1 = guiceService.createGift("Gift A", "First gift", 100.0, "Premium");
        Gift gift2 = guiceService.createGift("Gift B", "Second gift", 200.0, "Premium");
        
        // Utiliser le service pure Guice pour des calculs
        double total = pureService.calculate(gift1.price, gift2.price, "add");
        assertEquals(300.0, total, 0.001);
        
        String formatted = pureService.formatResult(total);
        assertTrue(formatted.contains("300"));
        
        // Vérifier les statistiques
        double[] prices = {gift1.price, gift2.price};
        String stats = pureService.getStats(prices);
        
        assertNotNull(stats);
        assertTrue(stats.contains("Count=2"));
        assertTrue(stats.contains("Sum=300"));
        assertTrue(stats.contains("Avg=150"));
        
        LOG.info("Complete operations test successful");
        LOG.info("Total: " + formatted);
        LOG.info("Stats: " + stats);
    }
    
    @Test
    @DisplayName("Test de validation et logique métier via Guice direct")
    public void testValidationAndBusinessLogic() {
        PureGuiceService pureService = directGuiceInjector.getInstance(PureGuiceService.class);
        
        // Test de validation
        assertTrue(pureService.isValidNumber("123.45"));
        assertFalse(pureService.isValidNumber("not-a-number"));
        assertTrue(pureService.isValidNumber("-67.89"));
        
        // Test de logique métier
        String businessResult = pureService.processBusinessLogic("50.0", 1.2);
        assertNotNull(businessResult);
        assertTrue(businessResult.contains("50.0 * 1.2"));
        assertTrue(businessResult.contains("60"));
        assertTrue(businessResult.contains("GUICE-"));
        
        LOG.info("Validation and business logic test successful");
        LOG.info("Business result: " + businessResult);
    }
}