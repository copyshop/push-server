package com.whf.common.sync;

import com.whf.common.util.AskMsg;
import com.whf.common.util.BaseMsg;
import com.whf.common.util.ReplyMsg;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author whfstudio@163.com
 * @date 2017/11/27
 */
public class SyncWrite {
    protected Logger logger = LoggerFactory.getLogger(SyncWrite.class);

    public ReplyMsg writeAndSync(final Channel channel, final AskMsg askMsg, final long timeout) throws Exception {

        if (channel == null) {
            throw new NullPointerException("channel");
        }
        if (askMsg == null) {
            throw new NullPointerException("askMsg");
        }
        if (timeout <= 0) {
            throw new IllegalArgumentException("timeout <= 0");
        }

        String requestId = UUID.randomUUID().toString();
        askMsg.setRequestId(requestId);

        WriteFuture<BaseMsg> future = new SyncWriteFuture(askMsg.getRequestId());
        SyncWriteMap.syncKey.put(askMsg.getRequestId(), future);

        logger.info("发起请求，请求id：" + requestId + "，请求参数：" + askMsg.getData());

        ReplyMsg response = doWriteAndSync(channel, askMsg, timeout, future);

        SyncWriteMap.syncKey.remove(askMsg.getRequestId());
        return response;
    }

    private ReplyMsg doWriteAndSync(final Channel channel, final AskMsg request, final long timeout, final WriteFuture<BaseMsg> writeFuture) throws Exception {

        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                writeFuture.setWriteResult(future.isSuccess());
                writeFuture.setCause(future.cause());
                //失败移除
                if (!writeFuture.isWriteSuccess()) {
                    SyncWriteMap.syncKey.remove(writeFuture.requestId());
                }
            }
        });

        ReplyMsg response = (ReplyMsg)writeFuture.get(timeout, TimeUnit.MILLISECONDS);
        if (response == null) {
            if (writeFuture.isTimeout()) {
                throw new TimeoutException();
            } else {
                // write exception
                throw new Exception(writeFuture.cause());
            }
        }
        return response;
    }

}
