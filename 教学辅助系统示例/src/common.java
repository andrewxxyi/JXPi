import cn.ijingxi.util.Trans;
import dal.*;

/**
 * 系统的初始化函数归集类
 *
 * Created by andrew on 16-5-24.
 */
public class common {

    public static void Init() throws Exception {
        //系统会用到很多的标记，先定义这边标记
        Tags.Init();

        //对所有的ORM数据类进行初始化
        Mission.Init();
        Schedule.Init();
        Plan.Init();
        Question.Init();

        PrjTeam.Init();
        TeamRole.Init();

        Subject.Init();
        Exercise.Init();
        Paper.Init();

        //系统用到的自定义枚举类型
        Trans.AddEunmType(MissionType.None);
        Trans.AddEunmType(MissionState.None);

    }

    public static void CreateDBTable() throws Exception {

        //init中初始化的这些ORM数据类中很多都是ObjTag类的子类，但并没有额外声明自己的属性，因此就不需要自己的数据表
        //只有这两个数据类由于ObjTag类中提供的属性不够用，所以自己定义了需要的属性，因此才需要建自己的数据表
        //Exercise和Paper都是继承自ObjTag类，所以这两个类所对应的数据表和他们的类一样，其实数据也是分散到Exercise和
        //ObjTag这两个数据表中的，利用相同的ID进行了数据合并，和子类的属性值其实也包括了父类中定义的部分一样
        Exercise.CreateDB();
        Paper.CreateDB();

    }
}
