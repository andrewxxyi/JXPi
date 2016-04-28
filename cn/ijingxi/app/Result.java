
package cn.ijingxi.app;

import cn.ijingxi.util.IjxEnum;

public enum Result implements IjxEnum
{
	None,
	OK,
	Error,
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

	@Override
	public String toChinese()
	{
		switch(this)
		{
		case None:
			return "空";
		case OK:
			return "正确";
		case Error:
			return "错误";
		case Reject:
			return "拒绝";
		case Accept:
			return "接受";
		case Success:
			return "成功";
		case Fail:
			return "失败";
		case GiveUp:
			return "放弃";
		case Part:
			return "部分完成";
		}
		return "";
	}
	
}