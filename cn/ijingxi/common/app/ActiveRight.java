package cn.ijingxi.common.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by andrew on 15-12-21.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD,ElementType.TYPE })
public @interface ActiveRight
{
    //默认策略，接受or拒绝;Manager则代表people的PeopleType是管理员则可接受
    public enum Policy {None,Accept, Refuse, Manager};
    Policy policy() default Policy.Accept;
    //可指定的参数
    //如启动流程，某人可启动x1,x2流程,某人可启动y1,y2流程等，就不需要针对每个流程写一个操作入口了
    //而只要指定paramName={"流程名"}
    String[] paramName() default {};


    //注释
    String Descr() default "";
}