package com.bear.vocation.netty.client;

import cn.hutool.crypto.digest.MD5;
import com.bear.vocation.netty.common.LoginReqWrapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
@ChannelHandler.Sharable
public class ConnectSuccessHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("输入用户名:");
        String username = scanner.nextLine();
        System.out.println("输入密码:");
        String originPassword = scanner.nextLine();
        String password = MD5.create().digestHex(originPassword, StandardCharsets.UTF_8);
        LoginReqWrapper.LoginReq loginReq = LoginReqWrapper.LoginReq.newBuilder().setMd5Pwd(password)
                .setUserName(username).build();
        // 会走ProtobufEncoder, 再走ProtobufVarint32LengthFieldPrepender,序列号和粘包/半包都处理了
        ctx.channel().writeAndFlush(loginReq);
    }
}
