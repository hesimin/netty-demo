package com.github.hesimin.keep;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author hesimin 2017-07-04
 */
public class SecureClient {
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int    PORT = Integer.parseInt(System.getProperty("port", "9999"));

    public static void main(String[] args) throws IOException, InterruptedException {
        final SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap boot = new Bootstrap();

        try {
            boot.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new SecureClientInitializer(sslContext));

            Channel channel = boot.connect(HOST, PORT).sync().channel();

            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = in.readLine();
                if (line == null||"".equals(line)) {
                    break;
                }
                lastWriteFuture = channel.writeAndFlush(line+ "\r\n");

                if ("close".equalsIgnoreCase(line)) {
                    channel.closeFuture().sync();
                    break;
                }

                if (lastWriteFuture != null) {
                    lastWriteFuture.sync();
                }
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
