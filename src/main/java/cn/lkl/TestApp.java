package cn.lkl;

import cn.lkl.demos.thread.TestPool;
import cn.lkl.util.BaseLockActionProxy;
import cn.lkl.util.LockActionProxy;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TestApp {
    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            Thread owner = new Thread(new Runnable() {
                @Override
                public void run() {
                    TestPool t = TestPool.getPOOL(1,3,1,TimeUnit.SECONDS);
                    System.out.println("============owner");
                    t.start();
                    Future submit = t.submit(new Callable() {

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
                    System.out.println("====="+(submit == null));
                    try {
                        TimeUnit.SECONDS.sleep(9);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    t.destroy();
                }
            },"owner thread");
            owner.start();
            System.out.println("*******************************"+i);
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
