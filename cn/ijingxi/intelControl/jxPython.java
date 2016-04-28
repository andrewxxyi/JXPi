package cn.ijingxi.intelControl;

import cn.ijingxi.orm.jxJson;
import cn.ijingxi.util.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 jxJson json=jxJson.GetObjectNode("rs");
 json.AddValue("a","t123");
 int pid= jxPython.exec("test.py",json,p->{
 if(p!=null)
 jxLog.debug(((jxJson)p).TransToString());
 });
 --------test.py--------
 import json
 import sys

 decodejson=json.loads(sys.argv[1]);

 data_string = json.dumps(decodejson)
 print data_string

 *
 *
 * Created by andrew on 16-3-18.
 */
public class jxPython {

    private static class pythonInfo {
        Process process = null;
        BufferedOutputStream bstdin = null;
        Map<Integer, jxJson> respons = new HashMap<>();
    }


    /**
     * getResult主要用于python函数主动通知，如果都是命令响应则可以不需要getResult
     *
     * @param pyFile
     * @param param
     * @return
     */
    public static Object run(String pyFile, jxJson param,IDo eventDual) {
        jxLog.logger.debug("jxPython run: " + pyFile);
        pythonInfo pi = new pythonInfo();
        try {
            String p = null;
            String[] cmds = null;
            if (param != null) {
                p = param.TransToString();
                cmds = new String[]{"/usr/bin/python", pyFile, p};
            } else
                cmds = new String[]{"/usr/bin/python", pyFile};
            pi.process = Runtime.getRuntime().exec(cmds);
            BufferedReader bstdout = new BufferedReader
                    (new InputStreamReader(pi.process.getInputStream()));
            BufferedReader bstderr = new BufferedReader
                    (new InputStreamReader(pi.process.getErrorStream()));
            pi.bstdin = new BufferedOutputStream(pi.process.getOutputStream());

            final Process finalProcess = pi.process;
            jxTimer.asyncRun(p1 -> {
                String line = null;
                while ((line = bstdout.readLine()) != null) {
                    jxLog.logger.debug("jxPython read from std: " + line);
                    jxJson json = jxJson.JsonToObject(line);
                    if (json != null) {
                        String cmd = (String) json.getSubObjectValue("cmd");
                        int mid = Trans.TransToInteger(json.getSubObjectValue("msgid"));
                        if (cmd.compareTo("event") == 0){
                            if(eventDual!=null)
                                eventDual.Do(json);
                        }
                        else
                            synchronized (pi.respons) {
                                pi.respons.put(mid, json);
                                pi.notify();
                            }
                    }
                }
                jxLog.logger.debug("jxPython read bstdout over");
                bstdout.close();
                finalProcess.destroy();
            }, null);
            jxTimer.asyncRun(p2 -> {
                String line = null;
                while ((line = bstderr.readLine()) != null) {
                    jxLog.error("jxPython exec error-" + pyFile + ":" + line);
                }
                jxLog.logger.debug("jxPython read bstderr over");
                bstderr.close();
            }, null);
        } catch (Exception e) {
            jxLog.error(e);
            return null;
        }
        return pi;
    }

    private static Integer msgid = 0;

    public static jxJson getCmd(String funcName) throws Exception {
        jxJson json = jxJson.GetObjectNode("rs");
        json.AddValue("cmd", funcName);
        synchronized (msgid) {
            msgid++;
            if (msgid == Integer.MAX_VALUE)
                msgid = 1;
            json.AddValue("msgid", msgid);
        }
        return json;
    }

    /**
     * 执行某参数
     *
     * @param pi
     * @param param 必须先调用getCmd
     * @throws Exception
     */
    public static void exec(Object pi, jxJson param, IDo getResult) throws Exception {
        jxLog.logger.debug("jxPython exec: " + param.getSubObjectValue("cmd"));
        utils.Check(!(pi instanceof pythonInfo), "需送入run返回值");
        pythonInfo p = (pythonInfo) pi;
        String pa = param.TransToString() + "\n";

        jxJson rs = null;
        p.bstdin.write(pa.getBytes());
        p.bstdin.flush();
        jxLog.logger.debug("jxPython bstdin write: " + pa);
        try {
            synchronized (p.respons) {
                p.wait(3000);
                rs = p.respons.remove(param.GetSubValue("msgid"));
            }
        } catch (Exception e) {
            jxLog.error(e);
        }
        if (getResult != null)
            getResult.Do(rs);
        jxLog.logger.debug("jxPython exec over");
    }

    public static void close(Object pi) throws Exception {
        utils.Check(!(pi instanceof pythonInfo), "需送入run返回值");
        pythonInfo p = (pythonInfo) pi;
        jxJson param = getCmd("close");
        exec(pi, param, null);
        Thread.sleep(100);
        p.bstdin.close();
        p.process.destroy();
    }
}
