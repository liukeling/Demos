package cn.lkl.demos.uri;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 网络资源、本地资源 访问样例
 * URI  统一资源限定符：[scheme:][//authority][path][?query][#fragment]
 * authority 可以表示： [user-info@]host[:port]
 * URL : 指向网络资源操作类
 */
public class URIDemo {
    static URI demo1,demo2;
    static {
        try {
            demo1 = new URI("file:///f:/ideawork/test.txt");
            demo2 = new URI("jdbc:mysql://test:liukeling@192.168.5.128/test");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 本地文件读取 这里我用 URL方式//可以用file 类 和fileInputstream 和 fileoutputstream
     * @throws Exception
     */
    public static void localFileRead() throws Exception {
        URL fileUrl = new URL(demo1.toString());
        InputStream inputStream = fileUrl.openStream();
        //读取流缓存大小
        byte[] temp = new byte[100];
        //读取长度 -1 表示结束了
        int len;
        while ((len = inputStream.read(temp)) != -1) {
            String msg = new String(temp, 0, len);
            System.out.println(msg);
        }

    }
    /**
     * jdbc 资源访问 - 我用的mysql
     * @throws Exception
     */
    public static void jdbcTest() throws Exception {
        //应用层 url是统一资源路径 如果要管理得封装成URI对资源进行管理，然后打开连接开启会话
        Connection connection = DriverManager.getConnection(demo2.toString());
        //会话层 - 可以查询各种数据
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from t_user");
        while (resultSet != null && resultSet.next()) {
            String name = resultSet.getString("name");
            System.out.println(name);
        }
    }

    /**
     * tcp流处理 - 服务端  客户端可以直接new 一个socket,我没写了，我使用telnet命令
     * BIO模式
     *
     * @throws Exception
     */
    public static void socketBIOTest() throws Exception {
        //服务循环监听连接，然后安排线程处理 系统层
        ServerSocket server = new ServerSocket(9988);
        while (true) {
            //应用层
            Socket accept = server.accept();
            //窗口大小 - cpu处理socket流读取周期内的缓存大小
            accept.setReceiveBufferSize(60);
            accept.setSendBufferSize(60);
            new Thread(() -> {
                try (InputStream inputStream = accept.getInputStream(); OutputStream outputStream = accept.getOutputStream()) {
                    //应用程序一个while周期内收到的缓存大小
                    byte[] temp = new byte[100];
                    //读取长度 -1 表示结束了
                    int len;
                    while (accept.isConnected() && (len = inputStream.read(temp)) != -1) {
                        //我简单封装成string ,实际你要自定义协议处理，进行拆包、封包 进行会话封装
                        String msg = new String(temp, 0, len);
                        System.out.println(Thread.currentThread().getName() + "   " + msg);
                        outputStream.write(("收到 "+msg).getBytes("GBK"));
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        accept.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * tcp流处理 - NIO模式
     */
    public static void socketNIOTest()throws Exception {
        //NIO提供 者
        SelectorProvider provider = SelectorProvider.provider();
        //选择器 - 循环监听通道关心的消息 - 是新来连接，还是要读取，还是怎么( SelectionKey)
        Selector selector = Selector.open();
        new Thread(()->{
            System.out.println("======is open "+selector.isOpen());
            while(selector.isOpen()){
                try {
                    //读取 周期内 关心的 key信息
                    selector.select(10000);
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    if(selectionKeys == null || selectionKeys.isEmpty()) continue;
                    System.out.println("==============in while====");
                    for (SelectionKey selectionKey : selectionKeys) {
                        //绑定的通道
                        SelectableChannel bindChannel = selectionKey.channel();
                        System.out.println("==============info "+selectionKey.isAcceptable()+"  "+ selectionKey.isReadable()+"  "+selectionKey.isWritable()+"  "+selectionKey.readyOps());
                        if(!selectionKey.isValid()){
                            //失效了
                            bindChannel.close();
                            continue;
                        }
                        //有连接
                        if(selectionKey.isAcceptable() && bindChannel instanceof ServerSocketChannel) {
                            SocketChannel accept = ((ServerSocketChannel) bindChannel).accept();
                            if(accept != null) {
                                //如果要 BIO 处理连接  则 拿到socket 的方法  accept.socket();
                                //继续NIO - 我这里服务通道、连接通道都是用一个selector  注册读、写
                                accept.configureBlocking(false);//配置为不是阻塞
                                System.out.println("   is connected:"+accept.isConnected());
                                accept.setOption(StandardSocketOptions.SO_RCVBUF, 60);
                                accept.setOption(StandardSocketOptions.SO_SNDBUF, 60);
                                accept.register(selector, SelectionKey.OP_READ);
                            }
                        }
                        //有读取
                        if(selectionKey.isReadable() && (bindChannel instanceof SocketChannel)){
                            // 直接用系统内存  ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
                            //用jvm的内存
                            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[1024]);
                            int len;
                            while((len = ((SocketChannel) bindChannel).read(byteBuffer)) >0){
                                byte[] array = byteBuffer.array();
                                String msg = new String(array,0, len);
                                System.out.println(msg);
                                msg = "收到了 "+msg;
                                byte[] buf = msg.getBytes("GBK");
                                ByteBuffer write = ByteBuffer.wrap(buf);
                                ((SocketChannel) bindChannel).write(write);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //绑定端口 设置最大连接数 开启一个服务通道
        ServerSocketChannel channel = provider.openServerSocketChannel();
        channel.bind(new InetSocketAddress(8899),20);
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }
}
