package cn.lkl;


import cn.lkl.demos.jni.TestNative;
import cn.lkl.demos.lock.LockTest;
import cn.lkl.demos.thread.ThreadTest;
import cn.lkl.demos.uri.URIDemo;

/**
 * demo 测试
 */
public class Application {
    public static void main(String[] args) throws Exception {
        System.out.println("hello word");
        lockTest();
    }

    /**
     * jni 测试类
     */
    public static void testJni(){
        for (int i = 0; i < 3; i++) {
            //c里面是每次累加，变量不在对象里面的
            System.out.println(new TestNative().helloWord());
        }
    }
    /**
     * 本地资源、网络资源读写 测试
     */
    public static void testURI() throws Exception {
        URIDemo.localFileRead();
//        URIDemo.jdbcTest();
//        URIDemo.socketBIOTest();
//        URIDemo.socketNIOTest();
    }

    /**
     * 锁测试 - 独占、读写、重入
     */
    public static void lockTest(){
//        LockTest.testReentrantLock();
        LockTest.readwriteLock();
    }

    /**
     * 线程测试
     */
    public static void threadTest(){
//        ThreadTest.testStopLock();
//        ThreadTest.testBlocker();
//        ThreadTest.testStopBlocker();
        ThreadTest.testPool();
    }
}
