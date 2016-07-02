import cn.ijingxi.app.ObjTag;

/**
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
        ObjTag.InitTagList(Tags,10000);
    }
}
