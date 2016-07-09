import cn.ijingxi.app.ObjTag;

/**
 * 所有的标记都是ObjTag的一个子集，用不同的TagID进行区分，而TagID很难记忆，所以用字符串进行了对应
 *
 * Created by andrew on 15-9-18.
 */
public class Tags {
    private static String[] Tags={
            "活动",
            "日程",
            "课程计划",


            "问题",

            "项目小组",

            "项目角色",

            "任务",

            "敲代码",
            "敲代码模板",

            "测验",
            "测验模板",


            "试卷",
            "测验题目",
            "测验练习"

    };

    public static void Init() throws Exception {
        //从10000开始给Tags中的每个标记分配相应的TagID
        ObjTag.InitTagList(Tags,10000);
    }
}
