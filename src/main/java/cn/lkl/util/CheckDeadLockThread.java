package cn.lkl.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

public class CheckDeadLockThread extends Thread {

    private DeadLockManager manager;

    public CheckDeadLockThread() {
        setDaemon(true);
        manager = DeadLockManager.getInstance();
    }

    @Override
    public void run() {
        //可以自己另外写定时任务啥的定时进行调度
        while (!isInterrupted()) {
            //维护中
            Node[] curAll = manager.getCurAll();
            System.out.println("=========begin check ");
            if (curAll != null && curAll.length > 0) {
                //构建table
                Hashtable<AbstractQueuedSynchronizer, Node> sourceIndex = new Hashtable<>(curAll[curAll.length - 1].tableSize);
                for (int i = 0; i < curAll.length; i++) {
                    Node node = curAll[i];
                    if (node == null)
                        continue;
                    node.index = i;
                    if (node.curSources != null && node.curSources.length > 0) {
                        for (AbstractQueuedSynchronizer curSource : node.curSources) {
                            sourceIndex.put(curSource, node);
                        }
                    }
                }
                //构建链\状态检测
                for (int i = 0; i < curAll.length; i++) {
                    Node node = curAll[i];
                    if (node == null) {
                        continue;
                    }
                    //所等待才给他构建
                    if (node.curThread.getState() == State.WAITING) {
                        Object blocker = LockSupport.getBlocker(node.curThread);
                        node.waitSource = blocker != null && (blocker instanceof AbstractQueuedSynchronizer) ? (AbstractQueuedSynchronizer) blocker : null;
                        node.pre = node.waitSource == null ? null : sourceIndex.get(node.waitSource);
                        if (node.pre != null) {
                            Node tmp = node.pre.nexts;
                            node.pre.nexts = node;
                            node.sameLevel = tmp;

                            if(needClean(node, curAll)) {
                                curAll[node.index] = null;
                                System.out.println("=====to clean handle:"+node.curThread.getName());
                            }
                        }
                    }
                }
                //检测回路
                for (int i = 0; i < curAll.length; i++) {
                    Node node = curAll[i];
                    if(node == null)continue;
                    System.out.println("==========to begin handle:"+node.curThread.getName());
                    node.level = 1;
                    setLevel(node,node.nexts, node.level+1,1);
                }
            }
            System.out.println("=========end check");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("===========退出了");
    }
    private boolean needClean(Node n,Node[] curAll){
        Node tmp = n.pre;
        while(tmp != null){
            if(tmp == n){
                return false;
            }
            if(curAll[tmp.index] != null){
                return true;
            }
            tmp =tmp.pre;
        }
        return false;
    }
    private void setLevel(Node check,Node next, int level,int deep){
        if(deep < 20){
            Node nextTmp = next;
            while(nextTmp != null) {
                if (nextTmp.level == 0) {
                    nextTmp.level = level;
                    setLevel(nextTmp, nextTmp.nexts, level+1,deep+1);
                } else {
                    System.out.println(level+"===============回路了：cur level:" + check.level + "     bak level:" + nextTmp.level + "  死锁:");
                    Node tmp = check.pre;
                    System.out.println(level+"===============begin:" + check.curThread.getName());
                    //等级最高的那个节点释放
                    int maxLevel = check.level;
                    Node maxLevlNode = check;
                    while (tmp != null) {
                        System.out.println(level+"=============wait:"+tmp.curThread.getName());
                        if(tmp.level > maxLevel){
                            maxLevel = tmp.level;
                            maxLevlNode = tmp;
                            maxLevlNode.pre = null;
                        }
                        if (hasNode(next, tmp)) {
                            break;
                        }
                        tmp = tmp.pre;
                    }
                    tryUnLock(maxLevlNode,level);
                }
                nextTmp = nextTmp.sameLevel;
            }
        }else{
            System.out.println("==========method deep too long:"+deep);
        }
    }
    private boolean hasNode(Node same, Node tmp){
        boolean has = false;
        Node tmp1 = same;
        while(tmp1 != null){
            if(tmp1 == tmp){
                return true;
            }
            tmp1 = tmp1.sameLevel;
        }
        return has;
    }
    private boolean tryUnLock(Node n,int level){
        boolean ok =true;
        try {
            Method setExclusiveOwnerThread = AbstractOwnableSynchronizer.class.getDeclaredMethod("setExclusiveOwnerThread", Thread.class);
            setExclusiveOwnerThread.setAccessible(true);
            for (AbstractQueuedSynchronizer curSource : n.curSources) {
                setExclusiveOwnerThread.invoke(curSource, Thread.currentThread());
                curSource.release(1);
            }
            n.curThread.stop();
            manager.remove(n.curThread);
        }catch (Exception e){
            e.printStackTrace();
            ok = false;
        }
        if(ok){
            n.curThread.interrupt();
        }
        System.out.println(level+"==============强制释放锁资源，并关闭线程："+n.curThread.getName()+"   result:"+ok);
        return ok;
    }
}
