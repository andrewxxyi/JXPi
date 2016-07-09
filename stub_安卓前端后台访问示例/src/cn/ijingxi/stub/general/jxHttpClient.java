package cn.ijingxi.stub.general;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Created by andrew on 15-11-18.
 */
public class jxHttpClient {

    public static final String SessionHeaderName="jxSessionID";
    private HttpClient client=null;
    private String URL=null;
    private String getURL(String path) {
        if (query == null)
            return URL+path;
        else
            return URL+path + "?" + query;
    }
    private String query=null;
    public void addQueryString(String name,String value) {
        String str = name + "=" + value;
        if (query == null)
            query = str;
        else
            query += "&" + str;
    }

    public jxHttpClient(String host){
        URL="http://"+host;
        client = new DefaultHttpClient();
    }
    public jxHttpClient(String host, int port){
        URL="http://"+host+":"+port;
        client = new DefaultHttpClient();
    }

    private String sessionID=null;
    public String getSessionID(){return sessionID;}
    public void clearSessionID(){ sessionID=null;}
    private Header sessionHeader=null;
    private void setSessionID(final String sessionID) {
        this.sessionID = sessionID;
        sessionHeader = new Header() {
            @Override
            public String getName() {
                return SessionHeaderName;
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


    public jxJson post(String path, jxJson jsonParam) throws Exception {
        HttpPost method = new HttpPost(getURL(path));
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
        return dualReturn(result);
    }
    public jxJson login(jxJson jsonParam) throws Exception {
        HttpPost method = new HttpPost(getURL("/Person/login/"));
        StringEntity entity = new StringEntity(jsonParam.TransToString(), "utf-8");
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        method.setEntity(entity);
        HttpResponse result = client.execute(method);
        return dualReturn(result);
    }

    public jxJson get(String path) throws Exception {
        HttpGet method = new HttpGet(getURL(path));
        if(sessionHeader!=null)
            method.setHeader(sessionHeader);

        HttpResponse result = client.execute(method);
        /**读取服务器返回过来的json字符串数据**/
        return dualReturn(EntityUtils.toString(result.getEntity()));
    }

    public jxJson put(String path,jxJson jsonParam) throws Exception {
        HttpPut method = new HttpPut(getURL(path));
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
        HttpDelete method = new HttpDelete(getURL(path));
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
    public static IDo dualNoResult=null;
    /**
     * Do(Object param)
     *  param:错误信息
     */
    public static IDo dualResultFormateError=null;
    /**
     * Do(Object param1,Object param2)
     *  param1:resultCode;param2:msg
     */
    public static IDo dualError=null;

    private jxJson dualReturn(HttpResponse response) throws Exception {
        Header[] hs = response.getHeaders(SessionHeaderName);
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
                jxJson meta=json.GetSubObject("meta");
                if(meta!=null){
                    jxJson rc=meta.GetSubObject("rc");
                    //utils.P("rc", rc.getValue().toString());
                    if(rc!=null&&((Integer)rc.getValue()==200)){
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
                        dualError.Do(rc.getValue()+":"+meta.GetSubObject("msg").getValue());
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
