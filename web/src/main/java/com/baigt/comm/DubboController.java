package com.baigt.comm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import org.apache.dubbo.common.utils.ReflectUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dubbo to Http tools
 * 依赖
  <dependency>
      <groupId>org.apache.dubbo</groupId>
      <artifactId>dubbo</artifactId>
      <version>2.7.3</version>
  </dependency>
  <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>2.5.1</version>
      <scope>compile</scope>
      <exclusions>
      <exclusion>
      <artifactId>commons-logging</artifactId>
      <groupId>commons-logging</groupId>
      </exclusion>
      </exclusions>
  </dependency>
 * @author baigt mail:baiguantao@126.com
 */
@RestController
@RequestMapping("/dubbotohttp/")
public class DubboController implements ApplicationContextAware {
    private HashMap<String, Object> consumers = new HashMap<>();
    private HashMap<String, Object> providers = new HashMap<>();
    private ApplicationContext applicationContext;

    @GetMapping("list")
    @ResponseBody
    public Object list() {
        HashMap<String, Object> data = new HashMap<>();
        getData(data, false);
        return data;
    }

    public void getData(HashMap<String, Object> data, Boolean notInstance) {
        consumers = new HashMap<>();
        providers = new HashMap<>();

        ApplicationModel.allConsumerModels().forEach(s -> {
            HashMap consumer = new HashMap();
            consumers.put(s.getServiceName(), consumer);
            s.getAllMethods().forEach(md -> {
                try {
                    HashMap consumerDetail = new HashMap();
                    String name = md.getMethod().getName();
                    String returnType = md.getMethod().getGenericReturnType().getTypeName();
                    Class<?>[] parameterTypes = md.getMethod().getParameterTypes();
                    consumerDetail.put("methodName", name);
                    if (notInstance) {
                        consumerDetail.put("instance", s.getProxyObject());
                        consumerDetail.put("methodInstance", md.getMethod());
                    }
                    consumerDetail.put("returnType", returnType);
                    consumerDetail.put("parameterTypes",
                        parameterTypes.length > 0 ? Arrays.asList(parameterTypes).stream().map(pt -> {
                            return pt.getName();
                        }).collect(Collectors.joining(",")) : "无入参");
                    consumerDetail.put("parameterTypesDemo",
                        parameterTypes.length > 0 ? getParameterInstance(parameterTypes) : "无入参");
                    consumer.put(name, consumerDetail);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        data.put("consumers", consumers);
        ApplicationModel.allProviderModels().forEach(s -> {
            HashMap provider = new HashMap();
            providers.put(s.getServiceName(), provider);
            s.getAllMethods().forEach(md -> {
                try {
                    HashMap providerDetail = new HashMap();
                    String name = md.getMethod().getName();
                    String returnType = md.getMethod().getGenericReturnType().getTypeName();
                    Class<?>[] parameterTypes = md.getMethod().getParameterTypes();
                    providerDetail.put("methodName", name);
                    if (notInstance) {
                        providerDetail.put("instance", s.getServiceInstance());
                        providerDetail.put("methodInstance", md.getMethod());
                    }
                    providerDetail.put("returnType", returnType);
                    providerDetail.put("parameterTypes",
                        parameterTypes.length > 0 ? Arrays.asList(parameterTypes).stream().map(pt -> {
                            return pt.getName();
                        }).collect(Collectors.joining(",")) : "无入参");
                    providerDetail.put("parameterTypesDemo",
                        parameterTypes.length > 0 ? getParameterInstance(parameterTypes) : "无入参");
                    provider.put(name, providerDetail);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        data.put("providers", providers);
    }

    public String getParameterInstance(Class<?>[] parameterTypes) {
        return Arrays.asList(parameterTypes).stream().map(pt -> {
            try {
                if (isPrimitive(pt.getName())) {
                    return pt.getSimpleName();
                } else {
                    return JsonUtil.write(pt.newInstance());
                }
            } catch (Exception e) {
                //                e.printStackTrace();
            }
            return pt.getSimpleName();
        }).collect(Collectors.joining("\n"));
    }

    public boolean isPrimitive(String name) {
        // String 不是Primitive  这里只是用来生成数据使用的判断
        Boolean flag =
            name.equals("java.lang.Boolean") || name.equals("java.lang.Character") || name.equals("java.lang.Byte")
                || name.equals("java.lang.Short") || name.equals("java.lang.Integer") || name.equals("java.lang.Long")
                || name.equals("java.lang.Float") || name.equals("java.lang.Double") || name.equals("java.lang.String")
                || name.equals("int") || name.equals("long");
        return flag;
    }

    @PostMapping("invoke")
    @ResponseBody
    public Object invoke(DubboEntity dubboEntity) {
        HashMap<String, Object> data = new HashMap<>();
        getData(new HashMap<>(), true);
        HashMap invoker = (HashMap) consumers.get(dubboEntity.getService());
        if (invoker == null) {
            invoker = (HashMap) providers.get(dubboEntity.getService());
        }
        HashMap Details = (HashMap) invoker.get(dubboEntity.getMethod());
        Object instance = Details.get("instance");
        Method methodInstance = (Method) Details.get("methodInstance");

        try {
            String parameterTypes = Details.get("parameterTypes").toString();
            String[] paramTypes = parameterTypes.split(",");
            Object result = null;
            if (StringUtils.hasText(dubboEntity.getUrl())) {
                Map<String, org.apache.dubbo.config.ApplicationConfig> applicationConfigMap =
                    applicationContext == null ?
                        null :
                        BeanFactoryUtils
                            .beansOfTypeIncludingAncestors(applicationContext, ApplicationConfig.class, false, false);
                ApplicationConfig applicationConfig=null;
                if (!applicationConfigMap.isEmpty() && applicationConfigMap.entrySet().size() == 1) {
                    Map.Entry<String, ApplicationConfig> next = applicationConfigMap.entrySet().iterator().next();
                    applicationConfig=next.getValue();
                } else {
                    applicationConfig=new ApplicationConfig();
                    applicationConfig.setRegistry(new RegistryConfig());
                }
                ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
                reference.setApplication(applicationConfig);
                reference.setInterface(dubboEntity.getService());
                reference.setGeneric(true);
                reference.setUrl(dubboEntity.getUrl());
                reference.setTimeout(10000);
                Object[] objectArray = null;
                if (!(paramTypes.length == 0 || "无入参".equals(parameterTypes))) {
                    objectArray = getObjectArray(dubboEntity, paramTypes);

                } else {
                    paramTypes = null;
                }
                result = reference.get().$invoke(dubboEntity.getMethod(), paramTypes, objectArray);
                data.put("result", PojoUtil.generalize(result));
            } else {
                if (paramTypes.length == 0 || "无入参".equals(parameterTypes)) {
                    result = methodInstance.invoke(instance);
                } else {
                    result = methodInstance.invoke(instance, getObjectArray(dubboEntity, paramTypes));
                }
                data.put("result", result);
            }

        } catch (Exception e) {
           data.put("result",e.getMessage());
        }

        return data;
    }

    Object[] getObjectArray(DubboEntity dubboEntity, String[] paramTypes) {
        int length = paramTypes.length;
        Object[] o = new Object[length];
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                o[i] = getArgs(dubboEntity.getParams(), i, paramTypes[i]);
            }
        }
        return o;
    }

    public Object getArgs(Object total, int index, String className) {
        Object backRes = null;
        if (Objects.nonNull(total)) {
            String[] inputArgs = total.toString().split("\n");
            try {
                backRes = inputArgs[index];
            } catch (Exception e) {
                backRes = inputArgs[0];
            }
            try {
                if (className.equals("int")) {
                    return Integer.valueOf(backRes.toString()).intValue();
                } else if (className.equals("long")) {
                    return Long.valueOf(backRes.toString()).longValue();
                } else {
                    backRes = JsonUtil.read((String) backRes, Class.forName(className));
                }
            } catch (Exception e) {
                //                e.printStackTrace();
                throw new RuntimeException("类型错误" + e.getMessage());
            }
        }
        return backRes;

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

class DubboEntity {
    private String service;
    private String method;
    private String url;
    private Object params;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

/**
 *  for remove class
 */
class PojoUtil {

    public static Object generalize(Object pojo) {
        return generalize(pojo, new IdentityHashMap<Object, Object>());
    }

    @SuppressWarnings("unchecked")
    private static Object generalize(Object pojo, Map<Object, Object> history) {
        if (pojo == null) {
            return null;
        }

        if (pojo instanceof Enum<?>) {
            return ((Enum<?>) pojo).name();
        }
        if (pojo.getClass().isArray() && Enum.class.isAssignableFrom(pojo.getClass().getComponentType())) {
            int len = Array.getLength(pojo);
            String[] values = new String[len];
            for (int i = 0; i < len; i++) {
                values[i] = ((Enum<?>) Array.get(pojo, i)).name();
            }
            return values;
        }

        if (ReflectUtils.isPrimitives(pojo.getClass())) {
            return pojo;
        }

        if (pojo instanceof Class) {
            return ((Class) pojo).getName();
        }

        Object o = history.get(pojo);
        if (o != null) {
            return o;
        }
        history.put(pojo, pojo);

        if (pojo.getClass().isArray()) {
            int len = Array.getLength(pojo);
            Object[] dest = new Object[len];
            history.put(pojo, dest);
            for (int i = 0; i < len; i++) {
                Object obj = Array.get(pojo, i);
                dest[i] = generalize(obj, history);
            }
            return dest;
        }
        if (pojo instanceof Collection<?>) {
            Collection<Object> src = (Collection<Object>) pojo;
            int len = src.size();
            Collection<Object> dest = (pojo instanceof List<?>) ? new ArrayList<Object>(len) : new HashSet<Object>(len);
            history.put(pojo, dest);
            for (Object obj : src) {
                dest.add(generalize(obj, history));
            }
            return dest;
        }
        if (pojo instanceof Map<?, ?>) {
            Map<Object, Object> src = (Map<Object, Object>) pojo;
            Map<Object, Object> tem = new HashMap<>();
            src.forEach((key, val) -> {
                if (!key.equals("class")) {
                    tem.put(key, val);
                }
            });
            src = tem;
            Map<Object, Object> dest = createMap(src);
            history.put(pojo, dest);
            for (Map.Entry<Object, Object> obj : src.entrySet()) {
                dest.put(generalize(obj.getKey(), history), generalize(obj.getValue(), history));
            }
            return dest;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        history.put(pojo, map);
        map.put("class", pojo.getClass().getName());
        for (Method method : pojo.getClass().getMethods()) {
            if (ReflectUtils.isBeanPropertyReadMethod(method)) {
                try {
                    map.put(ReflectUtils.getPropertyNameFromBeanReadMethod(method),
                        generalize(method.invoke(pojo), history));
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        // public field
        for (Field field : pojo.getClass().getFields()) {
            if (ReflectUtils.isPublicInstanceField(field)) {
                try {
                    Object fieldValue = field.get(pojo);
                    if (history.containsKey(pojo)) {
                        Object pojoGeneralizedValue = history.get(pojo);
                        if (pojoGeneralizedValue instanceof Map && ((Map) pojoGeneralizedValue)
                            .containsKey(field.getName())) {
                            continue;
                        }
                    }
                    if (fieldValue != null) {
                        map.put(field.getName(), generalize(fieldValue, history));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return map;
    }

    private static Map createMap(Map src) {
        Class<? extends Map> cl = src.getClass();
        Map result = null;
        if (HashMap.class == cl) {
            result = new HashMap();
        } else if (Hashtable.class == cl) {
            result = new Hashtable();
        } else if (IdentityHashMap.class == cl) {
            result = new IdentityHashMap();
        } else if (LinkedHashMap.class == cl) {
            result = new LinkedHashMap();
        } else if (Properties.class == cl) {
            result = new Properties();
        } else if (TreeMap.class == cl) {
            result = new TreeMap();
        } else if (WeakHashMap.class == cl) {
            return new WeakHashMap();
        } else if (ConcurrentHashMap.class == cl) {
            result = new ConcurrentHashMap();
        } else if (ConcurrentSkipListMap.class == cl) {
            result = new ConcurrentSkipListMap();
        } else {
            try {
                result = cl.newInstance();
            } catch (Exception e) { /* ignore */ }

            if (result == null) {
                try {
                    Constructor<?> constructor = cl.getConstructor(Map.class);
                    result = (Map) constructor.newInstance(Collections.EMPTY_MAP);
                } catch (Exception e) { /* ignore */ }
            }
        }

        if (result == null) {
            result = new HashMap<Object, Object>();
        }

        return result;
    }
}

class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * 反序列化为对象
     *
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T read(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return null;
        } else {
            try {
                return mapper.readValue(json, clazz);
            } catch (Exception var3) {
                throw new RuntimeException(var3);
            }
        }
    }

    /**
     * 序列化为json字符串
     *
     * @param obj
     * @return
     */
    public static String write(Object obj) {
        if (obj == null) {
            return "null";
        }

        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception var2) {
            throw new RuntimeException(var2);
        }
    }
}