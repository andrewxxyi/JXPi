package cn.ijingxi.ServerCommon.httpServer;

import cn.ijingxi.app.ActiveRight;
import cn.ijingxi.app.Right;
import cn.ijingxi.orm.jxJson;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.jxTimer;
import cn.ijingxi.util.utils;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;

import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class jxHttpRes {

	private static Map<String, ResAttr> ResAttrTree = new HashMap<String, ResAttr>();

	public static void InitResClass(Object obj) throws Exception {
		Class<?> cls = obj.getClass();
		Method[] ms = cls.getDeclaredMethods();
		String classname = utils.GetClassName(cls);

		Annotation[] anns = cls.getAnnotations();
		for (Annotation ann : anns) {
			if (ann.annotationType() == ActiveRight.class) {
				ActiveRight ar = (ActiveRight) ann;
				Right.addDefaultPolicy(classname, null, ar.policy());
			}
		}

		try {
			Method mi = cls.getMethod("init");
			if (mi != null)
				mi.invoke(null);
		} catch (Exception e) {
			jxLog.error(e);
		}

		ResAttr attr = new ResAttr();
		ResAttrTree.put(classname, attr);
		attr.clsType = cls;
		attr.ResObj = obj;

		if (ms != null && ms.length > 0)
			for (Method m : ms) {
				String mn = m.getName();
				if (mn.equals("GET"))
					attr.getRes = m;
				else if (mn.equals("POST"))
					attr.postRes = m;
				else if (mn.equals("DELETE"))
					attr.delRes = m;
				else if (mn.equals("PUT"))
					attr.putRes = m;
				RES ann = m.getAnnotation(RES.class);
				if (ann != null) {
					if (ann.isActive()) {
						activeAttr ma = new activeAttr();
						ma.active = m;
						attr.actives.put(mn, ma);
					}
				}
				ActiveRight ar = m.getAnnotation(ActiveRight.class);
				if (ar != null)
					Right.addDefaultPolicy(classname, mn, ar.policy());
			}
	}

	public static void InitResClass(Class<?> cls) throws Exception {
		Object obj = cls.newInstance();
		InitResClass(obj);
	}

	//
	//URL的结构是：
//			uri/resname/active/ID
//			其中active可选，ID可选，参数应当以json方式进行传递
//			所有的active均形如：
//			jxHttpData active(String UUIDstr,jxJson param)
	public static jxHttpData dualHttpRequest(final HttpResponse response,
											 String sessionid, String method, String uri, jxJson param, String docRoot) {
		UUID pid = jxSession.getPeopleID(sessionid);
		if (sessionid != null && pid == null)
			return new jxHttpData(402, "会话超时，请重新登陆");

		jxLog.logger.debug(method + ":" + uri);
		if (param != null)
			jxLog.logger.debug("param:" + param.TransToString());

		String url = null, cp = null;
		String[] pss = uri.split("\\?");
		if (pss.length > 1) {
			url = pss[0];
			cp = pss[1];
		} else
			url = uri;
		try {
			Map<String, Object> fp = getParamFromURI(cp);
			if (sessionid != null)
				fp.put("SessionID", sessionid);
			else {
				sessionid = (String) fp.get(jxHttpServer.SessionHeaderName);
				if (sessionid != null) {
					pid = jxSession.getPeopleID(sessionid);
					if (pid == null)
						return new jxHttpData(402, "会话超时，请重新登陆");
					fp.put("SessionID", sessionid);
				}
			}
			fp.put("Response", response);
			fp.put("docRoot", docRoot);

			String[] ss = url.split("/");
			String resname = null, activename = null;
			if (ss.length < 2)
				//目前jxJson还无法处理斜线和反斜线
				//return new jxHttpData(404,"资源访问格式错误："+uri);
				return new jxHttpData(404, "资源访问格式错误");

			resname = ss[1];
			ResAttr res = ResAttrTree.get(resname);
			if (res == null)
				return new jxHttpData(404, "资源：" + ss[1] + " 未找到");
			//String uuid = null;
			if (ss.length > 2) {
				//uri/resname/active/ID
				activename = ss[2];
				if (ss.length == 4)
					fp.put("ID", ss[3]);
				activeAttr active = res.actives.get(activename);
				if (active != null) {
					//ID可省略
					if (Right.check(pid, resname, activename, param)) {
						//jxLog.log();
						return (jxHttpData) active.active.invoke(res.ResObj, fp, param);
					} else
						return new jxHttpData(403, "无权执行--" + resname + ":" + activename);
				} else if (ss.length == 4)
					return new jxHttpData(404, "资源动作未找到--" + resname + ":" + activename);
			}
			//uri/resname/ID，ID可省略
			if (ss.length == 3)
				fp.put("ID", ss[2]);
			if (method.equals("GET")) {
				activename = "GET";
				if (res.getRes == null)
					return new jxHttpData(404, "资源类型：" + ss[1] + " 未定义操作--GET");
				jxHttpData rs = null;
				if (Right.check(pid, resname, activename, param)) {
					rs = (jxHttpData) res.getRes.invoke(res.ResObj, fp, param);
				} else
					rs = new jxHttpData(403, "无权执行--" + resname + ":GET");
				return rs;
			} else if (method.equals("POST")) {
				activename = "POST";
				if (res.postRes == null)
					return new jxHttpData(404, "资源类型：" + ss[1] + " 未定义操作--POST");
				jxHttpData rs = null;
				if (Right.check(pid, resname, activename, param)) {
					//jxLog.log();
					rs = (jxHttpData) res.postRes.invoke(res.ResObj, fp, param);
				} else
					rs = new jxHttpData(403, "无权执行--" + resname + ":POST");
				return rs;
			} else if (method.equals("DELETE")) {
				activename = "DELETE";
				if (res.delRes == null)
					return new jxHttpData(404, "资源类型：" + ss[1] + " 未定义操作--DELETE");
				jxHttpData rs = null;
				if (Right.check(pid, resname, activename, param)) {
					//jxLog.log();
					rs = (jxHttpData) res.delRes.invoke(res.ResObj, fp, param);
				} else
					rs = new jxHttpData(403, "无权执行--" + resname + ":DELETE");
				return rs;
			} else if (method.equals("PUT")) {
				activename = "PUT";
				if (res.putRes == null)
					return new jxHttpData(404, "资源类型：" + ss[1] + " 未定义操作--PUT");
				jxHttpData rs = null;
				if (Right.check(pid, resname, activename, param)) {
					//jxLog.log();
					rs = (jxHttpData) res.putRes.invoke(res.ResObj, fp, param);
				} else
					rs = new jxHttpData(403, "无权执行--" + resname + ":PUT");
				return rs;
			}
		} catch (Exception e) {
			jxLog.error(e);
			return new jxHttpData(503, method + " " + uri + " " + e.getMessage());
		}
		return new jxHttpData(404, "uri未成功映射：" + uri);
	}

	private static Map<String, Object> getParamFromURI(String cp) throws Exception {
		Map<String, Object> map = new HashMap<>();
		if (cp == null || cp == "") return map;
		String[] sss = cp.split("&");
		for (String s : sss) {
			String[] ssss = s.split("=");
			map.put(ssss[0], URLDecoder.decode(ssss[1], "utf-8"));
		}
		return map;
	}

	/**
	 * 设置一个管道流，可以将输出直接写到http
	 * @param response
	 * @param otherOut 如果otherOut不为空，则写入的数据也可从该流获得，如同时写出到http和file
	 * @return
	 * @throws Exception
     */
	public static PipedOutputStream pushStreamToClient(HttpResponse response,
													   final OutputStream otherOut) throws Exception {
		PipedInputStream pin = null;
		PipedOutputStream pos = null;
		PipedInputStream pis = null;

		final PipedOutputStream out = new PipedOutputStream();
		if (otherOut != null) {
			//将otherOut连接进管道中
			//out写到pis，然后pis读出后分别写到otherOut和pos，pos写到pin,pin写到response
			pis = new PipedInputStream(out);
			pos = new PipedOutputStream();
			pin = new PipedInputStream(pos);
		} else
			pin = new PipedInputStream(out);

		InputStreamEntity myEntity = new InputStreamEntity(pin, -1);
		//myEntity.setChunked(true);
		//BufferedHttpEntity mbEntity=new BufferedHttpEntity(myEntity);
		response.setEntity(myEntity);

		if (otherOut != null) {
			final PipedInputStream finalPis = pis;
			final PipedOutputStream finalPos = pos;
			jxTimer.asyncRun(param -> {

				byte[] buf = new byte[1024];
				int start = 0;
				int count = finalPis.read(buf, start, 1024);
				int total = 0;
				jxLog.logger.debug("read count:" + count);
				while (count > 0) {
					otherOut.write(buf, 0, 1024);
					otherOut.flush();
					finalPos.write(buf, 0, 1024);
					finalPos.flush();
					total += count;
					jxLog.logger.debug("write to client");
					count = finalPis.read(buf, 0, 1024);
					jxLog.logger.debug("read count:" + count);
				}
				jxLog.logger.debug("read total:" + total);
			}, null);
		}

		return out;

	}

}


//
//URL的结构是：
//		uri/resname/active/ID
//		其中active可选，ID可选，参数应当以json方式进行传递
//		所有的active均形如：
//		jxHttpData active(String UUIDstr,jxJson param)
class ResAttr
{
	Class<?> clsType=null;
	Object ResObj=null;
	Map<String,activeAttr> actives=new HashMap<String,activeAttr>();
	Method getRes=null;
	Method postRes=null;
	Method putRes=null;
	Method delRes=null;
}
class activeAttr
{
	Method active=null;
}


