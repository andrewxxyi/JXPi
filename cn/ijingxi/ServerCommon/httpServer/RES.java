package cn.ijingxi.ServerCommon.httpServer;


import java.lang.annotation.ElementType;import java.lang.annotation.Retention;import java.lang.annotation.RetentionPolicy;import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD })
public @interface RES 
{  
	public enum MethodType {GET, POST, DELETE, PUT};
	MethodType methodType() default MethodType.GET;  
	
	//动作
	boolean isActive() default true;  
}  