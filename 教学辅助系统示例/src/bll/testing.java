package bll;

import cn.ijingxi.Rest.httpServer.REST;
import cn.ijingxi.Rest.httpServer.jxHttpData;
import cn.ijingxi.Rest.httpServer.jxSession;
import cn.ijingxi.app.ActiveRight;
import cn.ijingxi.app.Department;
import cn.ijingxi.app.People;
import cn.ijingxi.orm.ORMType;
import cn.ijingxi.orm.jxJson;
import cn.ijingxi.orm.jxORMobj;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxTimer;
import cn.ijingxi.util.utils;
import dal.MissionType;
import dal.Paper;
import dal.Subject;

import java.util.*;

import static bll.Person.peopleType_student;
import static bll.Person.peopleType_teacher;

/**
 * 参考下coding的说明
 *
 * 测验
 */
public class testing {

    /**
     * 发布一张试卷，老师可以发布给全班，学生只能给自己发布自测题
     * @param ps
     * @param Param
     * @return
     * @throws Exception
     */
    @ActiveRight(policy = ActiveRight.Policy.PeopleType, peopleType = peopleType_student | peopleType_teacher)
    @REST
    public jxHttpData issuePaper(Map<String, Object> ps, jxJson Param) throws Exception {

        String sid = (String) ps.get("SessionID");
        UUID peopleID = jxSession.getPeopleID(sid);
        People p = (People) People.GetByID(People.class, peopleID);

        int tid = ORMType.People.ordinal();
        UUID id=null;
        if(p.PeopleType==Person.peopleType_student){
            id=p.ID;
        }
        else {
            id = Param.GetSubValue_UUID("DepID");
            tid = ORMType.Department.ordinal();
        }


        String day=Param.GetSubValue_String("Date");
        Date d=Trans.TransToDateNoTime(day);
        if(d==null){
            d=utils.today();
            day=Trans.TransToString_Date(d);
        }

        Queue<jxORMobj> pal = Paper.listPaper(d);

        //需要注意：
        //由于个人可以为自己出题，所以这里的order的计算是有问题的
        Paper paper = Paper.New(Param.GetSubValue_String("Name"),p.ID, tid, id, Param.GetSubValue_Boolean("Individuation"), Param.GetSubValue_String("Category"), Param.GetSubValue_Float("Difficulty"), Param.GetSubValue_Integer("totalSubjectNumber"));
        paper.Time=d;
        paper.IssueID=p.ID;
        paper.TagOrder=pal.size()+1;
        paper.Insert();

        String finalDay = day;
        int finalTid = tid;
        UUID finalId = id;
        jxTimer.asyncRun(param -> {
            if (paper.getIndividuation()) {
                //个性化试卷，要给班中的每个人单独出题
                Department dep = (Department) Department.GetByID(Department.class, finalId);
                int order=pal.size()+2;
                Queue<jxORMobj> pl = dep.listPeople();
                for (jxORMobj obj : pl) {
                    People peo = (People) obj;
                    Paper pa = paper.copy(peo.ID);
                    pa.TagOrder=order++;
                    pa.Insert();
                    pa.takeSubject();
                    Person.addMission(peo, finalDay,paper.Name, MissionType.Testing,paper.TagOrder,Trans.TransToString(pa.ID));
                }
            } else {
                //只出一份就够了
                paper.takeSubject();
                if (finalId == null) {
                    //目前尚未实现班级管理功能
                    Queue<jxORMobj> pl = Person.list(Person.peopleType_student,0,0);
                    for (jxORMobj obj : pl) {
                        //要给班中的每个人都分配一个任务
                        People peo = (People) obj;
                        Person.addMission(peo, finalDay, paper.Name, MissionType.Testing, paper.TagOrder, Trans.TransToString(paper.ID));
                    }
                } else if (finalTid == ORMType.Department.ordinal()) {
                    //对班级
                    Department dep = (Department) Department.GetByID(Department.class, paper.ObjID);
                    Queue<jxORMobj> pl = dep.listPeople();
                    for (jxORMobj obj : pl) {
                        //要给班中的每个人都分配一个任务
                        People peo = (People) obj;
                        Person.addMission(peo, finalDay, paper.Name, MissionType.Testing, paper.TagOrder, Trans.TransToString(paper.ID));
                    }
                } else {
                    //个人自测
                    People peo = (People) People.GetByID(People.class, paper.ObjID);

                    Person.addMission(peo, finalDay, paper.Name, MissionType.Testing, paper.TagOrder, Trans.TransToString(paper.ID));
                }
            }
        }, null);

        jxHttpData rs = new jxHttpData(200, "处理完毕");
        rs.setResult(true);
        return rs;
    }

    @ActiveRight(policy = ActiveRight.Policy.PeopleType,peopleType = Person.peopleType_student)
    @REST
    public jxHttpData listMyTesting(Map<String, Object> ps, jxJson Param) throws Exception {

        String sid = (String) ps.get("SessionID");
        UUID peopleID = jxSession.getPeopleID(sid);
        People p= (People) People.GetByID(People.class,peopleID);
        List<jxJson> list = Person.listTodayMission_WithType(p,MissionType.Testing.toString());

        jxHttpData rs = new jxHttpData(200, "处理完毕");
        jxJson ol = rs.addJsonList(list);
        return rs;
    }

    @ActiveRight(policy = ActiveRight.Policy.Accept)
    @REST
    public jxHttpData getPaper(Map<String, Object> ps, jxJson Param) throws Exception {

        String sid = (String) ps.get("PaperID");
        UUID id = Trans.TransToUUID(sid);
        if (id == null)
            return new jxHttpData(404, "应提供有效的试卷ID！");
        Paper p = (Paper) Paper.GetByID(Paper.class, id);

        jxHttpData rs = new jxHttpData(200, "处理完毕");
        rs.addObj(p);
        rs.addValue("totalSubjectNumber",p.getTotalSubjectNumber());
        return rs;
    }

    @ActiveRight(policy = ActiveRight.Policy.Accept)
    @REST
    public jxHttpData listSubject(Map<String, Object> ps, jxJson Param) throws Exception {

        String sid = (String) ps.get("PaperID");
        UUID id = Trans.TransToUUID(sid);
        if (id == null)
            return new jxHttpData(404, "应提供有效的试卷ID！");
        Paper p = (Paper) Paper.GetByID(Paper.class, id);
        Queue<jxORMobj> list = p.listSubject();

        jxHttpData rs = new jxHttpData(200, "处理完毕");
        rs.addObjList(list);
        return rs;
    }

    @ActiveRight(policy = ActiveRight.Policy.Accept)
    @REST
    public jxHttpData getSubject(Map<String, Object> ps, jxJson Param) throws Exception {

        String sid = Param.GetSubValue_String("SubjectID");
        UUID id = Trans.TransToUUID(sid);
        if (id == null)
            return new jxHttpData(404, "应提供有效的试题ID！");
        Subject p = (Subject) Subject.GetByID(Subject.class, id);

        jxHttpData rs = new jxHttpData(200, "处理完毕");
        rs.addValue("Descr", p.Descr);
        rs.addValue("answerTotal", p.getExtendValue("Info", "answerTotal"));
        rs.addValue("multiSelect", p.getExtendValue("Info", "multiSelect"));
        return rs;
    }


    @ActiveRight(policy = ActiveRight.Policy.PeopleType, peopleType = Person.peopleType_student)
    @REST
    public jxHttpData commit(Map<String, Object> ps, jxJson Param) throws Exception {

        String sid = (String) ps.get("SessionID");
        UUID peopleID = jxSession.getPeopleID(sid);
        String pid = (String) ps.get("PaperID");
        UUID paperID = jxSession.getPeopleID(pid);

        jxJson answerlist = Param.GetSubObject("AnswerList");
        jxJson oplist = Param.GetSubObject("OPList");

        jxHttpData rs = new jxHttpData(200, "处理完毕");
        rs.setResult(true);
        return rs;
    }


}