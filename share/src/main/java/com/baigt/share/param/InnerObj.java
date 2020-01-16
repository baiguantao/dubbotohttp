package com.baigt.share.param;

import java.io.Serializable;
import java.util.Random;

public class InnerObj implements Serializable {
    private String name="baigt"+new Random().nextInt(100);
    private int age=new Random().nextInt(100);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public InnerObj(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public InnerObj() {
    }
}