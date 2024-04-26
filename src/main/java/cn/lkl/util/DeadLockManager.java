package cn.lkl.util;

import javax.xml.transform.Source;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 * 死锁管理
 */
public class DeadLockManager {
    private DeadLockManager() {

    }

    private static final DeadLockManager manager = new DeadLockManager();


    public static DeadLockManager getInstance() {
        return manager;
    }

    //线程本地资源
    private final ThreadLocal<Stack<Lock>> threadLocal = new ThreadLocal<>();
    //所有线程资源栈
    private HashMap<Thread, Stack<Lock>> allStack = new HashMap<>(20, 8);

    //添加锁资源
    final void add(Thread thread, Lock lock) {
        if (lock == null)
            return;
        Stack<Lock> locks = threadLocal.get();
        if (locks == null) {
            locks = new Stack<>();
            threadLocal.set(locks);
            synchronized (allStack) {
                allStack.put(Thread.currentThread(), locks);
            }
        }
        locks.add(lock);
    }

    //获取锁并移出栈
    final Lock getLockAndRemove(Thread thread) {
        Stack<Lock> locks = threadLocal.get();
        Lock pop = locks.pop();
        if (locks.size() == 0) {
            synchronized (allStack) {
                allStack.remove(Thread.currentThread());
            }
            //gc
            threadLocal.set(null);
        }
        return pop;
    }

    //复制一个当前资源副本 - 深复制
    final Node[] getCurAll() {
        synchronized (allStack) {
            Node[] clones = new Node[allStack.size()];
            int index = 0;
            if (allStack.size() > 0) {
                int tableSize = 0;
                for (Thread thread : allStack.keySet()) {
                    Stack<Lock> locks = allStack.get(thread);
                    System.out.println("check for "+thread.getName()+"==============="+thread.getState());
                    Node item = new Node();
                    Lock[] locks_sources = locks.toArray(new Lock[locks.size()]);
                    //从lock中拿sync 给到curSources - 反射
                    if(locks_sources != null && locks_sources.length > 0)
                        item.curSources = classForGet(locks_sources, locks_sources[0].getClass());
                    item.curThread = thread;
                    clones[index++] = item;
                    tableSize += item.curSources == null ? 0 : item.curSources.length;

                }
                clones[index - 1].tableSize = tableSize;
                return clones;
            }
            return null;
        }
    }
    private AbstractQueuedSynchronizer[] classForGet(Lock[] locks,Class lockClz){
        try {
            Field sync = lockClz.getDeclaredField("sync");
            sync.setAccessible(true);
            AbstractQueuedSynchronizer[] aqs = new AbstractQueuedSynchronizer[locks.length];
            for (int i = 0; i < aqs.length; i++) {
                Object value = sync.get(locks[i]);
                if(value != null && (value instanceof AbstractQueuedSynchronizer)){
                    aqs[i] = (AbstractQueuedSynchronizer)value;
                }
            }
            return aqs;
        }catch (Exception e){
            System.out.println("==============failed to get sync .....");
            e.printStackTrace();
        }
        return null;
    }
    void remove(Thread t){
        synchronized (allStack){
            allStack.remove(t);
        }
    }
}
