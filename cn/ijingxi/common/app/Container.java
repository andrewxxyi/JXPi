
package cn.ijingxi.common.app;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.ijingxi.common.Process.IExecutor;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.jxCompare;

public class Container extends jxORMobj implements IExecutor
{	
	public static final int ContainerType_TopSpace=1;
	public static final int ContainerType_People=2;
	public static final int ContainerType_Organize=3;
	//圈子
	public static final int ContainerType_Circle=4;
	public static final int ContainerType_Process=5;
	public static final int ContainerType_ProcessInstance=6;
	public static final int ContainerType_ProcessNode=7;
	public static final int ContainerType_Role=8;

	
	protected Container()
	{
		super();
		CreateTime=new Date();
	}
	public static void Init() throws Exception{	InitClass(Container.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(Container.class);
	}
	
	public static ORMID GetORMID(Integer ID)
	{
		return new ORMID(GetTypeID("Container"),ID);
	}
	
	@ORM
	public String TypeName;
	
	@ORM(keyType=KeyType.AutoDBGenerated)
	public int ID;
	
	@ORM(Index=1,Descr="用于人、组织等的全局确定")
	public UUID UniqueID;	
	public static Container getByUniqueD(UUID uuid) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Container");
		s.AddContion("Container", "UniqueID", jxCompare.Equal, uuid);
		Container c=(Container) Get(Container.class,s);
		if(c!=null)
			return (Container) GetByID(c.TypeName,c.ID);
		return null;
	}
	private static Map<Integer,UUID> TopSpaceIDTree=new HashMap<Integer,UUID>();
	public static UUID getUUIDOfTopSpace(Integer TopSpaceID) throws Exception
	{
		UUID uuid=TopSpaceIDTree.get(TopSpaceID);
		if(uuid!=null)return uuid;
		SelectSql s=new SelectSql();
		s.AddTable("Container");
		s.AddContion("Container", "ID", jxCompare.Equal, TopSpaceID);
		Container c=(Container) Get(Container.class,s);
		if(c!=null)
		{
			uuid=c.UniqueID;
			TopSpaceIDTree.put(TopSpaceID, uuid);
		}
		return uuid;
	}
	
	@ORM(Index=2,Descr="用于隔离不同的组织、圈子等等")
	public int TopSpaceID;	

	@ORM(Index=3,Encrypted=true)
	public String Name;		

	//1-people
	//2-组织
	@ORM(Index=4,Descr="容器类型：1是顶层空间名、2是people、3是组织（隶属于顶层空间）、4是流程、5是流程实例等等")
	public int ContainerType;
	
	@ORM(Descr="说明信息",Encrypted=true)
	public String Descr;
	
	@ORM(Index=5,Descr="用于两者之间的同步")
	public Date CreateTime;

	@Override
	public IExecutor GetRealExecutor() {
		return this;
	}
	@Override
	public String getName() {
		return Name;
	}

	@Override
	public boolean CheckRight(Container c, String RoleName) throws Exception {
		return false;
	}
	@Override
	public UUID GetOwnerID() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public UUID getUniqueID() {
		return UniqueID;
	}

}