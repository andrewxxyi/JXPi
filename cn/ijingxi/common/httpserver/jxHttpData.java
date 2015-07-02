package cn.ijingxi.common.httpserver;

import java.util.Queue;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.util.Trans;
import cn.ijingxi.common.util.utils;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	        utils.P("getResultCode", sub.GetSubValue("rc").toString());
			int rc=Trans.TransToInteger(sub.GetSubValue("rc"));
			if(rc!=0)
				return rc;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	/**
	 * 只能是同一类对象，不能反向转回来
	 * @param list
	 */
	public jxJson addObjList(Queue<jxORMobj> list)
	{
		jxJson ol=null;
        utils.P("addObjList before",  getString());
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
		        utils.P("addObjList for", obj.ToJSON().TransToString());
				ol.AddArrayElement(obj.ToJSON());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
        utils.P("addObjList after",  getString());
        return ol;
	}

	public static jxJson getObjListNode()
	{
		return jxJson.GetArrayNode("oList");
	}
	public void addObjListNode(jxJson ol) throws Exception
	{
		jxJson dn = node.GetSubObject("data");
		if(dn==null)
		{
			dn = jxJson.GetObjectNode("data");
			node.AddSubObjNode(dn);
			jxJson sub = node.GetSubObject("meta");
			sub.AddValue("type", "arr");
		}
		dn.AddSubObjNode(ol);
	}
	
	public String getString()
	{
		return node.TransToString();
	}
	
	
	
}
