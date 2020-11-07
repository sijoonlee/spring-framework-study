package com.example.demo.beanDemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class BeanBeanMethod {
    @Bean
    Integer theBeanNumber(){
        log.info("theBeanNumber() called");
        return 1111;
    }

    @Bean
    BeanDemo beanDemo(Integer theBeanNumber){
        return new BeanDemo(theBeanNumber);
    }

//    @Bean
//    BeanDemo beanDemo(){
//        return new BeanDemo(1234);
//    }
}
