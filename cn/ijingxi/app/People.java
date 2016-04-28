
package cn.ijingxi.app;

import cn.ijingxi.Process.IExecutor;
import cn.ijingxi.orm.*;
import cn.ijingxi.orm.ORM.KeyType;
import cn.ijingxi.util.*;

import java.util.*;

/**
 * 全局的，所有topspace共享
 * @author andrew
 *
 */
public class People extends jxORMobj implements IExecutor
{
	static {
		jxEnum.addEnum("PeopleType","非用户");
		jxEnum.addEnum("PeopleType","管理员");
		jxEnum.addEnum("PeopleType","普通用户");
	}

	public static ORMID GetORMID(UUID ID)
	{
		return new ORMID(ORMType.People.ordinal(),ID);
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
		InitClass(ORMType.People.ordinal(),People.class,"人员");
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(People.class);
	}
	//如果在两台手机上都安装了，则在同步时自动以CreateTime在前的进行覆盖
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ID;

	@ORM(Index=1)
	public int PeopleType;
	public String getPeopleType() throws Exception {
		if(PeopleType==0)
			return "None";
		return jxEnum.getEnumName("PeopleType",PeopleType);
	}
	public void setPeopleType(String peopleType) throws Exception {
		if(peopleType==null||peopleType==""||peopleType=="None")
			PeopleType=0;
		else
			PeopleType=jxEnum.getEnumOrder("PeopleType",peopleType);
	}

	@ORM(Index=2)
	public String Name;		
	
	@ORM
	public String Descr;		
	
	@ORM(Index=3)
	public Date CreateTime;

	@ORM(Index=4)
	public String LoginName;

	@ORM(Index=5)
	public Date Birthday;
			
	@ORM
	public String Passwd;
	public void setPasswd(String passwd) throws Exception {
		if(passwd==null||passwd=="")
			//清空密码，则用户密码检验将始终无法通过
			Passwd=null;
		else
			Passwd=MD5.getHMAC_MD5(Name+CreateTime.toString(),passwd);
	}
	public boolean checkPasswd(String passwd) throws Exception {
		if(Passwd==null||Passwd=="")return false;
		if(passwd==null||passwd=="")return false;
		return MD5.getHMAC_MD5(Name+CreateTime.toString(),passwd).compareTo(Passwd)==0;
	}
	
	@ORM(Descr="json格式的安全问题与答案：Question、Answer")
	public String Secure;
	
	@ORM(Descr="json格式信息保存字段，如联系方式，包括但不限于Mail、Tel、Mobile、Fax、Address、国家等等")
	public String Info;

	@ORM(Descr="male:true")
	public Boolean IsMale;
	
	@ORM
	public Boolean NoUsed;

	/**
	 *
 	 * @param tsName 不同的topspace中，
	 * @param Purpose
	 * @param value
	 * @throws Exception
	 */
	public void setConf(String tsName,String Purpose,String value) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("ts", tsName);
		setExtendArrayValue("Info","Conf",ks,Purpose,value);
	}

	public jxJson getConf(String tsName) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("ts", tsName);
		List<jxJson> ls = getExtendArrayList("Info","Conf",ks);
		if(ls.size()==1)
			return ls.get(0);
		return null;
	}
	public String getConf(String tsName,String Purpose) throws Exception
	{
		jxJson nod=getConf(tsName);
		if(nod!=null)
			return (String) nod.getSubObjectValue(Purpose);
		return null;
	}


	@Override
	public String getName() {
		return Name;
	}

	@Override
	public UUID getOwnerID() throws Exception {
		return null;
	}

	@Override
	public IExecutor getRealExecutor() {
		return this;
	}

	@Override
	public boolean CheckRight(TopSpace ts,jxORMobj obj,String RoleName,boolean CheckParent) throws Exception
	{
		if(obj instanceof Organize)
		{
			//对组织角色是否匹配进行检查
			Organize org=(Organize)obj;
			while(org!=null)
			{
				Queue<jxORMobj> pl=org.ListRealMapTo(RoleName);
				if(pl!=null)
					for(jxORMobj p:pl)
						if(((People)p).getID().compareTo(getID())==0)
							return true;
				if(!CheckParent)
					return false;
				org=org.GetParentOrganize();
			}
			return false;
		}
		else if(obj instanceof TopSpace)
		{
			//对topspace角色是否匹配进行检查
			//TopSpace t=(TopSpace)obj;
			Queue<jxORMobj> rl=ts.ListRole();
			for(jxORMobj ro:rl)
				if(((Role)ro).Name.compareTo(RoleName)==0)
				{
					People p = ((Role)ro).GetMapTo();
					if(p.getID().compareTo(getID())==0)
						return true;
				}
		}
		return false;
	}

	public Role GetRoleMap(TopSpace ts)
	{



		return null;
	}
	public void SetRoleMap(Role role)
	{

	}

	public static People getPeopleByName(String Name) throws Exception {
		SelectSql s=new SelectSql();
		s.AddTable("People");
		s.AddContion("People", "Name", jxCompare.Equal, Name);
		return (People) People.Get(People.class,s);
	}

	public static Queue<UUID> listAllRoleID(DB db,UUID peopleID) throws Exception {
		Queue<UUID> rs=new LinkedList<>();
		Queue<jxORMobj> ar=Relation.listObj(db,ORMType.People.ordinal(),
				peopleID,RelationType.OneToMulti);
		for(jxORMobj obj:ar){
			Relation rrole=(Relation)obj;
			if(rrole.ObjTypeID==ORMType.Role.ordinal()){
				rs.offer(rrole.ObjID);
				Queue<UUID> rl = Role.listRoleMapToRoleID_SearchRightRole(db, rrole.ObjID);
				for(UUID rrid:rl)
					rs.offer(rrid);
			}
		}
		return rs;
	}

	/**
	 * 返回的People没有保存到数据库中，初始密码为123456
	 * @param Name
	 * @param PeopleTypeName
	 * @return
	 * @throws Exception
	 */
	public static People createPeople(String Name,String PeopleTypeName) throws Exception {
		People p=getPeopleByName(Name);
		utils.Check(p!=null,"用户已存在："+Name);
		p=(People) jxORMobj.Create(People.class);
		p.Name=Name;
		p.PeopleType= jxEnum.getEnumOrder("PeopleType", PeopleTypeName);
		p.setPasswd("123456");
		return p;
	}
	
}