package cn.ijingxi.stub.general;

/**
 * Created by andrew on 16-1-9.
 */
public interface IFunc2<TResult,TParam1,TParam2> {
    TResult Do(TParam1 param, TParam2 param2) throws Exception;
}
