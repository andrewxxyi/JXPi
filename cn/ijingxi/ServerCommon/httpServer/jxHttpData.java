package cn.ijingxi.ServerCommon.httpServer;

import cn.ijingxi.common.orm.jxJson;
import cn.ijingxi.common.orm.jxORMobj;
import cn.ijingxi.common.util.Trans;
import cn.ijingxi.common.util.jxLog;

import java.util.Queue;

public class jxHttpData {

	private jxJson node=null;
	
	public jxHttpData(int resultCode,String msg)
	{
		try { 
			node=jxJson.GetObjectNode("jxHttpData");
			jxJson sub = jxJson.GetObjectNode("meta");
			node.AddSubObjNode(sub);
			sub.AddValue("rc", resultCode);
			sub.AddValue("msg", msg);
			
		} catch (Exception e) {
			jxLog.error(e);
		}
		
	}
	public jxHttpData(String json)
	{
		node=jxJson.JsonToObject(json);
	}
	public int getResultCode()
	{
		jxJson sub;
		try {
			sub = node.GetSubObject("meta");
	        //utils.P("getResultCode", sub.GetSubValue("rc").toString());
			int rc=Trans.TransToInteger(sub.GetSubValue("rc"));
			if(rc!=0)
				return rc;
		} catch (Exception e) {
			jxLog.error(e);
		}
		return 501;
	}
	public String getMsg()
	{
		jxJson sub;
		try {
			sub = node.GetSubObject("meta");
			return (String) sub.GetSubValue("msg");
		} catch (Exception e) {
			jxLog.error(e);
		}
		return null;
	}

	public static jxHttpData getHttpData(jxORMobj obj) throws Exception
	{
		jxHttpData rs=null;
		if(obj!=null)
		{
			rs=new jxHttpData(200,"处理完毕");
			rs.addObj(obj);
		}
		else
			rs=new jxHttpData(404,"未找到信息");
		return rs;
	}
	public static jxHttpData getHttpData(Queue<jxORMobj> ol) throws Exception
	{
		jxHttpData rs=null;
		if(ol!=null)
		{
			rs=new jxHttpData(200,"处理完毕");
			rs.addObjList(ol);
		}
		else
			rs=new jxHttpData(404,"未找到信息");
		return rs;
	}

	public static boolean TypeIsObj(jxHttpData data){
		if(data!=null&&data.node!=null){
			try {
				jxJson sub = data.node.GetSubObject("meta");
				if(sub!=null){
					jxJson type = data.node.GetSubObject("type");
					if(type!=null)
						return "obj".compareTo((String) type.getValue())==0;
				}
			} catch (Exception e) {
				jxLog.error(e);
			}
		}
		return false;
	}
	public static boolean TypeIsArr(jxHttpData data){
		if(data!=null&&data.node!=null){
			try {
				jxJson sub = data.node.GetSubObject("meta");
				if(sub!=null){
					jxJson type = data.node.GetSubObject("type");
					if(type!=null)
						return "arr".compareTo((String) type.getValue())==0;
				}
			} catch (Exception e) {
				jxLog.error(e);
			}
		}
		return false;
	}
	public static boolean TypeIsValue(jxHttpData data){
		if(data!=null&&data.node!=null){
			try {
				jxJson sub = data.node.GetSubObject("meta");
				if(sub!=null){
					jxJson type = data.node.GetSubObject("type");
					if(type!=null)
						return "val".compareTo((String) type.getValue())==0;
				}
			} catch (Exception e) {
				jxLog.error(e);
			}
		}
		return false;
	}



	/**
	 * 如果data节点尚未设置，则创建，用于非对象情况下的返回结果设置
	 * @return
	 */
	public jxJson getDataNode()
	{
		try {
			jxJson dn = node.GetSubObject("data");
			if(dn==null)
			{
				dn = jxJson.GetObjectNode("data");
				node.AddSubObjNode(dn);
			}
			return dn;
		} catch (Exception e) {
			jxLog.error(e);
		}
		return null;
	}
	
	/**
	 * 不能反向转回来
	 * @param obj
	 */
	public void addObj(jxORMobj obj)
	{
		try {
			jxJson dn = node.GetSubObject("data");
			if(dn==null)
			{
				dn = jxJson.GetObjectNode("data");
				node.AddSubObjNode(dn);
				jxJson sub = node.GetSubObject("meta");
				sub.AddValue("type", "obj");
			}
			if(obj!=null)
			{
				jxJson sn=obj.ToJSON();
				for(jxJson sub:sn)
					dn.AddSubObjNode(sub);
			}
		} catch (Exception e) {
			jxLog.error(e);
		}		
	}
	public void addJson(jxJson json)
	{
		try {
			jxJson dn = node.GetSubObject("data");
			if(dn==null)
			{
				dn = jxJson.GetObjectNode("data");
				node.AddSubObjNode(dn);
				jxJson sub = node.GetSubObject("meta");
				sub.AddValue("type", "obj");
			}
			if(json!=null)
				dn.AddSubObjNode(json);
		} catch (Exception e) {
			jxLog.error(e);
		}
	}
	public void addValue(String Purpose,Object obj)
	{
		try {
			jxJson dn = node.GetSubObject("data");
			if(dn==null)
			{
				dn = jxJson.GetObjectNode("data");
				node.AddSubObjNode(dn);
				jxJson sub = node.GetSubObject("meta");
				sub.AddValue("type", "val");
			}
			dn.setSubObjectValue(Purpose, obj);
		} catch (Exception e) {
			jxLog.error(e);
		}		
	}
	public void setResult(boolean result)
	{
		addValue("Result",result);
	}
	/**
	 * 只能是同一类对象，不能反向转回来
	 * @param list
	 */
	public jxJson addObjList(Queue<jxORMobj> list)
	{
		jxJson ol=null;
		//utils.P("addObjList before",  getString());
		try {
			jxJson dn = node.GetSubObject("data");
			if(dn==null)
			{
				dn = jxJson.GetObjectNode("data");
				node.AddSubObjNode(dn);
				jxJson sub = node.GetSubObject("meta");
				sub.AddValue("type", "arr");
			}
			ol=dn.GetSubObject("oList");
			if(ol==null)
			{
				ol = jxJson.GetArrayNode("oList");
				dn.AddSubObjNode(ol);
			}
			if(list!=null)
				for(jxORMobj obj : list)
				{
					//utils.P("addObjList for", obj.ToJSON().TransToString());
					ol.AddArrayElement(obj.ToJSON());
				}
		} catch (Exception e) {
			jxLog.error(e);
		}
		//utils.P("addObjList after",  getString());
		return ol;
	}

	/**
	 * 向对象列表中添加对象
	 * @param Obj
	 * @return 返回该对象所对应的jxJson
	 */
	public jxJson addObjInList(jxORMobj Obj)
	{
		if(Obj==null)return null;
		jxJson js=null;
		//utils.P("addObjInList before",  getString());
		try {
			jxJson dn = node.GetSubObject("data");
			if(dn==null)
			{
				dn = jxJson.GetObjectNode("data");
				node.AddSubObjNode(dn);
				jxJson sub = node.GetSubObject("meta");
				sub.AddValue("type", "arr");
			}
			jxJson ol=dn.GetSubObject("oList");
			if(ol==null)
			{
				ol = jxJson.GetArrayNode("oList");
				dn.AddSubObjNode(ol);
			}
			js=Obj.ToJSON();
			ol.AddArrayElement(js);
		} catch (Exception e) {
			jxLog.error(e);
		}
		//utils.P("addObjInList after", getString());
		return js;
	}
	public jxJson addObjInList(jxJson sub)
	{
		if(sub==null)return null;
		jxJson js=null;
		//utils.P("addObjInList before",  getString());
		try {
			jxJson dn = node.GetSubObject("data");
			if(dn==null)
			{
				dn = jxJson.GetObjectNode("data");
				node.AddSubObjNode(dn);
				jxJson m = node.GetSubObject("meta");
				m.AddValue("type", "arr");
			}
			jxJson ol=dn.GetSubObject("oList");
			if(ol==null)
			{
				ol = jxJson.GetArrayNode("oList");
				dn.AddSubObjNode(ol);
			}
			ol.AddArrayElement(sub);
		} catch (Exception e) {
			jxLog.error(e);
		}
		//utils.P("addObjInList after", getString());
		return js;
	}


	public String getString()
	{
		return node.TransToString();
	}
	
	
	
}
