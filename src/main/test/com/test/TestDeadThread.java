package com.test;

import cn.lkl.util.BaseLockActionProxy;

import java.util.concurrent.locks.ReentrantLock;

public class TestDeadThread extends Thread{
    private BaseLockActionProxy proxy = new BaseLockActionProxy();
    private ReentrantLock lock1,lock2;
    public TestDeadThread(ReentrantLock lock1, ReentrantLock lock2, String name){
        super(name);
        this.lock1 = lock1;
        this.lock2 = lock2;
    }

    @Override
    public void run() {
        //WAITING
        proxy.doLock(lock1);
        System.out.println(Thread.currentThread().getName()+"=============locked1 "+lock1);
        //TIMED_WAITING
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+"=============sleep end need:"+lock2);
        proxy.doLock(lock2);
        System.out.println(Thread.currentThread().getName()+"=============locked2");


        System.out.println(Thread.currentThread().getName()+"=============to release 2");
        proxy.doUnlock(lock2);
        System.out.println(Thread.currentThread().getName()+"=============released 2 to release 1");
        proxy.doUnlock(lock1);
        System.out.println(Thread.currentThread().getName()+"=============released 1  end");


        //synchronized是BLOCKED  我没做blocked的检测，
//                    System.out.println(Thread.currentThread().getName()+"=============begin");
//                    synchronized (fl1){
//                        System.out.println(Thread.currentThread().getName()+"=============locked1");
//                        try {
//                            Thread.sleep(10000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        System.out.println(Thread.currentThread().getName()+"=============sleep end");
//                        synchronized (fl2){
//                            System.out.println(Thread.currentThread().getName()+"=============locked2");
//                        }
//                        System.out.println(Thread.currentThread().getName()+"================================end lock2");
//                    }
//                    System.out.println(Thread.currentThread().getName()+"================================end lock1");
    }
}
