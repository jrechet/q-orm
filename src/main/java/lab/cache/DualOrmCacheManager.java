package lab.cache;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.SessionFactory;
import org.hibernate.cache.spi.CacheImplementor;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.jboss.logging.Logger;

/**
 * Centralized cache manager for dual ORM setup.
 * Manages cache coordination and cleanup between Hibernate and EclipseLink.
 */
@ApplicationScoped
@Startup
public class DualOrmCacheManager {
    
    private static final Logger LOG = Logger.getLogger(DualOrmCacheManager.class);
    
    @Inject
    EntityManagerFactory hibernateEmf; // Quarkus-managed Hibernate
    
    private EntityManagerFactory eclipseLinkEmf;
    
    public void init() {
        LOG.info("Initializing Dual ORM Cache Manager");
        
        // Initialize EclipseLink EMF for cache management
        eclipseLinkEmf = Persistence.createEntityManagerFactory("eclipselink-pu");
        
        LOG.info("Cache coordination disabled between ORM systems (different tables)");
        LOG.info("Hibernate cache: L1 + L2 (Caffeine) for Gift entities");
        LOG.info("EclipseLink cache: L1 + L2 (SoftWeak) for Shop entities");
    }
    
    /**
     * Clear all caches - useful for testing or maintenance
     */
    public void clearAllCaches() {
        LOG.info("Clearing all ORM caches");
        
        // Clear Hibernate cache
        clearHibernateCache();
        
        // Clear EclipseLink cache
        clearEclipseLinkCache();
        
        LOG.info("All caches cleared");
    }
    
    /**
     * Clear only Hibernate (Quarkus) cache
     */
    public void clearHibernateCache() {
        try {
            SessionFactory sessionFactory = hibernateEmf.unwrap(SessionFactory.class);
            CacheImplementor cache = (CacheImplementor) sessionFactory.getCache();
            cache.evictAllRegions();
            LOG.info("Hibernate cache cleared");
        } catch (Exception e) {
            LOG.warn("Failed to clear Hibernate cache", e);
        }
    }
    
    /**
     * Clear only EclipseLink cache
     */
    public void clearEclipseLinkCache() {
        try {
            if (eclipseLinkEmf != null) {
                var em = eclipseLinkEmf.createEntityManager();
                try {
                    DatabaseSession session = em.unwrap(DatabaseSession.class);
                    session.getIdentityMapAccessor().initializeAllIdentityMaps();
                    LOG.info("EclipseLink cache cleared");
                } finally {
                    em.close();
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to clear EclipseLink cache", e);
        }
    }
    
    /**
     * Get cache statistics for monitoring
     */
    public CacheStatistics getCacheStatistics() {
        CacheStatistics stats = new CacheStatistics();
        
        // Hibernate stats
        try {
            SessionFactory sessionFactory = hibernateEmf.unwrap(SessionFactory.class);
            var hibernateStats = sessionFactory.getStatistics();
            stats.hibernateHitCount = hibernateStats.getSecondLevelCacheHitCount();
            stats.hibernateMissCount = hibernateStats.getSecondLevelCacheMissCount();
            stats.hibernatePutCount = hibernateStats.getSecondLevelCachePutCount();
        } catch (Exception e) {
            LOG.debug("Could not retrieve Hibernate cache stats", e);
        }
        
        // EclipseLink stats (simplified - avoiding complex API)
        try {
            if (eclipseLinkEmf != null) {
                // For simplicity, we'll track this manually or use basic indicators
                stats.eclipseLinkCacheSize = 0; // Could be enhanced with custom tracking
            }
        } catch (Exception e) {
            LOG.debug("Could not retrieve EclipseLink cache stats", e);
        }
        
        return stats;
    }
    
    public static class CacheStatistics {
        public long hibernateHitCount = 0;
        public long hibernateMissCount = 0;
        public long hibernatePutCount = 0;
        public int eclipseLinkCacheSize = 0;
        
        public double getHibernateHitRatio() {
            long total = hibernateHitCount + hibernateMissCount;
            return total > 0 ? (double) hibernateHitCount / total : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "CacheStats{hibernate: hits=%d, misses=%d, puts=%d, hitRatio=%.2f, eclipseLink: size=%d}",
                hibernateHitCount, hibernateMissCount, hibernatePutCount, 
                getHibernateHitRatio(), eclipseLinkCacheSize
            );
        }
    }
}