
package cn.ijingxi.app;

import cn.ijingxi.util.IjxEnum;

/**
 * 
 * @author andrew
 *
 */
public enum TSEvent implements IjxEnum
{
	//广播ts的存在
	BroadCast,
	//请求加入
	RequestAdd,
	//拒绝加入
	Reject,
	//同意加入
	Accept,
	//请求开始同步
	Request,
	//同步数据传递
	Sync,
	//最后发送一个数据传送完毕
	End,;

	@Override
	public Object TransToORMEnum(Integer param) 
	{
		return TSEvent.values()[param];
	}

	@Override
	public String toChinese()
	{
		switch(this)
		{
		case BroadCast:
			return "广播";
		case RequestAdd:
			return "请求加入";
		case Reject:
			return "拒绝";
		case Accept:
			return "同意";
		case Sync:
			return "同步";
		case Request:
			return "请求";
		case End:
			return "完毕";
		}
		return "";
	}
	
}