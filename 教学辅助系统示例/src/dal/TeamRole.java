package dal;

import cn.ijingxi.app.ObjTag;
import cn.ijingxi.app.People;
import cn.ijingxi.app.RelationType;
import cn.ijingxi.orm.ORMID;
import cn.ijingxi.orm.ORMType;
import cn.ijingxi.orm.SelectSql;
import cn.ijingxi.orm.jxORMobj;
import cn.ijingxi.util.jxCompare;

import java.util.Queue;
import java.util.UUID;

/**
 * 参考下Mission和Exercise的说明
 *
 * 项目组内岗位职责、角色
 *
 * Created by andrew on 15-9-19.
 */
public class TeamRole extends ObjTag {

    public static final String teamRole_QA = "需求分析";
    public static final String teamRole_UID = "界面设计";
    public static final String teamRole_DD = "数据设计";
    public static final String teamRole_Coding = "编码";
    public static final String teamRole_Testing = "测试";
    public static final String teamRole_Deployment = "部署";
    public static final String teamRole_PM = "项目经理";

    //目前的实现一个人就只能在一个小组，是无法区分不同小组岗位的
    public static TeamRole setTeamRoleToPeople(UUID peopleID, String role) throws Exception {
        TeamRole item = (TeamRole) TeamRole.Create(TeamRole.class);
        item.ObjTypeID = ORMType.People.ordinal();
        item.ObjID = peopleID;
        item.TagID = ObjTag.getTagID("项目角色");
        item.Category = role;
        item.Insert();
        return item;
    }

    public static Queue<jxORMobj> listMember(UUID teamID, String role) throws Exception {
        SelectSql s = new SelectSql();
        //如何进行多表联合查询
        s.AddTable("Relation");
        s.AddTable("ObjTag");
        s.AddTable("People");
        s.AddContion("Relation", "ObjID", jxCompare.Equal, teamID);
        s.AddContion("Relation", "RelType", jxCompare.Equal, RelationType.OneToMulti);
        s.AddContion("Relation", "TargetID", "People", "ID");
        s.AddContion("People", "ID", "ObjTag", "ObjID");
        s.AddContion("ObjTag", "TagID", jxCompare.Equal, ObjTag.getTagID("项目角色"));
        if (role != null && role.length() > 0)
            s.AddContion("ObjTag", "Category", jxCompare.Equal, role);
        return People.Select(People.class, s);
    }

    public static Queue<jxORMobj> getMyRole(UUID peopleID) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("ObjTag");
        s.AddContion("ObjTag", "TagID", jxCompare.Equal, ObjTag.getTagID("项目角色"));
        s.AddContion("ObjTag", "ObjID", jxCompare.Equal, peopleID);
        return TeamRole.Select(TeamRole.class, s);
    }

    public static void delMyRole(UUID peopleID, String role) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("ObjTag");
        s.AddContion("ObjTag", "TagID", jxCompare.Equal, ObjTag.getTagID("项目角色"));
        s.AddContion("ObjTag", "ObjID", jxCompare.Equal, peopleID);
        s.AddContion("ObjTag", "Category", jxCompare.Equal, role);
        TeamRole pt = (TeamRole) TeamRole.Get(TeamRole.class, s);
        if (pt != null)
            pt.Delete();
    }

    public static ORMID GetORMID(UUID ID) {
        return new ORMID(CommonObjTypeID.TeamRole, ID);
    }

    public static void Init() throws Exception {
        InitClass(CommonObjTypeID.TeamRole, TeamRole.class, "项目角色");
    }

}
