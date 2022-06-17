package com.bear.vocation.netty;

import com.bear.vocation.netty.common.CommonReqWrapper;
import com.bear.vocation.netty.server.BusinessHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 目标:聊天室,登录,给某人发消息,给群发消息创建群,加入群,退出群,退出系统
 * 编解码器:protobuf
 */
@Slf4j
public class NettyServer {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.group(boss, worker);
            // 因为是childHandler,所以泛型是NioSocketChannel
            bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel channel) throws Exception {
                    // 往pipeline增加handler
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new LoggingHandler());
                    // TODO 等基本功能完成后, 调整成带版本号等附加信息的
                    pipeline.addLast(new ProtobufVarint32FrameDecoder());
                    pipeline.addLast(new ProtobufDecoder(CommonReqWrapper.CommonReq.getDefaultInstance()));

                    pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                    pipeline.addLast(new ProtobufEncoder());
                    // 自定义handler要放到所有decoder之后,decoder把bytebuf转成java对象, 自定义handler的泛型为自定义对象,
                    // netty会自动匹配自定义handler,解码出来是哪个类的对象, 就找  泛型是该类的自定义handler处理,责任链模式
                    pipeline.addLast(new BusinessHandler());
                }
            });
            ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress("localhost", 1223));
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        log.debug("debug bind 1223 success");
                    }else{
                        log.debug("debug bind 1223 fail");
                    }
                }
            });
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
