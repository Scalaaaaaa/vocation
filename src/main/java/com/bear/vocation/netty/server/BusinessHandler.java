package com.bear.vocation.netty.server;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import com.bear.vocation.netty.common.CommonReqWrapper;
import com.bear.vocation.netty.common.CommonResWrapper;
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
public class BusinessHandler extends SimpleChannelInboundHandler<CommonReqWrapper.CommonReq> {

    public static volatile Map<String, Channel> ONLINE_USERS = new ConcurrentHashMap<>();

    public static volatile Map<Channel, String> ONLINE_CHANNELS = new ConcurrentHashMap<>();

    private static CommonResWrapper.CommonRes LOGIN_SUCCESS = CommonResWrapper.CommonRes.newBuilder()
            .setDataType(CommonResWrapper.CommonRes.DataType.StatusType)
            .setStatus(CommonResWrapper.CommonRes.Status.newBuilder()
                    .setCode(0)
                    .setMsg("")
                    .setStatusType(CommonResWrapper.CommonRes.Status.ReqTp.Login)
                    .build())
            .build();
    private static CommonResWrapper.CommonRes SENDMSG_SUCCESS = CommonResWrapper.CommonRes.newBuilder()
            .setDataType(CommonResWrapper.CommonRes.DataType.StatusType)
            .setStatus(CommonResWrapper.CommonRes.Status.newBuilder()
                    .setCode(0)
                    .setMsg("")
                    .setStatusType(CommonResWrapper.CommonRes.Status.ReqTp.Login)
                    .build())
            .build();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx,CommonReqWrapper.CommonReq req) throws Exception {
        log.info("reveivedRequest {}", req);
        CommonResWrapper.CommonRes res = null;
        // TODO 公钥认证, 服务端用公钥解密, 如果结果是约定的字符串,则认证通过
        if (req.getDataType() == CommonReqWrapper.CommonReq.DataType.LoginReqType) {
            res = LOGIN_SUCCESS;
            // 登录成功, 保存channel和username的映射关系
            Channel channel = ctx.channel();
            ONLINE_USERS.put(req.getLoginReq().getUserName(), channel);
            ONLINE_CHANNELS.put(channel, req.getLoginReq().getUserName());
            log.info("logRes {}",res);
            ctx.writeAndFlush(res);
        } else if (req.getDataType() == CommonReqWrapper.CommonReq.DataType.SendToUserReqType) {
            log.info("receivedSendToUser {}",req.getSendToUserReq());
            Channel channel = BusinessHandler.ONLINE_USERS.get(req.getSendToUserReq().getTo());
            if (channel != null) {
                res = CommonResWrapper.CommonRes.newBuilder()
                        .setDataType(CommonResWrapper.CommonRes.DataType.RecvFromUserType)
                        .setRecvFromUser(CommonResWrapper.CommonRes.RecvFromUser.newBuilder()
                                .setFrom(req.getSendToUserReq().getFrom())
                                .setContent(req.getSendToUserReq().getContent()).build())
                        .build();
                // 发给对方
                channel.writeAndFlush(res);
                // 回复给自己,表示发送成功
                ctx.writeAndFlush(SENDMSG_SUCCESS);
                log.info("writeSendToUser {}", res);
            }
        }
    }
}
