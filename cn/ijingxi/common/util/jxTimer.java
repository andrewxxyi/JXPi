
package cn.ijingxi.common.util;

import java.util.*;

public class jxTimer
{
	private Timer myTimer=null;
	private int secondNum=0;
	private IDo dual=null;
	private Object param=null;
	public jxTimer(){
		myTimer = new Timer(); 
	}
	/**
	 * 多少秒以后开始执行
	 * @param SecondNum
	 * @param Execer
	 * @param toDo
	 * @param param
	 */
	public static jxTimer DoAfter(int SecondNum,IDo toDo,Object param)
	{
		  jxTimer t=new jxTimer();
		  t.secondNum=SecondNum;
		  t.dual=toDo;
		  t.param=param;
	      t.myTimer.schedule(new myTask(toDo,param), SecondNum * 1000);
	      return t;
	}

	/**
	 * 在什么时间执行
	 * @param Time
	 * @param Execer
	 * @param toDo
	 * @param param
	 */
	public static jxTimer DoAt(Calendar Time,IDo toDo,Object param)
	{
		  jxTimer t=new jxTimer();		
		  t.myTimer.schedule(new myTask(toDo,param), Time.getTime());		
	      return t;
	}

	/**
	 * 周期性执行
	 * @param Period_Second
	 * @param Execer
	 * @param toDo
	 * @param param
	 */
	public static jxTimer DoPeriod(int Period_Second,IDo toDo,Object param)
	{
		  jxTimer t=new jxTimer();
		  t.myTimer.schedule(new myTask(toDo,param), Period_Second*1000,Period_Second*1000);	   
	      return t;
	}
	
	public void cancel()
	{
		myTimer.cancel();
	}
	public void reTick()
	{
		if(secondNum>0)
		{
			myTimer.cancel();
			myTimer = new Timer(); 
			myTimer.schedule(new myTask(dual,param), secondNum * 1000);			
		}
	}
	
}

class myTask extends TimerTask 
{
	private IDo toDo=null;
	private Object param=null;
	
	myTask(IDo toDo,Object param)
	{
		this.toDo=toDo;
		this.param=param;
	}
	public void run()
	{
		try {
			toDo.Do(param);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}