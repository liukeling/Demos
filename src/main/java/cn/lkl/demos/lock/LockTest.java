package cn.lkl.demos.lock;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

/**
 * 独占锁 - AbstractOwnableSynchronizer 记录资源使用的当前线程
 * AQS 指的是 AbstractQueuedSynchronizer  Q - 队列  等待线程的队列 双链结构
 * 底层 使用了 UNSAFE 提供了原子性操作，比较设置数字，以及 支持 LockSupper 的方法
 * LockSupper.park 没令牌就等待  有令牌就通行 - 重复给了令牌没用，只认一个、令牌必须再线程start之后才有效，starte之前给的没用
 * LockSupper.unpark 给令牌通行
 * state - 乐观锁+自旋来+原子比较设置来修改占用state，修改成功就拿到锁了，释放锁就再次修改state(不能重入就只有0和1)（能重入就记录了重入次数）
 *
 */
public class LockTest {

    /**
     * 公平锁、非公平锁，默认非公平锁
     * 公平锁：再释放锁资源的时候，优先通过最早park等待的。
     */
    public static void testReentrantLock(){
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
        reentrantLock.lock();
        new Thread(()->{
            System.out.println("=============beign");
            reentrantLock.lock();
            System.out.println("=============get lock");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                reentrantLock.unlock();
                System.out.println("============end unlock");
            }
        }).start();
        try {
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("==========lock 了多少次，unlock也要多少");
        reentrantLock.unlock();
        reentrantLock.unlock();
    }


    /**
     * 读写锁
     * 读锁 和 读锁 不互斥，是共享的
     * 写锁 和 读锁 互斥， 写锁和写锁 互斥
     * 互斥：一个占了，其他的等待。 不能同时占用
     */
    public static void readwriteLock(){
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        Lock r = readWriteLock.readLock();
        Lock w = readWriteLock.writeLock();
        for (int i = 0; i < 40; i ++){
            final int ti = i;
            new Thread(()->{
                r.lock();
                System.out.println(ti+"  read in============");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(ti+"  read out============");
                r.unlock();
            }).start();
            if(i %3 == 1) {
                new Thread(() -> {
                    w.lock();
                    System.out.println("wirte =========== in");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("wirte =========== out");
                    w.unlock();
                }).start();
            }
        }
    }
}
