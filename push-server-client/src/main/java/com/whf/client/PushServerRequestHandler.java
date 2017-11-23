package com.whf.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * push server 请求处理
 * @author wujianbo
 */
public class PushServerRequestHandler {

    private static Logger logger = LoggerFactory.getLogger(PushServerRequestHandler.class);

    public static String processRequest(String jsonStr) throws Exception {
        logger.info("请求数据：" + jsonStr);
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        String type = jsonObject.getString("type");
        return "未触发任何事件,请检查参数type!";
    }
}
