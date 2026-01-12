package com.assessment.bank.rak.service.student.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfiguration implements WebFluxConfigurer {

	@Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        // This explicitly tells WebFlux how to resolve the Pageable interface
		ReactivePageableHandlerMethodArgumentResolver resolver = new ReactivePageableHandlerMethodArgumentResolver();
	    resolver.setMaxPageSize(100); // Optional: protect your DB from huge page requests
	    resolver.setFallbackPageable(org.springframework.data.domain.PageRequest.of(0, 10));
	    
	    configurer.addCustomResolver(resolver);
    }
	
}
