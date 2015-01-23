
package cn.ijingxi.common.Process;

import cn.ijingxi.common.orm.jxJson;
import cn.ijingxi.common.util.*;

public class ProcessNode
{
    public static String Node_Start = "开始";
    public static String Node_End = "结束";
    public static String Node_Accept = "同意";
    public static String Node_Reject = "拒绝";  
    
	ProcessInstance process=null;
	private InstanceState state=InstanceState.Waiting;
	public InstanceState getState()
    {
		if(process==null)
			return InstanceState.NoActive;
		if(process.State!=InstanceState.Doing)
			//如果进程不活动则自身的状态没有意义
			return process.State;
        return state;
    }
	void setState(InstanceState state){this.state=state;}
	
	String Name=null;    	
	public String getName()
    {
        return Name;
    }    	
	//是否自动执行，或输出情况下Auto为true则从所有出口中按顺序挑选第一个可以执行的节点进行触发
	boolean Auto=false;    	
	public boolean getAuto()
    {
        return Auto;
    }    	
	//执行者
	IExecutor Execer=null;
	public void setExecer(IExecutor Execer)
    {
        this.Execer=Execer;
    }

	//输入类型与输出类型
	boolean InputTypeIsAnd=false;
	boolean OutputTypeIsAnd=false;
		
	ProcessNode(ProcessInstance process, String Name){this.process=process;this.Name=Name;}
	ProcessNode(ProcessInstance process, String Name,jxJson js) throws Exception
	{
		this.process=process;
		this.Name=Name;
		if(js==null) return;
		this.state=utils.TransToInstanceState((String)js.GetSubValue("state"));
		this.Auto=Boolean.parseBoolean((String)js.GetSubValue("Auto"));
		this.InputTypeIsAnd=Boolean.parseBoolean((String)js.GetSubValue("InputType"));
		this.OutputTypeIsAnd=Boolean.parseBoolean((String)js.GetSubValue("OutputType"));	
	}
	jxJson ToJsonNode() throws Exception
	{
		jxJson j=jxJson.GetObjectNode(Name);
		j.AddValue("state", state.name());
		j.AddValue("Auto", Auto);
		j.AddValue("InputType", InputTypeIsAnd);
		j.AddValue("OutputType", OutputTypeIsAnd);
		return j;
	}
	jxLink<String,String> getInBranch() 
	{
		if(process!=null)
			return process.getInBranch(Name);
		return null;
	}
	jxLink<String,String> getOutBranch()
	{
		if(process!=null)
			return process.getOutBranch(Name);
		return null;
	}

}
