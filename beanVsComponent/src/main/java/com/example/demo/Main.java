package com.example.demo;

import com.example.demo.beanDemo.BeanDemo;
import com.example.demo.component.ComponentDemo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
//@ComponentScan("com.example.demo")
@Component
public class Main {
    @Autowired
    ComponentDemo componentDemo;

    @Autowired
    BeanDemo beanDemo;

    @EventListener(classes={ContextRefreshedEvent.class})
    public void run() {
        log.info("EventListener working");
        System.out.println(componentDemo.getNumber());
        System.out.println(beanDemo.getTheBeanNumber());
    }
}
