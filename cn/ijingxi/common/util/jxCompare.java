
package cn.ijingxi.common.util;

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

	@Override
	public String toChinese()
	{
		switch(this)
		{
		case Equal:
			return "等于";
		case NoEqual:
			return "不等";
		case Less:
			return "小于";
		case LessEqual:
			return "小于等于";
		case Greate:
			return "大于";
		case GreateEqual:
			return "大于等于";
		}
		return "";
	}
}