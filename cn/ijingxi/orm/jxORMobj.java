
package cn.ijingxi.orm;

import cn.ijingxi.app.ObjTag;
import cn.ijingxi.app.SerialNumber;
import cn.ijingxi.common.msg.IMsgDual;
import cn.ijingxi.common.msg.Message;
import cn.ijingxi.orm.ORM.KeyType;
import cn.ijingxi.util.*;

import java.lang.reflect.Field;
import java.util.*;


public class jxORMobj implements IMsgDual
{
	//延迟5s保存，延迟保存只是在突发密集操作时才有效，必然设置用户个人属性等
	private static final int DelaySaveSecond=5;
	private static Map<String,ORMClassAttr> ClassAttrTree=new HashMap<String,ORMClassAttr>();
	private static Map<Integer,ORMClassAttr> ClassAttrTreeByTypeID=new HashMap<Integer,ORMClassAttr>();

	//如果对象被缓存，则当对象发生变化时进行通知
	private static Queue<IDo> changeInform_cache=new LinkedList<>();
	//类型ID到类型名的解释
	private static Map<Integer,String> typeNames=new HashMap<>();

	private ORMClassAttr myClassAttr=null;
	private Queue<Object> params=null;
	private Map<String,Object> backupparams=null;
	//延迟保存中，用于确定是否还需要执行保存动作（因可能有多个延迟保存命令陆续发出）
	private boolean NeedSave=false;
	
	private static LRU myLRU=new LRU();
	/*
	//如果是null则是本机自有对象，否则是其它人所拥有的对象
	private UUID OwnerID=null;
	public void isetOwnerID(UUID OwnerID){this.OwnerID=OwnerID;}
	*/
	//private static byte[] bs=null;

	/**
	 * 当对象被重建后被调用，如果一个对象创建后需要立刻被使用（而不是被保存到数据库中），则需要显示调用本函数
	 * @param db
	 * @throws Exception
	 */
	public void initInObjRebuild(DB db) throws Exception {
		registerMsg();
		myInit(db);
	}
	
	//从数据库中读出后的初始化，对象的初始化通过其构造函数进行即可
	protected void myInit(DB db) throws Exception{}
	/**
	 * 初始化时被调用，如果该函数需要操作数据库则db一定不能为空，否则如果在一个事务中创建函数则可能会引起死锁
	 * @throws Exception
	 */
	protected void Init_Create(DB db) throws Exception{}

	public void DualMsg(Message msg) { }
	public void registerMsg(){ }

	/**
	 * 系统消息分发
	 * @return 用于组织消息处理链条，返回true则已经处理完毕了，不必继续处理
	public boolean DualMsg(jxMsg msg) throws Exception
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
	protected boolean DualTextMsg(jxMsg msg) throws Exception{return false;}
	protected boolean DualRichTextMsg(jxMsg msg) throws Exception{return false;}
	protected boolean DualEventMsg(jxMsg msg) throws Exception{return false;}
	protected boolean DualSyncMsg(jxMsg msg) throws Exception{return false;}
	protected boolean DualReportMsg(jxMsg msg) throws Exception{return false;}
	 */

	//加密处理，加密解密是发生在从数据库中读出与写入之时
	static String Encrypt(String str) throws Exception
	{
		return str;
		/*
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
		*/
	}
	//通过orm中的设置确定是否需要加密，原则上只对字符串进行加密，为了便于查找时的比较
	static Object EncryptField(FieldAttr fa,Object value) throws Exception
	{
		if(value!=null)
			if(fa.Encrypted&&value instanceof String)
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
		return str;
		/*
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
		*/
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
		return DeEncryptField(fa, value);
	}

	public ObjTag getTag(DB db,int tagID) throws Exception {
		if(tagID==0)return null;
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag");
		s.AddContion("ObjTag", "ID", jxCompare.Equal, getID());
		s.AddContion("ObjTag", "TagID", jxCompare.Equal, tagID);
		return (ObjTag) Get(db, ObjTag.class, s);
	}
	public ObjTag getTag(DB db,String tag) throws Exception {
		return getTag(db,ObjTag.getTagID(tag));
	}
	public ObjTag addTag(DB db,int tagID,String Category,String Name,String Descr) throws Exception {
		ObjTag tag= (ObjTag) ObjTag.Create(db,ObjTag.class);
		tag.ObjTypeID=getTypeID();
		tag.ObjID=getID();
		tag.TagID=tagID;
		tag.Category=Category;
		tag.Name=Name;
		tag.Descr=Descr;
		tag.Insert(db);
		return tag;
	}

	
	public void setObj(String FieldName,jxORMobj obj) throws Exception
	{
		setExtendValue(FieldName,"Obj",obj.ToJSONString());
	}	
	public jxORMobj getObj(String FieldName) throws Exception
	{
		String str=getExtendValue(FieldName,"Obj");
		if(str!=null)
			return GetFromJSONString(str);
		return null;
	}
	
	
	//默认主键名为ID
	public UUID getID()
	{
		try {
			return (UUID) getFiledValue(this, "ID");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public String getName()
	{
		try {
			return (String) getFiledValue(this, "Name");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getTypeID()
	{
		return myClassAttr.TypeID;
	}

	public static int getTypeID(String clsName)
	{
		ORMClassAttr ca = ClassAttrTree.get(clsName);
		if(ca!=null)
			return ca.TypeID;
		return 0;
	}
	public static int getTypeID(Class<?> cls)
	{
		return getTypeID(utils.GetClassName(cls));
	}

	public static String getTypeName(int typeid){
		return typeNames.get(typeid);
	}
	public static void InitClass(int typeid,Class<?> cls,String typeName) throws Exception
	{
		utils.Check(typeNames.containsKey(typeid),
				"Type ID已存在："+typeid+"，类型名："+typeNames.get(typeid));
		typeNames.put(typeid,typeName);
		Field[] fs = cls.getDeclaredFields();
		//if(fs==null||fs.length==0)return;
		String classname=utils.GetClassName(cls);
		ORMClassAttr attr=new ORMClassAttr();
		attr.TypeID=typeid;
		ClassAttrTree.put(classname, attr);
		ClassAttrTreeByTypeID.put(typeid, attr);
		attr.ClsName=classname;
		attr.clsType=cls;		
		
		if(fs!=null&&fs.length>0)
		for(Field f:fs)
		{
			String fn=f.getName();
			ORM ann = f.getAnnotation(ORM.class);
			if(ann!=null)
			{
				//if(bs==null)
				//	bs=",PRIMARY KEY(".getBytes("UTF8");
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
				}
				else if(fa.keyType==KeyType.AutoSystemGenerated)
				{
					fa.IsPrimaryKey=true;
					attr.AutoGenerateKey=fn;
					if(attr.PrimaryKeys==null)
						attr.PrimaryKeys=new ArrayList<String>();
					attr.PrimaryKeys.add(fn);
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
		if(attr.PrimaryKeys!=null&&attr.PrimaryKeys.size()==1&&attr.PrimaryKeys.get(0).compareTo("ID")==0)
			attr.canCache=true;
	}
	//jxORMobj对象只能通过本函数生成，该函数所生成的都是正常对象，但如果是非拥有者所
	//建立的对象，则通过另外的函数来生成，如申请者发起申请流程，该流程流转到其它人那时就是非拥有者的
	public static jxORMobj Create(DB db,Class<?> cls) throws Exception
	{
		jxORMobj obj=New(cls);
		obj.Init_Create(db);
		return obj;
	}
	//只有明确知道自己的创建函数不需要操作数据库才能使用本函数
	public static jxORMobj Create(Class<?> cls) throws Exception
	{
		jxORMobj obj=New(cls);
		obj.Init_Create(null);
		return obj;
	}
	private static jxORMobj New(Class<?> cls) throws Exception
	{
		String classname=utils.GetClassName(cls);
		ORMClassAttr attr=getClassAttr(classname);
		if(attr==null)return null;
		jxORMobj obj=(jxORMobj) cls.newInstance();
		obj.myClassAttr=attr;
		return obj;
	}

	public static void setChangeInform(IDo dual){
		changeInform_cache.offer(dual);
	}
	private void changeInform(){
		for(IDo inform:changeInform_cache){
			try {
				inform.Do(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static void DropTable(Class<?> cls) throws Exception
	{
		ORMClassAttr attr=getClassAttr(cls);
		if(attr==null||attr.getDBTableName()==null)return;
		DB db=JdbcUtils.GetDB(null,attr);
        synchronized (db)
        {
			try {
				String sql = "DROP TABLE " + attr.getDBTableName();
				Exec(db, sql, null);
			}
			finally {
				db.Release(attr);
			}
        }
	}
	
	public static void CreateTableInDB(Class<?> cls) throws Exception
	{
		ORMClassAttr attr=getClassAttr(cls);
		if(attr==null||attr.getDBTableName()==null)return;
		jxLog.logger.debug(attr.ClsName);
		try
		{
			//如果建表，则先试图将旧表删除
			DropTable(cls);
		}
		catch(Exception e){}
		
		DB db=JdbcUtils.GetDB(null,cls);
        synchronized (db)
        {
			try {
				String pkl = null;
				String cl = null;
				for (String fn : attr.Fields.keySet()) {
					FieldAttr fa = attr.Fields.get(fn);
					if (fa.IsPrimaryKey && attr.PrimaryKeys.size() == 1) {
						pkl = fn + " " + db.TransDataTypeFromJavaToDB(fa.FieldType) + " PRIMARY KEY";
						if (fa.keyType == KeyType.AutoDBGenerated)
							pkl += " " + db.GetDBGeneratedSQL();
					} else
						cl = utils.StringAdd(cl, ",", fn + " " + db.TransDataTypeFromJavaToDB(fa.FieldType));
				}
				if (attr.PrimaryKeys != null && attr.PrimaryKeys.size() > 1) {
					String ksl = null;
					for (String ks : attr.PrimaryKeys)
						ksl = utils.StringAdd(ksl, ",", ks);
					cl += ",PRIMARY KEY(" + ksl + ")";
				}
				String scl = null;
				if (pkl != null && cl != null)
					scl = pkl + "," + cl;
				else if (pkl == null)
					scl = cl;
				else
					scl = pkl;
				String sql = "CREATE TABLE " + attr.getDBTableName() + "(" + scl + ")";
				jxLog.logger.debug(sql);
				Exec(db, sql, null);
				if (attr.Indexs != null) {
					for (LinkNode<Integer, ArrayList<String>> node : attr.Indexs) {
						String il = null;
						for (String index : node.getValue())
							il = utils.StringAdd(il, ",", index);
						sql = "CREATE INDEX index_" + node.getKey() + "_" + attr.getDBTableName() + " ON " + attr.getDBTableName() + " (" + il + ")";
						Exec(db, sql, null);
					}
				}
			}
			finally {
				db.Release(cls);
			}
        }
	}


	//读写删插
	String GetWherePrimaryKey(DB db,ORMClassAttr attr) throws Exception
	{
		if(attr.PrimaryKeys==null)return null;
		if(params==null)
			params=new LinkedList<Object>();
		String w=null;
		for(String s:attr.PrimaryKeys)
		{
			w=utils.StringAdd(w, " And ", s+"=?");
			FieldAttr fa = attr.Fields.get(s);
			Object ov=getFiledValue(this, s);


			Object odb=db.TransValueFromJavaToDB(fa, jxORMobj.Encrypte(attr.ClsName, s, ov));
			if(odb!=null)
				jxLog.logger.debug(s+":"+odb.toString());
			else
				jxLog.logger.debug(s+":null");
			params.offer(odb);
			if(dbBackup.needBackup())
				backupparams.put(s, ov);		
		}
		return w;
	}
	//数据出数据库要做：
	//1、db数据类型到java数据类型的转换
	//2、解密
	public static Queue<jxORMobj> Select(Class<?> cls,SelectSql s) throws Exception
	{
		Queue<jxORMobj> rs=null;
		DB db=JdbcUtils.GetDB(null,cls);
        synchronized (db)
        {
			try{
				rs=Select(db,cls,s);
			}
			finally {
				db.Release(cls);
			}
			return rs;
        }
	}

	/**
	 * 默认只是读取数据进行处理，而不是取出对象保存到LRU中
	 * @param db
	 * @param cls
	 * @param s
	 * @return
	 * @throws Exception
	 */
	public static Queue<jxORMobj> Select(DB db,Class<?> cls,SelectSql s) throws Exception
	{
		return Select(db,cls,s,false);
	}
	public static Queue<jxORMobj> Select(Class<?> cls,SelectSql s,boolean Cache) throws Exception
	{
		Queue<jxORMobj> rs=null;
		DB db=JdbcUtils.GetDB(null,cls);
		synchronized (db)
		{
			try{
				rs=Select(db,cls,s,Cache);}
			finally {
				db.Release(cls);
			}
			return rs;
		}
	}
	/**
	 *
	 * @param db
	 * @param cls
	 * @param s
	 * @param Cache 如果为true则读取对象后需进行初始化并进行缓存，否则只是读取对象后进行数据处理
	 * @return
	 * @throws Exception
	 */
	public static Queue<jxORMobj> Select(DB db,Class<?> cls,SelectSql s,boolean Cache,IDual dual) throws Exception
	{
		Queue<jxORMobj> rs=new LinkedList<jxORMobj>();
		String clsName=utils.GetClassName(cls);
		//String sql = s.GetSql(db,clsName);
		String sql = s.GetSql(db);
		//jxLog.logger.debug("Select", sql);
		
		Queue<jxLink<String, Object>> dbrs = null;
		db = JdbcUtils.GetDB(db,cls);
		try {
			dbrs = db.Search(cls, sql, s.params);
			for (jxLink<String, Object> map : dbrs) {
				jxORMobj obj = New(cls);
				for (LinkNode<String, Object> node : map) {
					Object ov = node.getValue();
					if (ov != null) {
						String ks = (String) node.getKey();
						FieldAttr fa = getFieldAttr(s.Tables, ks);
						if (fa == null)
							continue;
						//utils.P("属性不存在",clsName+":"+ks);
						Object dv = db.TransValueFromDBToJava(fa, ov);
						Object ev = DeEncryptField(fa, dv);
						setFiledValue(obj, ks, ev);
						if (dual != null)
							dual.Do(obj, ks, ev);
					}
				}
				if (Cache && obj.myClassAttr.canCache) {
					jxORMobj no = null;
					no = myLRU.put(obj);
					if (no == null)
						obj.initInObjRebuild(db);
					else
						obj = no;
				} else
					obj.initInObjRebuild(db);
				rs.offer(obj);
			}
			jxLog.logger.debug("select result:" + rs.size());
		}
		finally {
			db.Release(cls);
		}
		return rs;
	}
	public static Queue<jxORMobj> Select(Class<?> cls,SelectSql s,boolean Cache,IDual dual) throws Exception{
		DB db=JdbcUtils.GetDB(null,cls);
		synchronized (db)
		{
			try {
				Queue<jxORMobj> rs = Select(db, cls, s, Cache, dual);
				return rs;
			}
			finally {
				db.Release(cls);
			}
		}
	}
	public static Queue<jxORMobj> Select(DB db,Class<?> cls,SelectSql s,boolean Cache) throws Exception
	{
		return Select(db,cls,s,Cache,null);
	}
	/**
	 * 只能用于单一对象的读取
	 * @param db
	 * @param cls
	 * @param keys
	 * @return
	 * @throws Exception
	 */
	public static Queue<jxORMobj> Select(DB db,Class<?> cls,Map<String,Object> keys) throws Exception{
		SelectSql s=new SelectSql();
		s.AddTable(utils.GetClassName(cls));
		for(Map.Entry<String, Object> entry : keys.entrySet()){
			s.AddContion(utils.GetClassName(cls), entry.getKey(), jxCompare.Equal, entry.getValue());
		}
		return Select(db,cls,s);
	}
	public static Queue<jxORMobj> Select(Class<?> cls,Map<String,Object> keys) throws Exception{
		SelectSql s=new SelectSql();
		s.AddTable(utils.GetClassName(cls));
		for(Map.Entry<String, Object> entry : keys.entrySet()){
			s.AddContion(utils.GetClassName(cls), entry.getKey(), jxCompare.Equal, entry.getValue());
		}
		return Select(cls,s);
	}
	public static jxORMobj Get(Class<?> cls,Map<String,Object> keys) throws Exception{
		SelectSql s=new SelectSql();
		s.AddTable(utils.GetClassName(cls));
		for(Map.Entry<String, Object> entry : keys.entrySet()){
			s.AddContion(utils.GetClassName(cls), entry.getKey(), jxCompare.Equal, entry.getValue());
		}
		Queue<jxORMobj> list = Select(cls, s);
		if(list.size()==1)
			return list.poll();
		return null;
	}
	public static Queue<jxORMobj> Select(Class<?> cls,SelectSql s,String FieldName,Map<String,Object> extkeys) throws Exception{
		Queue<jxORMobj> list = Select(cls, s);
		if(extkeys==null||extkeys.size()==0)return list;
		Queue<jxORMobj> rs=new LinkedList<>();
		for(jxORMobj o:list)
			if(o.judgeExtendValue(FieldName,extkeys))
				rs.offer(o);
		return rs;
	}
	public static Queue<jxORMobj> Select(Class<?> cls,Map<String,Object> keys,String FieldName,Map<String,Object> extkeys) throws Exception{
		Queue<jxORMobj> list = Select(cls, keys);
		if(extkeys==null||extkeys.size()==0)return list;
		Queue<jxORMobj> rs=new LinkedList<>();
		for(jxORMobj o:list)
			if(o.judgeExtendValue(FieldName,extkeys))
				rs.offer(o);
		return rs;
	}

	public static Queue<jxORMobj> Sort_FieldInt(Queue<jxORMobj> ol,String fieldName) throws Exception
	{
		jxLink<Integer,jxORMobj> rl=null;
		for(jxORMobj obj:ol)
		{
			rl=addObjToSortList_FieldInt(rl, fieldName, obj);
		}
		if(rl!=null)
		{
			Queue<jxORMobj> rs=new LinkedList<jxORMobj>();
			for(LinkNode<Integer,jxORMobj> node:rl)
			{
				rs.add(node.getValue());
			}
			return rs;
		}
		return new LinkedList<jxORMobj>();
	}
	public static Queue<jxORMobj> Sort_FieldString(Queue<jxORMobj> ol,String fieldName) throws Exception
	{
		jxLink<String,jxORMobj> rl=null;
		for(jxORMobj obj:ol)
		{
			rl=addObjToSortList_FieldString(rl, fieldName, obj);
		}
		if(rl!=null)
		{
			Queue<jxORMobj> rs=new LinkedList<jxORMobj>();
			for(LinkNode<String,jxORMobj> node:rl)
			{
				rs.add(node.getValue());
			}
			return rs;
		}
		return new LinkedList<jxORMobj>();
	}
	public static jxORMobj Max_FieldInt(Queue<jxORMobj> ol,String fieldName) throws Exception
	{
		jxLink<Integer,jxORMobj> rl=null;
		for(jxORMobj obj:ol)
		{
			rl=addObjToSortList_FieldInt(rl, fieldName, obj);
		}
		if(rl!=null)
			return rl.LastValue();
		return null;
	}	
	public static jxORMobj Max_FieldString(Queue<jxORMobj> ol,String fieldName) throws Exception
	{
		jxLink<String,jxORMobj> rl=null;
		for(jxORMobj obj:ol)
		{
			rl=addObjToSortList_FieldString(rl, fieldName, obj);
		}
		if(rl!=null)
			return rl.LastValue();
		return null;
	}
	public static jxORMobj Min_FieldInt(Queue<jxORMobj> ol,String fieldName) throws Exception
	{
		jxLink<Integer,jxORMobj> rl=null;
		for(jxORMobj obj:ol)
		{
			rl=addObjToSortList_FieldInt(rl, fieldName, obj);
		}
		if(rl!=null)
			return rl.FirstValue();
		return null;
	}	
	public static jxORMobj Min_FieldString(Queue<jxORMobj> ol,String fieldName) throws Exception
	{
		jxLink<String,jxORMobj> rl=null;
		for(jxORMobj obj:ol)
		{
			rl=addObjToSortList_FieldString(rl, fieldName, obj);
		}
		if(rl!=null)
			return rl.FirstValue();
		return null;
	}
	
	public static Queue<jxORMobj> Sort_ExtraInt(Queue<jxORMobj> ol,String fieldName,String Purpose) throws Exception
	{
		jxLink<Integer,jxORMobj> rl=null;
		for(jxORMobj obj:ol)
		{
			rl=addObjToSortList_ExtraInt(rl, fieldName, Purpose, obj);
		}
		if(rl!=null)
		{
			Queue<jxORMobj> rs=new LinkedList<jxORMobj>();
			for(LinkNode<Integer,jxORMobj> node:rl)
			{
				rs.add(node.getValue());
			}
			return rs;
		}
		return new LinkedList<jxORMobj>();
	}
	public static Queue<jxORMobj> Sort_ExtraString(Queue<jxORMobj> ol,String fieldName,String Purpose) throws Exception
	{
		jxLink<String,jxORMobj> rl=null;
		for(jxORMobj obj:ol)
		{
			rl=addObjToSortList_ExtraString(rl, fieldName, Purpose, obj);
		}
		if(rl!=null)
		{
			Queue<jxORMobj> rs=new LinkedList<jxORMobj>();
			for(LinkNode<String,jxORMobj> node:rl)
			{
				rs.add(node.getValue());
			}
			return rs;
		}
		return new LinkedList<jxORMobj>();
	}
	public static jxORMobj Max_ExtraInt(Queue<jxORMobj> ol,String fieldName,String Purpose) throws Exception
	{
		jxLink<Integer,jxORMobj> rl=null;
		for(jxORMobj obj:ol)
		{
			rl=addObjToSortList_ExtraInt(rl, fieldName, Purpose, obj);
		}
		if(rl!=null)
			return rl.LastValue();
		return null;
	}	
	public static jxORMobj Max_ExtraString(Queue<jxORMobj> ol,String fieldName,String Purpose) throws Exception
	{
		jxLink<String,jxORMobj> rl=null;
		for(jxORMobj obj:ol)
		{
			rl=addObjToSortList_ExtraString(rl, fieldName, Purpose, obj);
		}
		if(rl!=null)
			return rl.LastValue();
		return null;
	}
	public static jxORMobj Min_ExtraInt(Queue<jxORMobj> ol,String fieldName,String Purpose) throws Exception
	{
		jxLink<Integer,jxORMobj> rl=null;
		for(jxORMobj obj:ol)
		{
			rl=addObjToSortList_ExtraInt(rl, fieldName, Purpose, obj);
		}
		if(rl!=null)
			return rl.FirstValue();
		return null;
	}	
	public static jxORMobj Min_ExtraString(Queue<jxORMobj> ol,String fieldName,String Purpose) throws Exception
	{
		jxLink<String,jxORMobj> rl=null;
		for(jxORMobj obj:ol)
		{
			rl=addObjToSortList_ExtraString(rl,fieldName,Purpose,obj);
		}
		if(rl!=null)
			return rl.FirstValue();
		return null;
	}
	

	private static jxLink<Integer,jxORMobj> addObjToSortList_FieldInt(jxLink<Integer,jxORMobj> list,String fieldName,jxORMobj obj) throws Exception
	{
		if(list==null)
			list=new  jxLink<Integer,jxORMobj>();
		list.addByRise((Integer) getFiledValue(obj, fieldName), obj);
		return list;
	}	
	private static jxLink<String,jxORMobj> addObjToSortList_FieldString(jxLink<String,jxORMobj> list,String fieldName,jxORMobj obj) throws Exception
	{
		if(list==null)
			list=new  jxLink<String,jxORMobj>();
		list.addByRise((String) getFiledValue(obj, fieldName), obj);
		return list;
	}
	private static jxLink<Integer,jxORMobj> addObjToSortList_ExtraInt(jxLink<Integer,jxORMobj> list,String fieldName,String Purpose,jxORMobj obj) throws Exception
	{
		if(list==null)
			list=new  jxLink<Integer,jxORMobj>();
		list.addByRise(Trans.TransToInteger(obj.getExtendValue(fieldName, Purpose)), obj);
		return list;
	}	
	private static jxLink<String,jxORMobj> addObjToSortList_ExtraString(jxLink<String,jxORMobj> list,String fieldName,String Purpose,jxORMobj obj) throws Exception
	{
		if(list==null)
			list=new  jxLink<String,jxORMobj>();
		list.addByRise(obj.getExtendValue(fieldName,Purpose), obj);
		return list;
	}
	
	public static int GetCount(Class<?> cls,SelectSql s) throws Exception
	{
		int rs=0;
		DB db=JdbcUtils.GetDB(null,cls);
        synchronized (db)
        {
			try {
				String clsName=utils.GetClassName(cls);
				String sql = s.GetSql_Count(db,clsName);
				rs=db.GetRS_int(sql, s.params);
			}
			finally {
				db.Release(cls);
			}
			return rs;
        }
	}
	public static jxORMobj Get(Class<?> cls,SelectSql s) throws Exception
	{
		Queue<jxORMobj> rs=null;
		DB db=JdbcUtils.GetDB(null,cls);
        synchronized (db)
        {
			try {
				rs = Select(db, cls, s, false);
			}
			finally {
				db.Release(cls);
			}
			return rs.poll();
        }
	}
	public static jxORMobj Get(DB db,Class<?> cls,SelectSql s) throws Exception
	{
		Queue<jxORMobj> rs=Select(db, cls, s, false);
		return rs.poll();
	}
	public static jxORMobj GetByID(Class<?> cls,UUID ID) throws Exception {
		jxORMobj cachers = myLRU.get(ID);
		if (cachers != null)
			return cachers;
		jxORMobj obj = null;
		DB db = JdbcUtils.GetDB(null, cls);
		synchronized (db) {
			try {
				obj = GetByID(db, cls, ID);
			} finally {
				db.Release(cls);
			}
			return obj;
		}
	}
	public static jxORMobj GetByID(String clsName,UUID ID) throws Exception {
		jxORMobj cachers = myLRU.get(ID);
		if (cachers != null)
			return cachers;
		Queue<jxORMobj> rs = null;
		DB db = JdbcUtils.GetDB(null, myLRU);
		synchronized (db) {
			try {
				ORMClassAttr attr = getClassAttr(clsName);
				SelectSql s = new SelectSql();
				s.AddTable(attr.ClsName);
				s.AddContion(attr.ClsName, attr.PrimaryKeys.get(0), jxCompare.Equal, ID);
				rs = Select(db, attr.clsType, s, true);
			} finally {
				db.Release(myLRU);
			}
			return rs.poll();
		}
	}
	public static jxORMobj GetByID(DB db,Class<?> cls,UUID ID) throws Exception {
		jxORMobj cachers = myLRU.get(ID);
		if (cachers != null)
			return cachers;
		Queue<jxORMobj> rs = null;
		db = JdbcUtils.GetDB(db, myLRU);
		try {
			ORMClassAttr attr = getClassAttr(cls);
			SelectSql s = new SelectSql();
			s.AddTable(attr.ClsName);
			s.AddContion(attr.ClsName, attr.PrimaryKeys.get(0), jxCompare.Equal, ID);
			rs = Select(db, cls, s, true);
		} finally {
			db.Release(myLRU);
		}
		return rs.poll();
	}
	public static jxORMobj GetByID(int typeid,UUID id) throws Exception {
		Queue<jxORMobj> rs = null;
		jxORMobj cachers = myLRU.get(id);
		if (cachers != null)
			return cachers;
		DB db = JdbcUtils.GetDB(null, myLRU);
		synchronized (db) {
			try {
				ORMClassAttr attr = getClassAttr(typeid);
				SelectSql s = new SelectSql();
				s.AddTable(attr.ClsName);
				s.AddContion(attr.ClsName, attr.PrimaryKeys.get(0), jxCompare.Equal, id);
				rs = Select(db, attr.clsType, s, true);
			} finally {
				db.Release(myLRU);
			}
			return rs.poll();
		}
	}
	//数据校验
	protected void Verify() throws Exception
	{	
		
	}
	//延迟3分钟后再保存
	public void DelaySave()
	{
		NeedSave=true;
		jxTimer.DoAfter(DelaySaveSecond, param -> {
            if(NeedSave)
                Update();
        },null);
	}
	
	public void Update() throws Exception
	{
		Update(null);
	}
	//数据进数据库要做：
	//1、加密
	//2、java数据类型到db数据类型的转换
	public void Update(DB db) throws Exception
	{
		db=JdbcUtils.GetDB(db,this);
		synchronized (db) {
			try {
				Verify();
				ORMClassAttr attr = myClassAttr;
				while (attr != null) {
					Update(db, attr);
					if (attr.SuperClassName != null)
						attr = getClassAttr(attr.SuperClassName);
					else
						break;
				}
				NeedSave = false;
			}
			finally {
				db.Release(this);
			}
		}
		changeInform();
	}
	private void Update(DB db,ORMClassAttr attr) throws Exception
	{
		if(attr.getDBTableName()==null)return;
		params=new LinkedList<Object>();
		String sql="Update "+attr.getDBTableName()+" Set ";
		//if(attr.sql_Update==null)
		//	sql="Update "+attr.getDBTableName(ts)+" Set ";
		if(dbBackup.needBackup())
			backupparams=new HashMap<String,Object>();
		String v=null;
		for(String fn:attr.Fields.keySet())
		{
			FieldAttr fa = attr.Fields.get(fn);
			if(fa!=null&&fa.keyType==KeyType.None)
			{
				//if(attr.sql_Update==null)
				v=utils.StringAdd(v, ",", fn+"=?");
				Object ov=getFiledValue(this, fn);
				Object odb=db.TransValueFromJavaToDB(fa,jxORMobj.Encrypte(attr.ClsName, fn, ov));
				params.offer(odb);
				/*
				if(odb!=null)
					jxLog.logger.debug(fn+":"+odb.toString());
				else
					jxLog.logger.debug(fn+":null");
				*/

				//if(dbBackup.needBackup())
				//	backupparams.put(fn, ov);
			}
		}
		/*
		//优化时由于GetWherePrimaryKey会设置参数，所以这里是不能缓存的
		if(attr.sql_Update==null)
		{
			sql+=v+" Where "+GetWherePrimaryKey(db);
			attr.sql_Update=sql;
		}
		else
			sql=attr.sql_Update;
		*/
		sql+=v+" Where "+GetWherePrimaryKey(db,attr);

		Exec(db,sql,params);
		//dbBackup.backup(attr.ClsName, sql, backupparams);
	}
	public void Delete() throws Exception
	{
		Delete(null);
	}
	public void Delete(DB db) throws Exception
	{
		db=JdbcUtils.GetDB(db,this);
		synchronized (db) {
			try {
				ORMClassAttr attr = myClassAttr;
				while (attr != null) {
					Delete(db, attr);
					if (attr.SuperClassName != null)
						attr = getClassAttr(attr.SuperClassName);
					else
						break;
				}
			}
			finally {
				db.Release(this);
			}
		}
		changeInform();
	}
	private void Delete(DB db,ORMClassAttr attr) throws Exception
	{
		if(attr.getDBTableName()==null)return;
		params=new LinkedList<Object>();
		//if(dbBackup.needBackup())
		//	backupparams=new HashMap<String,Object>();
		String sql="Delete From "+attr.getDBTableName()+" Where "+GetWherePrimaryKey(db,attr);
		/* 缓存还需设置参数
		if(attr.sql_Delete==null)
		{
			sql="Delete From "+attr.getDBTableName(ts)+" Where "+GetWherePrimaryKey(db);
			attr.sql_Delete=sql;
		}
		else
			sql=attr.sql_Delete;
		*/
		Exec(db,sql,params);
		//dbBackup.backup(attr.ClsName, sql, backupparams);
	}
	public static void Delete(DB db,Class<?> cls,Map<String,Object> keys) throws Exception {
		SelectSql s=new SelectSql();
		s.AddTable(utils.GetClassName(cls));
		for(Map.Entry<String, Object> entry : keys.entrySet()){
			s.AddContion(utils.GetClassName(cls), entry.getKey(), jxCompare.Equal, entry.getValue());
		}
		String sql="Delete "+s.GetSql_From()+s.GetSql_Where(db);
		Exec(db,sql,null);
	}

	/**
	 * 清空数据
	 * @throws Exception
	 */
	public void Clear() throws Exception
	{
		Clear(null);
	}
	public void Clear(DB db) throws Exception
	{
		db=JdbcUtils.GetDB(db,this);
		synchronized (db) {
			try {
				ORMClassAttr attr = myClassAttr;
				while (attr != null) {
					Clear(db, attr);
					if (attr.SuperClassName != null)
						attr = getClassAttr(attr.SuperClassName);
					else
						break;
				}
			}
			finally {
				db.Release(this);
			}
		}
	}
	private void Clear(DB db,ORMClassAttr attr) throws Exception
	{
		if(attr.getDBTableName()==null)return;
		String sql="Delete * From "+attr.getDBTableName();
		Exec(db,sql,params);
	}
	//数据进数据库要做：
	//1、加密
	//2、java数据类型到db数据类型的转换
	public void Insert() throws Exception
	{
		Insert(null);
	}
	public void Insert(DB db) throws Exception
	{
		db=JdbcUtils.GetDB(db,this);
		synchronized (db) {
			try {
				Verify();
		/*
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
		*/
				ORMClassAttr attr = myClassAttr;
				while (attr != null) {
					Insert(db, attr);
					if (attr.SuperClassName != null)
						attr = getClassAttr(attr.SuperClassName);
					else
						break;
				}
			}
			finally {
				db.Release(this);
			}
		}
	}
	private void Insert(DB db,ORMClassAttr attr) throws Exception
	{
		if(attr.getDBTableName()==null)return;

		params=new LinkedList<Object>();
		//if(dbBackup.needBackup())
		//	backupparams=new HashMap<String,Object>();
		String cl=null,vl=null;
		for(String s:attr.Fields.keySet())
		{
			FieldAttr fa = attr.Fields.get(s);
			if(fa.keyType==KeyType.AutoDBGenerated)
				continue;
			if(fa.keyType==KeyType.AutoSystemGenerated)
			{
				Object ov=getFiledValue(this,s);
				//if(Trans.JudgeIsDefaultValue(fa.FieldType, ov))
					//如果已设置了值则将其再次插入，主要是用于topspace中的数据复制
					//setFiledValue(this,s, jxSystem.System.GetAutoGeneratedID(attr.ClsName));
			}
			cl=utils.StringAdd(cl, ",", s);
			vl=utils.StringAdd(vl, ",", "?");
			Object v=getFiledValue(this, s);
			Object ev=Encrypte(attr.ClsName, s, v);
			Object dv=db.TransValueFromJavaToDB(fa,ev);

			if(dv!=null)
				jxLog.logger.debug("Param:"+s+ " "+dv.toString());
			else
				jxLog.logger.debug("Param:"+s+" null");

			params.offer(dv);
			//if(dbBackup.needBackup())
			//	backupparams.put(s, v);
		}
				
		String sql="Insert Into "+attr.getDBTableName()+"("+cl+") Values ("+vl+")";
		/*
		if(attr.sql_Insert==null)
		{
			sql="Insert Into "+attr.getDBTableName(ts)+"("+cl+") Values ("+vl+")";
			attr.sql_Insert=sql;
		}
		else
			sql=attr.sql_Insert;
		*/
		Exec(db, sql, params);

		//dbBackup.backup(attr.ClsName, sql, backupparams);
		/*
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
		*/
	}
	
	static boolean Exec(DB db,String sql,Queue<Object> param) throws Exception
	{
		return db.Exec(sql, param);
		/*
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
		*/
	}
	
	public jxJson ToJSONWithName() throws Exception
	{
		jxJson root=jxJson.GetObjectNode("Root");
		root.AddSubObjNode(ToJSON());
		//TransToString是不带对象自身名字的！
		return root;
	}
	private jxJson extJson=null;
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
		if(extJson!=null)
			for(jxJson sub:extJson){
				jxLog.logger.debug("ExtJsonAttr:"+sub.getName()+" " +sub.getValue().toString());
				js.AddSubObjNode(sub);
			}
		//TransToString是不带对象自身名字的！
		return js;
	}
	public void addExtJsonAttr(String attrName,Object v) throws Exception {
		jxLog.logger.debug("addExtJsonAttr:"+attrName+" " +v.toString());
		if(extJson==null)
			extJson=jxJson.GetObjectNode("Extra");
		extJson.setSubObjectValue(attrName,v);
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
	public static jxORMobj GetFromJSONString(String json) throws Exception
	{
		if(json==null)return null;
		return GetFromJSON(jxJson.JsonToObject(json));
	}
	
	//扩展属性的处理，子对象是值对象
	//private Map<String,jxJson> CachedExtend=null;
	protected jxJson getExtendJSON(String FieldName) throws Exception
	{
		//if(CachedExtend==null)CachedExtend=new HashMap<String,jxJson>();
		//jxJson js=CachedExtend.get(FieldName);
		//if(js!=null)return js;
		String spv=(String) getFiledValue(this, FieldName);
		if(spv==null)return null;
		//jxLog.logger.debug(spv);
		jxJson js=jxJson.JsonToObject(spv);
		//jxLog.logger.debug(js.TransToStringWithName());
		//CachedExtend.put(FieldName, js);
		return js;
	}
	public String getExtendValue(String FieldName,String Purpose) throws Exception
	{
		jxJson js=getExtendJSON(FieldName);
		if(js==null)return null;
		jxJson sub=js.GetSubObject(Purpose);
		if(sub!=null)
			return sub.getValue().toString();
		if(extJson!=null)
			return (String) extJson.getSubObjectValue(Purpose);
		return null;
	}
	public boolean judgeExtendValue(String FieldName,String Purpose,Object value) throws Exception{
		String v=getExtendValue(FieldName,Purpose);
		if(value!=null){
			String ov=jxJson.TransValueToJSONString(value);
			return ov.compareTo(v)==0;
		}
		return false;
	}
	public boolean judgeExtendValue(String FieldName,Map<String,Object> extkeys) throws Exception{
		for(Map.Entry<String,Object> e:extkeys.entrySet())
			if(!judgeExtendValue(FieldName,e.getKey(),e.getValue()))
				return false;
		return true;
	}


	//，属性列为数组，子元素是值对象所组成的对象
	/**
	 * 扩展属性的处理，读取某扩展属性的值
	 * @param FieldName 扩展属性所对应的列，其类型是String，该属性列为json对象数组，每个json对象是的子对象为值对象
	 * @param Purpose 想查询的该子对象的某列值
	 * @return
	 * @throws Exception
	 */
	public String getExtendArrayValue(String FieldName,Map<String,String> Keys,String Purpose) throws Exception
	{		
		jxJson js=getExtendArrayNode(FieldName,Keys);
		if(js==null)return null;
		jxJson jarr=js.GetSubObject("Array");
		if(jarr==null) return null;
		jxJson sub=jarr.GetSubObject(Purpose);
		if(sub!=null)
			return (String) sub.getValue();
		return null;
	}
	public jxJson getExtendArrayNode(String FieldName,Map<String,String> Keys) throws Exception
	{
		LinkedList<jxJson> rs=(LinkedList<jxJson>) getExtendArrayList(FieldName,Keys);
		return rs.poll();
	}
	public List<jxJson> getExtendArrayList(String FieldName,Map<String,String> Keys) throws Exception
	{
		return getExtendArrayList(FieldName, "Array", Keys);
	}
	public List<jxJson> getExtendArrayList(String FieldName,String ArrayName,Map<String,String> Keys) throws Exception
	{
		List<jxJson> rs=new LinkedList<jxJson>();
		jxJson js=getExtendJSON(FieldName);
		if(js==null)return rs;
		jxJson jarr=js.GetSubObject(ArrayName);
		if(jarr==null)return null;
		ArrayList< jxJson> els=jarr.SubEl();
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
	public void setExtendValue(String FieldName,String Purpose,Object value) throws Exception
	{
		jxJson js=getExtendJSON(FieldName);
		if(js==null)js=jxJson.GetObjectNode(FieldName);
		jxJson sub=js.GetSubObject(Purpose);
		if(sub!=null)
			sub.setValue(value);
		else
			js.AddValue(Purpose, value);
		//CachedExtend.put(FieldName, js);
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
	public void setExtendArrayValue(String FieldName,Map<String,String> Keys,String Purpose,Object value) throws Exception
	{
		setExtendArrayValue(FieldName, "Array", Keys, Purpose, value);
	}
	public void setExtendArrayValue(String FieldName,String ArrayName,Map<String,String> Keys,String Purpose,Object value) throws Exception
	{
		if(Keys==null)return;
		jxJson jarr=null;
		jxJson js=getExtendJSON(FieldName);
		if(js==null)
			js=jxJson.GetObjectNode(FieldName);
		jarr=js.GetSubObject(ArrayName);
		if(jarr==null)
		{
			jarr=jxJson.GetArrayNode(ArrayName);
			js.AddSubObjNode(jarr);
		}
		boolean nofind=true;
		ArrayList< jxJson> els=jarr.SubEl();
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
		//CachedExtend.put(FieldName, js);
		setFiledValue(this, FieldName,js.TransToString());
	}
	public void addExtendArraySubNode(String FieldName,jxJson subNode) throws Exception
	{
		addExtendArraySubNode(FieldName,"Array",subNode);
	}
	public void addExtendArraySubNode(String FieldName,String ArrayName,jxJson subNode) throws Exception
	{
		jxJson js=getExtendJSON(FieldName);
		jxJson jarr=null;
		if(js==null)
			js=jxJson.GetObjectNode(FieldName);
		jarr=js.GetSubObject(ArrayName);
		if(jarr==null)
		{
			jarr=jxJson.GetArrayNode(ArrayName);
			js.AddSubObjNode(jarr);
		}
		jarr.AddSubObjNode(subNode);
		//CachedExtend.put(FieldName, js);
		setFiledValue(this, FieldName, js.TransToString());
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
		jxJson jarr=null;
		jxJson js=getExtendJSON(FieldName);
		if(js==null)
			js=jxJson.GetObjectNode(FieldName);
		jarr=js.GetSubObject("Array");
		if(jarr==null)
		{
			jarr=jxJson.GetArrayNode("Array");
			js.AddSubObjNode(jarr);
		}
		boolean nofind=true;
		ArrayList< jxJson> els=jarr.SubEl();
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
		//CachedExtend.put(FieldName, js);
		setFiledValue(this, FieldName,js.TransToString());
	}
	protected void delExtendArraySubNode(String FieldName,Map<String,String> Keys) throws Exception
	{
		if(Keys==null)return;
		jxJson js=getExtendJSON(FieldName);
		if(js==null)
			js=jxJson.GetArrayNode(FieldName);
		boolean find=false;
		ArrayList<jxJson> els=js.SubEl();
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
			//CachedExtend.put(FieldName, js);
			setFiledValue(this, FieldName,js.TransToString());
		}
	}

	//获取通用信息
	public static String getFieldType(String ClassName, String FieldName)
	{
		FieldAttr fa = getFieldAttr(ClassName, FieldName);
		if(fa!=null)
			return utils.GetClassName(fa.FieldType);
		return null;
	}	public static String getFieldType(Class<?> cls, String FieldName)
	{
		FieldAttr fa = getFieldAttr(cls,FieldName);
		if(fa!=null)
			return utils.GetClassName(fa.FieldType);
		return null;
	}
	static FieldAttr getFieldAttr(Class<?> cls, String FieldName)
	{
		return getFieldAttr(utils.GetClassName(cls),FieldName);
	}
	static FieldAttr getFieldAttr(jxLink<String,Integer> clsNames, String FieldName)
	{
		if(clsNames!=null)
			for(LinkNode<String,Integer> node:clsNames){
				FieldAttr fa=getFieldAttr(node.getKey(),FieldName);
				if(fa!=null)
					return fa;
			}
		return null;
	}
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
		jxLog.logger.debug("getFieldAttr Null:"+ClassName+"->"+FieldName);
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
		jxLog.logger.debug("GetClassName Null:"+ClassName+"->"+ColName);
		return null;
	}
	public static String getClassName(int typeid)
	{
		ORMClassAttr attr=getClassAttr(typeid);
		if(attr!=null)
			return attr.ClsName;
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
	
	public static Object getFiledValue(jxORMobj obj,String FieldName) throws Exception
	{
		ORMClassAttr attr=obj.myClassAttr;
		while(attr!=null)
		{
			FieldAttr fa=attr.Fields.get(FieldName);
			if(fa!=null)
				return fa.field.get(obj);			
			attr=getClassAttr(attr.SuperClassName);
		}
		throw new Exception("getFiledValue Null:"+obj.myClassAttr.ClsName+"->"+FieldName);
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

	public static Map<String,Object> getMapParam(String k,Object v){
		Map<String,Object> rs=new HashMap<>();
		rs.put(k,v);
		return rs;
	}

	public void setSN(String Purpose,String Model) throws Exception {
		SerialNumber sn= SerialNumber.New(Purpose,Model);
		sn.ObjID=getID();
		sn.Update();
	}
	public SerialNumber getSN() throws Exception {
		SerialNumber sn= (SerialNumber) getTag(null,"序列号");
		return sn;
	}

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
	boolean canCache=false;
	jxLink<Integer,ArrayList<String>> Indexs=null;
	Map<String,FieldAttr> Fields=new HashMap<String,FieldAttr>();

	String getDBTableName()
	{
		//if(ts==null)return dbTableName;
		if(dbTableName==null)return null;
		//return dbTableName+"_"+Trans.TransToString(ts.ID);
		return dbTableName;
	}
}

