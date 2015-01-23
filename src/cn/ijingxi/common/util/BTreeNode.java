
package cn.ijingxi.common.util;

public class BTreeNode<TKey extends Comparable<TKey>, TValue>
{
	protected TKey Key;
	public TKey getKey()
    {
        return Key;
    }
	protected TValue Value;    
	public TValue getValue()
    {
        return Value;
    }
	//是否是叶子节点
	boolean IsLeaf=false;
    /// 当前已包含的实际节点数
    int NodeNum = 0;
    //父节点
    BTreeNode<TKey, TValue> Parent = null;
    /// 兄弟节点，按从小到大的顺序
    BTreeNode<TKey, TValue> Brother_Next = null;
    BTreeNode<TKey, TValue> Brother_Prev = null;
    //子节点拉链
    BTreeNode<TKey, TValue> SonNodeList=null;
    //最后一个子节点
    BTreeNode<TKey, TValue> LastSon=null;
    
    void AddNode(BTreeNode<TKey, TValue> Node)
    {
        if (Node != null)
        {
        	BTreeNode<TKey, TValue> tn = SonNodeList, pn = null;
            while (tn != null)
            {
                if (Node.Key.compareTo(tn.Key)==0)
                    return;
                if (Node.Key.compareTo(tn.Key)<0)
                    break;
                pn = tn;
                tn = tn.Brother_Next;
            }
            Node.Parent=this;
            //添加到pn之后，tn之前
            if (pn == null)
            {
                //添加到开头
                Node.Brother_Next = SonNodeList;
                Node.Brother_Prev = null;
                if (SonNodeList != null) SonNodeList.Brother_Prev = Node;
                SonNodeList = Node;
            }
            else
            {
                pn.Brother_Next = Node;
                Node.Brother_Prev = pn;
                Node.Brother_Next = tn;
                if (tn != null) tn.Brother_Prev = Node;
            }
            //添加到链尾
            if (tn == null)
            {
                LastSon = Node;
                Key = Node.Key;
                BTreeNode<TKey, TValue> parent = Parent;
                while (parent != null)
                {
                	//看是否需要更新父节点的键值
                    if (parent.Key.compareTo(Key)<0)
                        parent.Key = Key;
                    else
                        break;
                    parent = parent.Parent;
                }
            }
            NodeNum++;
        }
    }

    void AddLeaf(TKey Key, TValue Value)
    {
    	BTreeNode<TKey, TValue> node = new BTreeNode<TKey, TValue>();
    	node.IsLeaf=true;
    	node.Key=Key;
    	node.Value=Value;
        AddNode(node);
    }

    /**
     * 在本节点的子节点中寻找
     * @param Key
     * @param CompFunc
     * @return null则不在本节点中
     */
    BTreeNode<TKey, TValue> SearchNode(TKey Key)
    {
    	//有数据，且key可能在本节点的数据链条中
    	if (LastSon!=null && Key.compareTo(LastSon.Key)<=0)
    	{    	
    		BTreeNode<TKey, TValue> tn = SonNodeList;
            while (tn != null)
            {
                if (Key.compareTo(tn.Key)<=0)
                    return tn;      
                tn = tn.Brother_Next;
            }
    	}
        return null;
    }    

    /**
     * 从子节点中删去
     * @param Key
     * @param CompFunc
     */
    void DeleteNode(TKey Key)
    {
        if (SonNodeList != null)
        {
            if (LastSon.Key.compareTo(Key)<0)
                return;
            BTreeNode<TKey, TValue> tn = SonNodeList, pn = null;
            while (tn != null)
            {
                if (Key.compareTo(tn.Key)==0)
                {
                    if (pn == null)
                    {
                        //从头删
                    	SonNodeList = SonNodeList.Brother_Next;
                        if (SonNodeList != null) SonNodeList.Brother_Prev = null;
                    }
                    else
                    {
                        pn.Brother_Next = tn.Brother_Next;
                        if (tn.Brother_Next != null) tn.Brother_Next.Brother_Prev = pn;
                    }
                    NodeNum--;
                    break;
                }
                pn = tn;
                tn = tn.Brother_Next;
            }
        }
    }
    
    /**
     * 将本节点分裂为两个节点：将本节点HalfOrder之后的所有节点移动到新的节点中，并将新节点作为自己的兄弟插入到父节点的子节点链中
     * @param HalfOrder 节点数的一半
     * @return 新节点
     */
    BTreeNode<TKey, TValue> Split(int HalfOrder)
    {
    	BTreeNode<TKey, TValue> rs = new BTreeNode<TKey, TValue>();
        rs.NodeNum = NodeNum - HalfOrder;
        NodeNum = HalfOrder;
        BTreeNode<TKey, TValue> node = SonNodeList, pn = null; ;
        for (int i = 0; i < HalfOrder; i++)
        {
            pn = node;
            node = node.Brother_Next;
        }
        //将后继节点的父节点修改为新节点
        BTreeNode<TKey, TValue> n = node;
        while (n != null)
        {
            n.Parent = rs;
            n = n.Brother_Next;
        }
        rs.Parent = Parent;
        rs.SonNodeList = node;
        node.Brother_Prev = null;
        pn.Brother_Next = null;
        rs.LastSon = LastSon;
        rs.Key = LastSon.Key;
        LastSon = pn;
        Key = LastSon.Key;

        rs.Brother_Next = Brother_Next;
        if (Brother_Next != null) Brother_Next.Brother_Prev = rs;
        Brother_Next = rs;
        rs.Brother_Prev = this;            
        return rs;
    }

    /**
     * 将自己HalfOrder之后的节点移到AnotherNode中；AnotherNode在本节点之后
     * @param AnotherNode
     * @param HalfOrder
     */
    void MoveNodeTo(BTreeNode<TKey, TValue> AnotherNode, int HalfOrder)
    {
        if (AnotherNode != null && NodeNum > HalfOrder)
        {
        	BTreeNode<TKey, TValue> node = SonNodeList, pn = null; ;
            for (int i = 0; i < HalfOrder; i++)
            {
                pn = node;
                node = node.Brother_Next;
            }
            //将后继节点的父节点修改为AnotherNode
            BTreeNode<TKey, TValue> n = node;
            while (n != null)
            {
                n.Parent = AnotherNode;
                n = n.Brother_Next;
            }
            AnotherNode.NodeNum += (NodeNum - HalfOrder);
            NodeNum = HalfOrder;
            //将LastSon接到AnotherNode中
            LastSon.Brother_Next = AnotherNode.SonNodeList;
            AnotherNode.SonNodeList.Brother_Prev = LastSon;
            AnotherNode.SonNodeList = node;
            //将子链在pn处断开
            node.Brother_Prev = null;
            pn.Brother_Next = null;
            LastSon = pn;
            Key = LastSon.Key;
        }
    }

    /**
     * 将AnotherNode中从LastSon倒算HalfOrder之前的节点移到本节点中；AnotherNode在本节点之后
     * @param AnotherNode
     * @param HalfOrder
     */
    void MoveNodeFrom(BTreeNode<TKey, TValue> AnotherNode, int HalfOrder)
    {
        if (AnotherNode != null && AnotherNode.NodeNum > HalfOrder)
        {
        	BTreeNode<TKey, TValue> pn = AnotherNode.LastSon, node = null; ;
            for (int i = 0; i < HalfOrder; i++)
            {
                node = pn;
                pn = pn.Brother_Prev;
            }
            BTreeNode<TKey, TValue> n = pn;
            while (n != null)
            {
                n.Parent = this;
                n = n.Brother_Prev;
            }
            NodeNum += (AnotherNode.NodeNum - HalfOrder);
            AnotherNode.NodeNum = HalfOrder;
            //将AnotherNode的前半部分接入到自己的子节点链后面
            LastSon.Brother_Next = AnotherNode.SonNodeList;
            AnotherNode.SonNodeList.Brother_Prev = LastSon;
            AnotherNode.SonNodeList = node;
            //断开
            node.Brother_Prev = null;
            pn.Brother_Next = null;
            LastSon = pn;
            Key = LastSon.Key;
        }
    }

    /**
     * 将AnotherNode合并到本节点中；AnotherNode在本节点之后
     * @param AnotherNode
     */
    void Join(BTreeNode<TKey, TValue> AnotherNode)
    {
        if (AnotherNode != null)
        {
            NodeNum += AnotherNode.NodeNum;
            BTreeNode<TKey, TValue> node = AnotherNode.SonNodeList;
            while (node != null)
            {
            	node.Parent = this;
            	node = node.Brother_Next;
            }
            LastSon.Brother_Next = AnotherNode.SonNodeList;
            AnotherNode.SonNodeList.Brother_Prev = LastSon;
            Key = AnotherNode.Key;
            LastSon = AnotherNode.LastSon;
        }
    }
    

    /**
     * 从叶节点的父节点开始检查，如果当前LastSon的key比原来的key小，说明是LastSon出现了变动，需要更新key
     * @param CheckNode
     * @param CompFunc
     */
    void CheckKey()
    {
            //如果当前LastSon的key不等于原来的key，说明是LastSon出现了变动，需要更新key
            if (LastSon.Key.compareTo(Key)!=0)
            {
                Key = LastSon.Key;
                BTreeNode<TKey, TValue> p = Parent;
                if (p != null)
                    p.CheckKey();
            }
    }

    void RemoveNode(BTreeNode<TKey, TValue> Son)
    {
        if (Son.Brother_Prev != null) Son.Brother_Prev.Brother_Next = Son.Brother_Next;
        if (Son.Brother_Next != null) Son.Brother_Next.Brother_Prev = Son.Brother_Prev;
        if (SonNodeList.Key.compareTo(Son.Key)==0)
        	SonNodeList = Son.Brother_Next;
        if (LastSon.Key.compareTo(Son.Key)<=0)
        {
            LastSon = Son.Brother_Prev;
            if (LastSon != null)
            {
                Key = LastSon.Key;
	            if (Parent != null)
	                Parent.CheckKey();
            }
        }
        NodeNum--;
    }

}

