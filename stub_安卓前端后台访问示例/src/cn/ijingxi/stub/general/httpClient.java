package cn.ijingxi.stub.general;


/**
 * Created by andrew on 15-12-15.
 */
public class httpClient {
    private static final int ServerPort=10000;

    private static jxHttpClient client=null;
    static String serverIP_I=null;
    static String serverIP_E=null;
    static IDo tip=null;

    static boolean inExt=false;

    private static boolean setChannel(int channelID,boolean open){

        jxJson json=jxJson.GetObjectNode("param");
        try {
            json.setSubObjectValue("Channel",channelID);
            json.setSubObjectValue("Cmd",open?"Open":"Close");
            jxJson rs = send("/Control/setChannel", json);
            if(jxHttpClient.judgeResult(rs))
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String readInput(){

        try {
            jxJson rs = send("/Control/readInput", null);
            return rs.TransToStringWithName();
            //return (String) rs.getSubObjectValue("msg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void delayClose(int channelID){
        jxTimer.DoAfter(60,new IDo() {
            @Override
            public void Do(Object o) throws Exception {
                closeChannel((int)o);
            }
        },channelID);
    }
    private static void delayOpen(int channelID){
        jxTimer.DoAfter(60,new IDo() {
            @Override
            public void Do(Object o) throws Exception {
                openChannel((int)o);
            }
        },channelID);
    }

    public static boolean openChannel(int channelID){
        return setChannel(channelID,true);
    }
    public static boolean closeChannel(int channelID){
        return setChannel(channelID,false);
    }

    public static boolean openChannelWithReset(int channelID){
        delayClose(channelID);
        return openChannel(channelID);
    }
    public static boolean closeChannelWithReset(int channelID){
        delayOpen(channelID);
        return closeChannel(channelID);
    }


    public static boolean test(){

        jxJson json= jxJson.GetObjectNode("param");
        try {
            json.setSubObjectValue("Channel",0);
            json.setSubObjectValue("Cmd","Close");
            jxJson rs = send("/Control/test", json);
            if(jxHttpClient.judgeResult(rs))
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    static jxJson send(String url,jxJson json) throws Exception {
        if(serverIP_I==null){
            if(tip!=null)
                tip.Do("服务器地址未设置，请先设置服务器地址");
            return null;
        }
        if(inExt){
            //是外网则
            client=new jxHttpClient(serverIP_E,ServerPort);
            try {
                return client.post(url,json);
            } catch (Exception e1) {
                //可能突然回来了，则下次即可恢复
                inExt=false;
                tip.Do("无法连接外网服务器");
            }
        }
        else {
            //默认先尝试内网
            client=new jxHttpClient(serverIP_I,ServerPort);
            try {
                return client.post(url,json);
            } catch (Exception e) {
                //再尝试外网
                client=new jxHttpClient(serverIP_E,ServerPort);
                try {
                    jxJson rs = client.post(url, json);
                    //下次就旁路内网
                    inExt=true;
                    return rs;
                } catch (Exception e1) {
                    tip.Do("无法连接服务器");
                }
            }
        }
        return null;
    }

    static jxJson login(jxJson json) throws Exception {
        if(serverIP_I==null){
            if(tip!=null)
                tip.Do("服务器地址未设置，请先设置服务器地址");
            return null;
        }
        if(inExt){
            //是外网则
            client=new jxHttpClient(serverIP_E,ServerPort);
            try {
                return client.login(json);
            } catch (Exception e1) {
                //可能突然回来了，则下次即可恢复
                inExt=false;
                tip.Do("无法连接外网服务器");
            }
        }
        else {
            //默认先尝试内网
            client=new jxHttpClient(serverIP_I,ServerPort);
            try {
                return client.login(json);
            } catch (Exception e) {
                //再尝试外网
                client=new jxHttpClient(serverIP_E,ServerPort);
                try {
                    jxJson rs = client.login(json);
                    //下次就旁路内网
                    inExt=true;
                    return rs;
                } catch (Exception e1) {
                    tip.Do("无法连接服务器");
                }
            }
        }
        return null;
    }

}
