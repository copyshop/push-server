package com.whf.push.netty.server;

import com.whf.common.netty.util.AskMsg;
import com.whf.common.netty.util.NettyChannelMap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author whfstudio@163.com
 * @date 2017/11/20
 */
public class NettyServerBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerBootstrap.class);

    private int port;
    private SocketChannel socketChannel;

    public NettyServerBootstrap(int port) throws InterruptedException {
        this.port = port;
        bind();
    }

    private void bind() throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new NettyServerInitializer());

        ChannelFuture f = bootstrap.bind(port).sync();
        if (f.isSuccess()) {
            logger.info("server start---------------");
        }
    }

    private static final ThreadLocal<Long> TEST = new ThreadLocal<Long>(){
        @Override
        protected Long initialValue() {
            return System.currentTimeMillis();
        }
    };
    public static void main(String[] args) throws InterruptedException {
        TEST.set(System.currentTimeMillis());
        NettyServerBootstrap bootstrap = new NettyServerBootstrap(9999);
        while (true) {
            SocketChannel channel = (SocketChannel) NettyChannelMap.get("001");
            if (channel != null) {
                AskMsg askMsg = new AskMsg();
                channel.writeAndFlush(askMsg);
            }
            TimeUnit.SECONDS.sleep(10);
        }
    }
}
