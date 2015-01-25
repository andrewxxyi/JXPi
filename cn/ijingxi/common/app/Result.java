
package cn.ijingxi.common.app;

import cn.ijingxi.common.util.IjxEnum;

public enum Result implements IjxEnum
{
	None,
	OK,
	Erroy,
	Reject,
	Accept;

	@Override
	public Object TransToORMEnum(Integer param) 
	{
		return Result.values()[param];
	}
	
}