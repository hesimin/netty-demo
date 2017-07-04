package com.github.hesimin;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * @author hesimin 2017-07-04
 */
public class NettyClient {

    public void connect(String host, int port) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap boot = new Bootstrap();
        boot.group(group)
                .channel(NioSocketChannel.class)
//                .remoteAddress(host, port)
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("client initChannel..");
                        ch.pipeline().addLast(new ReadTimeoutHandler(5)).addLast(new NettyClientHandler());
                    }
                });

        try {
            ChannelFuture future = boot.connect(host, port).sync();
            future.addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    System.out.println("channel IO success.");
                } else {
                    System.out.println("channel IO fail.");
                    future1.cause().printStackTrace();
                }
            });
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new NettyClient().connect("127.0.0.1", 9999);
    }
}
