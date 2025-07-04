package lab.eclipselink.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import lab.eclipselink.entity.Shop;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ShopRepository {
    
    private EntityManagerFactory emf;
    
    public ShopRepository() {
        this.emf = Persistence.createEntityManagerFactory("eclipselink-pu");
    }
    
    public Shop save(Shop shop) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (shop.getId() == null) {
                em.persist(shop);
            } else {
                shop = em.merge(shop);
            }
            em.getTransaction().commit();
            return shop;
        } finally {
            em.close();
        }
    }
    
    public List<Shop> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Shop> query = em.createQuery("SELECT s FROM Shop s", Shop.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public Optional<Shop> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            Shop shop = em.find(Shop.class, id);
            return Optional.ofNullable(shop);
        } finally {
            em.close();
        }
    }
    
    public void deleteById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Shop shop = em.find(Shop.class, id);
            if (shop != null) {
                em.remove(shop);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    public List<Shop> findByCity(String city) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Shop> query = em.createQuery(
                "SELECT s FROM Shop s WHERE s.city = :city", Shop.class);
            query.setParameter("city", city);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Shop> findByCategory(String category) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Shop> query = em.createQuery(
                "SELECT s FROM Shop s WHERE s.category = :category", Shop.class);
            query.setParameter("category", category);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Shop> findByNameContaining(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Shop> query = em.createQuery(
                "SELECT s FROM Shop s WHERE s.name LIKE :name", Shop.class);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}