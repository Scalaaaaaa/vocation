package com.bear.vocation.netty;

import com.bear.vocation.netty.client.ResHandler;
import com.bear.vocation.netty.common.CommonResWrapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;
public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.group(group);
        // 聊天程序, 及时发送,短消息不合并
        //bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                /** TODO
                 * @Description:把客户端的所有的handler配置上
                 * 主要是处理服务器发来的数据
                 * @Author: yangyy
                 * @Date: 2022/6/14 下午6:00
                 **/
                // protobuf Codec
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new LoggingHandler());
                pipeline.addLast(new ProtobufVarint32FrameDecoder());
                pipeline.addLast(new ProtobufDecoder(CommonResWrapper.CommonRes.getDefaultInstance()));
                pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                pipeline.addLast(new ProtobufEncoder());
                // protobuf下的业务handler只有一个,因为处理参数的时候,不支持 handler->出参 的1:1关系,只支持1:n关系
                pipeline.addLast(new ResHandler());
            }
        });
        // TODO hostname/port都放到common里共享
        ChannelFuture cf = bootstrap.connect(new InetSocketAddress("localhost", 1223)).sync();
        cf.channel().closeFuture().sync();
        //LockSupport.park();
        /*cf.addListener(new ChannelFutureListener() {
            // 连接服务端有结果后(连接成功)触发的异步方法
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    // 成功后,启动新线程,简易菜单功能
                    loginThread = new Thread(() -> {
                        // 只要登录失败,就继续登录 TODO 优化成可以选择退出,输入q退出
                        while (loginFail) {
                            Scanner scanner = new Scanner(System.in);
                            System.out.println("输入用户名:");
                            String username = scanner.nextLine();
                            System.out.println("输入密码:");
                            String originPassword = scanner.nextLine();
                            String password = MD5.create().digestHex(originPassword, StandardCharsets.UTF_8);
                            LoginReqWrapper.LoginReq loginReq = LoginReqWrapper.LoginReq.newBuilder().setMd5Pwd(password)
                                    .setUserName(username).build();
                            // 会走ProtobufEncoder, 再走ProtobufVarint32LengthFieldPrepender,序列号和粘包/半包都处理了
                            channelFuture.channel().writeAndFlush(loginReq);
                            // 发送完了之后, 接受登录结果的是handler, 所以当前线程阻塞, 登录成功之后,设置loginSuccess,然后唤醒这里的代码
                            System.out.println("beforePark");
                            LockSupport.park();
                            System.out.println("afterPark");
                        }
                    });
                    loginThread.start();
                }
            }
        });*/
    }
}
