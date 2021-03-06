
package cn.ijingxi.stub.general;

import java.util.HashMap;
import java.util.Map;

public class CallParam
{
	public IExecutor Execer=null;
	public IExecutor Caller=null;
	public String Msg=null;
	public Object obj=null;

	Map<String,Object> Param=null;
	public CallParam(){}
	public CallParam(Object obj){this.obj=obj;}
	public CallParam(IExecutor Caller,IExecutor Execer,String Msg)
	{
		this.Execer=Execer;
		this.Caller=Caller;
		this.Msg=Msg;		
	}
	public void addParam(String paramName, Object param)
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