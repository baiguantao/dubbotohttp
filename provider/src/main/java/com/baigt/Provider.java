package com.baigt;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Provider {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"classpath:/provider.xml"});
        context.start();
        System.out.println("hi,baigt;Provider start success");
        System.in.read(); // Press any Key exit
    }
}
