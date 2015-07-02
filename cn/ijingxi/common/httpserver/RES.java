package cn.ijingxi.common.httpserver;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD })  
public @interface RES 
{  
	public enum MethodType {GET, POST, DELETE, PUT};
	MethodType methodType() default MethodType.GET;  
	
	//动作
	boolean isActive() default true;  
}  