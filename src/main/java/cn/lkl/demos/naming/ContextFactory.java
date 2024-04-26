package cn.lkl.demos.naming;

import cn.lkl.demos.naming.Test.TestContext;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;

public class ContextFactory implements InitialContextFactory {
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        System.out.println("================初始化上下文。。。。。");
        //使用URL工厂命名规则，不使用就要从env参数里面获取PROVIDER_URL判断返回自己的上下文，就不用定义urlfactory了
        return new InitialContext();
    }
}
