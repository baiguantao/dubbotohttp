package com.baigt.dubbo;

import com.baigt.share.PersonService;
import com.baigt.share.param.InnerObj;

import java.util.HashMap;
import java.util.Map;

public class PersonServiceImpl implements PersonService {
    private static Map<String ,Object> maps=new HashMap<>();
    static {
        maps.put("1",new InnerObj());
        maps.put("2",new InnerObj());
        maps.put("3",new InnerObj());
    }
    @Override
    public Object getById(String s) {
        System.out.println("开始执行");
        return maps.getOrDefault(s,new InnerObj("默认值",18));
    }
}
