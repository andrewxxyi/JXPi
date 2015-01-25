
package cn.ijingxi.common.util;

import cn.ijingxi.common.util.IjxEnum;

public enum jxCompare implements IjxEnum
{
	Equal,
	NoEqual,	
	Less,
	LessEqual,
	Greate,
	GreateEqual;
	@Override
	public Object TransToORMEnum(Integer param) 
	{
		return jxCompare.values()[param];
	}
}