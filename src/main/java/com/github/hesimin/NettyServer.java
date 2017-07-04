package com.github.hesimin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * @author hesimin 2017-06-16
 */
public class NettyServer {
    private int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void run() throws InterruptedException {
        // 单线程模型、多线程模型、主从多线程模型

        //服务器端的 ServerSocketChannel 只绑定到了 bossGroup 中的一个线程, 因此在调用 Java NIO 的 Selector.select 处理客户端的连接请求时, 实际上是在一个线程中的, 所以对只有一个服务的应用来说, bossGroup 设置多个线程是没有什么作用的, 反而还会造成资源浪费.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();// 不指定则：处理器核心数 * 2

        //启动NIO服务的辅助启动类
        ServerBootstrap boot = new ServerBootstrap();
        boot.group(bossGroup, workerGroup)// 单线程模型：boot.group(bossGroup, bossGroup);
                .channel(NioServerSocketChannel.class)// 设置nio类型的channel
//                .localAddress(port)// 设置监听端口
                .childHandler(new ChannelInitializer<SocketChannel>() {//有连接到达时会创建一个channel
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // pipeline管理channel中的Handler，在channel队列中添加一个handler来处理业务
                        ch.pipeline().addLast(new ReadTimeoutHandler(5)).addLast("myHandler", new NettyServerHandler());
                    }
                });

        try {
            ChannelFuture future = boot.bind(port).sync();// 配置完成，开始绑定server，通过调用sync同步方法阻塞直到绑定成功
            System.out.println(NettyServer.class.getName() + " started and listen on " + future.channel().localAddress());

            future.channel().closeFuture().sync();// 应用程序会一直等待，直到channel关闭
        } finally {
            bossGroup.shutdownGracefully().sync();//关闭EventLoopGroup，释放掉所有资源包括创建的线程
            workerGroup.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new NettyServer(9999).run();
    }
}
