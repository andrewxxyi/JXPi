package cn.ijingxi.stub.dataStructure;

/**
 * 老师也一样不小心会犯严重的错误！！
 * 所以不要相信任何人说的东西，一定要画图、写成文字，然后分析完处理逻辑后才能相信之！！
 *
 * Created by andrew on 16-6-16.
 */
public class myQueue_Link_Error<TValue> {
    class node{
        TValue value;
        node next;
    }
    //
    int count=0;
    //从头部取出数据
    node head;
    //从尾部放入数据
    node tail;

    public void offer(TValue v){
        node newNode=new node();
        newNode.value=v;
        //尾部放，是放到尾部，是放到尾部之后，是成为新的尾巴，但这里的实现，大家用笔画一下，是放到哪去了？！
        newNode.next=tail;
        tail=newNode;
        count++;
    }

    //找到某个节点的父节点
    node searchParnetNode(node n){
        //边界检查
        if(tail==null)return null;

        if(tail==n)return null;
        node pn=tail;
        node son = pn.next;
        while (son!=null){
            if(son==n)
                return pn;
            pn=son;
            son=pn.next;
        }
        return null;
    }

    //由于逻辑上的一个不小心，搞错了tail和head的语义，所以大家看一下这里的实现，然后再看看
    //myQueue_Link中正确的take，看看两者的代码量！！
    public TValue take(){
        if(count<=0)
            return null;
        //一定有数据
        if(head==tail){
            //头和尾指向同一个节点，那么意味着队列中只有一个数据，而且head也没有父节点
            node temp = head;
            head=null;
            tail=null;
            count=0;
            return temp.value;
        }
        //到这里，意味着head一定有父亲，即队列中有两个以上的数据
        node temp = searchParnetNode(head);
        if(temp!=null){
            head=temp;
            count--;
            return temp.value;
        }
        //从逻辑上讲该语句是不会得到执行的，为什么呢？
        return null;
    }

    /*
    这里犯的错误，其实是把链表的方向搞反了，应该是从head一路指向tail，
    但大家用这里的算法往里面送入几个数据后，会发现其实是从tail指向了head，我们说从头到尾，这里显然就不合语义了！！
     */


}
