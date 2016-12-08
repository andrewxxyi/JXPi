
function createuser_ok(){
    var node=document.getElementById("createuser_name");
    var name=$(node).val();
    if(name.Trim()!=""){
    var url=getUrlWithQueryString("/Person")
    //alert(url)
    $.jxREST(url,{Name:name}, function () {
    	alert("用户添加成功")
    });
    	}
}


