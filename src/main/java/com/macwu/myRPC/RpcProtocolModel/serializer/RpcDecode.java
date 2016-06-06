package com.macwu.myRPC.RpcProtocolModel.serializer;

import com.macwu.myRPC.RpcProtocolModel.RpcRequest;
import com.macwu.myRPC.RpcProtocolModel.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by macwu on 16-6-4.
 * this class is used for Byte2Message
 */
public class RpcDecode extends ByteToMessageDecoder
{
    private static byte[] requestCacheName = null;
    private static RpcRequest requestCacheValue = null;
    private static byte[] responseCacheName = null;
    private static RpcResponse responseCacheValue = null;
    private Class<?> genericClass;
    private KryoSerializer kryo;

    public RpcDecode(Class<?> genericClass)
    {
        this.genericClass = genericClass;
        kryo = new KryoSerializer();
        kryo.register(this.genericClass);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        /**
         * beacause the length of requestIDByte is 4, so the length of in_flow is at least 4.
         * */
        int HEAD_LENGTH_MIN = 4;
        if (in.readableBytes() < HEAD_LENGTH_MIN)
        {
            return;
        }

        /**Mark the current buffer position before reading the length field
         * because the whole frame might not be in the buffer yet.
         * We will reset the buffer position to the marked position if
         * there's not enough bytes in the buffer.
         */

        in.markReaderIndex();

        /**
         * this is the totalLength of the data
         * */
        int totalLength = in.readInt();

        if (totalLength < 0)
        {
            ctx.close();
        }

        if (in.readableBytes() < totalLength)
        {
            in.resetReaderIndex();
            return;
        }

        /**
         * read the requestID
         * */
        int requestIDByteLength =  in.readInt();
        byte[] requestIDByte = new byte[requestIDByteLength];
        in.readBytes(requestIDByte);
        String requestID = new String(requestIDByte);

        /**
         * read the body
         * */
        int bodyByteLength = totalLength - 4 - requestIDByteLength;

        byte[] body = new byte[bodyByteLength];
        in.readBytes(body);

        /**
         * decode switched to RpcResponse mode
         * */
        if (genericClass.equals(RpcResponse.class))
        {
            RpcResponse rpcResponse = new RpcResponse();

            if (responseCacheName != null && cacheEqual(responseCacheName, body))
            {
                rpcResponse.setRequestId(responseCacheValue.getRequestId());
                rpcResponse.setAppResponse(responseCacheValue.getAppResponse());
                rpcResponse.setClazz(responseCacheValue.getClazz());
                rpcResponse.setErrorMsg(responseCacheValue.getErrorMsg());
                rpcResponse.setExption(responseCacheValue.getExption());

                out.add(rpcResponse);
            }
            else
            {
                responseCacheName = body;
                rpcResponse = kryo.Deserialize(body);
                responseCacheValue = rpcResponse;
                rpcResponse.setRequestId(requestID);

                out.add(rpcResponse);
            }

System.out.println("Client Receiving and Decoding rpcResponse ID: " + rpcResponse.getRequestId());
        }

        /**
         * decode switched to RpcRequest mode
         * */
        else if (genericClass.equals(RpcRequest.class))
        {
            RpcRequest rpcRequest = new RpcRequest();

            if (requestCacheName != null && cacheEqual(requestCacheName, body))
            {
                rpcRequest.setRequestID(requestCacheValue.getRequestID());
                rpcRequest.setClassName(requestCacheValue.getClassName());
                rpcRequest.setMethodName(requestCacheValue.getMethodName());
                rpcRequest.setparameterTypes(requestCacheValue.getParameterTypes());
                rpcRequest.setParameters(requestCacheValue.getParameters());
                rpcRequest.setContext(requestCacheValue.getContext());

                out.add(rpcRequest);
            }
            else
            {
                requestCacheName = body;
                rpcRequest = kryo.Deserialize(body);
                requestCacheValue = rpcRequest;
                rpcRequest.setRequestID(requestID);

                out.add(rpcRequest);
            }

System.out.println("Server Receiving and Decoding rpcRequest ID: " + rpcRequest.getRequestID());
        }
        else
        {
            Object obj = kryo.Deserialize(body);
            out.add(obj);
        }
    }

    private static boolean cacheEqual(byte[] data1,byte[] data2)
    {
        if(data1==null)
        {
            if(data2!=null)
                return false;
        }
        else
        {
            if(data2==null)
                return false;

            if(data1.length!=data2.length)
                return false;

            for (int i = 0; i < data1.length; i++) {
                if(data1[i]!=data2[i])
                    return false;
            }
        }
        return true;
    }
}