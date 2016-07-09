package cn.ijingxi.stub;

import cn.ijingxi.stub.dal.People;

/**
 * Created by andrew on 16-6-12.
 */
public class common {

    //10001是端口号，目前已经给每个小组分配了一个后台服务进程，端口号从10001一直到10008，各个小组分别用自己的那个
    public static session s = new session("192.0.54.123",10001);


    public static  People currentUser=null;

}
