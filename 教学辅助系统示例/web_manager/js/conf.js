
function openConf(){
    openContainer("tab_Main","conf.html","配置",function(contentPane,obj){
        readConf(contentPane);
        setEventHandle("keydown","Conf_NetIOIP",contentPane,function(cp,e){
            if(e.keyCode==13) {
                saveConf(cp);
            }   
        });
        setEventHandle("click","Conf_OK",contentPane,function(cp,e){
            saveConf(cp); 
        });
    });
}

function readConf(cp){
    var url="/Person/getConf/";
    $.jxREST(url,{Purpose:"net"}, function (json) {
            //alert($.toJSON(json));
            if(json){
                setDomNodeValue("Conf_NetIOIP",cp,json.NetIOIP);
                setDomNodeValue("Conf_NetIOPasswd",cp,json.NetIOPasswd);
            }
    });
}

function saveConf(cp){
    alert("saveConf");
    var url="/Person/saveConf/";
    var netip=getDomNodeValue("Conf_NetIOIP",cp);
    var passwd=getDomNodeValue("Conf_NetIOPasswd",cp);
    if(netip.Trim()!="")
    $.jxREST(url,{NetIOIP:netip,NetIOPasswd:passwd}, function (json) {
        alert("配置设置成功")
    });
}

