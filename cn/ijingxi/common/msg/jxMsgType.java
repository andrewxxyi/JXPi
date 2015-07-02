
package cn.ijingxi.common.msg;

import cn.ijingxi.common.util.IjxEnum;

public enum jxMsgType implements IjxEnum
{
	//文字信息
	Text,
	//带格式的文本
	RichText,
	//日志信息
	Log,
	//事件
	Event,
	//后加入空间者开始下载空间中的基本信息与设置
	Sync,
	//上传服务器的报告信息
	Report,
	//查询请求
	Request;
		
	@Override
	public Object TransToORMEnum(Integer param) 
	{
		return jxMsgType.values()[param];
	}

	@Override
	public String toChinese()
	{
		switch(this)
		{
		case Text:
			return "文字信息";
		case RichText:
			return "格式文本";
		case Log:
			return "日志";
		case Event:
			return "事件";
		case Sync:
			return "同步";
		case Report:
			return "报告";
		case Request:
			return "查询";
		}
		return "";
	}
}