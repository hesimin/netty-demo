package com.github.hesimin.keep;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetAddress;

/**
 * @author hesimin 2017-07-04
 */
public class SecureServerHandler extends SimpleChannelInboundHandler<String> {

    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive...");

        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(future -> {
            ctx.writeAndFlush(
                    "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure service!\n");
            ctx.writeAndFlush(
                    "Your session is protected by " +
                            ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() +
                            " cipher suite.\n");
            channels.add(ctx.channel());
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        for (Channel c : channels) {
            if (c != ctx.channel()) {
                c.writeAndFlush("[" + ctx.channel().remoteAddress() + "]: " + msg + '\n');
            } else {
                c.writeAndFlush("[you]: " + msg + '\n');
            }
            System.out.println("Server recieve:" + msg);
        }

        // Close the connection.
        if ("close".equalsIgnoreCase(msg)) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelReadComplete...");
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelUnregistered...");
        for (Channel c : channels) {
            if (c != ctx.channel()) {
                c.writeAndFlush(" *** " + ctx.channel().remoteAddress() + " ==> exit" + '\n');
            }
        }
        super.channelUnregistered(ctx);
    }
}
