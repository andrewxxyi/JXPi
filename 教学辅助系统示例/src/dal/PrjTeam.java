package dal;

import cn.ijingxi.app.*;
import cn.ijingxi.orm.ORMID;
import cn.ijingxi.orm.ORMType;
import cn.ijingxi.orm.SelectSql;
import cn.ijingxi.orm.jxORMobj;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxCompare;

import java.util.Queue;
import java.util.UUID;

/**
 * 由于没有单独给出项目组的定义，而是采取给people打标记的方式来定义项目组，所以项目组在本系统中不表示为一个实体，没有
 * 对应的实体表来存储相关信息！！
 *
 * Created by andrew on 15-9-19.
 */
public class PrjTeam extends jxObj {

    public static final Integer teamRole_manager = 1;
    public static final Integer teamRole_member = 2;

    public static PrjTeam New(String name) throws Exception {
        PrjTeam item = (PrjTeam) Relation.Create(PrjTeam.class);
        item.Name=name;
        return item;
    }
    public static void setTeamToPeople(UUID teamID, UUID peopleID, Integer role) throws Exception {
        Relation rl = (Relation) Relation.Create(Relation.class);
        rl.ObjTypeID = CommonObjTypeID.PrjTeam;
        rl.ObjID = teamID;
        rl.TargetTypeID = ORMType.People.ordinal();
        rl.TargetID = peopleID;
        rl.RelType = RelationType.OneToMulti;
        rl.Number = role;
        rl.Insert();
    }
    public static void delTeam(UUID teamID) throws Exception {
        PrjTeam t = (PrjTeam) PrjTeam.GetByID(PrjTeam.class, teamID);
        if (t != null) {
            t.NoUsed = true;
            t.Update();
        }
    }

    public static Queue<jxORMobj> list() throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("jxObj");
        s.AddContion("jxObj", "NoUsed", jxCompare.Equal, false);
        return PrjTeam.Select(PrjTeam.class, s);
    }

    public static Queue<jxORMobj> listMember(UUID teamID, String role) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("Relation");
        s.AddTable("People");
        s.AddContion("Relation", "ObjID", jxCompare.Equal, teamID);
        s.AddContion("Relation", "RelType", jxCompare.Equal, RelationType.OneToMulti);
        s.AddContion("Relation", "TargetID", "People", "ID");
        if (role != null)
            s.AddContion("Relation", "Number", jxCompare.Equal, role.compareTo("组长")==0?teamRole_manager:teamRole_member);

        Queue<jxORMobj> rl = PrjTeam.Select(People.class, s, (obj, key, v) -> {
            if ("Number".compareTo(key) == 0) {
                int num = Trans.TransToInteger(v);
                if (num == teamRole_manager)
                    obj.addExtJsonAttr("Role", "组长");
                else
                    obj.addExtJsonAttr("Role", "组员");
            }
        });
        return rl;
    }

    public static PrjTeam getMyPrjTeam(UUID peopleID) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("PrjTeam");
        s.AddTable("Relation");
        s.AddContion("Relation", "TargetID", jxCompare.Equal, peopleID);
        s.AddContion("Relation", "RelType", jxCompare.Equal, RelationType.OneToMulti);
        s.AddContion("Relation", "ObjID", "PrjTeam", "ID");
        return (PrjTeam) PrjTeam.Get(PrjTeam.class, s);
    }

    public static void delFromPrjTeam(UUID peopleID) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("Relation");
        s.AddContion("Relation", "TargetID", jxCompare.Equal, peopleID);
        s.AddContion("Relation", "RelType", jxCompare.Equal, RelationType.OneToMulti);
        Relation rl = (Relation) Relation.Get(Relation.class, s);
        if (rl != null)
            rl.Delete();
    }

    public static ORMID GetORMID(UUID ID) {
        return new ORMID(CommonObjTypeID.PrjTeam, ID);
    }

    public static void Init() throws Exception {
        InitClass(CommonObjTypeID.PrjTeam, PrjTeam.class, "项目小组");
    }

}
