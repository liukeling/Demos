package cn.lkl;

import cn.lkl.demos.thread.TestPool;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class TestApp {
    public static void main(String[] args) {
        Thread owner = new Thread(new Runnable() {
            @Override
            public void run() {
                TestPool t = TestPool.getPOOL();
                System.out.println("============owner");
                t.submit(new Callable(){

                    @Override
                    public Object call() throws Exception {
                        System.out.println("============call....");
                        return null;
                    }
                });
                try {
                    TimeUnit.SECONDS.sleep(9);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"owner thread");
        owner.start();

        System.out.println("============");
    }
}
