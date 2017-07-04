package com.github.hesimin;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 具体业务类
 *
 * @author hesimin 2017-07-04
 */
@ChannelHandler.Sharable//注解@Sharable可以让它在channels间共享
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("server channelRead..");
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        System.out.println("server received data :" + body);

        ctx.write(Unpooled.copiedBuffer(("server time = " + System.currentTimeMillis()).getBytes()));//写回数据
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER) //flush掉所有写回的数据
                .addListener(ChannelFutureListener.CLOSE); //当flush完成后关闭channel
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();//捕捉异常信息
        ctx.close();//出现异常时关闭channel
    }
}
