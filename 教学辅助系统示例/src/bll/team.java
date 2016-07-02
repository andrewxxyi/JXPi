package bll;

import cn.ijingxi.Rest.httpServer.RES;
import cn.ijingxi.Rest.httpServer.jxHttpData;
import cn.ijingxi.app.ActiveRight;
import cn.ijingxi.orm.jxJson;
import cn.ijingxi.orm.jxORMobj;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxLog;
import dal.PrjTeam;
import dal.TeamRole;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import static dal.PrjTeam.teamRole_manager;
import static dal.PrjTeam.teamRole_member;

public class team {

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@RES
	public jxHttpData getMyTeam(Map<String, Object> ps, jxJson Param) throws Exception {

		UUID tn = Trans.TransToUUID(Param.GetSubValue_String("PeopleID"));
		if (tn == null)
			return new jxHttpData(404, "请指定一个成员");
		PrjTeam pt = PrjTeam.getMyPrjTeam(tn);
		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.addObj(pt);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@RES
	public jxHttpData listTeamMember(Map<String, Object> ps, jxJson Param) throws Exception {

		UUID tn = Trans.TransToUUID(Param.GetSubValue_String("TeamID"));
		if (tn == null)
			return new jxHttpData(404, "请指定一个项目组");
		Queue<jxORMobj> os = PrjTeam.listMember(tn, null);
		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.addObjList(os);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@RES
	public jxHttpData listTeamMemberByRole(Map<String, Object> ps, jxJson Param) throws Exception {

		UUID tn = Trans.TransToUUID(Param.GetSubValue_String("TeamID"));
		if (tn == null)
			return new jxHttpData(404, "请指定一个项目组");
		String role = Param.GetSubValue_String("Role");
		if (role == null)
			return new jxHttpData(404, "请指定一个项目角色");
		Queue<jxORMobj> os = TeamRole.listMember(tn, role);
		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.addObjList(os);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@RES
	public jxHttpData listMyRole(Map<String, Object> ps, jxJson Param) throws Exception {

		UUID tn = Trans.TransToUUID(Param.GetSubValue_String("PeopleID"));
		if (tn == null)
			return new jxHttpData(404, "请指定一个成员");

		Queue<jxORMobj> os = TeamRole.getMyRole(tn);
		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.addObjList(os);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.NormalUser)
	@RES
	public jxHttpData removeRole(Map<String, Object> ps, jxJson Param) throws Exception {

		UUID id = Trans.TransToUUID(Param.GetSubValue_String("RoleID"));
		if (id == null)
			return new jxHttpData(404, "请指定一个角色ID");
		TeamRole role= (TeamRole) TeamRole.GetByID(TeamRole.class,id);
		if (role == null)
			return new jxHttpData(404, "该角色不存在");
		role.Delete();

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.NormalUser)
	@RES
	public jxHttpData setRole(Map<String, Object> ps, jxJson Param) throws Exception {

		UUID tn = Trans.TransToUUID(Param.GetSubValue_String("PeopleID"));
		if (tn == null)
			return new jxHttpData(404, "请指定一个成员");

		String role = Param.GetSubValue_String("Role");
		if (role == null)
			return new jxHttpData(404, "请指定一个项目角色");

		TeamRole.setTeamRoleToPeople(tn, role);
		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.Manager)
	@RES
	public jxHttpData createTeam(Map<String, Object> ps, jxJson Param) throws Exception {

		String tn = Param.GetSubValue_String("Name");
		if (tn == null)
			return new jxHttpData(404, "请指定一个项目组名");
		PrjTeam pt = PrjTeam.New(tn);
		pt.Insert();

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.NormalUser)
	@RES
	public jxHttpData assignToTeam(Map<String, Object> ps, jxJson Param) throws Exception {

		jxLog.logger.debug("TeamID:"+ps.get("TeamID"));

		UUID tid = Trans.TransToUUID((String) ps.get("TeamID"));
		if (tid == null)
			return new jxHttpData(404, "请指定一个项目组");


		jxLog.logger.debug("PeopleID:"+ps.get("PeopleID"));

		UUID pid = Trans.TransToUUID((String) ps.get("PeopleID"));
		if (pid == null)
			return new jxHttpData(404, "请指定一个成员");

		String tn = (String) ps.get("Role");
		if (tn == null)
			return new jxHttpData(404, "请指定组长或组员");
		PrjTeam.setTeamToPeople(tid, pid, tn.compareTo("组长") == 0 ? teamRole_manager : teamRole_member);

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@RES
	public static jxHttpData list(Map<String, Object> ps, jxJson Param) throws Exception {

		Queue<jxORMobj> rl = PrjTeam.list();

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		jxJson ol = rs.addObjList(rl);
		return rs;

	}
}