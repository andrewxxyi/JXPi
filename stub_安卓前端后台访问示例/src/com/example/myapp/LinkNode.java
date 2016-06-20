
package com.example.myapp;

public class LinkNode<TKey, TValue>
{
	private TKey Key;
	public TKey getKey()
    {
        return Key;
    }	
	TValue Value;
	public TValue getValue()
    {
        return Value;
    }	
    /// 兄弟节点，按从小到大的顺序
	public	LinkNode<TKey, TValue> Next = null;
	public	LinkNode<TKey, TValue> Prev = null;
	
	public LinkNode(TKey Key,TValue Value)
	{
		this.Key=Key;
		this.Value=Value;
	}
}