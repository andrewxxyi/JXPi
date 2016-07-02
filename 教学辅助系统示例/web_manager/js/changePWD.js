
var changePWDContentPane;
function openchangePWD(){
    openContainer("tab_Main","changePWD.html","修改密码",function(contentPane,obj){
        changePWDContentPane=contentPane;
        setEventHandle("keydown","ChangePwd_Again",contentPane,function(cp,e){
            if(e.keyCode==13) {
                dual_changePWD_OK();
            }   
        });
        setEventHandle("click","ChangePwd_OK",contentPane,function(cp,e){
            dual_changePWD_OK(); 
        });
    });
}

function dual_changePWD_OK(cp){
    //alert("ClickForLogin");
    var url="/Person/setPasswd/"+me.ID;
    var passwd=getDomNodeValue("ChangePwd_Passwd",cp);
    var again=getDomNodeValue("ChangePwd_Again",cp);
    if(passwd.Trim()!=""&&passwd==again){
        $.jxREST(url,{Name:me.Name,Passwd:passwd}, function (json) {
                //alert($.toJSON(json));
                if(json)
                    alert("密码重置成功")
        });
    }
    else{
        alert("密码不能为空，且两次输入的密码应相等！")
    }
}


