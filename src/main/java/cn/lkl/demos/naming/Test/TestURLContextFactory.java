package cn.lkl.demos.naming.Test;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * 拿 Test这一 域的资源对象
 */
public class TestURLContextFactory implements ObjectFactory {
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        System.out.println(TestURLContextFactory.class.getName()+"=======in test url context facotry");
        return TestContext.getInstance();
    }
}
