package com.example.demo.component;

import org.springframework.stereotype.Component;

@Component
public class ComponentDemo {

    private int number;

    public ComponentDemo(Integer theNumber){
        this.number = theNumber.intValue();
    }

    public String getNumber(){
        return Integer.toString(this.number);
    }
}
