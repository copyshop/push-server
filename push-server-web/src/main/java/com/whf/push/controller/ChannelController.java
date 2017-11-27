package com.whf.push.controller;

import com.alibaba.fastjson.JSON;
import com.whf.common.sync.SyncWrite;
import com.whf.common.util.*;
import com.whf.push.netty.server.NettyChannelMap;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author whfstudio@163.com
 * @date 2017/11/20
 */
public class ChannelController {

    protected Logger logger = LoggerFactory.getLogger(ChannelController.class);

    public ResultCode<String> sendConnector(HttpServletRequest request, HttpServletResponse response, String connectorName, String jsonStrData){
        ResultCode<String> resultCode = new ResultCode<>();
        try {
            SocketChannel channel=(SocketChannel) NettyChannelMap.get(connectorName);
            if (channel == null) {
                resultCode.setCode(Messages.INPUT_ERROR_CODE);
                resultCode.setMsg(Messages.INPUT_ERROR_MSG);
            }

            /**
             * 构建发送参数.
             */
            AskMsg askMsg = new AskMsg();
            String replace = jsonStrData.replace("&quot;", "\"");
            askMsg.setData(replace);
            askMsg.setType(MsgType.REQUEST);
            askMsg.setClientId(connectorName);
            SyncWrite s = new SyncWrite();

            ReplyMsg replyMsg = s.writeAndSync(channel, askMsg, 5000);
            resultCode.setData(replyMsg.getData());
            logger.info("调用结果：" + JSON.toJSONString(replyMsg));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultCode;
    }
}
