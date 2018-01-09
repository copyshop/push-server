package com.whf.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 通道初始化.
 *
 * @author whfstudio@163.com
 * @date 2018/01/09
 */
public class NettyClientInitializer extends ChannelInitializer {

    @Override
    protected void initChannel(Channel channel) throws Exception {
        //IdleStateHandler检测心跳.
        ChannelPipeline p = channel.pipeline();
        p.addLast(new IdleStateHandler(20, 10, 0));
        p.addLast(new ObjectEncoder());
        p.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
        p.addLast(new NettyClientHandler());
    }
}
