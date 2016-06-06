package com.macwu.myRPC.test_Seriallization.builer;

import com.macwu.myRPC.server.RpcServerImpl;

/**
 * Created by macwu on 16-6-4.
 */
public class DemoServer extends RpcServerImpl{

    public static void main(String[] args) {
        DemoServer demoServer = new DemoServer();
        demoServer.publish("com.macwu.myRPC.RpcService.RpcCalc");
    }
}
