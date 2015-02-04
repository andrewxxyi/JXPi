
package cn.ijingxi.common.orm;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

import cn.ijingxi.common.app.TopSpace;
import cn.ijingxi.common.app.jxSystem;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.Base64;
import cn.ijingxi.common.util.CallParam;
import cn.ijingxi.common.util.IDoSomething;
import cn.ijingxi.common.util.IMsgHandle;
import cn.ijingxi.common.util.LRU;
import cn.ijingxi.common.util.LinkNode;
import cn.ijingxi.common.util.MsgCenter;
import cn.ijingxi.common.util.Trans;
import cn.ijingxi.common.util.jxCompare;
import cn.ijingxi.common.util.jxLink;
import cn.ijingxi.common.util.jxMsg;
import cn.ijingxi.common.util.jxTimer;
import cn.ijingxi.common.util.utils;


public class jxORMobj
{
	//延迟5s保存，延迟保存只是在突发密集操作时才有效，必然设置用户个人属性等
	private static final int DelaySaveSecond=5;
	private static Map<String,ORMClassAttr> ClassAttrTree=new HashMap<String,ORMClassAttr>();
	private static Map<Integer,ORMClassAttr> ClassAttrTreeByTypeID=new HashMap<Integer,ORMClassAttr>();

	private ORMClassAttr myClassAttr=null;
	private Queue<Object> params=null;
	//延迟保存中，用于确定是否还需要执行保存动作（因可能有多个延迟保存命令陆续发出）
	private boolean NeedSave=false;
	
	private static LRU myLRU=new LRU();
	/*
	//如果是null则是本机自有对象，否则是其它人所拥有的对象
	private UUID OwnerID=null;
	public void isetOwnerID(UUID OwnerID){this.OwnerID=OwnerID;}
	*/
	private static byte[] bs=null;

	
	//从数据库中读出后的初始化，对象的初始化通过其构造函数进行即可
	protected void myInit() throws Exception{}

	/**
	 * 系统消息分发
	 * @param msg
	 * @return 用于组织消息处理链条，返回true则已经处理完毕了，不必继续处理
	 */
	protected boolean DualMsg(jxMsg msg)
	{
		if(msg!=null)
			switch(msg.MsgType)
			{
			case Text:
				return DualTextMsg(msg);
			case RichText:
				return DualRichTextMsg(msg);
			case Event:
				return DualEventMsg(msg);
			case Sync:
				return DualSyncMsg(msg);
			case Report:
				return DualReportMsg(msg);
				
				
			default:
				break;
			}
		return false;
	}
	protected boolean DualTextMsg(jxMsg msg){return false;}
	protected boolean DualRichTextMsg(jxMsg msg){return false;}
	protected boolean DualEventMsg(jxMsg msg){return false;}
	protected boolean DualSyncMsg(jxMsg msg){return false;}
	protected boolean DualReportMsg(jxMsg msg){return false;}
	
	//检查是否需要注册消息接收条件，比如流程已经执行完毕了就不再接受消息
	protected boolean CheckForMsgRegister() throws Exception
	{
		return false;
	}
	
	//加密处理，加密解密是发生在从数据库中读出与写入之时
	static String Encrypt(String str) throws Exception
	{
		if(str==null)return null;
		byte[] btemp=str.getBytes("UTF8");
		int j=0;
		for(int i=0;i<btemp.length;i++)
		{
			if(j==bs.length)j=0;
			btemp[i]=(byte) (btemp[i]^bs[j]);
			j++;
		}
		return Base64.encoderBase64(btemp);
	}
	//通过orm中的设置确定是否需要加密，原则上只对字符串进行加密，为了便于查找时的比较
	static Object EncryptField(FieldAttr fa,Object value) throws Exception
	{
		if(value!=null&&fa.Encrypted&&value instanceof String)
			return Encrypt((String) value);
		return value;
	}
	static Object Encrypte(String className, String colName, Object value) throws Exception
	{
		FieldAttr fa=getFieldAttr(className,colName);
		return EncryptField(fa,value);
	}
	static String DeEncrypt(String str) throws Exception
	{
		if(str==null)return null;
		byte[] btemp=str.getBytes("UTF8");
		String s=Base64.decoderBase64(btemp);
		btemp=s.getBytes("UTF8");
		int j=0;
		for(int i=0;i<btemp.length;i++)
		{
			if(j==bs.length)j=0;
			btemp[i]=(byte) (btemp[i]^bs[j]);
			j++;
		}
		return new String(btemp,"UTF8");
	}
	static Object DeEncryptField(FieldAttr fa,Object value) throws Exception
	{
		if(value!=null&&fa.Encrypted&&value instanceof String)
			return DeEncrypt((String) value);
		return value;
	}
	static Object DeEncrypte(String className, String colName, Object value) throws Exception
	{
		FieldAttr fa=getFieldAttr(className,colName);
		return DeEncryptField(fa,value);
	}

	//默认主键名为ID
	ORMID myID=null;
	public ORMID GetID() throws Exception
	{
		if(myID==null&&myClassAttr.PrimaryKeys!=null&&myClassAttr.PrimaryKeys.size()==1&&myClassAttr.PrimaryKeys.get(0)=="ID")
			myID=new ORMID(myClassAttr.TypeID,(Integer) getFiledValue(this, "ID"));
		return myID;
	}
	public int getTypeID()
	{
		return myClassAttr.TypeID;
	}
	
	
	public static void InitClass(int Typeid,Class<?> cls) throws Exception
	{
		Field[] fs = cls.getDeclaredFields();
		if(fs==null||fs.length==0)return;
		String classname=utils.GetClassName(cls);
		ORMClassAttr attr=new ORMClassAttr();
		attr.TypeID=Typeid;
		ClassAttrTree.put(classname, attr);
		ClassAttrTreeByTypeID.put(Typeid, attr);
		attr.ClsName=classname;
		attr.clsType=cls;		
		
		for(Field f:fs)
		{
			String fn=f.getName();
			ORM ann = f.getAnnotation(ORM.class);
			if(ann!=null)
			{
				attr.dbTableName=Encrypt(classname);
				FieldAttr fa=new FieldAttr();
				fa.FieldType=f.getType();
				fa.field=f;
				if(utils.JudgeIsEnum(fa.FieldType))
					fa.IsEnum=true;
				attr.Fields.put(fn, fa);
				fa.Encrypted=ann.Encrypted();
				fa.keyType=ann.keyType();
				if(fa.keyType==KeyType.AutoDBGenerated)
				{
					fa.IsPrimaryKey=true;
					attr.AutoGenerateKey=fn;
					attr.IsDBGenerateKey=true;
					if(attr.PrimaryKeys==null)
						attr.PrimaryKeys=new ArrayList<String>();
					attr.PrimaryKeys.add(fn);
					if(bs==null)
						bs=",PRIMARY KEY(".getBytes("UTF8");
				}
				else if(fa.keyType==KeyType.AutoSystemGenerated)
				{
					fa.IsPrimaryKey=true;
					attr.AutoGenerateKey=fn;
					if(attr.PrimaryKeys==null)
						attr.PrimaryKeys=new ArrayList<String>();
					attr.PrimaryKeys.add(fn);
					if(bs==null)
						bs=",PRIMARY KEY(".getBytes("UTF8");
				}
				else if(fa.keyType==KeyType.PrimaryKey)
				{
					fa.IsPrimaryKey=true;
					if(attr.PrimaryKeys==null)
						attr.PrimaryKeys=new ArrayList<String>();
					attr.PrimaryKeys.add(fn);
				}
				if(ann.Index()>0)
				{
					if(attr.Indexs==null)
						attr.Indexs=new jxLink<Integer,ArrayList<String>>();
					ArrayList<String> iarr=attr.Indexs.search(ann.Index());
					if(iarr==null)
					{
						iarr=new ArrayList<String>();
						attr.Indexs.addByRise(ann.Index(), iarr);
					}
					iarr.add(fn);
				}
			}			
		}
		//如有继承则需要先初始化父类
		ORMClassAttr psa=getClassAttr(cls.getSuperclass());
		if(psa!=null)
			attr.SuperClassName=psa.ClsName;
	}
	//jxORMobj对象只能通过本函数生成，该函数所生成的都是正常对象，但如果是非拥有者所
	//建立的对象，则通过另外的函数来生成，如申请者发起申请流程，该流程流转到其它人那时就是非拥有者的
	public static jxORMobj New(Class<?> cls) throws Exception
	{
		String classname=utils.GetClassName(cls);
		ORMClassAttr attr=getClassAttr(classname);
		if(attr==null)return null;
		jxORMobj obj=(jxORMobj) cls.newInstance();
		obj.myClassAttr=attr;
		return obj;
	}
	
	static void DropTable(Class<?> cls,TopSpace ts) throws Exception
	{
		ORMClassAttr attr=getClassAttr(cls);
		if(attr==null||attr.getDBTableName(ts)==null)return;
		DB db=JdbcUtils.GetDB();
		String sql="DROP TABLE "+attr.getDBTableName(ts);
		Exec(db,sql,null);  	
		db.Release();				
	}
	
	public static void CreateTableInDB(Class<?> cls,TopSpace ts) throws Exception
	{
		ORMClassAttr attr=getClassAttr(cls);
		if(attr==null||attr.getDBTableName(ts)==null)return;
		try
		{
			//如果建表，则先试图将旧表删除
			DropTable(cls,ts);
		}
		catch(Exception e){}
		
		DB db=JdbcUtils.GetDB();
		String cl=null;
		for(String fn:attr.Fields.keySet())
		{
			FieldAttr fa = attr.Fields.get(fn);
			cl=utils.StringAdd(cl, ",", fn+" "+db.TransDataTypeFromJavaToDB(fa.FieldType));
			if(fa.IsPrimaryKey&&attr.PrimaryKeys.size()==1)
			{
				cl+=" PRIMARY KEY";
				if(fa.keyType==KeyType.AutoDBGenerated)
					cl+=" "+db.GetDBGeneratedSQL();
			}
		}
		if(attr.PrimaryKeys.size()>1)
		{
			String ksl=null;
			for(String ks:attr.PrimaryKeys)
				ksl=utils.StringAdd(ksl, ",", ks);
			cl+=",PRIMARY KEY("+ksl+")";			
		}
		String sql="CREATE TABLE "+attr.getDBTableName(ts)+"("+cl+")";
		Exec(db,sql,null);  	
		if(attr.Indexs!=null)
		{
			for(LinkNode<Integer, ArrayList<String>> node:attr.Indexs)
			{
				String il=null;
				for(String index:node.getValue())
					il=utils.StringAdd(il, ",", index);
				sql="CREATE INDEX index_"+node.getKey()+"_"+attr.getDBTableName(ts)+" ON "+attr.getDBTableName(ts)+" ("+il+")";		
				Exec(db,sql,null);  		
			}
		}
		db.Release();		
	}
	
	
	//读写删插
	String GetWherePrimaryKey(DB db) throws Exception
	{
		if(myClassAttr.PrimaryKeys==null)return null;
		if(params==null)
			params=new LinkedList<Object>();
		String w=null;
		for(String s:myClassAttr.PrimaryKeys)
		{
			w=utils.StringAdd(w, " And ", s+"=?");
			params.offer(db.TransValueFromJavaToDB(jxORMobj.Encrypte(myClassAttr.ClsName, s, getFiledValue(this, s))));			
		}
		return w;
	}
	//数据出数据库要做：
	//1、db数据类型到java数据类型的转换
	//2、解密
	public static Queue<jxORMobj> Select(Class<?> cls,SelectSql s,TopSpace ts) throws Exception
	{
		DB db=JdbcUtils.GetDB();
		Queue<jxORMobj> rs=Select(db,cls,s,ts);
		db.Release();
		return rs;
	}
	public static Queue<jxORMobj> Select(DB db,Class<?> cls,SelectSql s,TopSpace ts) throws Exception
	{
		return Select(db,cls,s,true,ts);
	}	
	static Queue<jxORMobj> Select(DB db,Class<?> cls,SelectSql s,boolean Cache,TopSpace ts) throws Exception
	{
		Queue<jxORMobj> rs=new LinkedList<jxORMobj>();
		String clsName=utils.GetClassName(cls);
		String sql = s.GetSql(clsName,ts);
		
		//utils.P(sql);
				
		Connection conn=db.GetConnection();
		PreparedStatement stmt = conn.prepareStatement(sql);
		int len=s.params.size();
		for(int i=1;i<=len;i++)
			stmt.setObject(i, s.params.poll());
		ResultSet result=stmt.executeQuery();
		ResultSetMetaData rsMetaData = result.getMetaData();
	    int numberOfColumns = rsMetaData.getColumnCount();
		while(result.next())
		{
			jxORMobj obj=New(cls);
			for(int i=1;i<=numberOfColumns;i++)
			{
				Object v =result.getObject(i);
				String cn=rsMetaData.getColumnName(i);
				FieldAttr fa = getFieldAttr(clsName,cn);
				
				Object dv=db.TransValueFromDBToJava(fa, v);
				Object ev=DeEncryptField(fa, dv);
				
				//utils.P(cn+"：v:"+v+"，dv:"+dv+"，ev:"+ev);
				
				setFiledValue(obj, cn, ev);
			}
			try {
				if(obj.CheckForMsgRegister())
					MsgCenter.RegisterMsgHandle(obj.GetID(), new ForDualMsg(obj));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			obj.myInit();
			rs.offer(obj);
			if(Cache)
				myLRU.add(obj);
		}
		return rs;
	}
	public static jxORMobj Get(Class<?> cls,SelectSql s,TopSpace ts) throws Exception
	{
		DB db=JdbcUtils.GetDB();
		Queue<jxORMobj> rs=Select(db,cls,s,false,ts);
		db.Release();
		return rs.poll();
	}
	public static jxORMobj GetByID(Class<?> cls,int ID,TopSpace ts) throws Exception
	{
		DB db=JdbcUtils.GetDB();
		jxORMobj obj=GetByID(db,cls,ID,ts);
		db.Release();		
		return obj;
	}
	public static jxORMobj GetByID(String clsName,int ID,TopSpace ts) throws Exception
	{
		DB db=JdbcUtils.GetDB();
		ORMClassAttr attr = getClassAttr(clsName);
		SelectSql s=new SelectSql();
		s.AddTable(attr.ClsName,ts);
		s.AddContion(attr.ClsName, attr.PrimaryKeys.get(0), jxCompare.Equal, ID);
		Queue<jxORMobj> rs=Select(db,attr.clsType,s,true,ts);
		db.Release();		
		return rs.poll();
	}
	public static jxORMobj GetByID(DB db,Class<?> cls,int ID,TopSpace ts) throws Exception
	{
		ORMClassAttr attr = getClassAttr(cls);
		SelectSql s=new SelectSql();
		s.AddTable(attr.ClsName,ts);
		s.AddContion(attr.ClsName, attr.PrimaryKeys.get(0), jxCompare.Equal, ID);
		Queue<jxORMobj> rs=Select(db,cls,s,true,ts);
		return rs.poll();
	}
	public static jxORMobj GetByID(int typeid,int id,TopSpace ts) throws Exception
	{
		DB db=JdbcUtils.GetDB();
		ORMClassAttr attr = getClassAttr(typeid);
		SelectSql s=new SelectSql();
		s.AddTable(attr.ClsName,ts);
		s.AddContion(attr.ClsName, attr.PrimaryKeys.get(0), jxCompare.Equal, id);
		Queue<jxORMobj> rs=Select(db,attr.clsType,s,true,ts);
		db.Release();		
		return rs.poll();
	}
	//数据校验
	protected void Verify() throws Exception
	{	
		
	}
	//延迟3分钟后再保存
	public void DelaySave(TopSpace ts)
	{
		NeedSave=true;
		jxTimer.DoAfter(DelaySaveSecond, null, new ForDelaySave(), ts);
	}
	class ForDelaySave implements IDoSomething
	{
		@Override
		public void Do(CallParam param) throws Exception {
	        synchronized (this)
			{
	        	TopSpace ts=(TopSpace) param.getParam();
				if(NeedSave)
					Update(ts);
			}
		}		
	}
	
	public void Update(TopSpace ts) throws Exception
	{
		DB db=JdbcUtils.GetDB();
        synchronized (db)
        {		
        	Update(db,ts);
			NeedSave=false;
			db.Release();		
        }
	}
	//数据进数据库要做：
	//1、加密
	//2、java数据类型到db数据类型的转换
	public void Update(DB db,TopSpace ts) throws Exception
	{
		Verify();
		ORMClassAttr attr=myClassAttr;
		while(attr!=null)
		{
			if(attr.getDBTableName(ts)!=null)
				Update(db,attr,ts);
			if(attr.SuperClassName!=null)
				attr=getClassAttr(attr.SuperClassName);
			else
				break;
		}		
	}
	private void Update(DB db,ORMClassAttr attr,TopSpace ts) throws Exception
	{
		if(params==null)
			params=new LinkedList<Object>();
		String sql="Update "+attr.getDBTableName(ts)+" Set ";
		String v=null;
		for(String fn:attr.Fields.keySet())
		{
			FieldAttr fa = attr.Fields.get(fn);
			if(fa!=null&&fa.keyType==KeyType.None)
			{
				v=utils.StringAdd(v, ",", fn+"=?");
				params.offer(db.TransValueFromJavaToDB(jxORMobj.Encrypte(attr.ClsName, fn, getFiledValue(this, fn))));
			}
		}
		sql+=v+" Where "+GetWherePrimaryKey(db);
		Exec(db,sql,params);
	}
	public void Delete(TopSpace ts) throws Exception
	{
		DB db=JdbcUtils.GetDB();
        synchronized (db)
        {		
        	Delete(db,ts);
        	myLRU.delete(GetID());        
    		db.Release();			
        }
	}
	public void Delete(DB db,TopSpace ts) throws Exception
	{
		ORMClassAttr attr=myClassAttr;
		while(attr!=null)
		{
			if(attr.getDBTableName(ts)!=null)
				Delete(db,attr,ts);
			if(attr.SuperClassName!=null)
				attr=getClassAttr(attr.SuperClassName);
			else
				break;
		}		
	}
	private void Delete(DB db,ORMClassAttr attr,TopSpace ts) throws Exception
	{
		String sql="Delete From "+attr.getDBTableName(ts)+" Where "+GetWherePrimaryKey(db);
		Exec(db,sql,params);
	}
	//数据进数据库要做：
	//1、加密
	//2、java数据类型到db数据类型的转换
	public Integer Insert(TopSpace ts) throws Exception
	{
		int id=0;
		DB db=JdbcUtils.GetDB();
        synchronized (db)
        {		
        	id=Insert(db,ts);
        	myLRU.add(this);
    		db.Release();		
        }
		return id;
	}
	public Integer Insert(DB db,TopSpace ts) throws Exception
	{
		Verify();
		//因为对象的id可能是自增长列，所以必须从最早的祖类开始插入
		Stack<ORMClassAttr> al=new Stack<ORMClassAttr>();
		ORMClassAttr attr=myClassAttr;
		while(attr!=null)
		{
			al.push(attr);
			if(attr.SuperClassName!=null)
				attr=getClassAttr(attr.SuperClassName);
			else
				break;
		}
		Integer id=0,tid=0;
		try {
			attr=al.pop();
		} catch (Exception e) {
			attr=null;
		}
		while(attr!=null)
		{
			tid=Insert(db,attr,ts);
			if(tid!=0)
				id=tid;
			try {
				attr=al.pop();
			} catch (Exception e) {
				attr=null;
			}
		}
		myLRU.add(this);
		return id;
	}
	private Integer Insert(DB db,ORMClassAttr attr,TopSpace ts) throws Exception
	{
		
		//utils.P(attr.ClsName);
		
		if(params==null)
			params=new LinkedList<Object>();
		String cl=null,vl=null;
		for(String s:attr.Fields.keySet())
		{
			FieldAttr fa = attr.Fields.get(s);
			if(fa.keyType==KeyType.AutoDBGenerated)
				continue;
			if(fa.keyType==KeyType.AutoSystemGenerated)
			{
				Object ov=getFiledValue(this,attr.AutoGenerateKey);
				if(ov==null)
					//如果已设置了值则将其再次插入，主要是用于topspace中的数据复制
					setFiledValue(this,s, jxSystem.System.GetAutoGeneratedID(attr.ClsName));
			}
			cl=utils.StringAdd(cl, ",", s);
			vl=utils.StringAdd(vl, ",", "?");
			Object v=getFiledValue(this, s);
			Object ev=Encrypte(attr.ClsName, s, v);
			Object dv=db.TransValueFromJavaToDB(ev);
			
			params.offer(dv);
		}
				
		String sql="Insert Into "+attr.getDBTableName(ts)+"("+cl+") Values ("+vl+")";
		Exec(db,sql,params);
		if(attr.AutoGenerateKey!=null)
		if(attr.IsDBGenerateKey)
		{
			Integer id=db.GetGeneratedKey(attr.AutoGenerateKey);
			setFiledValue(this, attr.AutoGenerateKey, id);
			return id;
		}
		else
			return (Integer) getFiledValue(this,attr.AutoGenerateKey);
		return 0;
	}
	
	static boolean Exec(DB db,String sql,Queue<Object> param) throws Exception
	{
		
		//utils.P(sql);
		Connection conn=db.GetConnection();
		PreparedStatement ps = conn.prepareStatement(sql);
		if(param!=null)
		{
			int len=param.size();
			for(int i=1;i<=len;i++)
			{
				Object obj=param.poll();								
				ps.setObject(i, obj);
			}
		}
		return ps.execute();
	}
	
	public jxJson ToJSONWithName() throws Exception
	{
		jxJson root=jxJson.GetObjectNode("Root");
		root.AddSubObjNode(ToJSON());
		//TransToString是不带对象自身名字的！
		return root;
	}
	public jxJson ToJSON() throws Exception
	{
		jxJson js=jxJson.GetObjectNode(myClassAttr.ClsName);
		ORMClassAttr attr=myClassAttr;
		while(attr!=null)
		{
			for(String s:attr.Fields.keySet())
			{
				jxJson sub=js.GetSubObject(s);
				if(sub==null)
				{
					FieldAttr fa = attr.Fields.get(s);
					js.AddValue(s, Trans.TransFromJavaToJSON(fa.FieldType, getFiledValue(this, s)));
				}
			}
			attr=getSuperClassAttr(myClassAttr.ClsName);
		}
		//TransToString是不带对象自身名字的！
		return js;
	}
	public String ToJSONString() throws Exception
	{
		//TransToString是不带对象自身名字的！
		return ToJSONWithName().TransToString();
	}
	//需要带名字，和ToJSONString配对使用
	public static jxORMobj GetFromJSON(jxJson js) throws Exception
	{
		if(js==null)return null;
		ORMClassAttr attr=getClassAttr(js.getName());
		if(attr==null)return null;
		jxORMobj obj=New(attr.clsType);
		for(jxJson sub:js)
		{
			FieldAttr fa = attr.Fields.get(sub.getName());
			setFiledValue(obj, sub.getName(), Trans.TransFromJSONToJava(fa.FieldType, sub.getValue()));
		}
		return obj;
	}
	
	//扩展属性的处理，子对象是值对象
	private Map<String,jxJson> CachedExtend=null;
	private jxJson getExtendJSON(String FieldName) throws Exception
	{
		if(CachedExtend==null)CachedExtend=new HashMap<String,jxJson>();		
		jxJson js=CachedExtend.get(FieldName);
		if(js!=null)return js;
		String spv=(String) getFiledValue(this, FieldName);
		if(spv==null)return null;
		js=jxJson.JsonToObject(spv);
		CachedExtend.put(FieldName, js);
		return js;
	}
	protected String getExtendValue(String FieldName,String Purpose) throws Exception
	{
		jxJson js=getExtendJSON(FieldName);
		if(js==null)return null;
		jxJson sub=js.GetSubObject(Purpose);
		if(sub!=null)
			return sub.getValue().toString();
		return null;
	}
	//，属性列为数组，子元素是值对象所组成的对象
	/**
	 * 扩展属性的处理，读取某扩展属性的值
	 * @param FieldName 扩展属性所对应的列，其类型是String，该属性列为json对象数组，每个json对象是的子对象为值对象
	 * @param KeyName 数组中各子对象的键名
	 * @param KeyValue 数组中各子对象的键值
	 * @param Purpose 想查询的该子对象的某列值
	 * @return
	 * @throws Exception
	 */
	protected String getExtendArrayValue(String FieldName,Map<String,String> Keys,String Purpose) throws Exception
	{		
		jxJson js=getExtendArrayNode(FieldName,Keys);
		if(js==null)return null;
		jxJson sub=js.GetSubObject(Purpose);
		if(sub!=null)
			return (String) sub.getValue();
		return null;
	}
	protected jxJson getExtendArrayNode(String FieldName,Map<String,String> Keys) throws Exception
	{
		LinkedList<jxJson> rs=(LinkedList<jxJson>) getExtendArrayList(FieldName,Keys);
		return rs.poll();
	}
	protected List<jxJson> getExtendArrayList(String FieldName,Map<String,String> Keys) throws Exception
	{
		List<jxJson> rs=new LinkedList<jxJson>();
		jxJson js=getExtendJSON(FieldName);
		if(js==null)return rs;
		ArrayList< jxJson> els=js.SubEl();
		for(jxJson j:els)
		{
			jxJson sub=null;
			boolean noequal=false;
			if(Keys!=null)
				for(String k:Keys.keySet())
				{
					if(sub==null)
						sub=j.GetSubObject(k);
					String v=(String) sub.getValue();
					String vk=Keys.get(k);
					if(v!=null&&vk!=null&&v.compareTo(vk)==0)
						continue;
					else
					{
						noequal=true;
						break;
					}
				}
			if(!noequal)
				rs.add(j);
		}
		return rs;
	}
	/**
	 * 单行（对象），设置某子对象的值，没有则增加，已有则修改，调用后要注意手动保存
	 * @param FieldName
	 * @param Purpose
	 * @param value
	 * @throws Exception
	 */
	protected void setExtendValue(String FieldName,String Purpose,Object value) throws Exception
	{
		jxJson js=getExtendJSON(FieldName);
		if(js==null)js=jxJson.GetObjectNode(FieldName);
		jxJson sub=js.GetSubObject(Purpose);
		if(sub!=null)
			sub.setValue(value);
		else
			js.AddValue(Purpose, value);
		CachedExtend.put(FieldName, js);
		setFiledValue(this, FieldName,js.TransToString());
	}
	/**
	 * 多行（数组对象，每行为一个对象）在行中设置值，如果没有该行则增加，有则修改，调用后要注意手动保存
	 * @param FieldName
	 * @param Keys
	 * @param Purpose
	 * @param value
	 * @throws Exception
	 */
	protected void setExtendArrayValue(String FieldName,Map<String,String> Keys,String Purpose,Object value) throws Exception
	{
		if(Keys==null)return;
		jxJson js=getExtendJSON(FieldName);
		if(js==null)
			js=jxJson.GetArrayNode(FieldName);
		boolean nofind=true;
		ArrayList< jxJson> els=js.SubEl();
		for(jxJson j:els)
		{
			jxJson sub=null;
			boolean noequal=false;
			for(String k:Keys.keySet())
			{
				if(sub==null)
					sub=j.GetSubObject(k);
				String v=(String) sub.getValue();
				String vk=Keys.get(k);
				if(v!=null&&vk!=null&&v.compareTo(vk)==0)
					continue;
				else
				{
					noequal=true;
					break;
				}
			}
			if(!noequal)
			{
				sub=j.GetSubObject(Purpose);
				if(sub!=null)
					sub.setValue(value);
				else
					j.AddValue(Purpose, value);
				nofind=false;
				break;
			}
		}
		if(nofind)
		{
			jxJson el=jxJson.GetObjectNode("sub");
			js.AddArrayElement(el);
			for(String k:Keys.keySet())
				el.AddValue(k, Keys.get(k));
			el.AddValue(Purpose, value);
		}
		CachedExtend.put(FieldName, js);
		setFiledValue(this, FieldName,js.TransToString());
	}
	protected void addExtendArraySubNode(String FieldName,jxJson subNode) throws Exception
	{
		jxJson js=getExtendJSON(FieldName);
		if(js==null)
			js=jxJson.GetArrayNode(FieldName);
		js.AddSubObjNode(subNode);
		CachedExtend.put(FieldName, js);
		setFiledValue(this, FieldName,js.TransToString());
	}

	/**
	 * 多行（数组对象，每行为一个对象）给行增加一个子节点，调用后要注意手动保存
	 * @param FieldName
	 * @param Keys
	 * @param subNode
	 * @throws Exception
	 */
	protected void setExtendArraySubNode(String FieldName,Map<String,String> Keys,jxJson subNode) throws Exception
	{
		if(Keys==null)return;
		jxJson js=getExtendJSON(FieldName);
		if(js==null)
			js=jxJson.GetArrayNode(FieldName);
		boolean nofind=true;
		ArrayList< jxJson> els=js.SubEl();
		for(jxJson j:els)
		{
			jxJson sub=null;
			boolean noequal=false;
			for(String k:Keys.keySet())
			{
				if(sub==null)
					sub=j.GetSubObject(k);
				String v=(String) sub.getValue();
				String vk=Keys.get(k);
				if(v!=null&&vk!=null&&v.compareTo(vk)==0)
					continue;
				else
				{
					noequal=true;
					break;
				}
			}
			if(!noequal)
			{				
				//如果已经添加过了，则更新
				j.AddSubObjNode(subNode);
				nofind=false;
				break;
			}
		}
		if(nofind)
		{			
			jxJson el=jxJson.GetObjectNode("sub");
			js.AddArrayElement(el);
			for(String k:Keys.keySet())
				el.AddValue(k, Keys.get(k));
			el.AddSubObjNode(subNode);
		}
		CachedExtend.put(FieldName, js);
		setFiledValue(this, FieldName,js.TransToString());
	}
	protected void delExtendArraySubNode(String FieldName,Map<String,String> Keys) throws Exception
	{
		if(Keys==null)return;
		jxJson js=getExtendJSON(FieldName);
		if(js==null)
			js=jxJson.GetArrayNode(FieldName);
		boolean find=false;
		ArrayList< jxJson> els=js.SubEl();
		for(jxJson j:els)
		{
			jxJson sub=null;
			boolean noequal=false;
			for(String k:Keys.keySet())
			{
				if(sub==null)
					sub=j.GetSubObject(k);
				String v=(String) sub.getValue();
				String vk=Keys.get(k);
				if(v!=null&&vk!=null&&v.compareTo(vk)==0)
					continue;
				else
				{
					noequal=true;
					break;
				}
			}
			if(!noequal)
			{
				js.RemoveArrayElement(j);
				find=true;
				break;
			}
		}
		if(find)
		{
			CachedExtend.put(FieldName, js);
			setFiledValue(this, FieldName,js.TransToString());
		}
	}

	//获取通用信息
	static FieldAttr getFieldAttr(String ClassName,String FieldName)
	{
		ORMClassAttr attr=getClassAttr(ClassName);
		while(attr!=null)
		{
			FieldAttr fa = attr.Fields.get(FieldName);
			if(fa!=null)
				return fa;
			attr=ClassAttrTree.get(attr.SuperClassName);
		}
		return null;
	}
	static String GetClassName(String ClassName,String ColName)
	{
		ORMClassAttr attr=getClassAttr(ClassName),p=null;
		while(attr!=null)
		{
			FieldAttr f=attr.Fields.get(ColName);
			if(f!=null)
				p=attr;
			if(attr.SuperClassName!=null)
				attr=getClassAttr(attr.SuperClassName);
			else				
				attr=null;
		}
		if(p!=null)
			return p.ClsName;
		return null;
	}
	static ORMClassAttr getClassAttr(int typeid)
	{
		return ClassAttrTreeByTypeID.get(typeid);
	}
	static ORMClassAttr getClassAttr(String ClassName)
	{
		if(ClassName==null)return null;
		return ClassAttrTree.get(ClassName);
	}
	static ORMClassAttr getClassAttr(Class<?> cls)
	{
		return ClassAttrTree.get(utils.GetClassName(cls));
	}
	static ORMClassAttr getSuperClassAttr(String ClassName)
	{
		ORMClassAttr attr = ClassAttrTree.get(ClassName);
		if(attr!=null&&attr.SuperClassName!=null)
			return ClassAttrTree.get(attr.SuperClassName);
		return null;
	}
	
	static Object getFiledValue(jxORMobj obj,String FieldName) throws Exception
	{
		ORMClassAttr attr=obj.myClassAttr;
		while(attr!=null)
		{
			FieldAttr fa=attr.Fields.get(FieldName);
			if(fa!=null)
				return fa.field.get(obj);			
			attr=getClassAttr(attr.SuperClassName);
		}
		return null;
	}
	static void setFiledValue(jxORMobj obj,String FieldName,Object value) throws Exception
	{
		ORMClassAttr attr=obj.myClassAttr;
		while(attr!=null)
		{
			FieldAttr fa=attr.Fields.get(FieldName);
			if(fa!=null)
			{
				fa.field.set(obj, value);
				obj.NeedSave=true;
				return;
			}
			attr=getClassAttr(attr.SuperClassName);
		}
	}

}

class FieldAttr
{
	Class<?> FieldType=null;
	Field field=null;
	boolean IsEnum=false;
	boolean  Encrypted=false;
	boolean IsPrimaryKey=false;
	ORM.KeyType keyType=ORM.KeyType.None;
}
class ORMClassAttr
{
	String ClsName=null;
	Class<?> clsType=null;
	int TypeID=0;
	String dbTableName=null;
	String SuperClassName=null;
	String AutoGenerateKey=null;
	boolean IsDBGenerateKey=false;
	ArrayList<String> PrimaryKeys=null;
	jxLink<Integer,ArrayList<String>> Indexs=null;
	Map<String,FieldAttr> Fields=new HashMap<String,FieldAttr>();
		
	String getDBTableName(TopSpace ts)
	{
		if(ts==null)return dbTableName;
		return dbTableName+"_"+Trans.TransToString(ts.UniqueID);
	}
}

class ForDualMsg implements IMsgHandle
{
	jxORMobj o=null;
	ForDualMsg(jxORMobj obj)		
	{
		o=obj;
	}
	@Override
	public void Do(jxMsg msg) 
	{
		o.DualMsg(msg);
	}		
}

