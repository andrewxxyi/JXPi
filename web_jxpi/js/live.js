
var liveIP;
var liveID;
function live_init(){
    var url="/medium/play/";
    $.jxREST(url,{Active:"live_ogg"}, function (json) {
        liveIP=json.ServerIP;
        liveID=json.SDP;
        var str="http://"+liveIP+":"+json.Port+"/"+json.Page;
        //alert(str+",type:"+json.Type);
        play_html5(str,json.Type);
        setInterval("keeplive()",15000)
    });
}
var source;
var thePlayer;


function play_html5(url,type){
        thePlayer=document.getElementById("div_live");
        if(source)
            $('#div_live').prop('src', url);
            //source.src=url;
        else{
            source = document.createElement('source');  
            source.src=url;
            thePlayer.appendChild(source); 
            //source.type= type;  
        }
        thePlayer.controls =true;
        thePlayer.play();
}

function live_stop(){
    video_stop(liveID);
}
function video_stop(liveid,record){
    if(!liveid)
        return;
    //alert("video_stop");
    var url="/medium/stop/";
    var param=null;
    if(record)
        param={RoomID:liveid};
    else
        param={LiveID:liveid};        
    $.jxREST(url,param, function (json) {
        if(record)
            window.location.href=document.referrer;
    });
}


function keeplive(){
    var url="/medium/keeplive";
    $.jxREST(url,{SDP:liveID});
}
