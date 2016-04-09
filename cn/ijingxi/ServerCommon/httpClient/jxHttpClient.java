package cn.ijingxi.ServerCommon.httpClient;

import cn.ijingxi.ServerCommon.httpServer.jxHttpServer;
import cn.ijingxi.common.orm.jxJson;
import cn.ijingxi.common.util.IDo;
import cn.ijingxi.common.util.IDo2;
import cn.ijingxi.common.util.jxLog;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * 基于REST请求：
 * 一般请求格式：uri/res/{active}/{ID}
 * get：读取
 * post：创建或执行active
 * put：修改
 * delete：删除
 *
 * post可以带参，以json方式发送
 *
 * get、put、delete不需要active：
 * uri/res/ID
 * put带参，以json方式发送
 *
 * 如果jxHttpServer以start(true)启动，则可以使用ssl
 *
 * Created by andrew on 15-11-18.
 */
public class jxHttpClient {

    private CloseableHttpClient client=null;
    private String url=null;

    public jxHttpClient(String host){
        url="http://"+host;
        client = HttpClients.createDefault();
    }
    public  jxHttpClient(String host,int port){
        url="http://"+host+":"+port;
        client = HttpClients.createDefault();
    }
    public  jxHttpClient(String host,int port,boolean useSSL) throws Exception {
        url="https://"+host+":"+port;

        X509TrustManager xtm = new X509TrustManager(){   //创建TrustManager
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            public X509Certificate[] getAcceptedIssuers() {
                return null;   //return new java.security.cert.X509Certificate[0];
            }
        };
        //TLS1.0与SSL3.0基本上没有太大的差别，可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext
        SSLContext sslContext = SSLContext.getInstance("SSLv3");

        //使用TrustManager来初始化该上下文，TrustManager只是被SSL的Socket所使用
        sslContext.init(null, new TrustManager[]{xtm}, null);
        /*
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            //信任所有
            public boolean isTrusted(X509Certificate[] chain,String authType) throws CertificateException {
                return true;
            }
        }).build();
        */
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
        client = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        /*
        X509TrustManager xtm = new X509TrustManager(){   //创建TrustManager
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            public X509Certificate[] getAcceptedIssuers() {
                return null;   //return new java.security.cert.X509Certificate[0];
            }
        };
        //TLS1.0与SSL3.0基本上没有太大的差别，可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext
        SSLContext ctx = SSLContext.getInstance("TLS");

        //使用TrustManager来初始化该上下文，TrustManager只是被SSL的Socket所使用
        ctx.init(null, new TrustManager[]{xtm}, null);

        //创建SSLSocketFactory
        SSLSocketFactory socketFactory = new SSLSocketFactory(ctx);

        //通过SchemeRegistry将SSLSocketFactory注册到我们的HttpClient上
        client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));

        */


        /*
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(new File(".keystore"), "secret".toCharArray(),
                new TrustSelfSignedStrategy())
                .build();
        // Allow TLSv1 protocol only
        String[] pa=String[] { "TLSv1" };
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, pa ,null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        client = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
        */
    }

    private Header sessionHeader=null;
    private void setSessionID(final String sessionID){
        sessionHeader=new Header() {
            @Override
            public String getName() {
                return jxHttpServer.SessionHeaderName;
            }
            @Override
            public String getValue() {
                return sessionID;
            }
            @Override
            public HeaderElement[] getElements() throws ParseException {
                return new HeaderElement[0];
            }
        };
    }

    /**
     * post有两个用途，一是一般性的访问资源处理动作，一个是创建资源
     * @param path
     * @param jsonParam
     * @return
     * @throws Exception
     */
    public jxJson post(String path,jxJson jsonParam) throws Exception {
        HttpPost method = new HttpPost(url+path);
        if (null != jsonParam) {
            //解决中文乱码问题
            StringEntity entity = new StringEntity(jsonParam.TransToString(), "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            method.setEntity(entity);
            if(sessionHeader!=null)
                method.setHeader(sessionHeader);

        }
        HttpResponse result = client.execute(method);
        /**读取服务器返回过来的json字符串数据**/
        return dualReturn(EntityUtils.toString(result.getEntity()));
    }
    public jxJson login(String name,String pass) throws Exception {
        HttpPost method = new HttpPost(url+"/Person/login/");
        jxJson json=jxJson.GetObjectNode("param");
        json.setSubObjectValue("Name",name);
        json.setSubObjectValue("Passwd",pass);
        StringEntity entity = new StringEntity(json.TransToString(), "utf-8");
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        method.setEntity(entity);
        HttpResponse result = client.execute(method);
        return dualReturn(result);
    }


    /**
     * get是不带参数的
     * @param path
     * @return
     * @throws Exception
     */
    public jxJson get(String path) throws Exception {
        HttpGet method = new HttpGet(url+path);
        if(sessionHeader!=null)
            method.setHeader(sessionHeader);

        HttpResponse result = client.execute(method);
        /**读取服务器返回过来的json字符串数据**/
        return dualReturn(EntityUtils.toString(result.getEntity()));
    }

    /**
     * 对于非jx（以jxHttpServer启动）的web服务，get可以在uri中传递参数
     * uri？k1=v1&k2=v2&k3=v3&...
     * @param path
     * @param params
     * @return
     * @throws Exception
     */
    public jxJson get(String path,Map<String,Object> params) throws Exception {
        String urls=url+path;
        if(params!=null&&params.size()>0){
            urls+="?";
            int i=1;
            for(Map.Entry<String, Object> entry:params.entrySet()){
                urls+=entry.getKey()+"="+entry.getValue();
                if(i<params.size())
                    urls+="&";
                i++;
            }
        }
        HttpGet method = new HttpGet(urls);
        if(sessionHeader!=null)
            method.setHeader(sessionHeader);

        HttpResponse result = client.execute(method);
        /**读取服务器返回过来的json字符串数据**/
        return dualReturn(EntityUtils.toString(result.getEntity()));
    }

    public jxJson put(String path,jxJson jsonParam) throws Exception {
        HttpPut method = new HttpPut(url+path);
        if (null != jsonParam) {
            //解决中文乱码问题
            StringEntity entity = new StringEntity(jsonParam.TransToString(), "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            method.setEntity(entity);
            if(sessionHeader!=null)
                method.setHeader(sessionHeader);
        }
        HttpResponse result = client.execute(method);
        /**读取服务器返回过来的json字符串数据**/
        return dualReturn(EntityUtils.toString(result.getEntity()));
    }

    public jxJson delete(String path) throws Exception {
        HttpDelete method = new HttpDelete(url+path);
        if(sessionHeader!=null)
            method.setHeader(sessionHeader);

        HttpResponse result = client.execute(method);
        /**读取服务器返回过来的json字符串数据**/
        return dualReturn(EntityUtils.toString(result.getEntity()));
    }


    /**
     * Do(Object param)
     *  param:错误信息
     */
    public IDo dualNoResult=null;
    /**
     * Do(Object param)
     *  param:错误信息
     */
    public IDo dualResultFormateError=null;
    /**
     * Do(Object param1,Object param2)
     *  param1:resultCode;param2:msg
     */
    public IDo2 dualError=null;

    private jxJson dualReturn(HttpResponse response) throws Exception {
        Header[] hs = response.getHeaders(jxHttpServer.SessionHeaderName);
        if(hs.length==1){
            setSessionID(hs[0].getValue());
        }
        return dualReturn(EntityUtils.toString(response.getEntity()));
    }
    private jxJson dualReturn(String str) throws Exception {
        //utils.P("dualReturn",str);
        if(str!=null&&str!=""){
            jxJson json=jxJson.JsonToObject(str);
            if(json!=null){
                //utils.P("json",json.TransToStringWithName());
                jxJson meta=json.GetSubObject("meta");
                if(meta!=null){
                    jxLog.logger.debug("meta:"+meta.TransToStringWithName());
                    jxJson rc=meta.GetSubObject("rc");
                    //utils.P("rc", rc.getValue().toString());
                    if(rc!=null&&((String)rc.getValue()).compareTo("200")==0){
                        //utils.P("rc Type", rc.getValue().getClass().getName());
                        jxJson ty=meta.GetSubObject("type");
                        if(ty!=null)
                            if(((String)ty.getValue()).compareTo("val")==0||((String)ty.getValue()).compareTo("obj")==0)
                                return json.GetSubObject("data");
                            else if(((String)ty.getValue()).compareTo("arr")==0){
                                jxJson data=json.GetSubObject("data");
                                if(data!=null)
                                    return data.GetSubObject("oList");
                            }
                            else if(dualResultFormateError!=null)
                                dualResultFormateError.Do("无法识别的类型:"+ty.getValue());
                    }
                    else if(dualError!=null)
                        dualError.Do(rc.getValue(),meta.GetSubObject("msg").getValue());
                }
                else if(dualResultFormateError!=null)
                    dualResultFormateError.Do("格式错误");
            }
            else if(dualNoResult!=null)
                dualNoResult.Do("没有返回结果");
        }
        else if(dualNoResult!=null)
            dualNoResult.Do("没有返回结果");
        return null;
    }

    public static boolean judgeResult(jxJson json){
        if(json!=null){
            jxJson sub= null;
            try {
                sub = json.GetSubObject("Result");
                if(sub!=null)
                    return "true".compareTo((String) sub.getValue())==0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
