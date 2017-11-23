package com.whf.server;

import com.whf.common.sync.SyncWriteFuture;
import com.whf.common.sync.SyncWriteMap;
import com.whf.common.util.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author whfstudio@163.com
 * @date 2017/11/20
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<BaseMsg> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyChannelMap.remove((SocketChannel) ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {
        if (MsgType.LOGIN.equals(baseMsg.getType())) {
            LoginMsg loginMsg = (LoginMsg) baseMsg;
            if ("wuhf".equals(loginMsg.getUserName()) && "123".equals(loginMsg.getPassword())) {
                /**
                 * 登录成功,把channel存到服务端的map中.
                 */
                NettyChannelMap.add(loginMsg.getClientId(), (SocketChannel) channelHandlerContext.channel());
                logger.info("util" + loginMsg.getClientId() + " 登录成功");
            }
        } else {
            if (NettyChannelMap.get(baseMsg.getClientId()) == null) {
                /**
                 * 说明未登录，或者连接断了，服务器向客户端发起登录请求，让客户端重新登录.
                 */
                LoginMsg loginMsg = new LoginMsg();
                channelHandlerContext.channel().writeAndFlush(loginMsg);
            }
        }
        switch (baseMsg.getType()) {

            case REQUEST: {
                String requestId = baseMsg.getRequestId();
                SyncWriteFuture future = (SyncWriteFuture) SyncWriteMap.syncKey.get(requestId);
                if (future != null) {
                    future.setResponse(baseMsg);
                }
            }
            break;

            case PING: {
                PingMsg pingMsg = (PingMsg) baseMsg;
                PingMsg replyPing = new PingMsg();
                NettyChannelMap.get(pingMsg.getClientId()).writeAndFlush(replyPing);
            }
            break;
            case ASK: {
                //收到客户端的请求
                AskMsg askMsg = (AskMsg) baseMsg;
                if ("authToken".equals(askMsg.getParams().getAuth())) {
                    ReplyServerBody replyBody = new ReplyServerBody("server info $$$$ !!!");
                    ReplyMsg replyMsg = new ReplyMsg();
                    replyMsg.setBody(replyBody);
                    NettyChannelMap.get(askMsg.getClientId()).writeAndFlush(replyMsg);
                }
            }
            break;
            case REPLY: {
                //收到客户端回复
                ReplyMsg replyMsg = (ReplyMsg) baseMsg;
                ReplyClientBody clientBody = (ReplyClientBody) replyMsg.getBody();
                logger.info("receive util msg: " + clientBody.getClientInfo());
            }
            break;
            case LOGIN:
                break;
            default:
                break;
        }
        ReferenceCountUtil.release(baseMsg);
    }
}
