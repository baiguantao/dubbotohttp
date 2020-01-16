package com.baigt.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/*
* https://docs.spring.io/spring-boot/docs/1.5.14.RELEASE/reference/html/
* */
@Configuration
@EnableWebMvc
@EnableAutoConfiguration
public class WebConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        super.addViewControllers(registry);
    }
    @Override
    public void configureContentNegotiation(
            ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //设置webjars资源路径
        if (!registry.hasMappingForPattern("/webjars/**")) {
            registry.addResourceHandler("/webjars/**").addResourceLocations(
                    "classpath:/META-INF/resources/webjars/");
        }

        registry.addResourceHandler("/static/**").addResourceLocations(
                "classpath:/META-INF/resources/static/" ,"classpath:/resources/static/","classpath:" +
                        "/static/");

        super.addResourceHandlers(registry);
    }
}
