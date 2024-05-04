package cn.lkl.demos.web;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 模拟跨域攻击：<br />
 1.开两个tomcat服务, 一个是localhost:8080（被攻击网站）  一个是127.0.0.1:8081 （攻击网站）<br />
 2.都部署 demo这个应用 为了方便，不写两个了 <br />
 3.可以通过浏览器控制台查看cookie,确保localhost:8080 生成了cookie, 127.0.0.1:8081他不需要kookie<br />
 4.访问攻击地址：http://127.0.0.1:8081/demo/test/attack  <br />
 5.自动提交了表单到 http://localhost:8080/demo/test/byAttack ,tomcat控制台可以看到cookie(用户在该网站的)和referer（攻击网站的） <br />
 6.预防：后端设置响应头 Access-Control-Allow-Origin 只允许的来源；接口头比较referer；不把登录信息绑定cookie,使用token <br />
 */
public class TestServlet implements Servlet {
    private ServletContext context;
    private ServletConfig config;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.context = config.getServletContext();
        this.config = config;
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        if (req instanceof HttpServletRequest) {
            String cookie = ((HttpServletRequest) req).getHeader("cookie");
            System.out.println("=============cookie:" + cookie);
            String referer = ((HttpServletRequest) req).getHeader("Referer");
            System.out.println("=============referer:"+referer);
            String requestURL = ((HttpServletRequest) req).getRequestURL().toString();
            System.out.println("=========url:"+requestURL);
            if(requestURL.lastIndexOf("test/attack") != -1) {
                //攻击  localhost:8080 网站的 demo/test/byAttack 这个地址 请求会自动带对应的cookie
                res.getWriter().write("<html>" +
                        "<body>" +
                        "<form action='http://localhost:8080/demo/test/byAttack' method='post'></form>" +
                        "</body>" +
                        "<script>document.forms[0].submit();</script>" +
                        "</html>");
            }
        }
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
