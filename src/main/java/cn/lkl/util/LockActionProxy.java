package cn.lkl.util;

import java.util.concurrent.locks.Lock;

/**
 * 锁操作静态代理 - 目前只代理锁动作和释放锁的动作
 * 复杂的使用反射动态代理后面再说
 */
interface LockActionProxy {
    void doLock(Lock lock);
    void doUnlock(Lock lock);
}
