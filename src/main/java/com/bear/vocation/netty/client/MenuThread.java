package com.bear.vocation.netty.client;

import cn.hutool.crypto.digest.MD5;
import com.bear.vocation.netty.common.CommonReqWrapper;
import com.bear.vocation.netty.common.LoginReqWrapper;
import com.bear.vocation.netty.common.SendToUserReqWrapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.locks.LockSupport;

import static com.bear.vocation.netty.client.ResHandler.loginFail;

@Slf4j
public class MenuThread extends Thread{
    Scanner scanner = new Scanner(System.in);
    ChannelHandlerContext ctx;
    Channel channel;
    String username;
    boolean notQuit = true;
    public MenuThread(ChannelHandlerContext context,String threadName){
        super(threadName);
        this.ctx = context;
        this.channel = ctx.channel();
    }
    @Override
    public void run() {
        while (loginFail) {
            log.info("ACTIVE ACTIVE ACTIVE ACTIVE");
            log.info("输入用户名:");
            String username = scanner.nextLine();
            this.username = username;
            log.info("输入密码:");
            String originPassword = scanner.nextLine();
            String password = MD5.create().digestHex(originPassword, StandardCharsets.UTF_8);
            CommonReqWrapper.CommonReq req = CommonReqWrapper.CommonReq.newBuilder()
                    .setDataType(CommonReqWrapper.CommonReq.DataType.LoginReqType)
                    .setLoginReq(CommonReqWrapper.LoginReq.newBuilder()
                            .setUserName(username)
                            .setMd5Pwd(password)
                            .build()).build();
            // 会走ProtobufEncoder, 再走ProtobufVarint32LengthFieldPrepender,序列号和粘包/半包都处理了
            // TODO 加上公钥认证, 客户端(此处)用私钥加密一个约定的固定的字符串, 然后服务端用公钥解密,如果解密出的记过是约定的字符串,则认证成功
            ctx.channel().writeAndFlush(req);
            // 阻塞等待登录结果,如果登录失败,则继续输入用户名/密码登录
            log.info("beforeParkThread {}",Thread.currentThread().getName());
            LockSupport.park();
            log.info("afterParkThread {}, loginFail = {}",Thread.currentThread().getName(),loginFail);
        }
        // 登录成功后,跳出上面的循环, 展示业务菜单,并用scanner等待输入
        while (notQuit) {
            /*log.info("search [username*]");
            log.info("add [username]");*/
            log.info("send-username-msg");
            log.info("createGroup-groupName");
            log.info("addToGroup-groupName-username");
            log.info("sendGroup-groupName-msg");
            String originCmd = scanner.nextLine();
            System.out.println("originCmd=" + originCmd);
            String[] cmd = originCmd.split("-");
            switch (cmd[0]) {
                case "send":
                    sendToUser(cmd[1], cmd[2]);
                    break;
                case "createGroup":
                    createGroup(cmd[1]);
                    break;
                case "addToGroup":
                    addToGroup(cmd[1], cmd[2]);
                    break;
                case "sendGroup":
                    sendGroup(cmd[1], cmd[2]);
                    break;
                case "Q":
                    notQuit = false;
                    break;
                default:

                    break;
            }
        }
        
    }

    private void sendToUser(String to, String content){
        CommonReqWrapper.CommonReq req = CommonReqWrapper.CommonReq.newBuilder()
                .setDataType(CommonReqWrapper.CommonReq.DataType.SendToUserReqType)
                .setSendToUserReq(CommonReqWrapper.SendToUserReq.newBuilder()
                        .setTo(to)
                        .setFrom(username)
                        .setContent(content)
                        .build()).build();
        channel.writeAndFlush(req);
        log.info("sendToUserSuccess {}",req);
    }

    public void createGroup(String groupName) {
        CommonReqWrapper.CreateGroupReq req = CommonReqWrapper.CreateGroupReq.newBuilder()
                .setGroupName(groupName)
                .setUsername(username).build();

        channel.writeAndFlush(CommonReqWrapper.CommonReq.newBuilder()
                .setDataType(CommonReqWrapper.CommonReq.DataType.CreateGroupReqType)
                .setCreateGroupReq(req).build());
    }
    public void addToGroup(String groupName, String username){
        CommonReqWrapper.AddToGroupReq req = CommonReqWrapper.AddToGroupReq.newBuilder()
                .setGroupName(groupName)
                .setUsername(username)
                .build();
        channel.writeAndFlush(CommonReqWrapper.CommonReq.newBuilder()
                .setDataType(CommonReqWrapper.CommonReq.DataType.AddToGroupReqType)
                .setAddToGroup(req).build());
    }
    public void sendGroup(String groupName, String content){
        CommonReqWrapper.SendToGroupReq req = CommonReqWrapper.SendToGroupReq.newBuilder()
                .setContent(content)
                .setGroupName(groupName)
                .setUsername(username).build();
        channel.writeAndFlush(CommonReqWrapper.CommonReq.newBuilder()
                .setDataType(CommonReqWrapper.CommonReq.DataType.SendToGroupReqType)
                .setSendToGroup(req).build());
    }
}
