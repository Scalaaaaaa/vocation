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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.bear.vocation.netty.common.Constants.*;

// 当某handler是处理业务消息时, 继承SimpleChannelInboundHandler
// 当某handler是处理连接/断开事件时, 继承ChannelInboundHandlerAdapter
@ChannelHandler.Sharable
@Slf4j
public class BusinessHandler extends SimpleChannelInboundHandler<CommonReqWrapper.CommonReq> {

    public static volatile Map<String, Channel> ONLINE_USERS = new ConcurrentHashMap<>();

    public static volatile Map<Channel, String> ONLINE_CHANNELS = new ConcurrentHashMap<>();

    public static volatile Map<String, Set<String>> GROUP_USERS = new ConcurrentHashMap<>();

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
        } else if (req.getDataType() == CommonReqWrapper.CommonReq.DataType.CreateGroupReqType) {
            CommonReqWrapper.CreateGroupReq groupInfo = req.getCreateGroupReq();
            if (GROUP_USERS.get(groupInfo.getGroupName()) == null) {
                Set<String> users = new HashSet<>();
                users.add(groupInfo.getUsername());
                GROUP_USERS.put(groupInfo.getGroupName(), users);
                ctx.writeAndFlush(CREATE_GROUP_SUCCESS);
            }else{
                ctx.writeAndFlush(GROUP_EXISTS);
            }
        } else if (req.getDataType() == CommonReqWrapper.CommonReq.DataType.AddToGroupReqType) {
            CommonReqWrapper.AddToGroupReq addToGroup = req.getAddToGroup();
            if (GROUP_USERS.get(addToGroup.getGroupName()) == null) {
                ctx.writeAndFlush(GROUP_NOT_EXISTS);
            }else{
                GROUP_USERS.get(addToGroup.getGroupName()).add(addToGroup.getUsername());
                ctx.writeAndFlush(ADD_TO_GROUP_SUCCESS);
            }
        }else if (req.getDataType() == CommonReqWrapper.CommonReq.DataType.SendToGroupReqType) {
            CommonReqWrapper.SendToGroupReq sendToGroup = req.getSendToGroup();
            if (GROUP_USERS.get(sendToGroup.getGroupName()) == null) {
                ctx.writeAndFlush(GROUP_NOT_EXISTS);
            }else{
                GROUP_USERS.get(sendToGroup.getGroupName()).forEach(username ->{
                    Channel channel = ONLINE_USERS.get(username);
                    if(channel != null){
                        CommonResWrapper.CommonRes sendRes = CommonResWrapper.CommonRes.newBuilder()
                                .setDataType(CommonResWrapper.CommonRes.DataType.RecvFromGroupType)
                                .setRecvFromGroup(CommonResWrapper.CommonRes.RecvFromGroup.newBuilder()
                                        .setFromUsername(sendToGroup.getUsername())
                                        .setContent(sendToGroup.getContent())
                                        .setGroupName(sendToGroup.getGroupName()).build())
                                .build();
                        channel.writeAndFlush(sendRes);
                        ctx.writeAndFlush(SENDMSG_SUCCESS);
                    }
                });
            }
        }
    }
}
