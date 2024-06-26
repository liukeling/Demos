package cn.lkl.demos.thread;

import cn.lkl.util.CheckDeadLockThread;
import javafx.concurrent.Task;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TestPool implements ThreadFactory {
    private static TestPool POOL;
    private ThreadPoolExecutor executor;
    private ThreadGroup envG;
    private CheckDeadLockThread mainT;
    //可见、时序
    private volatile boolean isRun = false;
    private ArrayBlockingQueue arrayQueue;
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private int corePoolSize,maxPoolSize,keepAlive;
    private TimeUnit timeUnit;

    public static synchronized TestPool getPOOL(int corePoolSize,int maxPoolSize,int keepAlive, TimeUnit timeUnit) {
        if(POOL == null) {
            POOL = new TestPool(corePoolSize, maxPoolSize, keepAlive, timeUnit);
        }
        return POOL;
    }

    private TestPool(int corePoolSize,int maxPoolSize,int keepAlive, TimeUnit timeUnit) {
        System.out.println("===========pool init.....");
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAlive = keepAlive;
        this.timeUnit = timeUnit;
        System.out.println("===========pool init end");
    }
    public boolean start() {
        rwLock.writeLock().lock();
        try {
            if (!isRun) {
                System.out.println("==========pool start begin");
                arrayQueue = new ArrayBlockingQueue(10);
                mainT = new CheckDeadLockThread(envG, "check main");
                mainT.setPriority(4);
                envG = mainT.getThreadGroup();
                System.out.println("===================init pool ok,ready to start...");
                isRun = true;
                executor = new ThreadPoolExecutor(1, 10, 20, TimeUnit.SECONDS, arrayQueue, this, new ThreadPoolExecutor.AbortPolicy());
                //开启一个死锁监测
                mainT.start();
                System.out.println("==========pool start ok");
            }
            return true;
        }catch (Exception e){
            return false;
        }finally {
            rwLock.writeLock().unlock();
        }
    }

    public void destroy() {
        if (isRunWithLock(rwLock.writeLock())) {
            try {
                System.out.println("==========pool destroy begin");
                isRun = false;
                if (executor != null) {
                    executor.shutdown();
                }
                if (mainT != null && mainT.isAlive()) {
                    mainT.interrupt();
                }
                System.out.println("==========pool destroy end");
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }

    public <T> Future<T> submit(Callable<T> r) {
        if (isRunWithLock(rwLock.readLock())) {
            try {
                return executor.submit(r);
            } finally {
                rwLock.readLock().unlock();
            }
        }
        return null;
    }

    @Override
    public Thread newThread(Runnable r) {
        //加入到mainT的线程组里面去
        if (isRunWithLock(rwLock.readLock())) {
            try {
                PoolThread sonThread = new PoolThread("pool son", r);
                System.out.println("======init thread:" + sonThread.getName());
                return sonThread;
            } finally {
                rwLock.readLock().unlock();
            }
        }
        return null;
    }

    private boolean isRunWithLock(Lock lock) {
        if (isRun) {
            lock.lock();
            if (isRun) {
                return true;
            } else {
                lock.unlock();
                return false;
            }
        }
        return false;
    }

    class PoolThread extends Thread {
        //目的是加入到mainT的线程组里面去
        PoolThread(String name, Runnable r) {
            this(envG, name, r);
        }

        PoolThread(ThreadGroup group, String name, Runnable r) {
            super(group, r, "=====poolThread-" + name);
            setDaemon(true);
        }

        @Override
        public void run() {
            System.out.println(getName() + " begin do...");
            super.run();
            System.out.println(getName() + " do end.");
        }
    }
}
