
package cn.ijingxi.stub.general;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class jxTimer {
	public static final int asyncTaskNum = 100;
	private Timer myTimer = null;
	private int millionSecondNum = 0;
	private IDo dual = null;
	private Object param = null;

	public jxTimer() {
		myTimer = new Timer(true);
	}

	/**
	 * 多少秒以后开始执行
	 *
	 * @param SecondNum
	 * @param toDo
	 * @param param
	 */
	public static jxTimer DoAfter(int SecondNum, IDo toDo, Object param) {
		return DoAfter_ms(SecondNum*1000, toDo, param);
	}
	public static jxTimer DoAfter(int SecondNum, IDo toDo) {
		return DoAfter(SecondNum, toDo, null);
	}
	public static jxTimer DoAfter_ms(int millionSecondNum, IDo toDo, Object param) {
		jxTimer t = new jxTimer();
		t.millionSecondNum = millionSecondNum;
		t.dual = toDo;
		t.param = param;
		t.myTimer.schedule(new myTask(toDo, param), millionSecondNum);
		return t;
	}
	public static jxTimer DoAfter_ms(int millionSecondNum, IDo toDo) {
		return DoAfter_ms(millionSecondNum, toDo, null);
	}
	/**
	 * 在什么时间执行
	 *
	 * @param Time
	 * @param toDo
	 * @param param
	 */
	public static jxTimer DoAt(Calendar Time, IDo toDo, Object param) {
		jxTimer t = new jxTimer();
		t.myTimer.schedule(new myTask(toDo, param), Time.getTime());
		return t;
	}

	/**
	 * 周期性执行
	 *
	 * @param Period_Second
	 * @param toDo
	 * @param param
	 */
	public static jxTimer DoPeriod(int Period_Second, IDo toDo, Object param) {
		return DoPeriod_ms(Period_Second*1000, toDo, param);
	}

	public static jxTimer DoPeriod(int Period_Second, IDo toDo) {
		return DoPeriod(Period_Second, toDo, null);
	}
	public static jxTimer DoPeriod_ms(int Period_ms, IDo toDo, Object param) {
		jxTimer t = new jxTimer();
		t.myTimer.schedule(new myTask(toDo, param), Period_ms, Period_ms);
		return t;
	}
	public static jxTimer DoPeriod_ms(int Period_ms, IDo toDo) {
		return DoPeriod_ms(Period_ms, toDo, null);
	}
	public void cancel() {
		myTimer.cancel();
	}

	public void reTick() {
		if (millionSecondNum > 0) {
			myTimer.cancel();
			myTimer = new Timer();
			myTimer.schedule(new myTask(dual, param), millionSecondNum);
		}
	}

	private static ExecutorService asyncService = Executors.newFixedThreadPool(asyncTaskNum);

	public static Object asyncFunc(final IFunc func, final Object param) {
		final Object[] rs = {null};
		final Object lock = new Object();
		synchronized (lock) {
			try {
				asyncService.execute(new myThreand1(new IDo() {
					@Override
					public void Do(Object p) throws Exception {
						synchronized (lock) {
							if (func != null) {
								try {
									rs[0] = func.Do(param);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}, null));
				lock.wait(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return rs[0];
	}
	public static void asyncRun(IDo dual, Object param) {
		asyncService.execute(new myThreand1(dual, param));
	}
	public static void asyncRun(IDo dual) {
		asyncRun(dual,null);
	}

	public static void asyncRun_CallParam(IDoSomething dual, CallParam param) {
		asyncService.execute(new myThreand2(dual, param));
	}

	public static Thread asyncRun_Repeat(final IFunc<Boolean,Object> dual, final Object param) {
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				boolean rs = true;
				while (rs) {
					try {
						rs = dual.Do(param);
					} catch (InterruptedException e) {
						rs = false;
					} catch (Exception ex) {
						//最好自己处理异常
						ex.printStackTrace();
						return;
					}
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
			e.printStackTrace();
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
			e.printStackTrace();
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
			e.printStackTrace();
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
			e.printStackTrace();
		}
	}

}