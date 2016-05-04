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
        jxJson param=null;
    }

    private static Map<String, IDo> eventDual = new HashMap<>();
    public static void addEventDual(String event,IDo dual) {
        eventDual.put(event, dual);
    }

    private static Object jxPythonObject = null;
    public static Object getPythonObject(){return jxPythonObject;}
    static {
        jxPythonObject = jxPython.run("./conf/piControl.py", null);
    }

    /**
     * getResult主要用于python函数主动通知，如果都是命令响应则可以不需要getResult
     *
     * @param pyFile
     * @param param
     * @return
     */
    public static Object run(String pyFile, jxJson param) {
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
                            IDo dual = eventDual.get(json.getSubObjectValue("resFor"));
                            if(dual!=null)
                                dual.Do(json.GetSubObject("data"));
                        }
                        else
                            synchronized (pi.respons) {
                                pi.respons.put(mid, json);
                                pi.respons.notify();
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
                    jxLog.logger.error("jxPython exec error-" + pyFile + ":" + line);
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

    public static void newCmd(Object pi, String funcName) throws Exception {
        utils.Check(!(pi instanceof pythonInfo), "需送入run返回值");
        pythonInfo p = (pythonInfo) pi;
        p.param = jxJson.GetObjectNode("rs");
        p.param.AddValue("cmd", funcName);
        synchronized (msgid) {
            msgid++;
            if (msgid == Integer.MAX_VALUE)
                msgid = 1;
            p.param.AddValue("msgid", msgid);
        }
    }
    public static void setParam(Object pi, String paramName,Object value) throws Exception {
        utils.Check(!(pi instanceof pythonInfo), "需送入run返回值");
        pythonInfo p = (pythonInfo) pi;
        p.param.AddValue(paramName, value);
    }

    /**
     * 执行某某命令，返回真，则getResult返回了值
     *
     * @param pi
     * @throws Exception
     */
    public static boolean exec(Object pi, IDo getResult) throws Exception {
        utils.Check(!(pi instanceof pythonInfo), "需送入run返回值");
        pythonInfo p = (pythonInfo) pi;
        jxLog.logger.debug("jxPython exec: " + p.param.getSubObjectValue("cmd"));
        String pa = p.param.TransToString() + "\n";

        jxJson rs = null;
        p.bstdin.write(pa.getBytes());
        p.bstdin.flush();
        jxLog.logger.debug("jxPython bstdin write: " + pa);
        if (getResult == null)
            return true;
        try {
            synchronized (p.respons) {
                p.respons.wait(3000);
                rs = p.respons.remove(p.param.GetSubValue("msgid"));
            }
            Object s = null;
            if (rs != null) {
                jxLog.logger.debug("get result:" + rs.TransToString());
                s = rs.getSubObjectValue("data");
            }
            getResult.Do(s);
            return true;
        } catch (Exception e) {
            jxLog.error(e);
            return false;
        }
    }

    public static void close(Object pi) throws Exception {
        utils.Check(!(pi instanceof pythonInfo), "需送入run返回值");
        pythonInfo p = (pythonInfo) pi;
        p.param = jxJson.GetObjectNode("rs");
        p.param.AddValue("cmd", "close");
        exec(pi, null);
        Thread.sleep(100);
        p.bstdin.close();
        p.process.destroy();
    }
}
