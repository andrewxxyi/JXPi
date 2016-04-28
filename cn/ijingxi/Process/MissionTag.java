package cn.ijingxi.Process;

import cn.ijingxi.app.ObjTag;

/**
 * Created by andrew on 15-12-8.
 */
public class MissionTag {

    public static final int MissionTagID=0x100;

    private static String[] Tags={
            "创建者",
            "责任人",
            "参与者",
            "需了解者"
    };

    public static void Init() throws Exception {
        ObjTag.InitTagList(Tags,MissionTagID);
    }


}
