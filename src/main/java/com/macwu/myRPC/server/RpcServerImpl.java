package com.macwu.myRPC.server;

import com.macwu.myRPC.RpcProtocolModel.RpcRequest;
import com.macwu.myRPC.RpcProtocolModel.RpcResponse;
import com.macwu.myRPC.RpcProtocolModel.serializer.RpcDecode;
import com.macwu.myRPC.RpcProtocolModel.serializer.RpcEncode;
import com.macwu.myRPC.server.handlers.RpcRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by macwu on 16-6-4.
 */
public class RpcServerImpl extends RpcServer{
    //<serviceName, instance>
    private Map<String, Object> serviceMap = new HashMap<String, Object>();
    //service class name
    private Class<?> serviceInterface;
    //service instance itself
    private Object serviceInstance;

    private int timeout;
    private String version;
    private String serializedType;

    @Override
    public RpcServer serviceInterface(Class<?> serviceInterface)
    {
        this.serviceInterface = serviceInterface;
        return this;
    }

    @Override
    public RpcServer serviceImpl(Object serviceInstance)
    {
        this.serviceInstance = serviceInstance;
        return this;
    }

    @Override
    public RpcServer timeout(int timeout)
    {
        this.timeout = timeout;
        return this;
    }

    @Override
    public RpcServer version(String version)
    {
        this.version = version;
        return this;
    }

    @Override
    public RpcServer serializedType(String serializedType)
    {
        this.serializedType = serializedType;
        return null;
    }

    @Override
    public void publish(String serviceName)
    {
        /**
         * use the reflection to implement a service from a service name
         * service name: PackagePath.ClassName
         * */
        try
        {
            Class clazz = Class.forName(serviceName);
            Object obj = clazz.newInstance();

            this.serviceImpl(obj);
            this.serviceInterface(clazz);
        }catch (Exception e)
        {
            System.out.println("class " + serviceName + "not found");
            e.printStackTrace();
        }

        serviceMap.put(serviceName, serviceInstance);

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try
        {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            /**
                             * the server process:
                             *          byte[] ->
                             *          decode the Request
                             *              -> request
                             *          -> RpcRequestHandler(request)
                             *              -> response
                             *          -> encode the Response
                             *              -> byte[]
                             *          -> RpcResponseHandler(byte[])
                             *          ->... sent to client
                             * */
                            ch.pipeline().addLast(new RpcEncode(RpcResponse.class));
                            ch.pipeline().addLast(new RpcDecode(RpcRequest.class));
                            ch.pipeline().addLast(new RpcRequestHandler(serviceMap));
                            //test for decode function, done.
//                            ch.pipeline().addLast(new RpcDecodeTest(Integer.class));
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_SNDBUF, 1024)
                    .option(ChannelOption.SO_RCVBUF, 2048)
                    .childOption(ChannelOption.TCP_NODELAY, true);

            ChannelFuture f = serverBootstrap.bind(8080).sync();
            f.channel().closeFuture().sync();

        }catch (InterruptedException e){
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
