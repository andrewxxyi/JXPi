
function changepwd_ok(){
    var node=document.getElementById("changepwd_pass");
    var pass=$(node).val();
    node=document.getElementById("changepwd_again");
    var again=$(node).val();
    
    if(pass.Trim()!=""&&pass==again){
    var url=getUrlWithQueryString("/system/setPasswd");
    //alert(url)
    $.jxREST(url,{Passwd:pass}, function () {
    	alert("密码修改成功");
        window.location.href=getUrlWithQueryString("index.html");
    });
    }else {
    	alert("密码不能为空，且两次应输入一致！");
    }
}

function createuser_ok(){
    var node=document.getElementById("createuser_name");
    var name=$(node).val();
    node=document.getElementById("createuser_descr");
    var descr=$(node).val();
    
    if(name.Trim()!=""){
    var url=getUrlWithQueryString("/system/createUser");
    //alert(url)
    $.jxREST(url,{Name:name,Descr:descr}, function () {
    	alert("用户添加成功")
    });
    }
}

function logout(){
    var url="/system/logout";
    $.jxREST(url,null,function(){
        window.location.href="login.html";
    });
}

function login_ok(){
    var node=document.getElementById("login_name");
    var name=$(node).val();
    node=document.getElementById("login_passwd");
    var pass=$(node).val();
    if(name.Trim()!=""&&pass.Trim()!="")
        $.login(name,pass,false,function(json){
            var url=getUrlWithQueryString('index.html');
            //alert(url);
            window.location.href=url;
        });
}

function startSSH(){
    var url="/ui/startSSHR/";
    $.jxREST(url,null,function (json) {
    	alert("系统已打开远程ssh连接！！！远程ssh只应用于系统出现故障时，由系统工程师远程接入进行排错!!!\n端口号："+json.Port);
    });
}

function stopSSH(){
    var url="/ui/stopSSHR/";
    $.jxREST(url,null,function () {
    	alert("远程ssh连接已关闭");
    });
}

function shutdown(){
    var url="/ui/shutdown";
    $.jxREST(url,null,function(){
        alert("Pi shutdown now!")
    });
}

//angularJS的相应代码
var jx = angular.module('jx', []);
//
//下载文件列表
//
jx.controller('controller_fileList', function ($scope,$http,$location) {
	    var url="/system/listDownloadFile/";
    $.jxRequest(url,null, function (json) {
        $scope.list=json;
    });
});