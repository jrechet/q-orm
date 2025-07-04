package lab.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lab.quarkus.entity.Gift;
import lab.quarkus.service.GiftService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class GiftServiceTest {
    
    @Inject
    GiftService giftService;
    
    @BeforeEach
    @Transactional
    public void setUp() {
        // Clean up before each test
        giftService.findAll().forEach(gift -> giftService.delete(gift.id));
    }
    
    @Test
    @DisplayName("Test creating a new gift")
    public void testCreateGift() {
        // Given
        Gift gift = new Gift("Test Gift", "Test Description", 25.99, "Test Category");
        
        // When
        Gift createdGift = giftService.create(gift);
        
        // Then
        assertNotNull(createdGift);
        assertNotNull(createdGift.id);
        assertEquals("Test Gift", createdGift.name);
        assertEquals("Test Description", createdGift.description);
        assertEquals(25.99, createdGift.price);
        assertEquals("Test Category", createdGift.category);
    }
    
    @Test
    @DisplayName("Test finding all gifts")
    public void testFindAllGifts() {
        // Given
        Gift gift1 = new Gift("Gift 1", "Description 1", 10.99, "Category 1");
        Gift gift2 = new Gift("Gift 2", "Description 2", 20.99, "Category 2");
        giftService.create(gift1);
        giftService.create(gift2);
        
        // When
        List<Gift> gifts = giftService.findAll();
        
        // Then
        assertEquals(2, gifts.size());
    }
    
    @Test
    @DisplayName("Test finding gift by ID")
    public void testFindGiftById() {
        // Given
        Gift gift = new Gift("Test Gift", "Test Description", 25.99, "Test Category");
        Gift createdGift = giftService.create(gift);
        
        // When
        Optional<Gift> foundGift = giftService.findById(createdGift.id);
        
        // Then
        assertTrue(foundGift.isPresent());
        assertEquals("Test Gift", foundGift.get().name);
        assertEquals(25.99, foundGift.get().price);
    }
    
    @Test
    @DisplayName("Test updating a gift")
    public void testUpdateGift() {
        // Given
        Gift gift = new Gift("Original Gift", "Original Description", 25.99, "Original Category");
        Gift createdGift = giftService.create(gift);
        
        // When
        Gift updatedGift = new Gift("Updated Gift", "Updated Description", 35.99, "Updated Category");
        Gift result = giftService.update(createdGift.id, updatedGift);
        
        // Then
        assertNotNull(result);
        assertEquals("Updated Gift", result.name);
        assertEquals("Updated Description", result.description);
        assertEquals(35.99, result.price);
        assertEquals("Updated Category", result.category);
    }
    
    @Test
    @DisplayName("Test deleting a gift")
    public void testDeleteGift() {
        // Given
        Gift gift = new Gift("Test Gift", "Test Description", 25.99, "Test Category");
        Gift createdGift = giftService.create(gift);
        
        // When
        boolean deleted = giftService.delete(createdGift.id);
        
        // Then
        assertTrue(deleted);
        Optional<Gift> foundGift = giftService.findById(createdGift.id);
        assertFalse(foundGift.isPresent());
    }
    
    @Test
    @DisplayName("Test finding gifts by category")
    public void testFindGiftsByCategory() {
        // Given
        Gift gift1 = new Gift("Gift 1", "Description 1", 10.99, "Electronics");
        Gift gift2 = new Gift("Gift 2", "Description 2", 20.99, "Electronics");
        Gift gift3 = new Gift("Gift 3", "Description 3", 30.99, "Books");
        giftService.create(gift1);
        giftService.create(gift2);
        giftService.create(gift3);
        
        // When
        List<Gift> electronicsGifts = giftService.findByCategory("Electronics");
        
        // Then
        assertEquals(2, electronicsGifts.size());
        assertTrue(electronicsGifts.stream().allMatch(g -> "Electronics".equals(g.category)));
    }
    
    @Test
    @DisplayName("Test finding gifts by price range")
    public void testFindGiftsByPriceRange() {
        // Given
        Gift gift1 = new Gift("Gift 1", "Description 1", 10.99, "Category 1");
        Gift gift2 = new Gift("Gift 2", "Description 2", 25.99, "Category 2");
        Gift gift3 = new Gift("Gift 3", "Description 3", 40.99, "Category 3");
        giftService.create(gift1);
        giftService.create(gift2);
        giftService.create(gift3);
        
        // When
        List<Gift> giftsInRange = giftService.findByPriceRange(15.0, 35.0);
        
        // Then
        assertEquals(1, giftsInRange.size());
        assertEquals("Gift 2", giftsInRange.get(0).name);
    }
    
    @Test
    @DisplayName("Test finding gifts by name containing")
    public void testFindGiftsByNameContaining() {
        // Given
        Gift gift1 = new Gift("Wireless Headphones", "Description 1", 89.99, "Electronics");
        Gift gift2 = new Gift("Bluetooth Speaker", "Description 2", 45.99, "Electronics");
        Gift gift3 = new Gift("Coffee Mug", "Description 3", 15.99, "Home");
        giftService.create(gift1);
        giftService.create(gift2);
        giftService.create(gift3);
        
        // When
        List<Gift> giftsWithWireless = giftService.findByNameContaining("Wireless");
        
        // Then
        assertEquals(1, giftsWithWireless.size());
        assertEquals("Wireless Headphones", giftsWithWireless.get(0).name);
    }
}