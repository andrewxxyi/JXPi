package cn.ijingxi.common.httpserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.apache.http.util.EntityUtils;

import cn.ijingxi.common.orm.jxJson;
import cn.ijingxi.common.util.IDo;
import cn.ijingxi.common.util.jxTimer;
import cn.ijingxi.common.util.utils;

public class jxHttpServer {

	private static IAcceptConn dualinputconn=null;
	
	private static Map<String,jxTimer> incomingconn=new HashMap<String,jxTimer>();
	private static String docRoot=null;
	private static int port=80;
	private static UriHttpRequestHandlerMapper reqistry=new UriHttpRequestHandlerMapper();
	
	private static Thread serviceThread=null;
	
    public static void init(int Port,String docRootDirectory,IAcceptConn dual) {
    	port=Port;
        // Document root directory
        docRoot = docRootDirectory;
        dualinputconn=dual;
        //String suffix=utils.getSuffix(filename);
    }
    public static void init(String docRootDirectory,IAcceptConn dual) {
        // Document root directory
        docRoot = docRootDirectory;
        dualinputconn=dual;
        //String suffix=utils.getSuffix(filename);
    }

    public static void addRreqistry(String rule,HttpRequestHandler handler)
    {
    	reqistry.register(rule, handler);
    }
    
    public static void start() throws Exception
    {
        // 上传文件
        reqistry.register("/share", new HttpFileUploadHandler());
        // 下载文件
        //reqistry.register("/FileDownload/*", new HttpFileDownloadHandler());
        // 如果处理器是按序的，则避免了通配符阻塞了其它
        reqistry.register("*", new HttpHandler());
        // Set up the HTTP protocol processor
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("Android Http Server/1.1"))
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();
        
        // Set up the HTTP service
        HttpService httpService = new HttpService(httpproc, reqistry);
        serviceThread = new RequestListenerThread(port, httpService);
        serviceThread.setDaemon(false);
    	serviceThread.start();
    }

    static class HttpHandler implements HttpRequestHandler  {

		@Override
        public void handle(final HttpRequest request,final HttpResponse response,final HttpContext context) throws HttpException, IOException {
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            
            
            if (!method.equals("GET") && !method.equals("POST") && !method.equals("PUT")&& !method.equals("DELETE")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            
            String target = request.getRequestLine().getUri();
            
            utils.P("handle", method + ":" + target);
            
            String suffix=utils.getSuffix(target);
            if(suffix!=null)
            	handleFileDownload(target,response,context);
            else
            	handleResAccess(method,target,request,response,context);
            
        }
        
        private void handleResAccess(String method,String uri,final HttpRequest request,final HttpResponse response,final HttpContext context) throws HttpException, IOException{
            jxHttpData rs=null;

            utils.P("handleResAccess", method + ":" + uri);
            
			try {
				rs = jxHttpRes.dualHttpRequest(method, uri,getJsonFromRequest(request));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if(rs==null)
            {
                response.setStatusCode(HttpStatus.SC_OK);
                jxHttpData rd=new jxHttpData(200,"处理完毕，无返回");
                StringEntity entity = new StringEntity(rd.getString());
                response.setEntity(entity);
                return;
            }
        	response.setStatusCode(rs.getResultCode());
            StringEntity entity = new StringEntity(rs.getString(),"UTF-8");
            response.setEntity(entity);
        }

        /**
         * 后台丢过来的json字符串，是包含了对象类名的，即通过：
         * var ts=new Object();
         * ts.TopSpace=new Object();
         * ts.TopSpace.Name="test";
         * 
         * @param request
         * @return
         * @throws Exception
         */
        private jxJson getJsonFromRequest(HttpRequest request) throws Exception
        {
        	if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                String js=EntityUtils.toString(entity,"UTF-8");
                
                utils.P("param string", js);                
                
                if(js!=null)
                	return jxJson.JsonToObject(js);
        	}
        	return null;
        }
        
        private void handleFileDownload(String target,final HttpResponse response,final HttpContext context) throws HttpException, IOException {
        	
            final File file = new File(docRoot, URLDecoder.decode(target, "UTF-8"));
            if (!file.exists()) {

                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                jxHttpData rd=new jxHttpData(404,"文件：" + file.getPath() + " 未找到");
                StringEntity entity = new StringEntity(rd.getString());
                response.setEntity(entity);
                utils.P("HttpFileHandler", "File " + file.getPath() + " not found");

            } else if (!file.canRead() || file.isDirectory()) {

                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                jxHttpData rd=new jxHttpData(403,"权限不够，拒绝访问");
                StringEntity entity = new StringEntity(rd.getString());
                response.setEntity(entity);
                utils.P("HttpFileHandler", "Cannot read file " + file.getPath());
                
            } else {

                response.setStatusCode(HttpStatus.SC_OK);
                FileEntity body = new FileEntity(file);
                //FileEntity body = new FileEntity(file, ContentType.create("text/html", (Charset) null));
                response.setEntity(body);
                //utils.P("HttpFileHandler", "Serving file " + file.getPath());
                
            }
        }

    }

    static class HttpFileDownloadHandler implements HttpRequestHandler  {

		@Override
		public void handle(final HttpRequest request,final HttpResponse response,final HttpContext context) throws HttpException, IOException {
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }

            String target = request.getRequestLine().getUri();
            utils.P("HttpFileDownloadHandler-target", target);
            final File file = new File(docRoot, URLDecoder.decode(target, "UTF-8"));
            if (!file.exists()) {

                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                jxHttpData rd=new jxHttpData(404,"文件：" + file.getPath() + " 未找到");
                StringEntity entity = new StringEntity(rd.getString());
                response.setEntity(entity);
                utils.P("HttpFileHandler", "File " + file.getPath() + " not found");

            } else if (!file.canRead() || file.isDirectory()) {

                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                jxHttpData rd=new jxHttpData(403,"权限不够，拒绝访问");
                StringEntity entity = new StringEntity(rd.getString());
                response.setEntity(entity);
                utils.P("HttpFileHandler", "Cannot read file " + file.getPath());
                
            } else {

                response.setStatusCode(HttpStatus.SC_OK);
                FileEntity body = new FileEntity(file);
                //FileEntity body = new FileEntity(file, ContentType.create("text/html", (Charset) null));
                response.setEntity(body);
                
                //utils.P("HttpFileHandler", "Serving file " + file.getPath());
                
            }            
            
            /*
            if (request instanceof FileEntity)
            {
            	FileEntity e=(FileEntity)request;
            	e.getContent()
            	List<FormBodyPart> pl = e.getBodyParts();
            	
            
            }
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                
                
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream is = conn.getInputStream();
                String result = IOUtils.toString(is, "UTF-8");
                
                byte[] entityContent = EntityUtils.toByteArray(entity);
                utils.P("HttpFileHandler", "Incoming entity content (bytes): " + entityContent.length);
            }
            */
        }
        
      }

    static class HttpFileUploadHandler implements HttpRequestHandler  {

		@Override
		public void handle(final HttpRequest request,final HttpResponse response,final HttpContext context) throws HttpException, IOException {
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
 
            
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            
            if (request instanceof FileEntity)
            {
            	FileEntity e=(FileEntity)request;
            	InputStream stream = e.getContent();
            	

            	
            
            }
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                
                
                //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //InputStream is = conn.getInputStream();
               // String result = IOUtils.toString(is, "UTF-8");
                
                byte[] entityContent = EntityUtils.toByteArray(entity);
                utils.P("HttpFileHandler", "Incoming entity content (bytes): " + entityContent.length);
            }
            
            
        }
        
      }

    static class RequestListenerThread extends Thread {

        private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
        private final ServerSocket serversocket;
        private final HttpService httpService;

        public RequestListenerThread(final int port,final HttpService httpService) throws IOException {
            this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
            this.serversocket = new ServerSocket(port);
            this.httpService = httpService;
        }

        @Override
        public void run() {
            utils.P("RequestListenerThread", "Listening on port " + this.serversocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = this.serversocket.accept();
                    utils.P("RequestListenerThread", "Incoming connection from " + socket.getInetAddress());
                    HttpServerConnection conn = this.connFactory.createConnection(socket);
                    String ip=socket.getInetAddress().getHostAddress();
                    WorkerThread t = new WorkerThread(this.httpService, conn);
                    if(incomingconn.containsKey(ip))
                    {
                        utils.P("run", "contains : "+ ip);
	                    // Start worker thread
	                    t.setDaemon(true);
	                    t.start();
	                    jxTimer timer = incomingconn.get(ip);
	                    timer.reTick();
                    }
                    else if(dualinputconn!=null)
                    {
                    	dualinputconn.DualConnRequest(ip);
                    	waitConn.put(ip, t);
                    	//如果没有同意，5分钟后自动删除
                    	jxTimer.DoAfter(300, new IDo(){
							@Override
							public void Do(Object param) throws Exception {
								waitConn.remove(param);
							}                    		
                    	}, ip);
                    }
                    else
                    {
                        utils.P("run", "dual : "+ ip);
                    	jxTimer timer=jxTimer.DoAfter(600, new IDo(){
                			@Override
                			public void Do(Object param) throws Exception {
                				incomingconn.remove(param);
                			}                    		
                    	}, ip);
                    	incomingconn.put(ip, timer);
	                    // Start worker thread
	                    t.setDaemon(true);
	                    t.start();
                    }
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    utils.P("RequestListenerThread", "I/O error initialising connection thread: "+ e.getMessage());
                    break;
                }
            }
        }
        
    }
    
    //key请求连接的ip
    private static Map<String,WorkerThread> waitConn=new HashMap<String,WorkerThread>();
    
    public static void AcceptConn(String IP)
    {
    	jxTimer timer=jxTimer.DoAfter(600, new IDo(){
			@Override
			public void Do(Object param) throws Exception {
				incomingconn.remove(param);
			}                    		
    	}, IP);
    	incomingconn.put(IP, timer);
    	WorkerThread t=waitConn.remove(IP);
    	if(t!=null)
    	{
            t.setDaemon(true);
            t.start();
    	}
    }

    static class WorkerThread extends Thread {

        private final HttpService httpservice;
        private final HttpServerConnection conn;

        public WorkerThread(final HttpService httpservice,final HttpServerConnection conn) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
        }

        @Override
        public void run() {
            utils.P("WorkerThread", "New connection thread");
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                }
            } catch (ConnectionClosedException ex) {
                //utils.P("WorkerThread", "error:Client closed connection");
            } catch (IOException ex) {
                utils.P("WorkerThread", "I/O error: " + ex.getMessage());
            } catch (HttpException ex) {
                utils.P("WorkerThread", "Unrecoverable HTTP protocol violation: " + ex.getMessage());
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {}
            }
        }

    }

}