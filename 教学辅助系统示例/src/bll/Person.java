package bll;

import cn.ijingxi.Rest.httpServer.REST;
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
 * 参考下coding的说明
 *
 * 关于人的处理
 *
 */
public class Person {

	//用于基于身份的权限访问控制，一定是标志位的定义方法
	public static final int peopleType_teacher = 0x10;
	public static final int peopleType_student = 0x100;

	/**
	 * 当是学生时，打开某些页面需要提醒他现在有任务没有完成，这种提醒是将菜单项标红来完成的
	 * @param ps
	 * @param Param
	 * @return
	 * @throws Exception
     */
	@ActiveRight(policy = ActiveRight.Policy.PeopleType, peopleType = peopleType_student)
	@REST
	public jxHttpData getMyState(Map<String, Object> ps, jxJson Param) throws Exception {

		String sid = (String) ps.get("SessionID");
		UUID pid = jxSession.getPeopleID(sid);
		if (pid == null)
			return new jxHttpData(404, "ID所对应的用户不存在");

		jxHttpData rs = new jxHttpData(200, "处理完毕");

		People p = (People) People.GetByID(People.class, pid);
		List<jxJson> tl = listAllMission(p);
		List<jxJson> todaylist = listTodayMission(p);
		if (todaylist != null) {
			if (tl.size() > todaylist.size())
				//有历史遗留的任务没有完成
				rs.addValue("leave", true);
			tl = listMission_WithType(p,"Coding");
			if (tl.size() > 0)
				//有敲代码的任务没有完成
				rs.addValue("Coding", true);
			tl = listMission_WithType(p,"Testing");
			if (tl.size() > 0)
				//有测验题的任务没有完成
				rs.addValue("Testing", true);
		}
		return rs;
	}


	/**
	 * 读取个人基本信息
	 */
	@ActiveRight(policy = ActiveRight.Policy.Accept)
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
	 * 创建个人
	 * 只有管理员才能执行后面的两个操作
	 */
	@ActiveRight(policy = ActiveRight.Policy.Manager)
	public jxHttpData POST(Map<String, Object> ps, jxJson Param) throws Exception {

		//这里是要求名字不重复
		String name = (String) Param.GetSubValue("Name");
		People p = People.getPeopleByName(name);
		if (p != null)
			return new jxHttpData(401, "用户已存在："+name);
		p = (People) jxORMobj.Create(People.class);
		p.Name = name;
		p.Descr = (String) Param.GetSubValue("Descr");
		//性别
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
		//性别
		p.IsMale = Trans.TransToBoolean(Param.GetSubValue("IsMale"));
		String pt = Param.GetSubValue_String("PeopleType");
		if (pt != null && pt.compareTo("teacher") == 0)
			p.PeopleType = Person.peopleType_teacher;
		else
			p.PeopleType = Person.peopleType_student;
		p.Update();

		jxHttpData rs = new jxHttpData(200, "更新完毕");
		rs.addObj(p);
		return rs;
	}

	/**
	 * 登陆
	 * @param ps
	 * @param Param
	 * @return
	 * @throws Exception
     */
	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@REST
	public jxHttpData login(Map<String, Object> ps, jxJson Param) throws Exception {
		String name = (String) Param.GetSubValue("Name");
		String pass = Param.GetSubValue("Passwd").toString();

		People p = People.getPeopleByName(name);
		if (p == null)
			return new jxHttpData(404, "用户不存在");
		if (!p.checkPasswd(pass))
			return new jxHttpData(403, "密码错误");

		//登陆成功之后，需要为其设置一个session
		final jxSession s = jxSession.create();
		s.setPeopleID(p.ID);
		//将sessionID放入返回的响应的http头中
		jxHttpData rs = new jxHttpData(200, "OK");
		//响应其实也被放到了ps的Response项中
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

	/**
	 * 登出，只要是登陆的正常用户都可以操作
	 * @param ps
	 * @param Param
	 * @return
	 * @throws Exception
     */
	@ActiveRight(policy = ActiveRight.Policy.NormalUser)
	@REST
	public jxHttpData logout(Map<String, Object> ps, jxJson Param) throws Exception {

		//主要是清理session
		String sid = (String) ps.get("SessionID");
		jxSession.clear(sid);
		jxHttpData rs = new jxHttpData(200, "OK");
		rs.setResult(true);
		return rs;
	}

	/**
	 * 修改密码
	 * @param ps
	 * @param Param
	 * @return
	 * @throws Exception
     */
	@ActiveRight(policy = ActiveRight.Policy.NormalUser)
	@REST
	public jxHttpData setPasswd(Map<String, Object> ps, jxJson Param) throws Exception {

		String sid = (String) ps.get("SessionID");
		UUID pid = jxSession.getPeopleID(sid);
		UUID id = Trans.TransToUUID((String) ps.get("PeopleID"));
		String pass = (String) Param.GetSubValue("Passwd");
		if (id == null) {
			//修改自己的密码
			People p = (People) People.GetByID(People.class, pid);
			p.setPasswd(pass);
			p.Update();
		} else if (pid.compareTo(ObjTag.SystemID) == 0) {
			//admin修改任何人的密码
			People p = (People) People.GetByID(People.class, id);
			p.setPasswd(pass);
			p.Update();
		} else
			return new jxHttpData(403, "您无权修改他人的密码");

		jxHttpData rs = new jxHttpData(200, "OK");
		rs.addValue("result", true);
		return rs;
	}


	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@REST
	public static jxHttpData list(Map<String, Object> ps, jxJson Param) throws Exception {
		try {
			int limit = 0;
			int offset = 0;
			if (Param != null) {
				//可以分页显示
				limit = Param.GetSubValue_Integer("Limit");
				offset = Param.GetSubValue_Integer("Offset");
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
		//如果limit为0则不进行分页显示
		s.setLimit(limit);
		s.setOffset(offset);
		return People.Select(People.class, s, false, (obj, key, v) -> {
			//检查是否有任务还未执行完毕，并进行提示
            if ("Info".compareTo(key) == 0) {
                List<jxJson> list = listAllMission((People) obj);
                if (list != null && list.size() > 0)
                    obj.addExtJsonAttr("Missions", true);
            }
        });
	}

	/**
	 * 列表我所有的任务
	 * @param ps
	 * @param Param
	 * @return
	 * @throws Exception
     */
	@ActiveRight(policy = ActiveRight.Policy.NormalUser)
	@REST
	public jxHttpData listMyMission(Map<String, Object> ps, jxJson Param) throws Exception {

		String sid = (String) ps.get("SessionID");
		UUID peopleID = jxSession.getPeopleID(sid);
		People p= (People) People.GetByID(People.class,peopleID);
		List<jxJson> list = listAllMission(p);

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		jxJson ol = rs.addJsonList(list);
		return rs;
	}

	/**
	 * 增加一个需要执行的任务的信息
	 * @param p
	 * @param date
	 * @param missionName
	 * @param type
	 * @param Order
	 * @param missionid
     * @throws Exception
     */
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