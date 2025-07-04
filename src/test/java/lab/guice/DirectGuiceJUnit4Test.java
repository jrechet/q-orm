package lab.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lab.guice.service.GuiceGiftService;
import lab.guice.service.PureGuiceService;
import lab.quarkus.entity.Gift;
import lab.quarkus.service.GiftService;
import org.jboss.logging.Logger;
import org.junit.Before;
import org.junit.Test;

import jakarta.transaction.Transactional;

import static org.junit.Assert.*;

/**
 * Test JUnit 4 qui démontre l'utilisation directe de Guice.createInjector() 
 * avec le QuarkusIntegrationModule dans un contexte Quarkus initialisé.
 * 
 * Note: Utilise JUnit 4 APIs (org.junit.Test, org.junit.Before) au lieu de JUnit 5
 */
@QuarkusTest
public class DirectGuiceJUnit4Test {
    
    private static final Logger LOG = Logger.getLogger(DirectGuiceJUnit4Test.class);
    
    @Inject
    GiftService quarkusGiftService;
    
    private Injector directGuiceInjector;
    
    @Before
    @Transactional
    public void setUp() {
        // Nettoyer les données existantes
        quarkusGiftService.findAll().forEach(gift -> quarkusGiftService.delete(gift.id));
        
        // Créer un injecteur Guice directement avec notre module
        // Quarkus/Arc est déjà initialisé grâce à @QuarkusTest
        LOG.info("JUnit4: Creating Guice injector directly with QuarkusIntegrationModule");
        directGuiceInjector = Guice.createInjector(new QuarkusIntegrationModule());
        
        assertNotNull("Direct Guice injector should be created", directGuiceInjector);
        LOG.info("JUnit4: Direct Guice injector created successfully");
    }
    
    @Test
    public void testDirectGuiceInjectorCreation() {
        // Vérifier que l'injecteur Guice a bien été créé
        assertNotNull(directGuiceInjector);
        
        // Tester l'injection d'un service purement Guice
        PureGuiceService pureGuiceService = directGuiceInjector.getInstance(PureGuiceService.class);
        assertNotNull(pureGuiceService);
        
        String serviceId = pureGuiceService.getServiceId();
        assertTrue("Service ID should start with GUICE-", serviceId.startsWith("GUICE-"));
        LOG.info("JUnit4: PureGuiceService ID: " + serviceId);
        
        // Tester un calcul simple
        double result = pureGuiceService.calculate(10.0, 5.0, "add");
        assertEquals("10.0 + 5.0 should equal 15.0", 15.0, result, 0.001);
        LOG.info("JUnit4: Calculation result: 10.0 + 5.0 = " + result);
    }
    
    @Test
    public void testQuarkusToGuiceInjection() {
        // Obtenir le service Guice qui utilise les composants Quarkus
        GuiceGiftService guiceGiftService = directGuiceInjector.getInstance(GuiceGiftService.class);
        assertNotNull("GuiceGiftService should be injected", guiceGiftService);
        
        // Vérifier qu'on peut créer un gift via le service Guice
        Gift createdGift = guiceGiftService.createGift(
            "JUnit4 Direct Guice Gift", "Created via direct Guice injector with JUnit4", 99.0, "JUnit4Test"
        );
        
        assertNotNull("Created gift should not be null", createdGift);
        assertNotNull("Created gift should have an ID", createdGift.id);
        assertEquals("Gift name should match", "JUnit4 Direct Guice Gift", createdGift.name);
        assertEquals("Gift price should match", 99.0, createdGift.price, 0.001);
        assertEquals("Gift category should match", "JUnit4Test", createdGift.category);
        
        LOG.info("JUnit4: Created gift via direct Guice: " + createdGift.name + " (ID: " + createdGift.id + ")");
        
        // Vérifier que le gift est visible depuis Quarkus aussi
        var foundByQuarkus = quarkusGiftService.findById(createdGift.id);
        assertTrue("Gift should be found by Quarkus", foundByQuarkus.isPresent());
        assertEquals("Gift name should match when found by Quarkus", "JUnit4 Direct Guice Gift", foundByQuarkus.get().name);
        
        LOG.info("JUnit4: Gift found by Quarkus service: " + foundByQuarkus.get().name);
    }
    
    @Test
    public void testComplexBusinessLogicViaGuice() {
        GuiceGiftService guiceService = directGuiceInjector.getInstance(GuiceGiftService.class);
        PureGuiceService pureService = directGuiceInjector.getInstance(PureGuiceService.class);
        
        // Créer quelques gifts pour tester
        Gift gift1 = guiceService.createGift("JUnit4 Gift 1", "First test gift", 50.0, "Test");
        Gift gift2 = guiceService.createGift("JUnit4 Gift 2", "Second test gift", 75.0, "Test");
        
        // Utiliser le service pure Guice pour des calculs
        double total = pureService.calculate(gift1.price, gift2.price, "add");
        assertEquals("Total should be 125.0", 125.0, total, 0.001);
        
        // Tester le formatage
        String formatted = pureService.formatResult(total);
        assertNotNull("Formatted result should not be null", formatted);
        assertTrue("Formatted result should contain 125", formatted.contains("125"));
        
        // Tester la validation
        assertTrue("Valid number should be recognized", pureService.isValidNumber("123.45"));
        assertFalse("Invalid string should not be recognized as number", pureService.isValidNumber("invalid"));
        
        // Tester la logique métier
        String businessResult = pureService.processBusinessLogic("100.0", 0.8);
        assertNotNull("Business result should not be null", businessResult);
        assertTrue("Business result should contain calculation", businessResult.contains("100.0 * 0.8"));
        assertTrue("Business result should contain service ID", businessResult.contains("GUICE-"));
        
        LOG.info("JUnit4: Complex business logic test successful");
        LOG.info("JUnit4: Total calculated: " + formatted);
        LOG.info("JUnit4: Business logic result: " + businessResult);
    }
    
    @Test
    public void testServiceInteroperability() {
        // Test que les services Guice et Quarkus peuvent travailler ensemble
        GuiceGiftService guiceService = directGuiceInjector.getInstance(GuiceGiftService.class);
        
        // Créer un gift via Quarkus
        Gift quarkusGift = quarkusGiftService.create(
            new Gift("Quarkus Gift", "Created by Quarkus", 30.0, "Interop")
        );
        
        // Vérifier qu'il est visible via Guice
        var allGiftsViaGuice = guiceService.listAllGifts();
        boolean foundQuarkusGift = allGiftsViaGuice.stream()
            .anyMatch(gift -> "Quarkus Gift".equals(gift.name));
        assertTrue("Quarkus gift should be visible via Guice", foundQuarkusGift);
        
        // Créer un gift via Guice
        Gift guiceGift = guiceService.createGift("Guice Gift", "Created by Guice", 40.0, "Interop");
        
        // Vérifier qu'il est visible via Quarkus
        var allGiftsViaQuarkus = quarkusGiftService.findAll();
        boolean foundGuiceGift = allGiftsViaQuarkus.stream()
            .anyMatch(gift -> "Guice Gift".equals(gift.name));
        assertTrue("Guice gift should be visible via Quarkus", foundGuiceGift);
        
        LOG.info("JUnit4: Service interoperability test successful - both frameworks see each other's data");
    }
    
    @Test
    public void testErrorHandling() {
        PureGuiceService pureService = directGuiceInjector.getInstance(PureGuiceService.class);
        
        // Test division par zéro
        try {
            pureService.calculate(10.0, 0.0, "divide");
            fail("Division by zero should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Exception message should be correct", "Division by zero", e.getMessage());
            LOG.info("JUnit4: Division by zero correctly handled");
        }
        
        // Test opération inconnue
        try {
            pureService.calculate(5.0, 3.0, "unknown");
            fail("Unknown operation should throw exception");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception should mention unknown operation", e.getMessage().contains("Unknown operation"));
            LOG.info("JUnit4: Unknown operation correctly handled");
        }
        
        // Test logique métier avec entrée invalide
        String result = pureService.processBusinessLogic("invalid-number", 2.0);
        assertEquals("Invalid input should return error", "ERROR: Invalid input", result);
        
        LOG.info("JUnit4: Error handling test successful");
    }
}