package com.example.demo;

import com.example.demo.beanDemo.BeanDemo;
import com.example.demo.component.ComponentDemo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class DemoApplication {

    public static void main(String[] args) {

        //SpringApplication.run(DemoApplication.class, args); not sure how to use this

        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext("com.example.demo");

        //DemoApplication demo = new DemoApplication();
        // -> this approach wouldn't work
        // Spring isn't involved in this instantiation
        // https://stackoverflow.com/questions/19896870/why-is-my-spring-autowired-field-null

        // --> moved to Main class(eventlistener)
        //DemoApplication demo = context.getBean(DemoApplication.class);
        //System.out.println(demo.componentDemo.getNumber());
        //System.out.println(demo.beanDemo.getTheBeanNumber());

        context.close();

    }

}
