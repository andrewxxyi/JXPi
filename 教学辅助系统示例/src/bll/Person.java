package bll;

import cn.ijingxi.Rest.httpServer.RES;
import cn.ijingxi.Rest.httpServer.jxHttpData;
import cn.ijingxi.Rest.httpServer.jxHttpServer;
import cn.ijingxi.Rest.httpServer.jxSession;
import cn.ijingxi.app.ActiveRight;
import cn.ijingxi.app.ObjTag;
import cn.ijingxi.app.People;
import cn.ijingxi.orm.*;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxCompare;
import cn.ijingxi.util.jxLog;
import dal.MissionType;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;

import java.util.*;

/**
 * 扩展列为：info
 * 	其中：ear为代数，始祖为1
 * 				order为兄弟姐妹中的排行，男女分开排
 * @author andrew
 *
 */
public class Person {

	public static final int peopleType_teacher = 0x10;
	public static final int peopleType_student = 0x100;

	@ActiveRight(policy = ActiveRight.Policy.Accept, peopleType = peopleType_student)
	@RES
	public jxHttpData getMyState(Map<String, Object> ps, jxJson Param) throws Exception {

		String sid = (String) ps.get("SessionID");
		UUID pid = jxSession.getPeopleID(sid);
		if (pid == null)
			return new jxHttpData(404, "ID所对应的用户不存在");

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		jxJson json = rs.getDataNode();

		People p = (People) People.GetByID(People.class, pid);
		List<jxJson> tl = listAllMission(p);
		List<jxJson> todaylist = listTodayMission(p);
		if (todaylist != null) {
			if (tl.size() > todaylist.size())
				json.setSubObjectValue("leave", true);
			tl = listMission_WithType(p,"Coding");
			if (tl.size() > 0)
				json.setSubObjectValue("Coding", true);
			tl = listMission_WithType(p,"Testing");
			if (tl.size() > 0)
				json.setSubObjectValue("Testing", true);
		}
		return rs;
	}


	/**
	 * 读取个人基本信息
	 */
	@ActiveRight(policy = ActiveRight.Policy.Manager)
	public jxHttpData GET(Map<String, Object> ps, jxJson Param) throws Exception {
		UUID id = Trans.TransToUUID((String) ps.get("PeopleID"));
		if (id == null)
			return new jxHttpData(404, "ID所对应的用户不存在");

		People p = (People) People.GetByID(People.class, id);
		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.addObj(p);
		return rs;
	}

	/**
	 * 创建个人基本信息
	 */
	@ActiveRight(policy = ActiveRight.Policy.Manager)
	public jxHttpData POST(Map<String, Object> ps, jxJson Param) throws Exception {

		String name = (String) Param.GetSubValue("Name");
		People p = People.getPeopleByName(name);
		if (p != null)
			return new jxHttpData(401, "用户已存在");
		p = (People) jxORMobj.Create(People.class);
		p.Name = name;
		p.Descr = (String) Param.GetSubValue("Descr");
		p.IsMale = Trans.TransToBoolean(Param.GetSubValue("IsMale"));

		String pt = Param.GetSubValue_String("PeopleType");
		if (pt != null && pt.compareTo("teacher") == 0)
			p.PeopleType = Person.peopleType_teacher;
		else
			p.PeopleType = Person.peopleType_student;
		//jxLog.logger.debug("PeopleType:"+p.PeopleType);
		p.setPasswd("123456");
		p.Insert();

		jxHttpData rs = new jxHttpData(200, "OK");
		rs.addValue("result", true);
		return rs;
	}

	/**
	 * 修改个人信息
	 */
	@ActiveRight(policy = ActiveRight.Policy.Manager)
	public jxHttpData PUT(Map<String, Object> ps, jxJson Param) throws Exception {
		UUID id = Trans.TransToUUID((String) ps.get("ID"));
		if (id == null)
			return new jxHttpData(404, "ID为空");
		People p = (People) People.GetByID(People.class, id);
		if (p == null)
			return new jxHttpData(404, "ID所对应的用户不存在");
		//p.Name = (String) Param.GetSubValue("Name");
		p.Descr = (String) Param.GetSubValue("Descr");


		DB db = JdbcUtils.GetDB(null, this);
		db.Trans_Begin();
		try {
			synchronized (db) {
				p.Update(db);
			}
			db.Trans_Commit();
			jxHttpData rs = new jxHttpData(200, "更新完毕");
			rs.addObj(p);
			return rs;
		} catch (Exception e) {
			db.Trans_Cancel();
			jxHttpData rs = new jxHttpData(503, "内部错误：" + e.getMessage());
			return rs;
		}
	}

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@RES
	public jxHttpData login(Map<String, Object> ps, jxJson Param) throws Exception {
		String name = (String) Param.GetSubValue("Name");
		String pass = Param.GetSubValue("Passwd").toString();

		People p = People.getPeopleByName(name);
		if (p == null)
			return new jxHttpData(404, "用户不存在");
		if (!p.checkPasswd(pass))
			return new jxHttpData(403, "密码错误");


		final jxSession s = jxSession.create();
		s.setPeopleID(p.ID);
		jxHttpData rs = new jxHttpData(200, "OK");
		HttpResponse fp = (HttpResponse) ps.get("Response");
		fp.setHeader(new Header() {
			@Override
			public String getName() {
				return jxHttpServer.SessionHeaderName;
			}

			@Override
			public String getValue() {
				return s.getID();
			}

			@Override
			public HeaderElement[] getElements() throws ParseException {
				return new HeaderElement[0];
			}
		});
		rs.addObj(p);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.NormalUser)
	@RES
	public jxHttpData logout(Map<String, Object> ps, jxJson Param) throws Exception {

		String sid = (String) ps.get("SessionID");
		jxSession.clear(sid);
		jxHttpData rs = new jxHttpData(200, "OK");
		rs.setResult(true);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@RES
	public jxHttpData setPasswd(Map<String, Object> ps, jxJson Param) throws Exception {

		String sid = (String) ps.get("SessionID");
		UUID pid = jxSession.getPeopleID(sid);

		UUID id = Trans.TransToUUID((String) ps.get("PeopleID"));
		if (id == null) return new jxHttpData(401, "需要给出用户ID");

		//可以自己修改自己的密码，也可以是admin修改任何人的密码
		if (pid.compareTo(id) == 0 || pid.compareTo(ObjTag.SystemID) == 0) {

			People p = (People) People.GetByID(People.class, id);
			if (p == null) return new jxHttpData(401, "用户不存在");

			jxLog.logger.debug(p.Name);

			String pass = (String) Param.GetSubValue("Passwd");
			p.setPasswd(pass);
			p.Update();

			jxHttpData rs = new jxHttpData(200, "OK");
			rs.addValue("result", true);
			return rs;
		} else
			return new jxHttpData(403, "您无权修改他人的密码");

	}


	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@RES
	public static jxHttpData list(Map<String, Object> ps, jxJson Param) throws Exception {
		try {
			//jxLog.logger.debug("Param:" + Param.TransToStringWithName());

			/*
			Queue<jxORMobj> rl = list(12, Param.GetSubValue_Integer("Limit"), Param.GetSubValue_Integer("Offset"));

			for(jxORMobj obj:rl){
				People p=(People)obj;
				p.PeopleType=Person.peopleType_student;
				p.Update();
			}
			rl = list(11, Param.GetSubValue_Integer("Limit"), Param.GetSubValue_Integer("Offset"));

			for(jxORMobj obj:rl){
				People p=(People)obj;
				p.PeopleType=Person.peopleType_teacher;
				p.Update();
			}

			*/

			int limit=0;
			int offset=0;
			if(Param!=null){
				limit=Param.GetSubValue_Integer("Limit");
				offset=Param.GetSubValue_Integer("Offset");
			}
			Queue<jxORMobj> rl = list(Person.peopleType_student, limit, offset);

			jxHttpData rs = new jxHttpData(200, "处理完毕");
			jxJson ol = rs.addObjList(rl);
			return rs;
		} catch (Exception e) {
			jxLog.error(e);
			throw e;
		}
	}

	public static Queue<jxORMobj> list(int peopleType, int limit, int offset) throws Exception {

		SelectSql s = new SelectSql();
		s.AddTable("People");
		s.AddContion("People", "PeopleType", jxCompare.Equal, peopleType);
		s.setLimit(limit);
		s.setOffset(offset);
		return People.Select(People.class, s, false, (obj, key, v) -> {
            if ("Info".compareTo(key) == 0) {
                List<jxJson> list = listAllMission((People) obj);
                if (list != null && list.size() > 0)
                    obj.addExtJsonAttr("Missions", true);
            }
        });
	}

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@RES
	public jxHttpData listMyMission(Map<String, Object> ps, jxJson Param) throws Exception {

		String sid = (String) ps.get("SessionID");
		UUID peopleID = jxSession.getPeopleID(sid);
		People p= (People) People.GetByID(People.class,peopleID);
		List<jxJson> list = listAllMission(p);

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		jxJson ol = rs.addJsonList(list);
		return rs;
	}

	public static void addMission(People p, String date, String missionName, MissionType type, Integer Order,String missionid) throws Exception {
		Map<String, String> ks = new HashMap<String, String>();
		ks.put("date", date);
		ks.put("name", missionName);
		ks.put("type", type.toString());
		ks.put("order", Order.toString());
		p.setExtendArrayValue("Info", "Mission", ks, "id", missionid);
		p.Update();
	}

	public static List<jxJson> listAllMission(People p) throws Exception {
		return p.getExtendArrayList("Info", "Mission", null);
	}
	public static List<jxJson> listTodayMission_WithType(People p,String type) throws Exception {
		String d= Trans.TransToString_Date(new Date());
		return listMission(p,d,type);
	}
	public static List<jxJson> listTodayMission(People p) throws Exception {
		String d= Trans.TransToString_Date(new Date());
		return listMission(p,d);
	}
	public static List<jxJson> listMission_WithType(People p,String type) throws Exception {
		Map<String, String> ks = new HashMap<String, String>();
		ks.put("type", type);
		return p.getExtendArrayList("Info", "Mission", ks);
	}
	public static List<jxJson> listMission(People p,String date) throws Exception {
		Map<String, String> ks = new HashMap<String, String>();
		ks.put("date", date);
		return p.getExtendArrayList("Info", "Mission", ks);
	}
	public static List<jxJson> listMission(People p,String date,String type) throws Exception {
		//jxLog.logger.debug("date:"+date+",type:"+type);
		Map<String, String> ks = new HashMap<String, String>();
		ks.put("date", date);
		ks.put("type", type);
		return p.getExtendArrayList("Info", "Mission", ks);
	}
	public static jxJson getMission(People p,String date,String type,Integer Order) throws Exception {
		Map<String, String> ks = new HashMap<String, String>();
		ks.put("date", date);
		ks.put("type", type);
		ks.put("order", Order.toString());
		List<jxJson> list = p.getExtendArrayList("Info", "Mission", ks);
		if (list != null && list.size() == 1)
			return list.get(0);
		return null;
	}
	public static void closeMission(People p,String type,String id) throws Exception {
		Map<String, String> ks = new HashMap<String, String>();
		ks.put("id", id);
		p.delExtendArraySubNode("Info", "Mission", ks);
		p.Update();
	}



}