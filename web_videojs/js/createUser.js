
function opencreateUser(){
    openContainer("tab_Main","createUser.html","创建用户",function(contentPane,obj){
        readConf(contentPane);
        setEventHandle("keydown","createUser_UserName",contentPane,function(cp,e){
            if(e.keyCode==13) {
                createUser(cp);
            }   
        });
        setEventHandle("click","createUser_OK",contentPane,function(cp,e){
            createUser(cp); 
        });
    });
}

function createUser(cp){
    var url="/Person/createUser/";
    var name=getDomNodeValue("createUser_UserName",cp);
    if(name.Trim()!="")
    $.jxREST(url,{Name:name}, function (json) {
        alert("创建用户成功")
    });
}

