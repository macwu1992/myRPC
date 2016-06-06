package com.macwu.myRPC.server.handlers;

import com.macwu.myRPC.RpcProtocolModel.RpcRequest;
import com.macwu.myRPC.RpcProtocolModel.RpcResponse;
import com.macwu.myRPC.RpcProtocolModel.serializer.KryoSerializer;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by macwu on 16-6-5.
 */
public class RpcServerInvoke
{
    private RpcRequest rpcRequest;

    private String requestID;
    private String className;
    private Object classImpl;
    private Class clazz;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    private Map<String, Object> resultMap = new HashMap<String, Object>();
    private Map<String, Object> serviceMap = null;
    private Map<String,FastMethod> methodMap = new HashMap<String, FastMethod>();

    private static RpcRequest methodCacheName = null;
    private static Object methodCacheValue = null;

    private static Object resultCacheName = null;
    private static Object resultCacheValue = null;

    public RpcServerInvoke(RpcRequest rpcRequest, Map<String, Object> serviceMap) throws Exception
    {
        this.serviceMap = serviceMap;
        this.rpcRequest = rpcRequest;
        this.requestID = rpcRequest.getRequestID();
        this.className = rpcRequest.getClassName();
        this.classImpl = serviceMap.get(className);
        this.clazz = classImpl.getClass();
        this.methodName = rpcRequest.getMethodName();
        this.parameterTypes = rpcRequest.getParameterTypes();
        this.parameters =  rpcRequest.getParameters();
    }

    public String getRequestID() {
        return requestID;
    }

    public Class getClazz() {
        return clazz;
    }

    public RpcResponse invoke()
    {
        KryoSerializer kryo = new KryoSerializer();
        RpcResponse rpcResponse = new RpcResponse();
        Object result = new Object();

        /**
         * set responseID and service class
         * */
        rpcResponse.setRequestId(getRequestID());
        rpcResponse.setClazz(getClazz());

        /**
         * invoke the function
         * */
        if (methodCacheName != null && methodCacheName.equals(rpcRequest))
        {
            result = methodCacheValue;
        }
        else
        {
            try
            {
                methodCacheName = rpcRequest;
                if (methodMap.containsKey(methodName))
                {
                    result = methodMap.get(methodName).invoke(classImpl, parameters);
                    methodCacheValue = result;
                }
                else
                {
                    FastClass servicefastClass = FastClass.create(clazz);
                    FastMethod serviceFastMethod = servicefastClass.getMethod(methodName, parameterTypes);
                    methodMap.put(methodName, serviceFastMethod);
                    result = serviceFastMethod.invoke(classImpl, parameters);
                    methodCacheValue = result;
                }
            } catch (Exception e)
            {
                e.getCause();
                /**
                 * set exception infomation of response
                 * */
                rpcResponse.setErrorMsg(e);
                rpcResponse.setExption(kryo.Serialize(e));
            }
        }

        if (resultCacheName != null && resultCacheName.equals(result))
        {
            rpcResponse.setAppResponse(resultCacheValue);
        }
        else
        {
            resultCacheName = result;

            String resultTypeName = result.getClass().getName();

            resultMap.put("resultTypeName", resultTypeName);
            resultMap.put("resultValue", kryo.Serialize(result));

            resultCacheValue = kryo.Serialize(resultMap);

            rpcResponse.setAppResponse(resultCacheValue);
        }

        return rpcResponse;
    }
}
