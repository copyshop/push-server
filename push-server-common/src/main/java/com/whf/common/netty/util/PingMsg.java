package com.whf.common.netty.util;

/**
 * 心跳检测的消息类型.
 * @author whfstudio@163.com
 * @date 2017/11/20
 */
public class PingMsg extends BaseMsg {
    public PingMsg() {
        super();
        setType(MsgType.PING);
    }
}
