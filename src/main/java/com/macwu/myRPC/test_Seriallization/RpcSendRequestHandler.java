package com.macwu.myRPC.test_Seriallization;

import com.macwu.myRPC.RpcContext;
import com.macwu.myRPC.RpcProtocolModel.RpcRequest;
import com.macwu.myRPC.RpcProtocolModel.RpcResponse;
import com.macwu.myRPC.RpcProtocolModel.serializer.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by macwu on 16-6-5.
 */

public class RpcSendRequestHandler extends ChannelInboundHandlerAdapter
{
    public RpcRequest generateRequest(Method method, Object[] args)
    {
        RpcRequest request = new RpcRequest();
        request.setRequestID(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setparameterTypes(method.getParameterTypes());
        request.setParameters(args);

        try
        {
            request.setContext(RpcContext.props);
        }
        catch (Exception e)
        {
            e.getCause();
        }
        return request;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx)
    {
        byte[] dataSent = null;

        try
        {
            Class clz = Class.forName("com.macwu.myRPC.RpcService.RpcCalc");

            Object test = clz.newInstance();

            Method method = clz.getMethod("doCalc", new Class[]{String.class, String.class, String.class});

            RpcRequest obj = generateRequest(method, new Object[]{"hello, ", "thank u ", "thank u very much..."});

            final ChannelFuture f = ctx.writeAndFlush(obj);

            f.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture){
                    assert f == channelFuture;
//                    ctx.close();
                }
            });
        }catch (Exception e){e.getCause();}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
