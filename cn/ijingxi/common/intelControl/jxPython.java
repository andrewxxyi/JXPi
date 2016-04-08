package cn.ijingxi.common.intelControl;

import cn.ijingxi.common.orm.jxJson;
import cn.ijingxi.common.util.IDo;
import cn.ijingxi.common.util.jxLog;
import cn.ijingxi.common.util.jxTimer;
import cn.ijingxi.common.util.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

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

    private static class pythonInfo{
        Process process = null;
        BufferedOutputStream bstdin=null;
    }

    public static Object run(String pyFile, jxJson param, IDo getResult) {
        jxLog.logger.debug("jxPython run: " + pyFile);
        pythonInfo pi=new pythonInfo();
        try {
            String p = null;
            String[] cmds = null;
            if (param != null) {
                p = param.TransToString();
                cmds = new String[]{"python", pyFile, p};
            } else
                cmds = new String[]{"python", pyFile};
            pi.process = Runtime.getRuntime().exec(cmds);
            BufferedInputStream bstdout = new BufferedInputStream(pi.process.getInputStream());
            BufferedInputStream bstderr = new BufferedInputStream(pi.process.getErrorStream());
            pi.bstdin=new BufferedOutputStream(pi.process.getOutputStream());

            final Process finalProcess = pi.process;
            jxTimer.asyncRun(p1 -> {
                byte buffer[] = new byte[1024];
                int num = 0;
                String readContent = null;
                while ((num = bstdout.read(buffer)) != -1) {
                    readContent = new String(buffer, 0, num);
                    if (readContent != null) {
                        jxLog.logger.debug("jxPython read from std: " + readContent);
                        String[] ss = utils.StringSplit(readContent, "\n");
                        for (String s : ss) {
                            jxJson json = jxJson.JsonToObject(s);
                            if (getResult != null)
                                getResult.Do(json);
                        }
                    }
                }
                jxLog.logger.debug("jxPython read bstdout over");
                bstdout.close();
                finalProcess.destroy();
            }, null);
            jxTimer.asyncRun(p2 -> {
                byte buffer[] = new byte[1024];
                int num = 0;
                String readContent = null;
                while ((num = bstderr.read(buffer)) != -1) {
                    readContent = new String(buffer, 0, num);
                    jxLog.error("jxPython exec error-" + pyFile + ":" + readContent);
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

    private static Integer msgid=0;
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
     * @param pi
     * @param param 必须先调用getCmd
     * @throws Exception
     */
    public static void exec(Object pi,jxJson param) throws Exception {
        jxLog.logger.debug("jxPython exec: " + param.getSubObjectValue("cmd"));
        utils.Check(!(pi instanceof pythonInfo),"需送入run返回值");
        pythonInfo p=(pythonInfo)pi;
        String pa = param.TransToString()+"\n";

        jxLog.logger.debug("jxPython bstdin write: " + pa);

        p.bstdin.write(pa.getBytes());
        p.bstdin.flush();
    }
    public static void close(Object pi) throws Exception {
        utils.Check(!(pi instanceof pythonInfo),"需送入run返回值");
        pythonInfo p=(pythonInfo)pi;
        jxJson param=getCmd("close");
        exec(pi,param);
        Thread.sleep(100);
        p.bstdin.close();
        p.process.destroy();
    }
}
