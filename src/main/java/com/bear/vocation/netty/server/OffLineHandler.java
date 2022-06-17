package com.bear.vocation.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
@Slf4j
@ChannelHandler.Sharable
public class OffLineHandler extends ChannelInboundHandlerAdapter {
    @Override
    // 正常调用api断开连接后, 从在线用户里删除
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        offLineFast(ctx);
        super.channelInactive(ctx);
    }
    // 异常断开(直接关掉程序)
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        offLineFast(ctx);
        super.exceptionCaught(ctx, cause);
    }
    // 效率太低, 要循环所有, 改成用一个新的map,key=channel,value=username
    private void offLine(ChannelHandlerContext ctx){
        Channel channel = ctx.channel();
        Optional<Map.Entry<String, Channel>> first = BusinessHandler.ONLINE_USERS.entrySet().stream().filter(item -> item.getValue() == channel)
                .findFirst();
        if (first.isPresent()) {
            BusinessHandler.ONLINE_USERS.remove(first.get().getKey());
        }
    }
    private void offLineFast(ChannelHandlerContext ctx){
        log.info("someoneLogOff");
        BusinessHandler.ONLINE_USERS.remove(BusinessHandler.ONLINE_CHANNELS.remove(ctx.channel()));
    }
}
