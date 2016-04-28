package cn.ijingxi.ServerCommon.httpServer;

import cn.ijingxi.orm.jxJson;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.jxTimer;
import cn.ijingxi.util.utils;
import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.*;
import org.apache.http.util.EntityUtils;
import org.apache.james.mime4j.MimeException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * 如果要使用ssl：
 * 使用前需通过keytool在主目录（启动目录）下创建一个ssl的key文件：.keystore，密码是：secret，
 * 然后根据访问的网址的host名来创建一个key，
 * host名就是客户端想访问的host名
 * jxHttpClient client=new jxHttpClient("127.0.0.1",10000,true);
 *
 * keytool -genkey -keystore .keystore
 * 您的名字与姓氏是什么?
 *  [Unknown]:  127.0.0.1
 *  您的组织单位名称是什么?
 *  [Unknown]:  JingXi
 *  您的组织名称是什么?
 *  [Unknown]:  iJingXi
 *  您所在的城市或区域名称是什么?
 *  [Unknown]:  ShangHai
 *  您所在的省/市/自治区名称是什么?
 *  [Unknown]:  ShangHai
 *  该单位的双字母国家/地区代码是什么?
 *  [Unknown]:  SH
 *  CN=127.0.0.1, OU=JingXi, O=iJingXi, L=ShangHai, ST=ShangHai, C=SH是否正确?
 *  [否]:  y
 *
 *
 *
 *
 *
 */
public class jxHttpServer {

    public static final String SessionHeaderName="jxSessionID";
    public static final String SessionPeopleIDName="PeopleID";

    public String UploadDir="upload";
    public String getFullUploadDir(){
        return docRoot+UploadDir+"/";
    }
	private IAcceptConn dualinputconn=null;
	
	private Map<String,jxTimer> incomingconn=new HashMap<String,jxTimer>();
    public String docRoot=null;
	private int port=80;
	private UriHttpRequestHandlerMapper reqistry=new UriHttpRequestHandlerMapper();
	
	private Thread serviceThread=null;
	
    public jxHttpServer(int Port,String docRootDirectory,IAcceptConn dual) {
    	port=Port;
        // Document root directory
        docRoot = docRootDirectory;
        dualinputconn=dual;
        //String suffix=utils.getSuffix(filename);
    }
    public jxHttpServer(String docRootDirectory,IAcceptConn dual) {
        // Document root directory
        docRoot = docRootDirectory;
        dualinputconn=dual;
        //String suffix=utils.getSuffix(filename);
    }

    public void addRreqistry(String rule,HttpRequestHandler handler)
    {
    	reqistry.register(rule, handler);
    }

    public void start() throws Exception{
        start(false);
    }

    public void start(boolean useSSL) throws Exception
    {
        // 读取流的处理
        //reqistry.register("/Stream/*", new HttpStreamHandler());
        // 上传文件
        reqistry.register("/"+UploadDir, new HttpFileUploadHandler());
        // 下载文件
        //reqistry.register("/FileDownload/*", new HttpFileDownloadHandler());
        // 如果处理器是按序的，则避免了通配符阻塞了其它
        reqistry.register("*", new HttpHandler());
        // Set up the HTTP protocol processor
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("JingXi Http Server/1.1"))
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();

        SSLServerSocketFactory sf = null;
        if (useSSL) {
            String key=docRoot+".keystore";
            KeyStore keystore=KeyStore.getInstance("JKS");
            //keystore的类型，默认是jks
            keystore.load(new FileInputStream(key),"secret".toCharArray());

            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keystore, "secret".toCharArray());
            KeyManager[] keymanagers = kmfactory.getKeyManagers();
            SSLContext sslcontext = SSLContext.getInstance("SSLv3");
            sslcontext.init(keymanagers, null, null);
            sf = sslcontext.getServerSocketFactory();
        }

        // Set up the HTTP service
        HttpService httpService = new HttpService(httpproc, reqistry);
        serviceThread = new RequestListenerThread(port, httpService, sf);
        serviceThread.setDaemon(false);
    	serviceThread.start();
    }

    class HttpHandler implements HttpRequestHandler  {

		@Override
        public void handle(final HttpRequest request,
                           final HttpResponse response,
                           final HttpContext context) throws HttpException, IOException {

            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            String target = request.getRequestLine().getUri();
            String suffix = null;
            jxLog.logger.debug(method + ":" + target);

            String[] ss = target.split("\\?");
            if (ss.length == 2) {
                suffix = utils.getSuffix(ss[0]);
                if (suffix != null) {
                    jxLog.logger.debug("file :" + ss[0]);
                    handleFileDownload(ss[0], response, context);
                    return;
                }
            } else {
                suffix = utils.getSuffix(target);
                if (suffix != null) {
                    handleFileDownload(target, response, context);
                    return;
                }
            }
            handleResAccess(method, target, request, response, context);
        }
        
        private void handleResAccess(String method,String uri,
                                     final HttpRequest request,
                                     final HttpResponse response,
                                     final HttpContext context) throws HttpException, IOException{
            jxHttpData rs=null;

            //jxLog.logger.debug(method + ":" + uri);
            
			try {
                String sessionid=null;
                Header[] head = request.getHeaders(SessionHeaderName);
                //utils.P("Header num",""+head.length);
                if(head.length==1)
                    sessionid=head[0].getValue();
                //jxLog.logger.debug("sessionid:"+sessionid);
				rs = jxHttpRes.dualHttpRequest(response,
                        sessionid,
                        method,
                        uri,
                        getJsonFromRequest(request),
                        docRoot);

			} catch (Exception e) {
                jxLog.error(e);
			}
            if(rs==null){
                /*
                //无返回则是处理函数自行进行了响应的处理
                response.setStatusCode(HttpStatus.SC_OK);
                jxHttpData rd=new jxHttpData(200,"处理完毕，无返回");
                StringEntity entity = new StringEntity(rd.getString(),"UTF-8");
                response.setEntity(entity);
                return;
                */
            }else{
                response.setStatusCode(rs.getResultCode());
                StringEntity entity = new StringEntity(rs.getString(),"UTF-8");
                response.setEntity(entity);
            }

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

                //jxLog.logger.debug("param string:"+js);
                
                if(js!=null)
                	return jxJson.JsonToObject(js);
        	}
        	return null;
        }
        
        private void handleFileDownload(String target,final HttpResponse response,
                                        final HttpContext context) throws HttpException, IOException {

            String fn=URLDecoder.decode(target, "UTF-8");
            String fileName=docRoot+fn;
            final File file = new File(fileName);
            if (!file.exists()) {

                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                jxHttpData rd=new jxHttpData(404,"文件：" + file.getPath() + " 未找到");
                StringEntity entity = new StringEntity(rd.getString());
                response.setEntity(entity);
                jxLog.logger.debug("File " + file.getPath() + " not found");

            } else if (!file.canRead() || file.isDirectory()) {

                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                jxHttpData rd=new jxHttpData(403,"权限不够，拒绝访问");
                StringEntity entity = new StringEntity(rd.getString());
                response.setEntity(entity);
                jxLog.logger.debug("Cannot read file " + file.getPath());
                
            } else {

                response.setStatusCode(HttpStatus.SC_OK);
                //FileEntity body = new FileEntity(file);

                // getMagicMatch accepts Files or byte[],
                // which is nice if you want to test streams
                String contentType=null;
                try {
                    //Magic parser = new Magic() ;
                    //MagicMatch match = parser.getMagicMatch(file, true);
                    //contentType = match.getMimeType();
                } catch (Exception e) {
                    jxLog.error(e);
                }


                //FileNameMap fileNameMap = URLConnection.getFileNameMap();
                Path path = Paths.get(fileName);
                contentType = Files.probeContentType(path);

                //jxLog.logger.debug("contentType:"+ contentType);

                //Tika tika = new Tika();
                //String contentType = tika.detect(file);
                //jxLog.logger.debug("contentType:"+ contentType);
                FileEntity body = new FileEntity(file,
                        ContentType.create(contentType, (Charset) null));

                //jxLog.logger.debug("contentType:"+ ContentType.getOrDefault(body));

                response.setEntity(body);
                //utils.P("HttpFileHandler", "Serving file " + file.getPath());
                
            }
        }

    }

    class HttpFileDownloadHandler implements HttpRequestHandler  {

		@Override
		public void handle(final HttpRequest request,final HttpResponse response,final HttpContext context) throws HttpException, IOException {
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }

            String target = request.getRequestLine().getUri();
            String fn=URLDecoder.decode(target, "UTF-8");
            //utils.P("HttpFileDownloadHandler-target", target);
            final File file = new File(docRoot, fn);
            if (!file.exists()) {

                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                jxHttpData rd=new jxHttpData(404,"文件：" + file.getPath() + " 未找到");
                StringEntity entity = new StringEntity(rd.getString());
                response.setEntity(entity);
                //utils.P("HttpFileHandler", "File " + file.getPath() + " not found");

            } else if (!file.canRead() || file.isDirectory()) {

                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                jxHttpData rd=new jxHttpData(403,"权限不够，拒绝访问");
                StringEntity entity = new StringEntity(rd.getString());
                response.setEntity(entity);
                //utils.P("HttpFileHandler", "Cannot read file " + file.getPath());
                
            } else {

                response.setStatusCode(HttpStatus.SC_OK);
                FileEntity body = new FileEntity(file);
                String contentType = Files.probeContentType(Paths.get(fn));
                jxLog.logger.debug("contentType:"+contentType);
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

    class HttpFileUploadHandler implements HttpRequestHandler  {
		@Override
		public void handle(final HttpRequest request,final HttpResponse response,final HttpContext context) throws HttpException, IOException {

            try {
                String dir=getFullUploadDir();
                jxHttpMime mime=new jxHttpMime(request,dir);
                response.setStatusCode(HttpStatus.SC_OK);
            } catch (MimeException e) {
                e.printStackTrace();
                response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        }
      }

    class RequestListenerThread extends Thread {

        private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
        private final ServerSocket serversocket;
        private final HttpService httpService;
        private final SSLServerSocketFactory sf;

        public RequestListenerThread(final int port,final HttpService httpService,SSLServerSocketFactory sf) throws IOException {
            this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
            this.serversocket = sf != null ? sf.createServerSocket(port) : new ServerSocket(port);
            this.httpService = httpService;
            this.sf=sf;
        }

        @Override
        public void run() {
            jxLog.logger.debug("Listening on port " + this.serversocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = this.serversocket.accept();
                    jxLog.logger.debug("Incoming connection from " + socket.getInetAddress());
                    HttpServerConnection conn = this.connFactory.createConnection(socket);
                    String ip=socket.getInetAddress().getHostAddress();
                    WorkerThread t = new WorkerThread(this.httpService, conn);
                    if(incomingconn.containsKey(ip))
                    {
                        //utils.P("run", "contains : "+ ip);
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
                    	jxTimer.DoAfter(300, param -> waitConn.remove(param), ip);
                    }
                    else
                    {
                        //utils.P("run", "dual : "+ ip);
                    	jxTimer timer=jxTimer.DoAfter(600, param -> incomingconn.remove(param), ip);
                    	incomingconn.put(ip, timer);
	                    // Start worker thread
	                    t.setDaemon(true);
	                    t.start();
                    }
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    jxLog.logger.error("I/O error initialising connection thread: "+ e.getMessage());
                    break;
                }
            }
        }
        
    }
    
    //key请求连接的ip
    private Map<String,WorkerThread> waitConn=new HashMap<String,WorkerThread>();
    
    public void AcceptConn(String IP)
    {
    	jxTimer timer=jxTimer.DoAfter(600, param -> incomingconn.remove(param), IP);
    	incomingconn.put(IP, timer);
    	WorkerThread t=waitConn.remove(IP);
    	if(t!=null)
    	{
            t.setDaemon(true);
            t.start();
    	}
    }

    class WorkerThread extends Thread {

        private final HttpService httpservice;
        private final HttpServerConnection conn;

        public WorkerThread(final HttpService httpservice,final HttpServerConnection conn) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
        }

        @Override
        public void run() {
            jxLog.logger.debug("New connection thread");
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                }
            } catch (ConnectionClosedException ex) {
                //utils.P("WorkerThread", "error:Client closed connection");
            } catch (IOException ex) {
                jxLog.logger.error("I/O error: " + ex.getMessage());
            } catch (HttpException ex) {
                jxLog.logger.error("Unrecoverable HTTP protocol violation: " + ex.getMessage());
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {}
            }
        }

    }

}