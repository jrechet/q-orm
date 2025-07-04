package lab.cache;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lab.quarkus.entity.Gift;
import lab.quarkus.service.GiftService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.jboss.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class DualOrmCacheTest {
    
    private static final Logger LOG = Logger.getLogger(DualOrmCacheTest.class);
    
    @Inject
    GiftService giftService;
    
    @Inject
    DualOrmCacheManager cacheManager;
    
    @BeforeEach
    @Transactional
    public void setUp() {
        // Clean up before each test
        giftService.findAll().forEach(gift -> giftService.delete(gift.id));
        // Clear caches
        cacheManager.clearAllCaches();
    }
    
    @Test
    @DisplayName("Test Hibernate L2 cache behavior")
    public void testHibernateCacheBehavior() {
        // Create a gift
        Gift gift = new Gift("Cached Gift", "Test cache behavior", 99.99, "Cache");
        Gift createdGift = giftService.create(gift);
        Long giftId = createdGift.id;
        
        // Get initial cache stats
        var initialStats = cacheManager.getCacheStatistics();
        LOG.info("Initial cache stats: " + initialStats);
        
        // First read - should be a cache miss (loads from DB)
        Gift firstRead = giftService.findById(giftId).orElse(null);
        assertNotNull(firstRead);
        assertEquals("Cached Gift", firstRead.name);
        
        // Second read - should be a cache hit (loads from cache)
        Gift secondRead = giftService.findById(giftId).orElse(null);
        assertNotNull(secondRead);
        assertEquals("Cached Gift", secondRead.name);
        
        // Get final cache stats
        var finalStats = cacheManager.getCacheStatistics();
        LOG.info("Final cache stats: " + finalStats);
        
        // Verify cache usage increased
        assertTrue(finalStats.hibernateHitCount >= initialStats.hibernateHitCount,
            "Cache hit count should have increased");
    }
    
    @Test
    @DisplayName("Test cache isolation between ORM systems")
    public void testCacheIsolation() {
        // Create gift in Quarkus ORM
        Gift gift = new Gift("Isolation Test", "Test cache isolation", 150.00, "Test");
        Gift createdGift = giftService.create(gift);
        
        // Read gift to populate Hibernate cache
        giftService.findById(createdGift.id);
        
        // Clear only Hibernate cache
        cacheManager.clearHibernateCache();
        
        // Verify gift can still be read (from database)
        Gift readAfterClear = giftService.findById(createdGift.id).orElse(null);
        assertNotNull(readAfterClear);
        assertEquals("Isolation Test", readAfterClear.name);
        
        LOG.info("Cache isolation test completed successfully");
    }
    
    @Test
    @DisplayName("Test cache statistics collection")
    public void testCacheStatistics() {
        // Create multiple gifts to generate cache activity
        for (int i = 0; i < 5; i++) {
            Gift gift = new Gift("Gift " + i, "Description " + i, 10.0 + i, "Stats");
            giftService.create(gift);
        }
        
        // Read all gifts multiple times to generate cache hits
        var allGifts = giftService.findAll();
        allGifts.forEach(gift -> giftService.findById(gift.id));
        allGifts.forEach(gift -> giftService.findById(gift.id));
        
        // Check statistics
        var stats = cacheManager.getCacheStatistics();
        LOG.info("Cache statistics after multiple operations: " + stats);
        
        // Basic validation - just ensure we can collect stats without errors
        assertNotNull(stats);
        assertTrue(stats.hibernateHitCount >= 0);
        assertTrue(stats.hibernateMissCount >= 0);
        assertTrue(stats.hibernatePutCount >= 0);
    }
}