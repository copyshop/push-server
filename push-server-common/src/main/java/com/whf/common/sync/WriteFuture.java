package com.whf.common.sync;


import com.whf.common.util.BaseMsg;

import java.util.concurrent.Future;

/**
 * @author whfstudio@163.com
 * @date 2017/11/27
 */
public interface WriteFuture<T> extends Future<T> {

    Throwable cause();

    void setCause(Throwable cause);

    boolean isWriteSuccess();

    void setWriteResult(boolean result);

    String requestId();

    T response();

    void setResponse(BaseMsg response);

    boolean isTimeout();


}
