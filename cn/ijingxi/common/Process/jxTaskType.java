
package cn.ijingxi.common.Process;

import cn.ijingxi.common.util.IjxEnum;

public enum jxTaskType implements IjxEnum
{
	//普通任务
	Task,
	//流程实例
	ProcessInstance,
	//任务节点实例
	ProcessNodeInstance;
		
	@Override
	public Object TransToORMEnum(Integer param) 
	{
		return jxTaskType.values()[param];
	}

	@Override
	public String toChinese()
	{
		switch(this)
		{
		case Task:
			return "普通任务";
		case ProcessInstance:
			return "流程实例";
		case ProcessNodeInstance:
			return "任务节点实例";
		}
		return "";
	}
}