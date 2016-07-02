

function login_ok(){
    var node=document.getElementById("login_name");
    var name=$(node).val();
    node=document.getElementById("login_passwd");
    var pass=$(node).val();
    if(name.Trim()!=""&&pass.Trim()!="")
        $.login(name,pass,function(json){
            var url=getUrlWithQueryString('missionList.html');
            //alert(url);
            window.location.href=url;
        });
}


