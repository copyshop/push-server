package com.whf.common.sync;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求future Map
 * @author wujianbo
 */
public class SyncWriteMap {

    public static Map<String, WriteFuture> syncKey = new ConcurrentHashMap<String, WriteFuture>();

}
