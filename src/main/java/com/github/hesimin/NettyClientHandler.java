package com.github.hesimin;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * 最主要的区别就是SimpleChannelInboundHandler在接收到数据后会自动release掉数据占用的Bytebuffer资源(自动调用Bytebuffer.release())。
 * 而为何服务器端不能用呢，因为我们想让服务器把客户端请求的数据发送回去，而服务器端有可能在channelRead方法返回前还没有写完数据，因此不能让它自动release。
 * @author hesimin 2017-07-04
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * 此方法会在连接到服务器后被调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client send...");
        final ChannelFuture f = ctx.writeAndFlush(Unpooled.copiedBuffer("client time = "+System.currentTimeMillis(), CharsetUtil.UTF_8));
    }

    /**
     * 此方法会在接收到服务器数据后调用
     * <p>此方法接收到的可能是一些数据片段，比如服务器发送了5个字节数据，Client端不能保证一次全部收到，比如第一次收到3个字节，第二次收到2个字节。我们可能还会关心收到这些片段的顺序是否可发送顺序一致，这要看具体是什么协议，比如基于TCP协议的字节流是能保证顺序的。</p>
     **/
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
//        System.out.println("Client received: " + ByteBufUtil.hexDump(msg.readBytes(msg.readableBytes())));
        byte[] req = new byte[msg.readableBytes()];
        msg.readBytes(req);
        String body = new String(req, "UTF-8");
        System.out.println("client received data :" + body);
    }

    /**
     * 捕捉到异常
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
