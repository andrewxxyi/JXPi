package cn.ijingxi.stub.dataStructure;

/**
 * Created by andrew on 16-6-16.
 */
public class myQueue_Link<TValue> {
    class node{
        TValue value;
        node next;
    }
    //
    int count=0;
    //从头部取，是从链中取出第一个数据
    node head;
    //从尾部放，是将新数据放在链中的最后一个数据之后
    node tail;

    //手算放入1、2、3，然后再取出来
    //放入
    public void offer(TValue v){
        node newNode=new node();
        newNode.value=v;
        if(count==0){
            //原本栈中没有数据，所以加入数据后head和tail应该都指向这个数据
            head=newNode;
            tail=newNode;
        }else {
            tail.next=newNode;
            tail=newNode;
        }
        count++;
    }

    //取出
    public TValue take(){
        if(count<=0)
            return null;
        node temp = head;
        head=head.next;
        count--;
        return temp.value;
    }

}
