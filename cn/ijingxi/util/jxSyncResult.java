package cn.ijingxi.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 用于将异步处理组装为同步获取结果：
 * 使用者调用exec启动多线程异步处理，另一个线程计算完毕后，调用setRS设置结果，如果出错或超时则设置error
 *
 * Created by andrew on 15-10-5.
 */
public class jxSyncResult<TValue> {

    //默认等待5s
    public long waitTime_SecondNum=60*5;
    private Lock lock=new ReentrantLock();
    private Condition getRSCondition=lock.newCondition();

    private TValue rs=null;
    public boolean isResult=false;
    public boolean isError=false;
    public String errorMsg=null;

    public TValue exec(IDo dual,Object param) throws Exception {
        lock.lock();
        try{
            dual.Do(param);
            while (!isResult&&!isError)
                if(!getRSCondition.await(waitTime_SecondNum, TimeUnit.SECONDS)){
                    isError=true;
                    this.errorMsg="超时";
                    break;
                }
        } finally {
            lock.unlock();
        }
        return rs;
    }

    public void setRS(TValue value){
        lock.lock();
        try{
            isResult=true;
            rs=value;
            getRSCondition.signal();
        }
        finally {
            lock.unlock();
        }
    }

    public void error(String errorMsg){
        lock.lock();
        try{
            isError=true;
            this.errorMsg=errorMsg;
            getRSCondition.signal();
        }
        finally {
            lock.unlock();
        }
    }

}
