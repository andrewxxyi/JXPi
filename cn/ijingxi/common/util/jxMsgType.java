
package cn.ijingxi.common.util;

import cn.ijingxi.common.util.IjxEnum;

public enum jxMsgType implements IjxEnum
{
	None,
	//文字信息
	Text,
	//带格式的文本
	RichText,
	//事件
	Event,
	//后加入空间者开始下载空间中的基本信息与设置
	Sync,
	//上传服务器的报告信息
	Report;
		
	@Override
	public Object TransToORMEnum(Integer param) 
	{
		return jxMsgType.values()[param];
	}
}