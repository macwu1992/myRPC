package com.macwu.myRPC.RpcProtocolModel.serializer;

import com.macwu.myRPC.RpcProtocolModel.RpcRequest;
import com.macwu.myRPC.RpcProtocolModel.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by macwu on 16-6-4.
 * this class is used for Message2Byte
 */
public class RpcEncode extends MessageToByteEncoder
{
    /**
     * use xxxCacheName and xxx CacheValue to deal with the loss of packet
     * */
    private static Object responseCacheName=null;
    private static byte[] responseCacheValue=null;
    private static Object requestCacheName=null;
    private static byte[] requestCacheValue=null;
    private Class<?> genericClass;
    private KryoSerializer kryo;
    public RpcEncode(Class<?> genericClass) {
        this.genericClass = genericClass;
        kryo=new KryoSerializer();
        kryo.register(genericClass);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception
    {
        /**
         * encode switched to RpcResponse mode
         * */
        if (genericClass == RpcResponse.class)
        {
            RpcResponse rpcResponse = (RpcResponse) msg;
            String requestID = rpcResponse.getRequestId();
System.out.println("Server Sending and Encoding rpcResponse ID: " + rpcResponse.getRequestId());

            //set the ID in the msg to "" inorder to minize the data transferd
            rpcResponse.setRequestId("");

            byte[] requestIDByte = requestID.getBytes();

            byte[] body = null;
            if (responseCacheName != null && responseCacheName.equals(rpcResponse))
            {
                body = responseCacheValue;
            }else
            {
                responseCacheName = rpcResponse;
                body = kryo.Serialize(rpcResponse);
                responseCacheValue = body;
            }

            /**
             * the totalLength tells the length of incoming request, not the component of request.
             * */
            int totalLength = requestIDByte.length + 4 + body.length;

            out.writeInt(totalLength);
            out.writeInt(requestIDByte.length);
            out.writeBytes(requestIDByte);
            out.writeBytes(body);
        }

        /**
         * encode switched to RpcRequest mode
         * */
        else if (genericClass == RpcRequest.class)
        {
            RpcRequest rpcRequest = (RpcRequest) msg;
            String requestID = rpcRequest.getRequestID();

System.out.println("Client Sending and Encoding rpcRequest ID: " + rpcRequest.getRequestID());
            //set the ID in the msg to "" inorder to minize the data transferd
            rpcRequest.setRequestID("");

            byte[] requestIDByte = requestID.getBytes();

            byte[] body = null;

            if (requestCacheName != null && requestCacheName.equals(rpcRequest))
            {
                body = requestCacheValue;
            }else
            {
                requestCacheName = rpcRequest;
                body = kryo.Serialize(rpcRequest);
                requestCacheValue = body;
            }

            int totalLength = requestIDByte.length + 4 + body.length;

            out.writeInt(totalLength);
            out.writeInt(requestIDByte.length);
            out.writeBytes(requestIDByte);
            out.writeBytes(body);
        }

        /**
         * encode switched to default mode
         * */
        else
        {
            /**
             * if genericClass is unknown,
             * the structure is : msgBodyLength + Msgbody
             * */
            byte[] body = kryo.Serialize(msg);
            out.writeInt(body.length);
            out.writeBytes(body);
        }
    }
}
