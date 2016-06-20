package com.example;

import com.example.myapp.IDo;
import com.example.myapp.jxJson;
import com.example.stub.dal.*;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 使用方法：
 * 1、初始化
 * session s=new session("192.0.54.123",10008,null);
 * 2、使用
 * s.Person_login("admin","123456");
 *
 *
 * Created by andrew on 16-6-8.
 */
public class session {

    jxHttpClient client = null;

    private String sessionid = null;

    private boolean error=false;
    private String errMsg=null;

    private void clear(){
        client.clearQueryString();
        error=false;
        errMsg=null;
    }
    public boolean isError(){return error;}
    public String getErrorMsg(){return errMsg; }

    public session(String host, int port) {
        client = new jxHttpClient(host, port);
        client.dualResultFormateError = new IDo() {
            @Override
            public void Do(Object param) throws Exception {
                error=true;
                errMsg=(String)param;
            }
        };
        client.dualError = client.dualResultFormateError ;
    }

    private void setSessionID() {
        if (sessionid != null)
            client.addQueryString("jxSessionID", sessionid);
    }

    //
    //team
    //
    //获取用户所属项目组
    public Team team_getMyTeam(String peopleid) {
        try {
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("PeopleID", peopleid);

            //访问准备
            clear();

            //发起REST访问
            jxJson rs = client.post("/team/getMyTeam", json);
            if (rs != null)
                //获取结果
                return new Team(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //列表项目组成员
    public Queue<People> team_listTeamMember(String teamID) {
        try {
            Queue<People> result=new LinkedList<>();
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("TeamID", teamID);

            //访问准备
            clear();

            //发起REST访问
            jxJson rs = client.post("/team/listTeamMember",json);
            if (rs != null) {
                for (jxJson sub : rs) {
                    result.offer(new People(sub));
                }
                //获取结果
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //按角色列表项目组成员
    //
    //role由前端自行指定，如：需求分析、界面设计等，后台不做校验
    //
    public Queue<People> team_listTeamMemberByRole(String teamID,String role) {
        try {
            Queue<People> result=new LinkedList<>();
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("TeamID", teamID);
            json.setSubObjectValue("Role", role);

            //访问准备
            clear();

            //发起REST访问
            jxJson rs = client.post("/team/listTeamMemberByRole",json);
            if (rs != null) {
                for (jxJson sub : rs) {
                    result.offer(new People(sub));
                }
                //获取结果
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //列表用户被指派的角色
    //
    //role由前端自行指定，如：需求分析、界面设计等，后台不做校验
    //
    public Queue<Role> team_listMyRole(String peopleid) {
        try {
            Queue<Role> result=new LinkedList<>();
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("PeopleID", peopleid);

            //访问准备
            clear();

            //发起REST访问
            jxJson rs = client.post("/team/listMyRole",json);
            if (rs != null) {
                for (jxJson sub : rs) {
                    result.offer(new Role(sub));
                }
                //获取结果
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //指派项目角色
    //
    //role由前端自行指定，如：需求分析、界面设计等，后台不做校验
    //
    public boolean team_setRole(String peopleid,String role) {
        try {
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("PeopleID", peopleid);
            json.setSubObjectValue("Role", role);

            //访问准备
            clear();
            setSessionID();
            //client.addQueryString("QuestionID", QuestionID);

            //发起REST访问
            jxJson rs = client.post("/team/setRole",json);
            if (rs != null)
                return jxJson.checkResult(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //删除项目角色
    //
    //role由前端自行指定，如：需求分析、界面设计等，后台不做校验
    //
    public boolean team_removeRole(String roleid) {
        try {
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("RoleID", roleid);

            //访问准备
            clear();
            setSessionID();
            //client.addQueryString("QuestionID", QuestionID);

            //发起REST访问
            jxJson rs = client.post("/team/removeRole",json);
            if (rs != null)
                return jxJson.checkResult(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }



    //
    //schedule
    //
    //time：形如2016-06-07
    //
    //读取问题的信息
    public Queue<Mission> schedule_disp(String peopleid,String time) {
        try {
            Queue<Mission> result=new LinkedList<>();
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("PeopleID", peopleid);
            json.setSubObjectValue("Date", time);

            //访问准备
            clear();

            //发起REST访问
            jxJson rs = client.post("/schedule/disp",json);
            if (rs != null) {
                for (jxJson sub : rs) {
                    result.offer(new Mission(sub));
                }
                //获取结果
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //创建工作汇报
    public boolean schedule_POST(String c1,String c2,String c3,String c4,String c5) {
        try {
            Queue<Mission> result=new LinkedList<>();
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("content1", c1);
            json.setSubObjectValue("content2", c2);
            json.setSubObjectValue("content3", c3);
            json.setSubObjectValue("content4", c4);
            json.setSubObjectValue("content5", c5);

            //访问准备
            clear();
            setSessionID();

            //发起REST访问
            jxJson rs = client.post("/schedule/",json);
            if (rs != null)
                return jxJson.checkResult(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    //
    //plan
    //
    //time：形如2016-06-07
    //
    //读取计划
    public Queue<Mission> plan_disp(String time) {
        try {
            Queue<Mission> result=new LinkedList<>();
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("Date", time);

            //访问准备
            clear();

            //发起REST访问
            jxJson rs = client.post("/plan/disp",json);
            if (rs != null) {
                for (jxJson sub : rs) {
                    result.offer(new Mission(sub));
                }
                //获取结果
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //创建计划
    public boolean plan_POST(String c1,String c2,String c3,String c4,String c5) {
        try {
            Queue<Mission> result=new LinkedList<>();
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("content1", c1);
            json.setSubObjectValue("content2", c2);
            json.setSubObjectValue("content3", c3);
            json.setSubObjectValue("content4", c4);
            json.setSubObjectValue("content5", c5);

            //访问准备
            clear();
            setSessionID();

            //发起REST访问
            jxJson rs = client.post("/plan/",json);
            if (rs != null)
                return jxJson.checkResult(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    //
    //question
    //
    //读取问题的信息
    public Question question_GET(String QuestionID) {
        try {
            //访问准备
            clear();
            client.addQueryString("QuestionID", QuestionID);

            //发起REST访问
            jxJson rs = client.get("/question");
            if (rs != null) {
                //获取结果
                return new Question(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //灭除问题
    public boolean question_PUT(String QuestionID,String content) {
        try {
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("content", content);

            //访问准备
            clear();
            setSessionID();
            client.addQueryString("QuestionID", QuestionID);

            //发起REST访问
            jxJson rs = client.put("/question",json);
            if (rs != null)
                return jxJson.checkResult(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //创建问题
    public boolean question_POST(String content) {
        try {
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("content", content);

            //访问准备
            clear();
            setSessionID();

            //发起REST访问
            jxJson rs = client.post("/question",json);
            if (rs != null)
                return jxJson.checkResult(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    //
    //Person
    //
    //如果Limit和Offset被设置了大于0的值，则为分页显示
    //
    //列表用户
    public Queue<People> Person_list(int limit,int offset) {
        try {
            Queue<People> result=new LinkedList<>();
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("Offset", offset);
            json.setSubObjectValue("Limit", limit);

            //访问准备
            clear();

            //发起REST访问
            jxJson rs = client.post("/Person/list",json);
            if (rs != null) {
                for (jxJson sub : rs) {
                    result.offer(new People(sub));
                }
                //获取结果
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //登出
    public boolean Person_Logout() {
        try {
            //访问准备
            clear();
            setSessionID();

            //发起REST访问
            jxJson rs = client.post("/Person/logout", null);
            if (rs != null)
                return jxJson.checkResult(rs);
            //获取结果
            sessionid = null;
            client.clearSessionID();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //登录
    public People Person_Login(String name, String passwd) {
        try {
            //参数准备
            jxJson json = jxJson.GetObjectNode("r");
            json.setSubObjectValue("Name", name);
            json.setSubObjectValue("Passwd", passwd);

            //访问准备
            clear();

            //发起REST访问
            jxJson rs = client.post("/Person/login", json);
            if (rs != null) {
                //获取结果
                sessionid = client.getSessionID();
                //转换为java对象
                return new People(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
