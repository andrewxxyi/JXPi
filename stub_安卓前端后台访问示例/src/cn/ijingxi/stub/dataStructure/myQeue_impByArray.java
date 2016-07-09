package cn.ijingxi.stub.dataStructure;

/**
 * 用数组方式实现的对了，泛型即可以支持任意类型
 * 使用方法：
 * 如想使用一个100字符串的栈
 * myQeue_impByArray<String> stack=new myQeue_impByArray<String>(100);
 *
 * 或想使用10整数的栈
 * myQeue_impByArray<Integer> stack=new myQeue_impByArray<Integer>(10);
 *
 *
 *
 * Created by andrew on 16-6-14.
 */
public class myQeue_impByArray<TValue> {

    //最大容量
    int maxLength = 0;
    //当前队列中保存的对象数量
    int count;

    public int getCount() {
        return count;
    }

    //尾部放入数据
    int tail;
    //头部取出数据
    int head;
    //实际存储数据的数组
    Object[] arr;

    /**
     * 初始化
     *
     * @param maxLength 需指定最大容量
     */
    public myQeue_impByArray(int maxLength) {
        this.maxLength = maxLength;
        arr = new Object[maxLength];
        count = 0;
        tail = -1;
        head = -1;
    }

    /**
     * 从尾部送入数据
     *
     * @param v
     * @throws Exception
     */
    public void offer(TValue v) throws Exception {
        //容量已满，则掷出异常
        if (count >= maxLength)
            throw new Exception("已达到最大容量，无法继续添加");
        //对指针进行修正
        if (count == 0) {
            head = 0;
            tail = 0;
        } else {
            //头部为何不动？
            tail++;
            //如果尾部要超出数组范围了，则折回到开始
            //那会不会出现冲撞呢？即新增加的数据把还没用完的老数据给覆盖掉了
            if (tail == 100)
                tail = 0;
        }
        //将数据保存到栈顶
        arr[tail] = v;
        count++;
    }

    /**
     * 从头部取出数据
     *
     * @return
     */
    public TValue take() {
        //没有数据则返回空值，但如果当初就送入了null，则此处是无法分辨的，应当如何处理呢？
        if (count == 0)
            return null;
        //后面会修正top的值，所以预先取出栈顶的值临时保存一下
        Object temp = arr[head];
        count--;
        //修正top的值，和push一样可以进行优化
        if (count == 0) {
            head = -1;
            tail = -1;
        } else {
            //咦？和stack好像不一样啊？！为什么呢？？？
            head++;
            if (head == 100)
                head = 0;

        }
        return (TValue) temp;
    }


}
