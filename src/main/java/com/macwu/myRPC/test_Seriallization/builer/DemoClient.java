package com.macwu.myRPC.test_Seriallization.builer;

import com.macwu.myRPC.RpcProtocolModel.RpcRequest;
import com.macwu.myRPC.RpcProtocolModel.RpcResponse;
import com.macwu.myRPC.RpcProtocolModel.serializer.RpcDecode;
import com.macwu.myRPC.RpcProtocolModel.serializer.RpcEncode;
import com.macwu.myRPC.test_Seriallization.RpcReveiveResponseHandler;
import com.macwu.myRPC.test_Seriallization.RpcSendRequestHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by macwu on 16-6-5.
 */

public class DemoClient {

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 8080;
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new RpcDecode(RpcResponse.class));
                    ch.pipeline().addLast(new RpcReveiveResponseHandler());
                    ch.pipeline().addLast(new RpcEncode(RpcRequest.class));
                    ch.pipeline().addLast(new RpcSendRequestHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
