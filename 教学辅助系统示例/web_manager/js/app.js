
//录入试题
function subject_ok() {
	//难度
	var diff=$('#txt_Difficulty').val();
	//所有的答案
	var asw=getAnswer();	
	//所给出的领域
	var ca=getCategory();

//题目内容
	var  c1=$('#editor_subject').html();
	//由于采用了富文本，所以其实获取到的内容可以是一个html格式的字符串，含有特殊字符，为避免意外，因此转成了base64编码，后台只负责保存，解码工作由前端负责
	//如果是用安卓手机进行做题，也需要注意该问题
		var c=$.base64.btoa(c1,true);
	//var name=$('#txt_Name').val();
	//答案数量，其实并没有太多的意义，以后可能会用于在向学生显示答案时显示几个
	var totalnum=countAnswer();
	//由于是带session的，而为了兼容性的考虑，开始sessionID的信息并没有放入到http头中，而是放到了url中，所以如果后台需要session中的信息来做
	//权限控制之类就需要调用jxCommon.js中的getUrlWithQueryString函数将打开页面时的url中的信息再次拼入新的url中
     var url=getUrlWithQueryString("/subject");
     //发起一个REST请求，jxREST在jxCommon.js中定义，是同步的post访问：访问地址、传向后台的参数、执行成功后的回调函数并带回后台返回的结果
    $.jxREST(url,{Category:ca,Descr:c,Difficulty:diff,answerTotal:answerNum,Answer:asw,MultiSelect:subjectMultiSelect},function (json) {
		alert("题目已录入");
    });

}

//发布题目：老师是向全班发布，学生是发布给自己
function testing_issue_ok() {
	
	var ca=getCategory();
	
	var name=$('#txt_Name').val();
	var totalnum=$('#txt_TotalNum').val();
	var diff=$('#txt_Difficulty').val();
     var url=getUrlWithQueryString("/testing/issuePaper");
    $.jxREST(url,{Name:name,totalSubjectNumber:totalnum,Difficulty:diff,Category:ca},function (json) {
		alert("题目已发布");
    });

}
//用户选择了该试卷覆盖了所有的技术领域
function testing_issue_cbAll_click() {
	categorySelectAll($('#cb_testing_issue_CategoryAll').is(':checked'));
}

//答题过程中使用到的各种变量
//当前试卷
var paper;
//本试卷包含的所有题目ID列表，不包含题目的具体信息
var subjectList;
//本试卷所包含的所有题目
var subjectArr;
//记录所有的答案
var answerList;
//记录当前所回答的答案
var answerArr=new Array();
//本试卷所包含的总题目数
var totalSubjectNumber=0;
//当前正在做的题目的序号，从0开始
var currentSubjectIndex=0;
//本试卷答题的总时间，以秒为单位
var testing_TotalSeconds=0;
//答当前题目已花费的时间，以秒为单位
var testing_seconds=0;
//答题的计时器
var Interval_testing;
//系统会跟踪用户在答题时的所有操作，未来可用于用户的行为分析
var testing_opArr;

//显示答题的总用时情况
function dispTime() {
    	$('#testing_TotalTime').val("已用时："+dispTime_Chinese(testing_TotalSeconds,true));
}
//答题的计时器，每秒执行一次
function testing_count() {
	testing_TotalSeconds++;
	testing_seconds++;
}
//关闭计时器
function clearTimer_testing() {
	if(Interval_testing){
		clearInterval(Interval_testing);
		Interval_testing=null;
	}
}

//获取当前试卷中的所有试题
function getPaper(){
        var url=getUrlWithQueryString("/testing/getPaper");
    $.jxREST(url,null,function (json) {
    	paper=json;
    	currentSubjectIndex=0;
    	totalSubjectNumber=paper.totalSubjectNumber;
    	
    	//每分钟更新下当前的时间信息
      setInterval("dispTime()",60000);
		//启动计数器
		testing_TotalSeconds=0;
		testing_seconds=0;
        Interval_testing=setInterval("testing_count()",1000);
        //显示下当前的时间信息
        dispTime();
      
    	$('#testing_TotalNum').val("题目总数量："+totalSubjectNumber);
    	//getUrlWithQueryStringAndID("/testing/listSubject",json[i].ID,"PaperID");
    	url=getUrlWithQueryString("/testing/listSubject");
    	//首先需要获取本试卷中包括的所有题目
    $.jxREST(url,null,function (json) {
    	subjectList=json;
    	answerList=[];
    	subjectArr=[];
    	testing_opArr=[];
    	if(currentSubjectIndex<totalSubjectNumber)
    		//获取当前试题并显示给做题者
    		getSubject(currentSubjectIndex,function () {    		
    			dispSubject();
    		});
    });
    });
}
//显示题目
function dispSubject(){
	if(currentSubjectIndex>=totalSubjectNumber){
		alert("题目已做完");
		checkTestingAnswer();
		return;
	}
	if(subjectArr[currentSubjectIndex]){
		//每次答题都需要将本题所花费的时间清0
		testing_seconds=0;
		//学生答题的动作记录清空
      testing_opArr[currentSubjectIndex]=[];
      
		var s=subjectArr[currentSubjectIndex];
		//目前还没有去做单选题答题的限制
		var ms='单选题';
		if(s.multiSelect=="true")
			ms='多选题';
		//解码题目内容
		var c=$.base64.atob(s.Descr,true);
		//更改说明信息
		$('#testing_Info').html("第"+totalSubjectNumber+"题："+ms);
		//显示题目内容
		$('#testing_exercise').html(c);
		//显示答案选择项的列表，主要是为了日后题目内容中所给出的答案表和这里的答案选项相符合
		var anum=parseInt(s.answerTotal);
		for(var i=1;i<=anum;i++)
			$('#testing_answer_'+i).show();
		for(var i=anum+1;i<=8;i++)
			$('#testing_answer_'+i).hide();
	}
	//预取下一个，避免网络较差时学生等待时间过长
	if(currentSubjectIndex+1<totalSubjectNumber)
    			getSubject(currentSubjectIndex+1);		
}
//读取题目
function getSubject(subjectIndex,dual) {
		var url=getUrlWithQueryString("/testing/getSubject");
    $.jxREST(url,{SubjectID:subjectList[subjectIndex].TargetID},function (json) {
    	//将读取到的题目保存到题目数组中
    	subjectArr[subjectIndex]=json;
    	if(dual)
    		dual();
    });
}
//全部题目回答完毕
function checkTestingAnswer() {
	//clearTimer_testing();
		var url=getUrlWithQueryStringNameWithSessionID("/testing/commit","PaperID");
    $.jxREST(url,{AnswerList:answerList,OPList:testing_opArr,TotalTime:testing_TotalSeconds},function (json) {
    	//做完进行跳转，以后应视情况是否进行答卷分析
    	gotoUrl('testingList.html');
    });	
}
//题目录入时的处理
var subjectMultiSelect;
var answerNum=0;
//点击了多选
function subject_MultiSelect_click() {	
	subjectMultiSelect=$('#scb_subject_MultiSelect').is(':checked');
}
//答案数量其实没有什么意义，属于早期的思维不严谨
function countAnswer() {
	answerNum=0;
	for(key in answerArr){
		if(answerArr[key])
			answerNum++;
	}
}
//收集所有选择的答案选项，然后用空格分隔
function getAnswer() {
	var asw;
	for(key in answerArr){
		if(answerArr[key])
			if(asw)
				asw=asw + " "+key;
			else
				asw=key;
	}
	return asw;
}
//题目录入时的答案点击事件，主要是进行了单选时的一个处理
function subject_cb_click(alph) {
	 var cbs=$('#subject_answer_'+alph).is(':checked');
		countAnswer();
		if(cbs){
	if(!subjectMultiSelect&&answerNum>0){
		alert("单选题只能选择一个答案");
		$('#subject_answer_'+alph).attr('checked',false);
		answerArr[alph]=false;
		return;
	}
		}
	//alert(alph);
		answerArr[alph]=$('#subject_answer_'+alph).is(':checked');		
}

//当前题目答题完毕
function testing_ok() {
	//先收集答案
	var asw;	
	for(key in answerArr){
		if(answerArr[key])
			if(asw)
				asw=asw + " "+k;
			else
				asw=k;
	}
	
	answerList[currentSubjectIndex]=asw;
	//显示下一道题目
	currentSubjectIndex++;
	dispSubject();
}

//记录用户的操作序列
//answer是用户点击的答案
//select是用户选择或取消选择
function testing_cb_click(alph) {
	//alert(alph);
	//记录用户选择了哪个答案
	var select;
	if($('#testing_answer_'+alph).is(':checked')){
		select='s';
		answerArr[alph]=true;
	}
	else {
		select='u';
		answerArr[alph]=false;
	}
	//将用户的操作和该操作发生在答当前题目的时间（从题目显示出来起算）记录下来形成操作序列
	testing_opArr[currentSubjectIndex].push({a:alph,s:select,t:testing_seconds});
	alert($.toJSON(testing_opArr[currentSubjectIndex]));
}



//发布敲代码任务
function coding_issue_commit(){
	var  c1=$('#coding_teacher').html();
	if(c1.length>750){
		//目前还不清楚为什么长度超过750会出现问题
		alert('输入的字符串太长，请不要超过750个字符');
		return;
	}
		if(!c1){
			alert("请输入需要发布的内容！");
			return;
		}
		var c=$.base64.btoa(c1,true);
		//需要敲几遍，默认是5次
	var  c2=$('#txt_coding_issue_num').val();
		alert(c2);
	if(!c2)
		c2="5";
		//alert(c1);
		//alert(c);
        var url=getUrlWithQueryString("/coding/issue");
        
    $.jxREST(url,{Content:c,Number:c2},function (json) {
    	alert("已发布！");
    });
}


//学生随时可记录自己不会的问题
//显示问题
function qc_init(){
        var url=getUrlWithQueryString("/question/");
    $.jxGet(url,null,function (json) {
    	$('#qc_question').val(json.Name);
    });
}
//问题灭除
function qc_ok(){
	var  c1=$('#qc_desc').val();
        var url=getUrlWithQueryString("/question/");        
    $.jxUpdae(url,{content:c1},function (json) {
    	alert("问题已灭除！");
    });
}

//提交问题
function question_commit(){
	var  c1=$('#txt_question').val();
        var url="/question/";        
    $.jxREST(url,{content:c1},function (json) {
    	//关闭问题对话框
    	$("#modal_question").modal("hide");
    });
}


//
//查看某位同学的计划执行情况
function mission_disp(peopleid,time){
	//alert(peopleid);
	//alert(time);
	$('#mission_disp_editor1').html("");
	$('#mission_disp_editor2').html("");
	$('#mission_disp_editor3').html("");
	$('#mission_disp_editor4').html("");
	$('#mission_disp_editor5').html("");
		
    var url="/schedule/disp";
    $.jxREST(url,{Date:time,PeopleID:peopleid},function (json) {
    	if(!json)return;
    	$.each(json, function(i, sub) {  
    	var desc=$.base64.atob(sub.Descr,true);
//alert(desc);    	
    	if(sub.Name=="基础练习")
    		$('#mission_disp_editor1').html(desc);
    	else if(sub.Name=="问题灭除")
    		$('#mission_disp_editor2').html(desc);
    	else if(sub.Name=="项目实训")
    		$('#mission_disp_editor3').html(desc);
    	else if(sub.Name=="文体活动")
    		$('#mission_disp_editor4').html(desc);
    	else if(sub.Name=="讲座")
    		$('#mission_disp_editor5').html(desc);
    });
             }); 
    
}

//显示当天的计划作为活动的模板
function mission_record_disp(time){
	//alert(time);
	var  c1=$('#mission_record_editor1').html();
	var  c2=$('#mission_record_editor2').html();
	var  c3=$('#mission_record_editor3').html();
	var  c4=$('#mission_record_editor4').html();
	var  c5=$('#mission_record_editor5').html();
		
    var url="/plan/disp";
    $.jxREST(url,{Date:time},function (json) {
    	if(!json)return;
    	//alert($.toJSON(json));
    	$.each(json, function(i, sub) {  
    	//alert(sub.Name);
    	//alert(sub.Descr);    	
    	var desc=$.base64.atob(sub.Descr,true);
    	//lert(desc);    	
    	if(sub.Name=="基础练习")
    		$('#mission_record_editor1').html(desc);
    	else if(sub.Name=="问题灭除")
    		$('#mission_record_editor2').html(desc);
    	else if(sub.Name=="项目实训")
    		$('#mission_record_editor3').html(desc);
    	else if(sub.Name=="文体活动")
    		$('#mission_record_editor4').html(desc);
    	else if(sub.Name=="讲座")
    		$('#mission_record_editor5').html(desc);
    });
             }); 
}

//创建项目组
function createteam_ok(){
    var node=document.getElementById("createteam_name");
    var name=$(node).val();
    if(name.Trim()!=""){
    var url=getUrlWithQueryString("/team/createTeam")
    //alert(url)
    $.jxREST(url,{Name:name}, function () {
    	alert("项目组添加成功")
    });
    	}
}

//创建用户
function createuser_ok(){
    var node=document.getElementById("createuser_name");
    var peopletype="student";
    var n1=document.getElementById("createUser_cb_student");
    if(!n1.checked)
    	peopletype="teacher";
    
    var name=$(node).val();
    if(name.Trim()!=""){
    var url=getUrlWithQueryString("/Person")
    //alert(url)
    $.jxREST(url,{Name:name,PeopleType:peopletype}, function () {
    	alert("用户添加成功")
    });
    	}
}

//将某用户分配到某项目组
function assign_ok(){
    var url=getUrlWithQueryString("/team/assignToTeam")
    //alert(url)
    $.jxREST(url,null, function () {
    	alert("用户指派成功");
    });
}

//登记今天自己的计划完成情况
function mission_record_commit(){
	var  c1=$('#mission_record_editor1').html();
	var  c2=$('#mission_record_editor2').html();
	var  c3=$('#mission_record_editor3').html();
	var  c4=$('#mission_record_editor4').html();
	var  c5=$('#mission_record_editor5').html();
		//alert(c1)
		c1=$.base64.btoa(c1,true);
		c2=$.base64.btoa(c2,true);
		c3=$.base64.btoa(c3,true);
		c4=$.base64.btoa(c4,true);
		c5=$.base64.btoa(c5,true);
    var url="/schedule";
    $.jxREST(url,{content1:c1,content2:c2,content3:c3,content4:c4,content5:c5},function () {
    	alert("计划已提交！");
    });
}
//发布计划
function plan_edit_commit(){
	var  c1=$('#plan_edit_editor1').html();
	var  c2=$('#plan_edit_editor2').html();
	var  c3=$('#plan_edit_editor3').html();
	var  c4=$('#plan_edit_editor4').html();
	var  c5=$('#plan_edit_editor5').html();
		//alert(c1)
		c1=$.base64.btoa(c1,true);
		c2=$.base64.btoa(c2,true);
		c3=$.base64.btoa(c3,true);
		c4=$.base64.btoa(c4,true);
		c5=$.base64.btoa(c5,true);
    var url="/plan";
    $.jxREST(url,{content1:c1,content2:c2,content3:c3,content4:c4,content5:c5},function () {
    	alert("计划已提交！");    	
    });    
}
//显示计划
function plan_disp(time){
	//alert(time);
	var  c1=$('#plan_disp_editor1').html();
	var  c2=$('#plan_disp_editor2').html();
	var  c3=$('#plan_disp_editor3').html();
	var  c4=$('#plan_disp_editor4').html();
	var  c5=$('#plan_disp_editor5').html();
		
    var url="/plan/disp";
    $.jxREST(url,{Date:time},function (json) {
    	if(!json)return;
    	//alert($.toJSON(json));
    	$.each(json, function(i, sub) {  
    	//alert(sub.Name);
    	//alert(sub.Descr);    	
    	var desc=$.base64.atob(sub.Descr,true);
    	//alert(desc);    	
    	if(sub.Name=="基础练习")
    		$('#plan_disp_editor1').html(desc);
    	else if(sub.Name=="问题灭除")
    		$('#plan_disp_editor2').html(desc);
    	else if(sub.Name=="项目实训")
    		$('#plan_disp_editor3').html(desc);
    	else if(sub.Name=="文体活动")
    		$('#plan_disp_editor4').html(desc);
    	else if(sub.Name=="讲座")
    		$('#plan_disp_editor5').html(desc);
    });
             });     
}

//
function checkPaste(controlID) {
	$('#'+controlID).on("paste", function(e) { 
	var c;
	if(widow.clipboardData && widow.clipboardData.getDate())
		c=widow.clipboardData.getDate('Text');
	else 
		c=e.originalEvent.clipboardData.getDate('Test');
		
	if(c1.length>750){
		alert('输入的字符串太长，请不要超过750个字符');
			e.preventDefault();
	}
        }); 
}

//显示富文本编辑器
function dispACEEditor (controlID) {
		$('#'+controlID).ace_wysiwyg({
		toolbar:
		[
			'font',
			null,
			'fontSize',
			null,
			{name:'bold', className:'btn-info'},
			{name:'italic', className:'btn-info'},
			{name:'strikethrough', className:'btn-info'},
			{name:'underline', className:'btn-info'},
			null,
			{name:'insertunorderedlist', className:'btn-success'},
			{name:'insertorderedlist', className:'btn-success'},
			{name:'outdent', className:'btn-purple'},
			{name:'indent', className:'btn-purple'},
			null,
			{name:'justifyleft', className:'btn-primary'},
			{name:'justifycenter', className:'btn-primary'},
			{name:'justifyright', className:'btn-primary'},
			{name:'justifyfull', className:'btn-inverse'},
			null,
			{name:'createLink', className:'btn-pink'},
			{name:'unlink', className:'btn-pink'},
			null,
			{name:'insertImage', className:'btn-success'},
			null,
			'foreColor',
			null,
			{name:'undo', className:'btn-grey'},
			{name:'redo', className:'btn-grey'}
		]
	}).prev().addClass('wysiwyg-style2');
}

//angularJS的相应代码
var ahnd = angular.module('ahnd', []);
//
//导航条
//
//通用的菜单选项
ahnd.controller('NavbarCtrl', function ($scope,$http,$location) {

    var array=[
        {
            "label": "首页",
            "href": "missionList.html",
            "children": []
        }, {
            "label": "人员列表",
            "href": "peopleList.html",
            "children": []
        }, {
            "label": "创建计划",
            "href": "plan_edit.html",
            "children": []
        }, {
            "label": "查看计划",
            "href": "plan_disp.html",
            "children": []
        }, {
            "label": "录入执行情况",
            "href": "mission_record.html",
            "children": []
        },{
            "label": "录入试题",
            "href": "testing_subject.html",
            "children": []
        },{
            "label": "发布任务",
            "href": "#",
            "children": [
                {
                    "label": "敲代码",
                    "href": "coding_issue.html"
                },
                {
                    "label": "测验",
                    "href": "testing_issue.html"
                }]
        },{
            "label": "执行任务",
            "href": "#",
            "children": [
                {
                    "label": "敲代码",
                    "href": "codingList.html"
                },
                {
                    "label": "测验",
                    "href": "testingList.html"
                }]
        }]

			//收集当前页面url中的session信息
			checkSession();
        // for 循环
        var length = array.length;
        for (var i=0; i < length; i++) {
            array[i].href=getUrlWithQueryString(array[i].href)
            //alert(array[i].href)
            if(array[i].children&&array[i].children.length>0){
                var ch=array[i].children;
                var len = ch.length;
                for (var j=0; j < len; j++) {
                    ch[j].href=getUrlWithQueryString(ch[j].href)     
                }
            }
        }
        $scope.navbar=array;
});
//保活，敲代码时由于都可以在前端完成，所以为避免session的15分钟无操作则自动删除，所以需要在敲代码练习是保活
function keeplive(){
    var url="/system/keeplive";
    $.jxREST(url,null);
}
//敲代码所用到的变量
//老师发布的内容，之前采取了多种方案来避免学生通过修改老师的内容来躲过检查，最简单的还是将老师发布的内容进行保存
var code_teacher;
//完成本任务所花费的时间
var duration=0;
//总要求练习次数
var coding_total=0;
//当前练习次数
var coding_num=0;
//当前练习的id
var coding_id;
var coding_url;
//定时器：保活和代码检验
var Interval_keeplive;
var Interval_checkCode;

function clearTimer() {
	if(Interval_checkCode){
		clearInterval(Interval_keeplive);
		clearInterval(Interval_checkCode);
		Interval_keeplive=null;
		Interval_checkCode=null;
	}
}
//定时检查代码，如果匹配则自动要求下一次，次数达到了则自动结束
function checkCode() {	
//5秒检查一次
	duration+=5;
	//var  c1=zip($('#coding_teacher').html());	
	//需要将内容中的空格之类的空白符全部去掉
	var  c2=zip($('#coding_student').html());
	if(code_teacher==c2){
		coding_num++;
		alert("总次数："+coding_total+",已完成次数："+coding_num)
		if(coding_num<coding_total) {
			alert("输入了"+coding_num+"次，请继续！");
        	$('#coding_student').html("");
			return;
		}
			clearTimer();
			var url=getUrlWithQueryString("/coding/close/");
			$.jxREST(url,{CodingID:coding_id,Duration:duration}, function (json) {
		alert("checkCode OK!");				
			});
	}
}
//用正则表达式将空白符全部去掉：由于是富文本编辑器，所以回车是<br>
function  zip(str) {
	var re = /\s/gmi;
	var re1 = /\<br\>/gmi;
	var re2 = /\&nbsp\;/gmi;
	if(str)
		return str.replace(re,'').replace(re1,'').replace(re2,'');
	return str;
}
//控制器：活动列表
ahnd.controller('controller_missionList', function ($scope,$http,$location) {
	
	    var url=getUrlWithQueryString("/Person/listMyMission/");
    $.jxREST(url,null, function (json) {

    	//alert($.toJSON(json));
        	var array=[];
        // for 循环
        var length = json.length;
        for (var i=0; i < length; i++) {
        		array[i]={};
        		if(json[i].type=="Coding"){
            	array[i].href=getUrlWithQueryStringAndID("/coding.html",json[i].id,"CodingID");      
            	array[i].Type="敲代码";
            }      	
        		else if(json[i].type=="Testing"){
            	array[i].href=getUrlWithQueryStringAndID("/testing.html",json[i].id,"PaperID");
            	array[i].Type="测验";
            }
            	
            array[i].date=json[i].date;
            array[i].name=json[i].name;
        }
        $scope.list=array;
        
    });
});

//控制器：敲代码任务列表
ahnd.controller('controller_codeList', function ($scope,$http,$location) {				
	    var url="/coding/listMyCoding/";
    $.jxREST(url,null, function (json) {
        $scope.list=json;
        $scope.disp=function(id){
        	//用户点击列表后，执行跳转
    		window.location.href=getUrlWithQueryStringAndID("/coding.html",id,"CodingID");
    };
    });
});
//显示敲代码任务
//为了避免学生通过作弊来逃避任务，如复制粘贴、将老师发布内容全部删除等，所以执行了很多处理，请查看coding.html网页中的onload事件中的说明
function dispCode() {
        	var url=getUrlWithQueryString("/coding/getCoding/");
    $.jxREST(url,null, function (json) {    	
    	$("#coding_InputNum ").val("需要敲入："+json.Num+"次");
			coding_id=json.ID;
			coding_total=json.Num;
			coding_num=0;
        	var desc=$.base64.atob(json.Descr,true);
        	//alert(desc);
        	code_teacher=zip(desc);
        	$('#coding_teacher').html(desc);
        	$('#coding_student').html("");
			clearTimer();
        Interval_keeplive=setInterval("keeplive()",300000)
        Interval_checkCode=setInterval("checkCode()",5000)
});
}
//测验列表
ahnd.controller('controller_testList', function ($scope,$http,$location) {	
	    var url="/testing/listMyTesting/";
    $.jxREST(url,null, function (json) {
        $scope.list=json;        
        $scope.dispTest=function(id){
    		window.location.href=getUrlWithQueryStringAndID("testing.html",id,"PaperID");
    };
    });
});

//学生如果有任务没完成则在某些页面中通过标红加以提示
var menu_st;
ahnd.controller('controller_menu_sutdent', function ($scope,$http,$location) {	
			//因为是菜单选项，所以一定要先进行session的检查动作
			checkSession();			
			menu_st=[];        
			var u1="/Person/getMyState/";
    $.jxREST(u1,null, function (state) {
        if(state.leave)
        		$scope.leave="btn-danger";
        if(state.Coding)
        		menu_st["coding"]="btn-danger";
        if(state.Testing)
        		menu_st["testing"]="btn-danger";        		
	    var url="/system/getPageMenu/";
	    
    $.jxREST(url,{Page:getUrlPage()}, function (json) {
    	
    	//alert($.toJSON(json));
    	
        	var array=[];
        // for 循环
        var length = json.length;
        for (var i=0; i < length; i++) {
        	array[i]={};
            array[i].href=getUrlWithQueryString(json[i].href);
            array[i].label=json[i].lable;
            array[i].class=menu_st[json[i].name];
        }
        $scope.list=array;

    	//alert($.toJSON(array));
    });
    });
});


//
//可在后台自定义的导航条
//用户可以在./conf/menu.lua中定义自己的菜单
ahnd.controller('controller_menu', function ($scope,$http,$location) {
	
			checkSession();

	    var url="/system/getPageMenu/";
	    //获取当前页面的名字，如test.html则返回test
	    var pageName=getUrlPage();
    $.jxREST(url,{Page:pageName}, function (json) {
    	
    	//alert($.toJSON(json));
    	
        	var array=[];
        // for 循环
        var length = json.length;
        for (var i=0; i < length; i++) {
        	array[i]={};
            array[i].href=getUrlWithQueryString(json[i].href);
            array[i].label=json[i].lable;
            array[i].class="btn-"+json[i].state;
        }
        $scope.list=array;
        $scope.test="btn-success";
    });
});


//
//peoplelist
//
ahnd.controller("controller_peopleList",function ($scope,$http,$location) {
    //alter("controller_roomList");
    //var arr=[{ID:"12345678",Name:"roomName",Family:"roomFamily",Descr:"roomDescr",Record:false},
    //{ID:"123456781111111",Name:"roomName11111",Family:"roomFamily11111111",Descr:"roomDescr11111111",Record:true}];

    var url="/Person/list/";
    $.jxREST(url,null, function (json) {
        var href="mission_disp.html";
        var href2="questionList.html";
        var href3="missionList.html";
        var length = json.length;
        //alert(length);
        for (var i=0; i < length; i++) {
            json[i].href=getUrlWithQueryStringAndID(href,json[i].ID,"PeopleID");
            json[i].href2=getUrlWithQueryStringAndID(href2,json[i].ID,"PeopleID");
            json[i].href3=getUrlWithQueryStringAndID(href3,json[i].ID,"PeopleID");
            if(json[i].Missions)
            	json[i].MissionState="btn-danger";
            //alert(json[i].href)
        }
        $scope.list=json;
 });
});
//
//assignToTeam
//
ahnd.controller('controller_peopleList_assignToTeam', function ($scope,$http,$location) {
    var url="/Person/list/";
    $.jxREST(url,null, function (json) {
        var href="assign.html";
        var length = json.length;
        //alert(length);
        for (var i=0; i < length; i++) {
//alert($.toJSON(json[i]));
            json[i].href=getUrlWithQueryStringAndID(href,json[i].ID,"PeopleID");
            //alert(json[i].href)
        }
        $scope.list=json;
    });
});

//
//teamlist
//
ahnd.controller('controller_TeamList', function ($scope,$http,$location) {
    var url="/team/list/";
    $.jxREST(url,null, function (json) {
        var href="assign_ok.html";
        var length = json.length;
        //alert(length);
        for (var i=0; i < length; i++) {
//alert($.toJSON(json[i]));
				var s=getUrlWithQueryStringAndID(href,json[i].ID,"TeamID");				
            json[i].href=getUrlAddParam(s,"组长","Role");
            json[i].href2=getUrlAddParam(s,"组员","Role");
            //alert(json[i].href)
        }
        $scope.list=json;
    });
});

//
//问题清单
//
ahnd.controller('controller_questionList', function ($scope,$http,$location) {
    var url="/question/list/";
    $.jxREST(url,{PeopleID:getQueryString("PeopleID")}, function (json) {
        var href="question_close.html";
        var length = json.length;
        //alert(length);
        for (var i=0; i < length; i++) {        	
        	if(!json[i].Time){
            json[i].href=getUrlWithQueryStringAndID(href,json[i].ID,"QuestionID");
            json[i].OP="灭除";
            //alert(json[i].href)
        }else{
        	json[i].href="#";
        	json[i].OP="无";
        }
     }
        $scope.list=json;
    });
});


//
//下载文件列表
//
ahnd.controller('controller_fileList', function ($scope,$http,$location) {
	    var url="/system/listDownloadFile/";
    $.jxREST(url,null, function (json) {
        $scope.list=json;
    });
});