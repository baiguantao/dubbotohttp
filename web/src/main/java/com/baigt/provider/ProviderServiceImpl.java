package com.baigt.provider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.dubbo.config.annotation.Service;

@Service
public class ProviderServiceImpl implements ProviderService{
    public Map<String,Object> test() {
        System.out.println("something");
        ReusltBo bo=new ReusltBo("baigt",100);
        ReusltBo bo2=new ReusltBo("baigt2",100);
        ReusltBo bo3=new ReusltBo("baigt3",100);
        return Arrays.asList(bo,bo2,bo3).stream().collect(HashMap::new,(a,b)->{
            a.put(b.getName(),b.getAge());
        },Map::putAll);
    }
}
class ReusltBo{
    private String name;
    private int age;

    public ReusltBo(String name, int age) {
        this.name = name;
        this.age = age;
    }

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
}
