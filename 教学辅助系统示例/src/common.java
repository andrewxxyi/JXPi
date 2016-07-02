import cn.ijingxi.util.Trans;
import dal.*;

/**
 * Created by andrew on 16-5-24.
 */
public class common {

    public static void Init() throws Exception {
        Tags.Init();


        Mission.Init();
        Schedule.Init();
        Plan.Init();
        Question.Init();

        PrjTeam.Init();
        TeamRole.Init();

        Subject.Init();
        Exercise.Init();
        Paper.Init();

        Trans.AddEunmType(MissionType.None);
        Trans.AddEunmType(MissionState.None);

    }

    public static void CreateDBTable() throws Exception {
        //不应该调用，而应是重写基类的CreateDB
        //Mission.CreateDB();
        //Schedule.CreateDB();
        //Plan.CreateDB();


        Exercise.CreateDB();
        Paper.CreateDB();

    }
}
