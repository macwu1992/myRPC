package com.macwu.myRPC.test_Seriallization;

import com.macwu.myRPC.RpcProtocolModel.RpcResponse;
import com.macwu.myRPC.RpcProtocolModel.serializer.KryoSerializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by macwu on 16-6-6.
 */
public class RpcReveiveResponseHandler extends ChannelInboundHandlerAdapter
{
    private KryoSerializer kryo = new KryoSerializer();
    private Map<String, Object> resultMap = new HashMap<String, Object>();

    private Map<String, Map<String, Object>> serviceMap = new HashMap<String, Map<String, Object>>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        RpcResponse rpcResponse = (RpcResponse) msg;

        kryo.register(Map.class);

        Class clazz = rpcResponse.getClazz();
        String clazzName = clazz.getName();

        byte[] appResponseByte = (byte[]) rpcResponse.getAppResponse();

        this.resultMap = kryo.Deserialize(appResponseByte);

        serviceMap.put(clazzName, this.resultMap);

        Object resultTypeName = this.serviceMap.get(clazzName).get("resultTypeName");
        byte[] resultValueByte = (byte[]) this.serviceMap.get(clazzName).get("resultValue");

        Class resultType = Class.forName(resultTypeName.toString());

        kryo.register(resultType);

        Object resulteValue = resultType.cast(kryo.Deserialize(resultValueByte));

        System.out.println("Sevice Name: " + clazzName);
        System.out.println("Result Type : " + resultType.getName());
        System.out.println("Result Value: " + resulteValue);
    }
}
