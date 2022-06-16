package com.bear.vocation.netty.client;

import com.bear.vocation.netty.NettyClient;
import com.bear.vocation.netty.common.LoginResWrapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.LockSupport;

@Slf4j
@ChannelHandler.Sharable
public class LoginResHandler extends SimpleChannelInboundHandler<LoginResWrapper.LoginRes> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LoginResWrapper.LoginRes loginRes) throws Exception {
        log.info("clientRecvLoginRes");
        if (loginRes.getIsSuccess()) {
            NettyClient.loginFail = false;
            LockSupport.unpark(NettyClient.loginThread);
            System.out.println("search [username*]");
            System.out.println("add [username]");
            System.out.println("send [username] [msg]");
        }
        log.info("loginRes :{}" ,loginRes);
    }
}
