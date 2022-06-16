package com.bear.vocation.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Map;
import java.util.Optional;

@ChannelHandler.Sharable
public class OffLineHandler extends ChannelInboundHandlerAdapter {
    @Override
    // 正常调用api断开连接后, 从在线用户里删除
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        offLine(ctx);
        super.channelInactive(ctx);
    }
    // 异常断开(直接关掉程序)
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        offLine(ctx);
        super.exceptionCaught(ctx, cause);
    }
    private void offLine(ChannelHandlerContext ctx){
        Channel channel = ctx.channel();
        Optional<Map.Entry<String, Channel>> first = LoginReqHandler.ONLINE_USERS.entrySet().stream().filter(item -> item.getValue() == channel)
                .findFirst();
        if (first.isPresent()) {
            LoginReqHandler.ONLINE_USERS.remove(first.get().getKey());
        }
    }
}
