package cn.ijingxi.common.httpserver;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import cn.ijingxi.common.orm.jxJson;
import cn.ijingxi.common.util.utils;


public class jxHttpRes {

	private static Map<String,ResAttr> ResAttrTree=new HashMap<String,ResAttr>();
	
	public static void InitResClass(Class<?> cls) throws Exception
	{
		Method[] ms = cls.getDeclaredMethods();
		String classname=utils.GetClassName(cls);
		ResAttr attr=new ResAttr();
		ResAttrTree.put(classname, attr);
		attr.clsType=cls;
		attr.ResObj=cls.newInstance();
		
		if(ms!=null&&ms.length>0)
		for(Method m:ms)
		{
			String mn=m.getName();
			switch(mn)
			{
			case "GET":
				attr.getRes=m;
				break;
			case "POST":
				//insert
				attr.postRes=m;
				break;
			case "DELETE":
				attr.delRes=m;
				break;
			case "PUT":
				//update
				attr.putRes=m;
				break;
			}
			RES ann = m.getAnnotation(RES.class);
			if(ann!=null)
			{
				if(ann.isActive())
				{
					activeAttr ma=new activeAttr();
					ma.active=m;
					attr.actives.put(mn, ma);
				}
			}
		}
		
	}
	//
	//URL的结构是：
//			uri/resname/active/ID
//			其中active可选，ID可选，参数应当以json方式进行传递
//			所有的active均形如：
//			jxHttpData active(String UUIDstr,jxJson param)
	public static jxHttpData dualHttpRequest(String method,String uri,jxJson param)
	{
        utils.P("dualHttpRequest 1", method + ":" + uri);
        if(param!=null)
        	utils.P("dualHttpRequest 2", "param:" + param.TransToString());
        
		String[] ss=uri.split("/");
		if(ss.length<2)
			return new jxHttpData(404,"资源访问格式错误："+uri);
		ResAttr res=ResAttrTree.get(ss[1]);
		if(res==null)
			return new jxHttpData(404,"资源："+ss[1]+" 未找到");
		try
		{
			String uuid=null;
	        utils.P("dualHttpRequest 3",  "ss.length:" + ss.length);
			if(ss.length>2)
			{
				activeAttr active=res.actives.get(ss[2]);
				if(active!=null)
				{
					if(ss.length==4)
						uuid=ss[3];	
			        utils.P("dualHttpRequest 4",  "call active:"+ss[2]);
					return (jxHttpData) active.active.invoke(res.ResObj,uuid, param);
				}
				else if(ss.length==4)
					return new jxHttpData(404,"资源动作未找到--"+ss[1]+":"+ss[2]);
			}
			if(ss.length==3)
				uuid=ss[2];
			switch(method)
			{
			case "GET":
		        utils.P("dualHttpRequest 5",  "call Get");
				if(res.getRes==null)
					return new jxHttpData(404,"资源类型："+ss[1]+" 未定义操作--GET");
				jxHttpData rs=(jxHttpData) res.getRes.invoke(res.ResObj, uuid, param);
		        utils.P("dualHttpRequest 6",  rs.getString());
		        return rs;
			case "POST":
		        utils.P("dualHttpRequest 7",  "call POST");
				if(res.postRes==null)
					return new jxHttpData(404,"资源类型："+ss[1]+" 未定义操作--POST");
				return (jxHttpData) res.postRes.invoke(res.ResObj, uuid, param);
			case "DELETE":
		        utils.P("dualHttpRequest 8",  "call DELETE");
				if(res.delRes==null)
					return new jxHttpData(404,"资源类型："+ss[1]+" 未定义操作--DELETE");
				return (jxHttpData) res.delRes.invoke(res.ResObj, uuid, param);
			case "PUT":
		        utils.P("dualHttpRequest 9",  "call PUT");
				if(res.putRes==null)
					return new jxHttpData(404,"资源类型："+ss[1]+" 未定义操作--PUT");
				return (jxHttpData) res.putRes.invoke(res.ResObj, uuid, param);
			}
		}
		catch(Exception e){ 
			utils.LogException("dualHttpRequest 10", e);
			return new jxHttpData(503,method +" "+uri+" "+e.getMessage());
			}
		return new jxHttpData(404,"uri未成功映射："+uri);		
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


