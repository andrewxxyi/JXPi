
package cn.ijingxi.common.orm;

import cn.ijingxi.common.util.*;

public class jxORMSM<TState extends Enum<?>,TEvent extends Enum<?>>
{
	jxStateMachine<TState ,TEvent> SM=new jxStateMachine<TState,TEvent>();
	public void AddTrans(TState FromState,TEvent Event,TState ToState,IDoSomething ToDo)
	{
		SM.AddTrans(FromState, Event, ToState, ToDo);
	}
	/**
	 * 本函数对obj进行了加锁，要注意避免死锁
	 * @param obj 目标对象
	 * @param StateFieldName 该对象的状态字段
	 * @param Event
	 * @param param
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void Happen(jxORMobj obj,String StateFieldName,TEvent Event,CallParam param) throws Exception
	{
        synchronized (obj)
        {
        	jxORMobj.setFiledValue(obj, StateFieldName, SM.Happen((TState) jxORMobj.getFiledValue(obj, StateFieldName), Event, param));
        }
	}
	
}