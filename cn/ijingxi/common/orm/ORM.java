package cn.ijingxi.common.orm;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface ORM 
{
	//如果没有PrimaryKey，则无法update
	public enum KeyType {None, PrimaryKey, AutoDBGenerated, AutoSystemGenerated};
	KeyType keyType() default KeyType.None;  

	//是否作为索引项，0不做索引；同一索引号则标识为同一组索引
	int Index() default 0;  
	
	//注释
	String Descr() default "";  
	
	boolean Encrypted() default false;
}  