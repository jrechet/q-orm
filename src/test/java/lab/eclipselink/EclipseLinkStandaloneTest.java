package lab.eclipselink;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lab.eclipselink.entity.Shop;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class EclipseLinkStandaloneTest {
    
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    private EntityManagerFactory emf;
    
    @BeforeEach
    public void setUp() {
        // Create persistence properties with test container connection
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
        properties.put("jakarta.persistence.jdbc.url", postgres.getJdbcUrl());
        properties.put("jakarta.persistence.jdbc.user", postgres.getUsername());
        properties.put("jakarta.persistence.jdbc.password", postgres.getPassword());
        properties.put("eclipselink.target-database", "PostgreSQL");
        properties.put("eclipselink.ddl-generation", "drop-and-create-tables");
        properties.put("eclipselink.ddl-generation.output-mode", "database");
        properties.put("eclipselink.logging.level", "FINE");
        
        emf = Persistence.createEntityManagerFactory("eclipselink-pu", properties);
    }
    
    @AfterEach
    public void tearDown() {
        if (emf != null) {
            emf.close();
        }
    }
    
    @Test
    @DisplayName("Test EclipseLink entity CRUD operations")
    public void testShopCrudOperations() {
        EntityManager em = emf.createEntityManager();
        
        try {
            // Create
            em.getTransaction().begin();
            Shop shop = new Shop("Test Shop", "123 Test St", "Test City", "555-0100", "Test Category");
            em.persist(shop);
            em.getTransaction().commit();
            
            assertNotNull(shop.getId());
            Long shopId = shop.getId();
            
            // Read
            Shop foundShop = em.find(Shop.class, shopId);
            assertNotNull(foundShop);
            assertEquals("Test Shop", foundShop.getName());
            assertEquals("Test City", foundShop.getCity());
            
            // Update
            em.getTransaction().begin();
            foundShop.setName("Updated Shop");
            foundShop.setCity("Updated City");
            em.getTransaction().commit();
            
            Shop updatedShop = em.find(Shop.class, shopId);
            assertEquals("Updated Shop", updatedShop.getName());
            assertEquals("Updated City", updatedShop.getCity());
            
            // Query
            List<Shop> shops = em.createQuery("SELECT s FROM Shop s", Shop.class).getResultList();
            assertEquals(1, shops.size());
            
            // Delete
            em.getTransaction().begin();
            em.remove(updatedShop);
            em.getTransaction().commit();
            
            Shop deletedShop = em.find(Shop.class, shopId);
            assertNull(deletedShop);
            
        } finally {
            em.close();
        }
    }
    
    @Test
    @DisplayName("Test EclipseLink query operations")
    public void testShopQueries() {
        EntityManager em = emf.createEntityManager();
        
        try {
            // Create test data
            em.getTransaction().begin();
            Shop shop1 = new Shop("Tech Store", "123 Main St", "New York", "555-0101", "Electronics");
            Shop shop2 = new Shop("Book Corner", "456 Oak Ave", "Los Angeles", "555-0102", "Books");
            Shop shop3 = new Shop("Tech Hub", "789 Pine Rd", "New York", "555-0103", "Electronics");
            
            em.persist(shop1);
            em.persist(shop2);
            em.persist(shop3);
            em.getTransaction().commit();
            
            // Test city query
            List<Shop> newYorkShops = em.createQuery(
                "SELECT s FROM Shop s WHERE s.city = :city", Shop.class)
                .setParameter("city", "New York")
                .getResultList();
            assertEquals(2, newYorkShops.size());
            
            // Test category query
            List<Shop> electronicsShops = em.createQuery(
                "SELECT s FROM Shop s WHERE s.category = :category", Shop.class)
                .setParameter("category", "Electronics")
                .getResultList();
            assertEquals(2, electronicsShops.size());
            
            // Test name containing query
            List<Shop> techShops = em.createQuery(
                "SELECT s FROM Shop s WHERE s.name LIKE :name", Shop.class)
                .setParameter("name", "%Tech%")
                .getResultList();
            assertEquals(2, techShops.size());
            
        } finally {
            em.close();
        }
    }
}