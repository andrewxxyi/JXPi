
package cn.ijingxi.common.Process;

import cn.ijingxi.common.util.IjxEnum;

public enum InstanceEvent implements IjxEnum
{
	Error,
	//触碰节点，但该节点未必能得到执行
	Touch,
	//触发节点，该节点得以执行
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
}