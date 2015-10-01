
package cn.ijingxi.common.Process;

import cn.ijingxi.common.util.IjxEnum;

public enum InstanceEvent implements IjxEnum
{
	Error,
	Create,
	//触发节点，但节点能否执行要看情况
	Trigger,
	//正常结束
	Close,
	//取消
	Cancel,
	//暂停
	Pause,
	//设置流程实例的结果
	SetResult;
		
	@Override
	public Object TransToORMEnum(Integer param) 
	{
		return InstanceEvent.values()[param];
	}

	@Override
	public String toChinese()
	{
		switch(this)
		{
		case Error:
			return "错误";
		case Create:
			return "创建";
		case Trigger:
			return "激发";
		case Close:
			return "结束";
		case Cancel:
			return "取消";
		case Pause:
			return "暂停";
		case SetResult:
			return "设置结果";
		}
		return "";
	}
}