package cn.lkl.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 简单实现代理锁操作
 * -- 目的：1.存储线程目前持有的锁资源、实现压栈、出栈顺序
 * 2.使用单例模式，保证栈信息只有一个方便简单管理
 */
public class BaseLockActionProxy implements LockActionProxy {
    @Override
    public void doLock(Lock lock) {
        lock.lock();
        //获取到锁资源 - 放入资源栈
        DeadLockManager.getInstance().add(Thread.currentThread(), lock);
    }

    @Override
    public void doUnlock(Lock lock) {
        //这里不支持他自己释放锁，由出栈顺序释放
        Lock lockAndRemove = DeadLockManager.getInstance().getLockAndRemove(Thread.currentThread());
        if(lockAndRemove != null)
            lockAndRemove.unlock();
    }
}
