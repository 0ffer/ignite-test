package ru.offer.ignite.config;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.services.ServiceConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.offer.ignite.model.ProductOffering;
import ru.offer.ignite.model.Transition;
import ru.offer.ignite.service.ProductOfferingServiceImpl;

/**
 * @author Stas Melnichuk
 */
@Configuration
public class IgniteConfig {

    @Bean
    public CacheConfiguration productOfferingCacheConf() {
        return new CacheConfiguration<Integer, ProductOffering>()
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL_SNAPSHOT)
                .setName(CacheNames.PRODUCT_OFFERING);
    }

    @Bean
    public CacheConfiguration transitionCacheConf() {
        return new CacheConfiguration<Integer, Transition>()
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL_SNAPSHOT)
                .setName(CacheNames.TRANSITION);
    }

    @Bean
    public ServiceConfiguration productOfferingServiceConfiguration() {
        return new ServiceConfiguration()
                .setName(ServiceNames.PRODUCT_OFFERING_SERVICE)
                .setService(new ProductOfferingServiceImpl())
                .setTotalCount(1);
    }

    @Bean
    IgniteConfiguration igniteConfiguration(CacheConfiguration[] cacheConfigurations,
                                            ServiceConfiguration[] serviceConfigurations) {
        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();

        igniteConfiguration.setCacheConfiguration(cacheConfigurations);
        igniteConfiguration.setServiceConfiguration(serviceConfigurations);
        return igniteConfiguration;
    }

    @Bean(destroyMethod = "close")
    public Ignite ignite(IgniteConfiguration igniteConfiguration) throws IgniteException {
        final Ignite ignite = Ignition.start(igniteConfiguration);

        ignite.cluster().active(true);

        return ignite;
    }

}
