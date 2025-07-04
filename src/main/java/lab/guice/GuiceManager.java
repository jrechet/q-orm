package lab.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

/**
 * Gestionnaire pour l'intégration Guice dans Quarkus.
 * Initialise l'injecteur Guice au démarrage de l'application.
 */
@ApplicationScoped
@Startup
public class GuiceManager {
    
    private static final Logger LOG = Logger.getLogger(GuiceManager.class);
    
    private Injector injector;
    
    @PostConstruct
    public void init() {
        LOG.info("Initializing Guice injector with Quarkus integration");
        try {
            injector = Guice.createInjector(new QuarkusIntegrationModule());
            LOG.info("Guice injector successfully initialized");
        } catch (Exception e) {
            LOG.error("Failed to initialize Guice injector", e);
            throw new RuntimeException("Guice initialization failed", e);
        }
    }
    
    /**
     * Retourne l'injecteur Guice
     */
    public Injector getInjector() {
        if (injector == null) {
            throw new IllegalStateException("Guice injector not initialized");
        }
        return injector;
    }
    
    /**
     * Méthode utilitaire pour obtenir une instance depuis Guice
     */
    public <T> T getInstance(Class<T> type) {
        return getInjector().getInstance(type);
    }
}