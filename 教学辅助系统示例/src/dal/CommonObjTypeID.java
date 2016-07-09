package dal;

/**
 * 每个数据类的类型ID
 * 由于本系统的所有数据类都使用UUID作为对象ID，所以类型ID的用处不是很大，但在复杂应用下还是必要的
 *
 * Created by andrew on 15-9-4.
 */
public class CommonObjTypeID {
    public static final int Mission=0x1000;
    public static final int Schedule=Mission+1;
    public static final int Plan=Schedule+1;
    public static final int Question=Plan+1;
    public static final int PrjTeam=Question+1;
    public static final int TeamRole=PrjTeam+1;
    public static final int Exercise=TeamRole+1;
    public static final int Subject=Exercise+1;
    public static final int Paper=Subject+1;


}