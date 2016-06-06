package com.macwu.myRPC.test_Seriallization;

import com.macwu.myRPC.RpcProtocolModel.serializer.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by macwu on 16-6-5.
 */
public class RpcDecodeTest extends ByteToMessageDecoder
{
    private KryoSerializer kryo;
    private Class<?> genericClass;

    public RpcDecodeTest(Class<?> genericClass)
    {
        this.genericClass = genericClass;
        this.kryo = new KryoSerializer();
        this.kryo.register(genericClass);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        byte[] byteMsg = new byte[3];
        if (in.readableBytes() < 3)
        {
            return;
        }

        in.readBytes(byteMsg);

        int realMsg = kryo.Deserialize(byteMsg);

        System.out.println(realMsg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
