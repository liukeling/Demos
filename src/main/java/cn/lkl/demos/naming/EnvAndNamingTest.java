package cn.lkl.demos.naming;

import javax.naming.*;
import java.util.Hashtable;

/**
 * 命名空间、上下文 测试  主要是体现一个分区、以及命名空间的层级关系，比如URI的各资源层级，或者自定义层级
 * 命名空间：javax.naming.Name  命名空间
 * 上下文： context             每个name空间的上下文
 * provider url  根据 url解析 需要实现 url Object factory  他是可以返回具体资源对象或者上下文
 *      需要指定包前缀 以及 包名和url一致
 * naming factory 需要实现 contextfactory  只能是上下文
 */

public class EnvAndNamingTest {
    //环境参数
    private static final Hashtable<String,String> testenv = new Hashtable();
    static{
        //根据命名空间解析 - contextfactory 里面直接返回TestContext( 直接返回TestContext的话，你得自己解析所有的scheme ,哪怕你不用的scheme)
        testenv.put(Context.INITIAL_CONTEXT_FACTORY,"cn.lkl.demos.naming.ContextFactory");
        //使用url 解析 contextfactory 里面 返回initialContext  （除了自己的 Test 域 解析外，其他的域交由initialContext解析）
        //包名有一定规则，前缀+URL 类名：URL+"URLContextFacotry" initialContext的规则，
        // 不用这规则可以在ContextFactory判断下URL，返回自己的context还是返回initalContext
        testenv.put(Context.PROVIDER_URL,"Test");
        testenv.put(Context.URL_PKG_PREFIXES,"cn.lkl.demos.naming");
    }

    public static void contextTest() throws NamingException {
        Context context = new InitialContext(testenv);
        //每个层级上下文得自己去实现。。。
        context.list("Test:env");
    }
}
