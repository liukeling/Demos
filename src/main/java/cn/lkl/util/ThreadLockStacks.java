package cn.lkl.util;

import sun.misc.Lock;

import java.util.Stack;

public class ThreadLockStacks {
    private static volatile ThreadLocal<Stack<Lock>[]> ALL_STACK = new ThreadLocal<>();
    public static void doLock(Lock lock) throws InterruptedException {
        Thread t = Thread.currentThread();
        lock.lock();
    }
}
