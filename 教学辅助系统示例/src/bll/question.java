package bll;

import cn.ijingxi.Rest.httpServer.RES;
import cn.ijingxi.Rest.httpServer.jxHttpData;
import cn.ijingxi.Rest.httpServer.jxSession;
import cn.ijingxi.app.ActiveRight;
import cn.ijingxi.app.ObjTag;
import cn.ijingxi.orm.IDual;
import cn.ijingxi.orm.SelectSql;
import cn.ijingxi.orm.jxJson;
import cn.ijingxi.orm.jxORMobj;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxCompare;
import cn.ijingxi.util.jxLog;
import dal.Question;
import dal.QuestionState;

import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class question {

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	public jxHttpData GET(Map<String, Object> ps, jxJson Param) throws Exception {

		jxLog.logger.debug("QuestionID:"+(String) ps.get("QuestionID"));
		UUID qid = Trans.TransToUUID((String) ps.get("QuestionID"));
		Question q=(Question)Question.GetByID(Question.class,qid);
		if(q==null)
			return new jxHttpData(404, "没找到指定的问题");

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.addObj(q);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.NormalUser)
	public jxHttpData PUT(Map<String, Object> ps, jxJson Param) throws Exception {

		String sid = (String) ps.get("SessionID");
		UUID pid = jxSession.getPeopleID(sid);
		UUID qid = Trans.TransToUUID((String) ps.get("QuestionID"));
		Question q=(Question)Question.GetByID(Question.class,qid);
		if(q==null)
			return new jxHttpData(404, "没找到指定的问题");
		if(q.ObjID.compareTo(pid)!=0)
			return new jxHttpData(403, "您只能对自己的问题灭除确认");

		String c1 = (String) Param.GetSubValue("content");
		q.Descr=c1;
		q.TagState= QuestionState.Done.ordinal();
		q.Time=new Date();
		q.Update();

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.NormalUser)
	public jxHttpData POST(Map<String, Object> ps, jxJson Param) throws Exception {

		String sid = (String) ps.get("SessionID");
		UUID pid = jxSession.getPeopleID(sid);

		String c1 = (String) Param.GetSubValue("content");
		Question q=Question.New(pid);
		q.Name=c1;
		q.Insert();

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@RES
	public jxHttpData list(Map<String, Object> ps, jxJson Param) throws Exception {

		UUID peopleID = Trans.TransToUUID((String) Param.getSubObjectValue("PeopleID"));
		if(peopleID==null)
			return new jxHttpData(401, "请指定一个人员");
		SelectSql s = new SelectSql();
		s.AddTable("ObjTag");
		s.AddContion("ObjTag", "TagID", jxCompare.Equal, ObjTag.getTagID("问题"));
		if (peopleID != null)
			s.AddContion("ObjTag", "ObjID", jxCompare.Equal, peopleID);
		Queue<jxORMobj> rl=Question.Select(Question.class,s,false,new IDual(){
			@Override
			public void Do(jxORMobj obj, String key, Object v) throws Exception {
				if ("TagState".compareTo(key) == 0) {
					int num = Trans.TransToInteger(v);
					if (num == QuestionState.Waiting.ordinal())
						obj.addExtJsonAttr("State", "等待解决");
					else
						obj.addExtJsonAttr("State", "已经解决");
				} else if ("CreateTime".compareTo(key) == 0)
					obj.addExtJsonAttr("CStartTime", Trans.TransToChinese((Date) v));
				else if ("Time".compareTo(key) == 0)
					obj.addExtJsonAttr("CEndTime", Trans.TransToChinese((Date) v));
			}
		});

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		jxJson ol = rs.addObjList(rl);
		return rs;
	}



}