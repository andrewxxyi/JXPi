package cn.ijingxi.app;

import cn.ijingxi.orm.*;
import cn.ijingxi.util.jxCompare;

import java.util.Date;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by andrew on 16-1-4.
 */
public class FileDesc  extends jxORMobj {

    public static FileDesc add(String Category,String fileName,String desc,String storePath) throws Exception {
        FileDesc fd=(FileDesc)FileDesc.Create(FileDesc.class);
        fd.Category=Category;
        fd.Name=fileName;
        fd.Descr=desc;
        fd.Path=storePath;
        return fd;
    }

    public static Queue<jxORMobj> list(String Category, String fileName) throws Exception {
        SelectSql s=new SelectSql();
        s.AddTable("FileDesc");
        if(Category!=null&&Category!="")
            s.AddContion("FileDesc", "Category", jxCompare.Equal, Category);
        if(fileName!=null&&fileName!="")
            s.AddContion("FileDesc", "Name", jxCompare.Like, fileName);
        return Select(FileDesc.class,s);
    }

    @ORM(keyType = ORM.KeyType.PrimaryKey)
    public UUID ID;

    @ORM(Index = 1)
    public String Name;

    @ORM
    public String Descr;

    @ORM(Descr="相对路径")
    public String Path;

    @ORM(Index = 2,Descr = "文件的分类")
    public String Category;

    @ORM(Descr = "json格式的附加信息")
    public String Info;

    @ORM(Index = 3)
    public Date CreateTime;

    @ORM
    public String addStringInfo;

    @ORM
    public int addNumberInfo;

    @ORM
    public int State;


    public static ORMID GetORMID(UUID ID)
    {
        return new ORMID(ORMType.FileDesc.ordinal(),ID);
    }

    @Override
    protected void Init_Create(DB db) throws Exception
    {
        ID= UUID.randomUUID();
        CreateTime=new Date();
    }
    /**
     * 要在Container之后执行
     * @throws Exception
     */
    public static void Init() throws Exception{
        InitClass(ORMType.FileDesc.ordinal(),FileDesc.class,"文件描述");
    }
    public static void CreateDB() throws Exception
    {
        CreateTableInDB(FileDesc.class);
    }
}
