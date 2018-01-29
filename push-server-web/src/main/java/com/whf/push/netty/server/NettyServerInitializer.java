package com.whf.push.netty.server;

import com.whf.common.netty.codec.MessageDecoder;
import com.whf.common.netty.codec.MessageEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

/**
 * 通道初始化.
 *
 * @author wuhf
 * @date 2018/01/29
 */
public class NettyServerInitializer extends ChannelInitializer{
    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline p = channel.pipeline();
        p.addLast(new MessageEncoder());
        p.addLast(new MessageDecoder());
        p.addLast(new NettyServerHandler());
    }
}
