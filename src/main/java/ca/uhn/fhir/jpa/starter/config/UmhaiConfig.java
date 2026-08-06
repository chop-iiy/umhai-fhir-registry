package ca.uhn.fhir.jpa.starter.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import ca.uhn.fhir.jpa.starter.rest.UmhaiRegistrationEndpoint;
import ca.uhn.fhir.jpa.starter.service.UmhaiRegistrationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UmhaiConfig {

    @Bean
    public UmhaiRegistrationService umhaiRegistrationService() {
        return new UmhaiRegistrationService();
    }

    @Bean
    public UmhaiRegistrationEndpoint umhaiRegistrationEndpoint(UmhaiRegistrationService service) {
        return new UmhaiRegistrationEndpoint(service);
    }
    @Bean
    public FilterRegistrationBean<org.apache.catalina.filters.RequestDumperFilter> requestDumperFilter() {
        FilterRegistrationBean<org.apache.catalina.filters.RequestDumperFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new org.apache.catalina.filters.RequestDumperFilter());
        registration.addUrlPatterns("/api/umhai/*");
        return registration;
    }

}