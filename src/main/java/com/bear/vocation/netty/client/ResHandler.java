package com.bear.vocation.netty.client;

import com.bear.vocation.netty.common.CommonResWrapper;
import com.bear.vocation.netty.common.LoginResWrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.LockSupport;

@Slf4j
public class ResHandler extends SimpleChannelInboundHandler<CommonResWrapper.CommonRes> {
    int success = 0;
    // 提取成变量是为了 登录成功的线程调用LockSupport.unpark(loginThread)
    public static volatile Thread menuThread;
    public static volatile Boolean loginFail = true;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 不用匿名类的原因是, 要把每个命令的执行放到一个单独的方法里,这样switch更易读
        menuThread = new MenuThread(ctx,"menuThread");
        menuThread.start();
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, CommonResWrapper.CommonRes commonRes) throws Exception {
        log.info("recvFromServer {}", commonRes);
        if(commonRes.getDataType() == CommonResWrapper.CommonRes.DataType.RecvFromUserType){
            CommonResWrapper.CommonRes.RecvFromUser recvFromUser = commonRes.getRecvFromUser();
            log.info("recvMsgFrom: {}, content: {}", recvFromUser.getFrom(),
                    recvFromUser.getContent());
        } else if (commonRes.getDataType() == CommonResWrapper.CommonRes.DataType.StatusType) {
            if(commonRes.getStatus().getStatusType() == CommonResWrapper.CommonRes.Status.ReqTp.Login){
                if (commonRes.getStatus().getCode() == success) {
                    log.info("登录成功 clientRecvLoginRes {}",commonRes.getStatus());
                    loginFail = false;
                    log.info("beforeUnparkThread {}",menuThread.getName());
                    LockSupport.unpark(menuThread);
                    log.info("afterUnparkThread {}",menuThread.getName());
                }
            }else if(commonRes.getStatus().getStatusType() == CommonResWrapper.CommonRes.Status.ReqTp.SendMsg){
                log.info("发送消息成功....");
            }else if(commonRes.getStatus().getStatusType() == CommonResWrapper.CommonRes.Status.ReqTp.CreateGroup){
                log.info("创建群成功....");
            }else if(commonRes.getStatus().getStatusType() == CommonResWrapper.CommonRes.Status.ReqTp.AddToGroup){
                log.info("加入群成功....");
            }
            // 其他仅有状态的出参解析  比如给别人发信息时,得到的回复应该是发送成功
        } else if (commonRes.getDataType() == CommonResWrapper.CommonRes.DataType.RecvFromGroupType) {
            CommonResWrapper.CommonRes.RecvFromGroup data = commonRes.getRecvFromGroup();
            log.info("[{}] 群里的  {} 给你发信息:{}",data.getGroupName(),
                    data.getFromUsername(),data.getContent());
        }
    }
}
