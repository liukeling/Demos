package cn.lkl.demos.web;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XssServlet extends HttpServlet {
    volatile Map<String, String> messages = new HashMap<>(3);
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURL().toString();
        if(url.lastIndexOf("userMsg") != -1){
            //用户提交过来的
            String msg = req.getParameter("msg");
            System.out.println(msg);
            String sessionId = req.getSession().getId();
            System.out.println(sessionId);
            //存数据库的，我这里用map缓存
            messages.put(sessionId, msg);
            resp.sendRedirect("/demo/xssUser.html");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURL().toString();
        if(url.lastIndexOf("getMsgList") != -1){
            //获取msg
            List<Map<String,String>> detils = new ArrayList<>(messages.size());
            for (String k : messages.keySet()) {
                detils.add(new HashMap<String,String>(2){{put("sessionId",k);put("msg",messages.get(k));}});
            }
            resp.getWriter().write(JSONObject.toJSONString(detils));
        }
    }
}
