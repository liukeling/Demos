package cn.lkl.demos.thread;

import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程、线程组、线程池
 * 线程 - 主线程、守护线程  是那种类型是线程加入的组决定的，默认是当前线程所在组，new的时候初始，后面可以修改
 * 中断：Interrupt ,别的线程可以调用中断方法，给线程一个信号，线程通过isInterrupted 判断是否被中断了。
 * sleep等方法会抛出中断异常，锁等待不会
 * 停止：stop 如果线程拿到锁了，被停止了，不会释放资源 - 可以拿到block
 */
public class ThreadTest {
    /**
     * 中断、停止测试 - 中断可以继续跑  停止就不会跑了
     */
    public static void testInterruptAndStop() {

        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
        Thread t = new Thread(() -> {
            reentrantLock.lock();
            System.out.println("================in...." + Thread.currentThread().isInterrupted());
            reentrantLock.unlock();
        });
        t.start();
//        t.interrupt();
        t.stop();
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        reentrantLock.unlock();
    }

    /**
     * 拿到锁了，被停止了，会释放锁吗  不会
     */
    public static void testStopLock() {
        ReentrantLock lock = new ReentrantLock();
//        Object lock = new Object();
        Thread t = new Thread(() -> {
            lock.lock();
//            synchronized (lock){
            System.out.println("==========in sleep");
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("===========to unlock");
//        }
            lock.unlock();
        });
        t.start();

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.stop();
        lock.lock();
//        synchronized (lock) {
        System.out.println("===============进不来，锁没释放");
//        }
        lock.unlock();
    }

    /**
     * sleep 和 synchronized锁等待 没有blocker
     * 只有 基于lockSupper 的才有blocker
     */
    public static void testBlocker() {
        ReentrantLock lock1 = new ReentrantLock();
//        ReentrantLock lock2 = new ReentrantLock();
//        lock2.lock();
        Object lock2 = new Object();
        Thread t = new Thread(() -> {
            lock1.lock();
            System.out.println("========to wait or sleep==");
//            try {
//                TimeUnit.SECONDS.sleep(5);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            lock2.lock();
//            lock2.unlock();
            synchronized (lock2) {
                try {
                    System.out.println("=========wait");
                    lock2.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("=========to unlock");
            lock1.unlock();
        });
        t.start();

        System.out.println(t.getState()+"   "+LockSupport.getBlocker(t));
        synchronized (lock2) {
            System.out.println(t.getState()+"   "+LockSupport.getBlocker(t));
            lock2.notify();
        }
        System.out.println(t.getState()+"   "+LockSupport.getBlocker(t));
//        lock2.unlock();

    }

    /**
     * 线程停止了会有blocker吗 会
     */
    public static void testStopBlocker() {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        Thread t = new Thread(() -> {
            lock.lock();

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.unlock();
        });
        t.start();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(LockSupport.getBlocker(t));
        t.stop();
        System.out.println(LockSupport.getBlocker(t));
        lock.unlock();
    }

    /**
     * 线程池 先核心线程跑满,然后塞队列,队列满了开临时线程 到了最大线程数了启动拒绝策略
     * execute : 执行任务 每返回
     * submit : 有feature返回,通过get获取结果 - 等待子线程执行完
     * corepoolsize 核心线程数
     * maximumpoolsize 最大线程数量
     * keepAliveTime  非核心线程再等待任务时候最多等多久
     * weekqueue 任务队列
     * threadFactory 线程工厂
     * RejectedExecutionHandler 拒绝策略 有四个可以选,默认是抛出异常,还有:让调用者执行、丢弃任务、丢弃一个队尾任务、丢弃一个队列前任务
     */
    public static void testPool() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 10, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(20));
        //23 不会开启临时线程  超过23会开启   超过33 会启动拒绝策略
        for (int i = 0; i < 26; i++) {
            final int ri = i;
            executor.execute(() -> {
                System.out.println(ri + "  begin ====" + executor.getPoolSize());
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(ri + "  end ====" + executor.getPoolSize());
            });
        }
    }
}
