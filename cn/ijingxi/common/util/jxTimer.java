
package cn.ijingxi.common.util;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class jxTimer
{
	public static final int asyncTaskNum=100;
	private Timer myTimer=null;
	private int secondNum=0;
	private IDo dual=null;
	private Object param=null;
	public jxTimer(){
		myTimer = new Timer(true);
	}
	/**
	 * 多少秒以后开始执行
	 * @param SecondNum
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

	private static ExecutorService asyncService= Executors.newFixedThreadPool(asyncTaskNum);
	public static void asyncRun(IDo dual,Object param){
		asyncService.execute(new myThreand1(dual,param));
	}
	public static void asyncRun_CallParam(IDoSomething dual,CallParam param){
		asyncService.execute(new myThreand2(dual,param));
	}
	public static Thread asyncRun_Repeat(IDo dual,Object param,boolean canInterrupt){
		Thread th=new Thread(() -> {
			while (true) {
				try {
					dual.Do(param);
				} catch (InterruptedException e) {
					if (canInterrupt)
						break;
				} catch (Exception ex) {
					//最好自己处理异常
					jxLog.error(ex);
					return;
				}
			}
		});
		th.start();
		return th;
	}

}

class myThreand1 implements Runnable
{
	private IDo toDo=null;
	private Object param=null;

	myThreand1(IDo toDo,Object param)
	{
		this.toDo=toDo;
		this.param=param;
	}
	@Override
	public void run()
	{
		try {
			toDo.Do(param);
		} catch (Exception e) {
			jxLog.error(e);
		}
	}

}
class myThreand2 implements Runnable
{
	private IDoSomething toDo=null;
	private CallParam param=null;

	myThreand2(IDoSomething toDo,CallParam param)
	{
		this.toDo=toDo;
		this.param=param;
	}
	@Override
	public void run()
	{
		try {
			toDo.Do(param);
		} catch (Exception e) {
			jxLog.error(e);
		}
	}

}

class myThreand3 implements Runnable
{
	private IFunc toDo=null;
	private Object param=null;
	private Object result=null;
	private Object sync_lock=new Object();

	myThreand3(IFunc toDo,Object param)
	{
		this.toDo=toDo;
		this.param=param;
	}
	@Override
	public void run()
	{
		try {
			toDo.Do(param);
		} catch (Exception e) {
			jxLog.error(e);
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
	@Override
	public void run()
	{
		try {
			toDo.Do(param);
		} catch (Exception e) {
			jxLog.error(e);
		}
	}

}