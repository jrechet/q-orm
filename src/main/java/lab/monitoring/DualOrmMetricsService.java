package lab.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lab.cache.DualOrmCacheManager;
import org.jboss.logging.Logger;

/**
 * Metrics service for monitoring dual ORM performance and cache behavior
 */
@ApplicationScoped
public class DualOrmMetricsService {
    
    private static final Logger LOG = Logger.getLogger(DualOrmMetricsService.class);
    
    @Inject
    MeterRegistry meterRegistry;
    
    @Inject
    DualOrmCacheManager cacheManager;
    
    private Counter quarkusOperations;
    private Counter eclipseLinkOperations;
    private Timer quarkusQueryTimer;
    private Timer eclipseLinkQueryTimer;
    
    public void init() {
        // Initialize counters
        quarkusOperations = Counter.builder("orm.operations")
            .tag("system", "quarkus")
            .description("Number of Quarkus ORM operations")
            .register(meterRegistry);
            
        eclipseLinkOperations = Counter.builder("orm.operations")
            .tag("system", "eclipselink")
            .description("Number of EclipseLink ORM operations")
            .register(meterRegistry);
        
        // Initialize timers
        quarkusQueryTimer = Timer.builder("orm.query.duration")
            .tag("system", "quarkus")
            .description("Quarkus query execution time")
            .register(meterRegistry);
            
        eclipseLinkQueryTimer = Timer.builder("orm.query.duration")
            .tag("system", "eclipselink")
            .description("EclipseLink query execution time")
            .register(meterRegistry);
        
        // Register cache metrics
        registerCacheMetrics();
        
        LOG.info("Dual ORM metrics initialized");
    }
    
    private void registerCacheMetrics() {
        // Hibernate cache hit ratio
        Gauge.builder("orm.cache.hit.ratio", this, metrics -> {
                try {
                    return cacheManager.getCacheStatistics().getHibernateHitRatio();
                } catch (Exception e) {
                    return 0.0;
                }
            })
            .tag("system", "hibernate")
            .description("Hibernate cache hit ratio")
            .register(meterRegistry);
        
        // EclipseLink cache size
        Gauge.builder("orm.cache.size", this, metrics -> {
                try {
                    return (double) cacheManager.getCacheStatistics().eclipseLinkCacheSize;
                } catch (Exception e) {
                    return 0.0;
                }
            })
            .tag("system", "eclipselink")
            .description("EclipseLink cache size")
            .register(meterRegistry);
    }
    
    public void recordQuarkusOperation() {
        quarkusOperations.increment();
    }
    
    public void recordEclipseLinkOperation() {
        eclipseLinkOperations.increment();
    }
    
    public Timer.Sample startQuarkusTimer() {
        return Timer.start(meterRegistry);
    }
    
    public Timer.Sample startEclipseLinkTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopQuarkusTimer(Timer.Sample sample) {
        sample.stop(quarkusQueryTimer);
    }
    
    public void stopEclipseLinkTimer(Timer.Sample sample) {
        sample.stop(eclipseLinkQueryTimer);
    }
}