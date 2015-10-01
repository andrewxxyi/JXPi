
package cn.ijingxi.common.util;

import cn.ijingxi.common.Process.IExecutor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CallParam
{
	public IExecutor Execer=null;
	public IExecutor Caller=null;
	public String Msg=null;

	Map<String,Object> Param=null;
	public CallParam(IExecutor Caller,IExecutor Execer,String Msg)
	{
		this.Execer=Execer;
		this.Caller=Caller;
		this.Msg=Msg;		
	}
	public void addParam(String paramName,Object param)
	{
		if(Param==null)
			Param=new HashMap<>();
		Param.put(paramName,param);
	}
	public Object getParam(String paramName)
	{
		if(Param!=null)
			return Param.get(paramName);
		return null;
	}
}