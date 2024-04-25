package cn.lkl.demos.jni;

/**
 * jni - java调用c方法
 * 1.javac 生成class         javac src\main\java\cn\lkl\demos\jni\TestNative.java
 * 2.javah 生成 h 头文件     需要cd 到 java目录（classpath）下  javah cn.lkl.demos.jni.TestNative
 * 3.编写c文件实现方法
 * 4. gcc生成动态连接库（windows是dll  liunx是os）  so动态链接 不需要( -Wl,--add-stdcall-alias )
 * gcc -Wl,--add-stdcall-alias -shared  -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32"  -o native.dll native.c
 * 5.动态两节库放到library path下  启动命令可以指定
 * -Djava.library.path=$ProjectFileDir$\src\main\java
 * 6.你gcc打的是32位的连接，jvm运行环境也要32位。
 */
public class TestNative {
    static {
        System.out.println(System.getProperty("java.library.path"));
        //加载动态连接 - path下的native.dll
        System.loadLibrary("native");
    }

    public native int helloWord();

}
