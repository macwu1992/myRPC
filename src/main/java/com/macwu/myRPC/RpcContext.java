package com.macwu.myRPC;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by macwu on 16-6-5.
 */
public class RpcContext {
    //TODO how can I get props as a provider? tip:ThreadLocal
    public static Map<String,Object> props = new HashMap<String, Object>();

    public static void addProp(String key ,Object value){
        props.put(key,value);
    }

    public static Object getProp(String key){
        return props.get(key);
    }

    public static Map<String,Object> getProps(){
        return Collections.unmodifiableMap(props);
    }
}
