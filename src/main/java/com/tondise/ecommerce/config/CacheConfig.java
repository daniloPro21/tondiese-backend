package com.tondise.ecommerce.config;

import com.tondise.utils.absrtractServices.AbstractService;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code AbstractService} (tondise-util) déclare ses méthodes CRUD avec
 * {@code @Cacheable(keyGenerator = "customKeyGenerator")} : ce bean doit donc
 * exister ici, sans quoi le premier appel à une méthode héritée
 * ({@code findById}, {@code findAll}...) échoue avec un
 * {@code NoSuchBeanDefinitionException}. Même géométrie de clé que
 * task-force-remita ({@code RedisCacheConfig}) : {@code entityName-method(params)}.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public KeyGenerator customKeyGenerator() {
        return (target, method, params) -> {
            String entityName = target instanceof AbstractService<?, ?, ?> service
                    ? service.getEntityName()
                    : target.getClass().getSimpleName();
            String paramsKey = Arrays.stream(params)
                    .map(p -> p != null ? p.toString() : "null")
                    .collect(Collectors.joining(","));
            return entityName + "-" + method.getName() + "(" + paramsKey + ")";
        };
    }
}
