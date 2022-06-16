package com.bear.vocation.netty.server;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import com.bear.vocation.netty.common.LoginReqWrapper;
import com.bear.vocation.netty.common.LoginResWrapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
// 当某handler是处理业务消息时, 继承SimpleChannelInboundHandler
// 当某handler是处理连接/断开事件时, 继承ChannelInboundHandlerAdapter
@ChannelHandler.Sharable
@Slf4j
public class LoginReqHandler extends SimpleChannelInboundHandler<LoginReqWrapper.LoginReq> {

    public static volatile Map<String, Channel> ONLINE_USERS = new ConcurrentHashMap<>();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginReqWrapper.LoginReq loginReq) throws Exception {
        log.info("loginInfo {}", loginReq);
        LoginResWrapper.LoginRes res = LoginResWrapper.LoginRes.newBuilder().setIsSuccess(true)
                .setMsg("success").build();
        // 登录成功, 保存channel和username的映射关系
        Channel channel = ctx.channel();
        ONLINE_USERS.put(loginReq.getUserName(), channel);
        log.info("logRes {}",res);
        ctx.writeAndFlush(res);
    }
}
