package cn.lkl.demos.jni;

/**
 * jni - java����c����
 * 1.javac ����class         javac src\main\java\cn\lkl\demos\jni\TestNative.java
 * 2.javah ���� h ͷ�ļ�     ��Ҫcd �� javaĿ¼��classpath����  javah cn.lkl.demos.jni.TestNative
 * 3.��дc�ļ�ʵ�ַ���
 * 4. gcc���ɶ�̬���ӿ⣨windows��dll  liunx��os��  so��̬���� ����Ҫ( -Wl,--add-stdcall-alias )
 * gcc -Wl,--add-stdcall-alias -shared  -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32"  -o native.dll native.c
 * 5.��̬���ڿ�ŵ�library path��  �����������ָ��
 * -Djava.library.path=$ProjectFileDir$\src\main\java
 * 6.��gcc�����32λ�����ӣ�jvm���л���ҲҪ32λ��
 */
public class TestNative {
    static {
        System.out.println(System.getProperty("java.library.path"));
        //���ض�̬���� - path�µ�native.dll
        System.loadLibrary("native");
    }

    public native int helloWord();

}
