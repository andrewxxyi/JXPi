package cn.ijingxi.orm;

import java.lang.reflect.Field;

/**
 * Created by andrew on 15-11-24.
 */
public class FieldAttr {
    public  Class<?> FieldType=null;
    public  Field field=null;
    public boolean IsEnum=false;
    public  boolean  Encrypted=false;
    public  boolean IsPrimaryKey=false;
    public  ORM.KeyType keyType=ORM.KeyType.None;
}
