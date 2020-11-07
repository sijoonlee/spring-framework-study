package com.example.demo.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ComponentBeanMethod {
    @Bean
    Integer theNumber(){
        log.info("theNumber() called");
        return 3456;
    }
}
