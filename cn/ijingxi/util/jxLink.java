
package cn.ijingxi.util;

import java.util.Iterator;

public class jxLink<TKey extends Comparable<TKey>, TValue> implements Iterable<LinkNode<TKey, TValue>>
{
	//链中的节点数
	private int Count;
	public int getCount()
    {
        return Count;
    }
	public TKey FirstKey()
    {
		if(SonNodeList!=null)
			return SonNodeList.getKey();
        return null;
    }
	public TValue FirstValue()
    {
		if(SonNodeList!=null)
			return SonNodeList.getValue();
        return null;
    }
	public TKey LastKey()
    {
		if(LastSonNode!=null)
			return LastSonNode.getKey();
        return null;
    }
	public TValue LastValue()
    {
		if(LastSonNode!=null)
			return LastSonNode.getValue();
        return null;
    }

	//是否允许存在相等的key
	public boolean permitKeyEqual=false;
		
    //子节点拉链
    LinkNode<TKey, TValue> SonNodeList = null;
	public LinkNode<TKey, TValue> getFirst()
    {
        return SonNodeList;
    }	
    //最后一个子节点
    LinkNode<TKey, TValue> LastSonNode = null;
	public LinkNode<TKey, TValue> getLast()
    {
        return LastSonNode;
    }	

    LinkNode<TKey, TValue> searchNode(TKey Key)
	{
		LinkNode<TKey, TValue> node=SonNodeList;
		while(node!=null)
		{
			if(Key.compareTo( node.getKey())==0)
				return node;
			node=node.Next;			
		}
		return null;		
	}
	
    public TValue search(TKey Key)
    {
    	LinkNode<TKey, TValue> node=searchNode(Key);
    	if(node!=null)
    		return node.getValue();
		return null; 
    }

    public boolean Exist(TKey Key)
    {
    	LinkNode<TKey, TValue> node=searchNode(Key);
    	return node!=null;
    }
    /**
	 * 尾部添加，头部移除
     * 直接在尾部加入
     * @param Key
     * @param Value
     */
    public void offer(TKey Key,TValue Value)
    {
		synchronized (this)
        {
			if(SonNodeList==null)
			{
				SonNodeList=new LinkNode<TKey, TValue>(Key,Value);
				LastSonNode=SonNodeList;
			}
			else
			{
				LinkNode<TKey, TValue> n=new LinkNode<TKey, TValue>(Key,Value);
				LastSonNode.Next=n;
				n.Prev=LastSonNode;
				LastSonNode=n;
			}
			Count++;
        }
    }
    /**
     * 从头部弹出
     * @return
     */
    public TValue poll()
    {
		return pollFromHead();
    }
	public TValue pollFromHead()
	{
		synchronized (this)
		{
			if(SonNodeList==null)
				return null;
			else
			{
				LinkNode<TKey, TValue> n=SonNodeList;
				SonNodeList=SonNodeList.Next;
				if(SonNodeList!=null)
					SonNodeList.Prev=null;
				else
					LastSonNode=null;
				Count--;
				return n.Value;
			}
		}
	}
	public TValue pollFromTail()
	{
		synchronized (this)
		{
			if(LastSonNode==null)
				return null;
			else
			{
				LinkNode<TKey, TValue> n=LastSonNode;
				LastSonNode=LastSonNode.Prev;
				if(LastSonNode!=null)
					LastSonNode.Next=null;
				else
					SonNodeList=null;
				Count--;
				return n.Value;
			}
		}
	}
	public void addByRise(TKey Key,TValue Value)
	{ 
		synchronized (this)
        {
			LinkNode<TKey, TValue> node=SonNodeList;
			while(node!=null)
			{
				if(permitKeyEqual){
					if(Key.compareTo(node.getKey())<=0)
						break;
				}
				else {
					if (Key.compareTo(node.getKey()) == 0) {
						//已在链中，则更新其值
						node.Value = Value;
						return;
					}
					if (Key.compareTo(node.getKey()) < 0)
						break;
				}
				node=node.Next;			
			}
			LinkNode<TKey, TValue> n=new LinkNode<TKey, TValue>(Key,Value);
			if(node==SonNodeList)
			{
				//在链头插入
				n.Next=SonNodeList;
				if(SonNodeList!=null)
					SonNodeList.Prev=n;
				else
					LastSonNode=n;
				SonNodeList=n;
			}
			else
			{
				n.Next=node;
				if(node!=null)
				{
					//在node前插入
					n.Prev=node.Prev;
					node.Prev.Next=n;
					node.Prev=n;
				}
				else
				{
					//插到链尾
					n.Prev=LastSonNode;
					LastSonNode.Next=n;
					LastSonNode=n;
				}
			}
			Count++;
		}
	}
	
	public void delete(TKey Key)
	{
		LinkNode<TKey, TValue> node=searchNode(Key);
		delete(node);
	}
	public void delete(LinkNode<TKey, TValue> node)
	{
		synchronized (this)
        {
			if(node!=null)
			{
				if(node.Prev!=null)
					node.Prev.Next=node.Next;
				else
					SonNodeList=node.Next;
				if(node.Next!=null)
					node.Next.Prev=node.Prev;
				else
					LastSonNode=node.Prev;				
				Count--;
			}
        }
	}
	public void moveToHead(LinkNode<TKey, TValue> node)
	{
		synchronized (this)
		{
			if(node!=null)
			{
				if(node.Prev!=null){
					node.Prev.Next=node.Next;
					node.Prev=null;
				}
				else
				//node就是head
					return;
				if(node.Next!=null)
					node.Next.Prev=node.Prev;
				else
					LastSonNode=node.Prev;
				node.Next=SonNodeList;
				SonNodeList=node;
			}
		}
	}
	public void moveToTail(LinkNode<TKey, TValue> node)
	{
		synchronized (this)
		{
			if(node!=null)
			{
				if(node.Next!=null){
					node.Next.Prev=node.Prev;
					node.Next=null;
				}
				else
				//node就是tail
					return;
				if(node.Prev!=null)
					node.Prev.Next=node.Next;
				else
					SonNodeList=node.Next;
				node.Prev=LastSonNode;
				LastSonNode=node;
			}
		}
	}
	
    /** 
     * 实现Iterable接口中要求实现的方法 
     */  
    @Override  
    public Iterator<LinkNode<TKey, TValue>> iterator()
    {  
        return new MyIterator();//返回一个MyIterator实例对象  
    }        
    /** 
     * MyIterator是内部类，实现了Iterator<E>接口的类 
     */  
    class MyIterator implements Iterator<LinkNode<TKey, TValue>>
    {
        private LinkNode<TKey, TValue> myNode =SonNodeList;  
        @Override  
        public boolean hasNext() {  
            //只要在调用next()后，index自加，确保index不等于person的长度  
            return myNode!=null;  
        }
        @Override  
        public LinkNode<TKey, TValue> next() {  
            //使用索引来获取person数组中的某一项  
        	LinkNode<TKey, TValue> n= myNode;
        	myNode=myNode.Next;
        	return n;
        } 
        @Override  
        public void remove() {  
            //未实现这个方法  
        }            
    }	
}
