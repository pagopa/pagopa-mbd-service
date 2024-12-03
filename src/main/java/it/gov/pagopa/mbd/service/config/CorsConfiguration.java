package it.gov.pagopa.mbd.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mbd.service.model.AppCorsConfiguration;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class CorsConfiguration implements WebFluxConfigurer {

    @Value("${cors.configuration}")
    private String corsConfiguration;


    @Override
    @SneakyThrows
    public void addCorsMappings(CorsRegistry registry) {
        AppCorsConfiguration appCorsConfiguration = new ObjectMapper().readValue(corsConfiguration,
                AppCorsConfiguration.class);
        registry.addMapping("/**")
                .allowedOrigins(appCorsConfiguration.getOrigins())
                .allowedMethods(appCorsConfiguration.getMethods());
    }

}
