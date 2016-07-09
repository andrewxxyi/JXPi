package cn.ijingxi.stub.general;

/**
 * Created by andrew on 16-1-9.
 */
public interface IFunc<TResult,TParam1> {
    TResult Do(TParam1 param) throws Exception;
}
