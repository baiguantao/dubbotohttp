package com.baigt.consumer;

import com.baigt.share.PersonService;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Person3rdService implements InitializingBean {
    @Autowired
    private PersonService personService;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("result::"+personService.getById("1"));
    }
}
