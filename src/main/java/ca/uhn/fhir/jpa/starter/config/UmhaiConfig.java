package ca.uhn.fhir.jpa.starter.config;

import ca.uhn.fhir.jpa.starter.repository.UmhaiInstanceRepository;
import ca.uhn.fhir.jpa.starter.repository.UmhaiHistoryRepository;
import ca.uhn.fhir.jpa.starter.mapper.UmhaiMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import ca.uhn.fhir.jpa.starter.rest.UmhaiRegistrationEndpoint;
import ca.uhn.fhir.jpa.starter.service.UmhaiRegistrationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UmhaiConfig {

	@Bean
	public UmhaiRegistrationService umhaiRegistrationService(
        UmhaiInstanceRepository instanceRepository,
        UmhaiHistoryRepository historyRepository,
        UmhaiMapper mapper) {
    return new UmhaiRegistrationService(instanceRepository, historyRepository, mapper);
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