package com.test;


import cn.lkl.demos.jni.TestNative;
import cn.lkl.demos.lock.LockTest;
import cn.lkl.demos.naming.EnvAndNamingTest;
import cn.lkl.demos.thread.ThreadTest;
import cn.lkl.demos.uri.URIDemo;
import cn.lkl.util.BaseLockActionProxy;
import cn.lkl.util.CheckDeadLockThread;
import org.junit.Test;

import javax.naming.*;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * demo 测试
 */
public class Application {

    public static void main(String[] args) throws Exception {
        new Application().threadTest();
    }

    /**
     * jni 测试类
     */
    @Test
    public void testJni() {
        for (int i = 0; i < 3; i++) {
            //c里面是每次累加，变量不在对象里面的
            System.out.println(new TestNative().helloWord());
        }
    }

    /**
     * 本地资源、网络资源读写 测试
     */
    @Test
    public void testURI() throws Exception {
        URIDemo.localFileRead();
//        URIDemo.jdbcTest();
//        URIDemo.socketBIOTest();
//        URIDemo.socketNIOTest();
    }

    /**
     * 锁测试 - 独占、读写、重入
     */
    @Test
    public void lockTest() {
//        LockTest.testReentrantLock();
        LockTest.readwriteLock();
    }

    /**
     * 线程测试
     */
    @Test
    public void threadTest() throws NamingException {
//        ThreadTest.testStopLock();
        ThreadTest.testBlocker();
//        ThreadTest.testStopBlocker();
//        ThreadTest.testPool();
//        Hashtable<String, String> deadLockEnv = new Hashtable<>();
//        deadLockEnv.put(Context.INITIAL_CONTEXT_FACTORY, "cn.lkl.util.DeadLockFactory");
//        deadLockEnv.put(Context.PROVIDER_URL,"check:asse/env");
//        Context c = new InitialContext();
//        c.bind("dead");
//        try {
//            InitialContext ct = new InitialContext();
//            NamingEnumeration<NameClassPair> list = ct.list("check:asse");
//            while (list.hasMoreElements()) {
//                NameClassPair next = list.next();
//                System.out.println(next.getName() + "============" + next.getClassName());
//            }
//        } catch (NamingException e) {
//            e.printStackTrace();
//        }
    }

    @Test
    public void testContext() throws Exception {
        EnvAndNamingTest.contextTest();
    }

    @Test
    /**
     * 测试单链死锁
     */
    public void testDeadLock() throws Exception {
        Thread checkDead = new CheckDeadLockThread(Thread.currentThread().getThreadGroup(), "my check");
        checkDead.start();
        BaseLockActionProxy proxyLockAction = new BaseLockActionProxy();
        int count = 7;
        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock tmp = lock1;
        Thread[] ts = new Thread[count];
        for (int i = 0; i < count; i++) {

            ReentrantLock lock2 = (i == (count - 1)) ? tmp : new ReentrantLock();
            ts[i] = new TestDeadThread(lock1, lock2, "my thread---" + i);
            ts[i].start();
//            System.out.println(i+"====lock "+lock1);
//            System.out.println(i+"====need "+lock2);
            lock1 = lock2;
        }
        //状态检测
        while(true) {
            boolean allTerminated = true;
            for (Thread t : ts) {
                System.out.println(t.getName() + "=========" + t.getState());
                if(t.getState() != Thread.State.TERMINATED){
                    allTerminated = false;
                }
            }
            if(allTerminated){
                System.out.println("====== all terminated....");
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }



    @Test
    public void testPool() {
        BlockingQueue queue = new ArrayBlockingQueue(10);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 10, 4, TimeUnit.SECONDS, queue);
        for (int i = 0; i < 10; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("===============do....");
                }
            });
        }
        executor.shutdown();
        while(!executor.isShutdown()){

        }
        System.out.println("queue size:"+queue.size());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("===========test....");
            }
        });
    }
}
