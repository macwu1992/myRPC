package com.macwu.myRPC.server.handlers;

import com.macwu.myRPC.RpcContext;
import com.macwu.myRPC.RpcProtocolModel.RpcRequest;
import com.macwu.myRPC.RpcProtocolModel.RpcResponse;
import com.macwu.myRPC.RpcProtocolModel.serializer.KryoSerializer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by macwu on 16-6-4.
 */
public class RpcRequestHandler extends ChannelInboundHandlerAdapter
{
    private RpcResponse rpcResponse;

    public RpcResponse getRpcResponse() {
        return rpcResponse;
    }

    public void setRpcResponse(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
    }

    /**
     * for every (requestID : RpcContext)
     * */
    private static Map<String,Map<String,Object>> ThreadLocalMap=new HashMap<String, Map<String,Object>>();

    private Map<String, Object> serviceMap;

    /**
     * tool to serialize the response
     * */
    private KryoSerializer kryo = new KryoSerializer();

    public RpcRequestHandler(Map<String, Object> serviceMap)
    {
        this.serviceMap = serviceMap;
    }

    /**
     * resovle the request, get all the attribute.
     * and invoke the function,
     * then generate a response
     * */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        RpcRequest rpcRequest = (RpcRequest) msg;
        String host = ctx.channel().remoteAddress().toString();

        //update the context, to get the actual context in the request
        UpdateRpcContext(host, rpcRequest.getContext());

        RpcServerInvoke rpcServerInvoke = new RpcServerInvoke(rpcRequest, serviceMap);
        /**
         * the response has been serialized in the invoke() function
         * */
        this.setRpcResponse(rpcServerInvoke.invoke());

        /**
         * put response into the pipe again.
         * */
        ctx.writeAndFlush(this.getRpcResponse());
    }

    private void UpdateRpcContext(String host, Map<String,Object> map)
    {
        if(ThreadLocalMap.containsKey(host))
        {
            Map<String,Object> local = ThreadLocalMap.get(host);
            //put all RpcContext into map
            local.putAll(map);
            ThreadLocalMap.put(host, local);
            for(Map.Entry<String, Object> entry:map.entrySet()){
                RpcContext.addProp(entry.getKey(), entry.getValue());
            }
        }
        else
        {
            ThreadLocalMap.put(host, map);
            for(Map.Entry<String, Object> entry:map.entrySet()){
                RpcContext.addProp(entry.getKey(), entry.getValue());
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.getCause();
    }
}
