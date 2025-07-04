package lab.bridge;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import lab.guice.GuiceManager;
import lab.guice.service.PureGuiceService;
import org.jboss.logging.Logger;

/**
 * Provider qui permet d'injecter des composants Guice dans Quarkus CDI.
 * Utilise @Produces pour exposer les instances Guice comme beans CDI.
 */
@ApplicationScoped
public class GuiceToCdiProvider {
    
    private static final Logger LOG = Logger.getLogger(GuiceToCdiProvider.class);
    
    @Inject
    GuiceManager guiceManager;
    
    /**
     * Produit PureGuiceService depuis Guice pour injection CDI
     */
    @Produces
    @ApplicationScoped
    public PureGuiceService producePureGuiceService() {
        LOG.info("Producing PureGuiceService from Guice for CDI injection");
        return guiceManager.getInstance(PureGuiceService.class);
    }
}