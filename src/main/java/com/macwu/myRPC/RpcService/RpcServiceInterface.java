package com.macwu.myRPC.RpcService;

import java.util.Map;

/**
 * Created by macwu on 16-6-5.
 */
public interface RpcServiceInterface {
    public Map<String,Object> getMap();
    public String getString();
    public boolean longTimeMethod();
}
