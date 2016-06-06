package com.macwu.myRPC.RpcService;

import java.util.Map;

/**
 * Created by macwu on 16-6-5.
 */
public class RpcCalc implements RpcServiceInterface
{
    public Map<String, Object> getMap()
    {
        return null;
    }

    public String getString()
    {
        String serviceName = this.getClass().toString();
        System.out.println("this is Rpc Service named : " + serviceName);
        return serviceName;
    }

    public boolean longTimeMethod()
    {
        return false;
    }

    public String doCalc(String a, String b, String c)
    {
        String d = a + b +c;
        System.out.println(d);
        return d;
    }
}
