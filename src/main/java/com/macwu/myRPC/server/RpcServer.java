package com.macwu.myRPC.server;

/**
 * Created by macwu on 16-6-4.
 * @author macwu
 * @version 0.01
 */

import java.util.Map;

/**
 * the class RpcServer defines the modules that a rpc server consists of.
 * And in this class, the service is exposed,
 * by the func publish().
 * see the implemntion in class RpcServerImpl
 */

public abstract class RpcServer
{
    /**
     *Export the service for the remote invokation
     * In func serviceInterface, returns the server class, setter
     * In func serviceImpl, returns the instance of service, setter
    */
    public abstract RpcServer serviceInterface(Class<?> serviceInterface);

    /**
     * returns the service instance
     * */
    public abstract RpcServer serviceImpl(Object serviceInstance);

    /**
     * setter
     * timeout setter definition
     * */
    public abstract RpcServer timeout(int timeout);

    /**
     * setter
     * version setter definition
     * */
    public abstract RpcServer version(String version);

    /**
     * type setter definition
     * */
    public abstract RpcServer serializedType(String serializedType);

    /**
     * the publish() func is used to publish the service on the server
     * this func is implemented by RpcServerImpl
     * */
    public abstract void publish(String serviceName);
}
