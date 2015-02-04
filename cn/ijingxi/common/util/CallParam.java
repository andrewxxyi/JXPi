
package cn.ijingxi.common.util;

import java.util.LinkedList;
import java.util.Queue;

import cn.ijingxi.common.Process.IExecutor;

public class CallParam
{
	IExecutor Execer=null;
	public IExecutor getExecer()
    {
        return Execer;
    }    	
	IExecutor Caller=null;
	public IExecutor getCaller()
    {
        return Caller;
    }    	
	String Msg=null;
	public String getMsg()
    {
        return Msg;
    }    	
	Queue<Object> Param=null;
	public CallParam(IExecutor Caller,IExecutor Execer,String Msg)
	{
		this.Execer=Execer;
		this.Caller=Caller;
		this.Msg=Msg;		
	}
	public void addParam(Object param)
	{
		if(Param==null)
			Param=new LinkedList<Object>();
		Param.offer(param);
	}
	public Object getParam()
	{
		if(Param!=null)
			return Param.poll();
		return null;
	}
}