package com.example;

import com.example.stub.dal.People;
import com.example.stub.dal.Role;

import java.util.Queue;

/**
 * 使用方法：
 * 1、初始化
 * session s=new session("192.0.54.123",10008,null);
 * 2、使用
 * s.Person_login("admin","123456");
 *
 *
 * Created by andrew on 16-6-8.
 */
public class sessionDome {

    /**
     * 如何登陆
     */
    public void ffffffffffffff(){



        People result = common.s.Person_Login("admin", "123456");
        if(result!=null){
            //有返回结果
            //此时一定成功
            //可以直接使用result中的数据，如result.Name


        }else if(!common.s.isError()){
            //没有返回结果，但也没有错误
        }else {
            //出现错误
            //错误原因可通过getErrorMsg取回
            String errmsg=common.s.getErrorMsg();
        }



    }

    /**
     * 移除已指派的角色
     */
    public void ttttttttttttttt(){
        boolean result = common.s.team_removeRole("12345678_你用team_listMyRole函数取出的角色ID_uuid");
        if(result){
            //此时一定成功

        }else if(!common.s.isError()){
            //没有返回结果，但也没有错误
        }else {
            //出现错误
            //错误原因可通过getErrorMsg取回
            String errmsg=common.s.getErrorMsg();
        }
    }


    /**
     * 移除已指派的角色
     */
    public void xxxxxxxxxxxx(){
        Queue<Role> list  =common. s.team_listMyRole("12345678_某同学的ID_uuid");
        if(list!=null){
            //此时一定成功
            for(Role r:list){
                //可以直接使用r中的数据，如r.Name

            }

        }else if(!common.s.isError()){
            //没有返回结果，但也没有错误
        }else {
            //出现错误
            //错误原因可通过getErrorMsg取回
            String errmsg=common.s.getErrorMsg();
        }
    }



}
