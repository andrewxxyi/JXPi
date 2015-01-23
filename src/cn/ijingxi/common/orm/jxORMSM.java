
package cn.ijingxi.common.orm;

import cn.ijingxi.common.Process.CheckNodeInput;
import cn.ijingxi.common.Process.InstanceEvent;
import cn.ijingxi.common.Process.InstanceState;
import cn.ijingxi.common.Process.NodeDual;
import cn.ijingxi.common.Process.NodePause;
import cn.ijingxi.common.util.IDoSomething;
import cn.ijingxi.common.util.jxStateMachine;

public class jxORMSM<TState extends Enum<?>,TEvent extends Enum<?>,TParam>
{
	jxStateMachine<TState ,TEvent,TParam> SM=new jxStateMachine<TState,TEvent,TParam>();
	public void AddTrans(TState FromState,TEvent Event,TState ToState,IDoSomething ToDo)
	{
		SM.AddTrans(FromState, Event, ToState, ToDo);
	}
	public TState Happen(jxORMobj obj,String StateFiledName,TEvent Event,TParam param)
	{
		
	}
	
}