
package cn.ijingxi.common.util;

import java.util.LinkedList;
import java.util.Queue;

public class jxEvent<TEvent extends Enum<?>>
{
	public TEvent event=null;
	//事件参数由使用者自行约定，建议为：
	//事件的触发者
	//e.Param.offer(node.Caller);
	//事件的现场，这里是传递路径
	//e.Param.offer(trans);
	//字符串消息
	//e.Param.offer(null);
	public Queue<Object> Param=new LinkedList<Object>();
	public jxEvent(TEvent event){this.event=event;}
	
}