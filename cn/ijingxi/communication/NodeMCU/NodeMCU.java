package cn.ijingxi.communication.NodeMCU;

import cn.ijingxi.communication.jxTCPServer;
import cn.ijingxi.orm.jxJson;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.jxTimer;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrew on 16-2-3.
 */
public class NodeMCU {
    public static final int gpio_HIGH = 1;
    public static final int gpio_LOW = 0;
    public static final String Type_gpio = "g";
    public static final String Type_uart = "u";
    //逻辑联动
    public static final String Type_logic = "l";
    public static final int Mode_input = 0;
    public static final int Mode_output = 1;

    private static jxTCPServer server = null;
    private static Map<String, devInfo> allDev = new HashMap<>();

    public static jxJson send(String devID, jxJson json) throws Exception {

        jxLog.logger.debug(String.format("nodeMCU to %s send:%s", devID, json.TransToString()));

        devInfo dv = allDev.get(devID);
        if (dv != null) {
            dv.wr.write(json.TransToString());
            dv.wr.newLine();
            dv.wr.flush();
            dv.result = null;
            try {
                synchronized (dv) {
                    dv.wait(1000);
                    jxLog.logger.debug("发送成功");
                    return dv.result;
                }
            } catch (InterruptedException e) {
                throw new Exception("发送命令无响应：" + devID);
                //jxLog.error(e);
            }
        } else
            throw new Exception("设备不存在：" + devID);
    }

    public static void startServer(int port) throws Exception {
        server = new jxTCPServer();
        server.start(port, (callparam, in, out) -> {

            jxLog.logger.debug("新的连接进入...");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(out));
            jxJson json= jxJson.GetObjectNode("node");
            try {
                json.AddValue("c", "reg");
                wr.write(json.TransToString());
                wr.newLine();
                wr.flush();
            } catch (Exception e) {
                jxLog.error(e);
                return;
            }
            String line = null;
            try {
                line = br.readLine();
            } catch (IOException e) {
                jxLog.error(e);
                line = null;
            }
            while (line != null) {
                try {
                    jxLog.logger.debug("输入：" + line);
                    json = jxJson.JsonToObject(line);
                    if (json != null) {
                        String cmd = (String) json.getSubObjectValue("c");
                        String devid;
                        devInfo dv;
                        switch (cmd) {
                            case "reg":
                                devid = (String) json.getSubObjectValue("ID");
                                jxLog.logger.debug("register:" + devid);
                                dv =allDev.get(devid);
                                if(dv!=null)
                                    dv.wr.close();
                                dv = new devInfo();
                                dv.devID = devid;
                                dv.wr = wr;

                                allDev.put(devid, dv);
                                break;
                            case "r":
                                devid = (String) json.getSubObjectValue("ID");
                                jxLog.logger.debug("response:" + devid);
                                dv = allDev.get(devid);
                                if (dv != null)
                                    synchronized (dv) {
                                        dv.result = json;
                                        dv.notify();
                                    }
                                break;
                        }
                    }
                    line = br.readLine();

                } catch (Exception e) {
                    jxLog.error(e);
                    line = null;
                }
            }
            try {
                br.close();
            } catch (IOException e) {
                jxLog.error(e);
            }
        }, null);

        jxTimer.DoPeriod(30, param -> {
            try {
                Map<String, devInfo> tmp = new HashMap<>();
                for (Map.Entry<String, devInfo> e : allDev.entrySet())
                    tmp.put(e.getKey(),e.getValue());

                for (Map.Entry<String, devInfo> e : tmp.entrySet()) {
                    //目前的实现，如果有大量的前端设备，则会导致瞬间有大量的线程被创建出来，数量为jxTimer.asyncTaskNum
                    jxTimer.asyncRun(p1 -> {
                        //发送保活命令
                        jxLog.logger.debug("nodeMCU 发送保活命令");
                        jxJson json = jxJson.GetObjectNode("NodeMCU");
                        json.AddValue("c", "l");
                        String key = e.getKey();
                        try {
                            jxJson rs = send(key, json);
                            if (rs != null)
                                jxLog.logger.debug("nodeMCU live:" + rs.TransToString());
                        } catch (Exception e1) {
                            jxLog.error(e1);
                            allDev.remove(key);
                        }
                    }, null);
                }
            }
            catch (Exception e){
                jxLog.error(e);
            }
        },null);
    }

    private static class devInfo {
        String devID = null;
        BufferedWriter wr = null;
        jxJson result = null;
    }

    public static String getDevID() {
        for (Map.Entry<String, devInfo> e : allDev.entrySet())
            return e.getKey();
        return null;
    }

    public static jxJson getCmd_config(int pin, String type, int mode) throws Exception {
        jxJson json = jxJson.GetObjectNode("NodeMCU");
        json.AddValue("cmd", "c");
        json.AddValue("pin", pin);
        json.AddValue("type", type);
        json.AddValue("mode", mode);
        return json;
    }

    public static jxJson getCmd_set(int pin, String type, int active) throws Exception {
        jxJson json = jxJson.GetObjectNode("NodeMCU");
        json.AddValue("cmd", "s");
        json.AddValue("type", type);
        json.AddValue("pin", pin);
        json.AddValue("active", active);
        return json;
    }
    public static jxJson getCmd_set_gpio(int pin, int active) throws Exception {
        return getCmd_set(pin,Type_gpio,active);
    }

    public static jxJson getCmd_get(int pin, String type) throws Exception {
        jxJson json = jxJson.GetObjectNode("NodeMCU");
        json.AddValue("cmd", "g");
        json.AddValue("type", type);
        json.AddValue("pin", pin);
        return json;
    }
    public static jxJson getCmd_get_gpio(int pin) throws Exception {
        return getCmd_get(pin,Type_gpio);
    }

}
