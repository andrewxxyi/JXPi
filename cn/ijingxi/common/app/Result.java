
package cn.ijingxi.common.app;

import cn.ijingxi.common.util.IjxEnum;

public enum Result implements IjxEnum
{
	None,
	OK,
	Erroy,
	Reject,
	Accept,
	Success,
	Fail,
	//放弃
	GiveUp,
	//部分完成
	Part;

	@Override
	public Object TransToORMEnum(Integer param) 
	{
		return Result.values()[param];
	}
	
}