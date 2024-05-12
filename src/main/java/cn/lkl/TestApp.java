package cn.lkl;

import cn.lkl.demos.thread.TestPool;
import cn.lkl.util.BaseLockActionProxy;
import cn.lkl.util.LockActionProxy;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

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
                        ReentrantLock lock = new ReentrantLock();
                        LockActionProxy proxy = new BaseLockActionProxy();
                        proxy.doLock(lock);
                        System.out.println("============call....");
                        proxy.doUnlock(lock);
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
