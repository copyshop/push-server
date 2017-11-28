package com.whf.client.init;

import com.whf.client.NettyClientBootstrap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NettyClientInitListener implements ServletContextListener {

    private static ExecutorService executorService;

    static {
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            InitNettyClientThread initNettyClientThread = new InitNettyClientThread();
            executorService.submit(initNettyClientThread);
        } catch (Exception e){

        }
    }



    class InitNettyClientThread implements Runnable{


        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            try {
                NettyClientBootstrap.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RuntimeException e){
                e.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        executorService.shutdownNow();
    }

}
