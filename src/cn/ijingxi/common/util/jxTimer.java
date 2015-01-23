
package cn.ijingxi.common.util;

import java.util.*;

import cn.ijingxi.common.Process.IExecutor;

public class jxTimer
{
	/**
	 * 多少秒以后开始执行
	 * @param SecondNum
	 * @param Execer
	 * @param toDo
	 * @param param
	 */
	public static Timer DoAfter(int SecondNum,IExecutor Execer,IDoSomething<CallParam> toDo,Object param)
	{
		 Timer timer = new Timer(); 
	      timer.schedule(new myTask(Execer,toDo,param), SecondNum * 1000);		
	      return timer;
	}

	/**
	 * 在什么时间执行
	 * @param Time
	 * @param Execer
	 * @param toDo
	 * @param param
	 */
	public static Timer DoAt(Calendar Time,IExecutor Execer,IDoSomething<CallParam> toDo,Object param)
	{
		 Timer timer = new Timer(); 
	      timer.schedule(new myTask(Execer,toDo,param), Time.getTime());		
	      return timer;
	}

	/**
	 * 周期性执行
	 * @param Period_Second
	 * @param Execer
	 * @param toDo
	 * @param param
	 */
	public static Timer DoPeriod(int Period_Second,IExecutor Execer,IDoSomething<CallParam> toDo,Object param)
	{
		 Timer timer = new Timer(); 
	      timer.schedule(new myTask(Execer,toDo,param), Period_Second*1000,Period_Second*1000);	      
	      return timer;
	}
}

class myTask extends TimerTask 
{
	private IExecutor Execer=null;
	private IDoSomething<CallParam> toDo=null;
	private Object param=null;
	
	myTask(IExecutor Execer,IDoSomething<CallParam> toDo,Object param)
	{
		this.Execer=Execer;
		this.toDo=toDo;
		this.param=param;
	}
	public void run()
	{
		try {
			CallParam p=new CallParam(Execer,null,null);
			p.addParam(param);			
			toDo.Do(p);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}