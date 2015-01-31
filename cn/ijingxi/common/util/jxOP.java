
package cn.ijingxi.common.util;

import cn.ijingxi.common.util.IjxEnum;

public enum jxOP implements IjxEnum
{
	Equal;
	@Override
	public Object TransToORMEnum(Integer param) 
	{
		return jxOP.values()[param];
	}
}