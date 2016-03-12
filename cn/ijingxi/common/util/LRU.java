  
package cn.ijingxi.common.util;

import cn.ijingxi.common.orm.jxORMobj;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
   
/**  
 * 对象缓存
 * @author Andrew Xu  
 */   
public class LRU
{
	//默认存储100万个对象
	private int MaxSize=1000000;
	private final int BulkSize=64;
	public void setMaxSize(int size)
    {
		MaxSize=size;
    }

	private int count=0;
	public int getCount(){return count;}

	private Map<UUID,store> all=new HashMap<>();
	private Map<Integer,bulk> bulks=new HashMap<>();

	private int minbulkID=0;
	private bulk currentbulk=null;
	public LRU(){
		currentbulk=new bulk();
		currentbulk.id=1;
		minbulkID=1;
	}

	/**
	 * 添加对象，如果已存在则会更新对象
	 * @param obj
	 * @throws Exception
     */
	public void add(jxORMobj obj) throws Exception
	{		
		if(obj!=null)
			synchronized (this)
			{
				UUID id = obj.getID();
				store s=all.get(id);
				if(s==null)
					s=new store();
				s.obj=obj;
				s.putToBulk(id);
				clearBulk();
			}
	}

	/**
	 * 添加对象，如果对象已存在则不会更新对象而只将其放到最新的桶里
	 * @param obj
	 * @return null:对象不存在，非null:原对象
	 * @throws Exception
     */
	public jxORMobj put(jxORMobj obj) throws Exception
	{
		jxORMobj rs=null;
		if(obj!=null)
			synchronized (this)
			{
				UUID id = obj.getID();
				store s=all.get(id);
				if(s==null){
					s=new store();
					s.obj=obj;
					clearBulk();
				}
				else
					rs=s.obj;
				s.putToBulk(id);
			}
		return rs;
	}
	public jxORMobj remove(UUID id)
	{
		synchronized (this)
		{
			store s=all.get(id);
			if(s!=null){
				bulk b=bulks.get(s.bulkID);
				b.remove(id);
				all.remove(id);
				count--;
				return s.obj;
			}
			return null;
		}
	}
	public jxORMobj get(UUID id)
	{
		synchronized (this)
		{
			store s=all.get(id);
			if(s!=null){
				s.putToBulk(id);
				return s.obj;
			}
			return null;
		}
	}

	private int addToBulk(UUID id){
		boolean rs=currentbulk.add(id);
		if(!rs){
			bulk b=new bulk();
			b.id=getNextNumber(currentbulk.id);
			b.add(id);
			bulks.put(b.id,b);
			currentbulk=b;
		}
		return currentbulk.id;
	}
	private void clearBulk(){
		while (count>MaxSize){
			bulk b=bulks.get(minbulkID);
			if(b!=null){
				int dn=b.drop();
				count-=dn;
			}
			minbulkID=getNextNumber(minbulkID);
		}
	}
	private int getNextNumber(int num){
		if(num>=Integer.MAX_VALUE)
			return 1;
		return num++;
	}

	//存储区按64个存储单元为一个桶，达到容量上限则将最旧的桶给倒了
	private class bulk{
		int id=0;
		int count=0;
		Map<UUID,Boolean> member=new HashMap<>();
		boolean add(UUID id){
			if(count<BulkSize){
				member.put(id,true);
				count++;
				return true;
			}
			return false;
		}
		void remove(UUID id){
			Boolean rs = member.remove(id);
			if(rs){
				count--;
				if(count==0)
					bulks.remove(id);
			}
		}
		int drop(){
			for(Map.Entry<UUID,Boolean> entry:member.entrySet()){
				all.remove(entry.getKey());
			}
			bulks.remove(id);
			return count;
		}
	}
	private class store{
		jxORMobj obj=null;
		int bulkID=0;
		void putToBulk(UUID id){
			if(bulkID>0){
				bulk oldbulk=bulks.get(bulkID);
				oldbulk.remove(id);
			}
			bulkID=addToBulk(id);
		}
	}

}