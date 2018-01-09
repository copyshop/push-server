package com.whf.client;

import com.whf.common.netty.util.AskMsg;
import com.whf.common.netty.util.AskParams;
import com.whf.common.netty.util.Constants;
import com.whf.common.netty.util.LoginMsg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author whfstudio@163.com
 * @date 2017/11/20
 */
public class NettyClientBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientBootstrap.class);

    private int port;
    private String host;
    private int retryDelay;
    private SocketChannel socketChannel;
    private Bootstrap bootstrap;
    private NioEventLoopGroup workGroup = new NioEventLoopGroup(4);

    static {
//        Config config = new Config(true);
//        PushServerConfig pushServerConfig = config.getPushServerConfig();
//        IamConfig iamConfig = config.getIamConfig();
//        ConnectorConfig connectorConfig = config.getConnectorConfig();
//        port = pushServerConfig.getPort();
//        host = pushServerConfig.getIp();
//        retryDelay = pushServerConfig.getRetryDelay();
//        connectorName = connectorConfig.getName();
//        publicKey = iamConfig.getPublicKey();
//
//        //使用 connector name 作为客户端唯一标识
//        Constants.setClientId(connectorConfig.getName());
    }

    public NettyClientBootstrap(int port, String host) throws InterruptedException {
        this.port = port;
        this.host = host;
        start();
    }

    public void start() throws InterruptedException {

        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .group(workGroup)
                .remoteAddress(host, port)
                .handler(new NettyClientInitializer());
        doConnect(port, host);
    }

    public static void main(String[] args) throws InterruptedException {

        /**
         * 设置客户端id.
         */
        Constants.setClientId("001");
        NettyClientBootstrap bootstrap = new NettyClientBootstrap(9999, "localhost");

        LoginMsg loginMsg = new LoginMsg();
        loginMsg.setPassword("123");
        loginMsg.setUserName("wuhf");
        bootstrap.socketChannel.writeAndFlush(loginMsg);
        while (true) {
            TimeUnit.SECONDS.sleep(3);
            AskMsg askMsg = new AskMsg();
            AskParams askParams = new AskParams();
            askParams.setAuth("authToken");
            askMsg.setParams(askParams);
            bootstrap.socketChannel.writeAndFlush(askMsg);
        }
    }

    /**
     * 建立连接，并且可以实现自动重连.
     * @param port port.
     * @param host host.
     * @throws InterruptedException InterruptedException.
     */
    protected void doConnect(int port, String host) throws InterruptedException {
        if (socketChannel != null && socketChannel.isActive()) {
            return;
        }

        final int portConnect = port;
        final String hostConnect = host;

        ChannelFuture future = bootstrap.connect(host, port);

        future.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture futureListener) throws Exception {
                if (futureListener.isSuccess()) {
                    socketChannel = (SocketChannel) futureListener.channel();
                    logger.info("Connect to server successfully!");
                } else {
                    logger.info("Failed to connect to server, try connect after 10s");

                    futureListener.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                doConnect(portConnect, hostConnect);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 10, TimeUnit.SECONDS);
                }
            }
        }).sync();
    }
}
