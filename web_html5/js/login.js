

function checkForLogin(){
    //alert("ClickForLogin");
    var node=document.getElementById("inputName");
    var name=$(node).val();
    node=document.getElementById("inputPassword");
    var pass=$(node).val();
    if(name.Trim()!=""&&pass.Trim())
        $.login(name,pass,function(json){
            var url=getUrlWithQueryString('main.html');
            alert(url);
            window.location.href=url;
        });
}


