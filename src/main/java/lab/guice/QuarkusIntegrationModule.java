package lab.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import lab.quarkus.service.GiftService;
import lab.quarkus.repository.GiftRepository;
import lab.guice.service.GuiceGiftService;
import org.jboss.logging.Logger;

/**
 * Module Guice qui intègre les composants Quarkus CDI.
 * Permet d'injecter des services et repositories Quarkus dans des composants Guice.
 */
public class QuarkusIntegrationModule extends AbstractModule {
    
    private static final Logger LOG = Logger.getLogger(QuarkusIntegrationModule.class);
    
    @Override
    protected void configure() {
        LOG.info("Configuring Guice module with Quarkus CDI integration");
        
        // Configuration des providers pour les composants Quarkus
        bind(GiftService.class).toProvider(QuarkusGiftServiceProvider.class).in(Singleton.class);
        bind(GiftRepository.class).toProvider(QuarkusGiftRepositoryProvider.class).in(Singleton.class);
        
        // Configuration du service Guice qui utilise les composants Quarkus
        bind(GuiceGiftService.class).toProvider(GuiceGiftServiceProvider.class).in(Singleton.class);
    }
    
    /**
     * Provider pour injecter GiftService depuis Quarkus CDI dans Guice
     */
    public static class QuarkusGiftServiceProvider implements Provider<GiftService> {
        @Override
        public GiftService get() {
            LOG.debug("Retrieving GiftService from Quarkus CDI container");
            InstanceHandle<GiftService> instance = Arc.container().instance(GiftService.class);
            if (instance.isAvailable()) {
                return instance.get();
            }
            throw new RuntimeException("GiftService not available in Quarkus CDI container");
        }
    }
    
    /**
     * Provider pour injecter GiftRepository depuis Quarkus CDI dans Guice
     */
    public static class QuarkusGiftRepositoryProvider implements Provider<GiftRepository> {
        @Override
        public GiftRepository get() {
            LOG.debug("Retrieving GiftRepository from Quarkus CDI container");
            InstanceHandle<GiftRepository> instance = Arc.container().instance(GiftRepository.class);
            if (instance.isAvailable()) {
                return instance.get();
            }
            throw new RuntimeException("GiftRepository not available in Quarkus CDI container");
        }
    }
    
    /**
     * Provider pour créer GuiceGiftService avec les composants Quarkus injectés
     */
    public static class GuiceGiftServiceProvider implements Provider<GuiceGiftService> {
        @Override
        public GuiceGiftService get() {
            LOG.debug("Creating GuiceGiftService with Quarkus components");
            
            // Récupérer les composants Quarkus
            InstanceHandle<GiftService> giftServiceInstance = Arc.container().instance(GiftService.class);
            InstanceHandle<GiftRepository> giftRepositoryInstance = Arc.container().instance(GiftRepository.class);
            
            if (giftServiceInstance.isAvailable() && giftRepositoryInstance.isAvailable()) {
                return new GuiceGiftService(giftServiceInstance.get(), giftRepositoryInstance.get());
            }
            
            throw new RuntimeException("Required Quarkus components not available in CDI container");
        }
    }
}