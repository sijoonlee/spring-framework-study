package com.example.demo.beanDemo;

public class BeanDemo {
    //Note: no @Component tag

    private int number;

    public BeanDemo(Integer theBeanNumber){
        this.number = theBeanNumber.intValue();
    }

    public String getTheBeanNumber(){
        return Integer.toString(this.number);
    }
}
