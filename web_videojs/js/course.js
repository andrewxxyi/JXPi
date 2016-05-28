
//
//roomlist
//
ssts.controller('controller_roomList', function ($scope,$http,$location) {
    //alter("controller_roomList");
    //var arr=[{ID:"12345678",Name:"roomName",Family:"roomFamily",Descr:"roomDescr",Record:false},
    //{ID:"123456781111111",Name:"roomName11111",Family:"roomFamily11111111",Descr:"roomDescr11111111",Record:true}];

    var url="/Room/list/";
    $.jxREST(url,null, function (json) {

        var href10="record.html";
        var href11="recordstop.html";
        //参数为：RoomID则代表实时播放
        var href2="live.html";
        var href3="courseList.html";
        var href4="roomEdit.html";
        var length = json.length;
        for (var i=0; i < length; i++) {
            var recording=parseInt(json[i].CourseState);
            json[i].href1=getUrlWithQueryStringAndID(recording==1?href11:href10,json[i].ID,"RoomID");
            json[i].op1=recording==1?"停止":"录制";
            json[i].button=recording==1?"glyphicon-stop":"glyphicon-facetime-video";
            //arr[i].button="glyphicon-facetime-video";
            json[i].href2=getUrlWithQueryStringAndID(href2,json[i].ID,"RoomID");
            json[i].href3=getUrlWithQueryStringAndID(href3,json[i].ID,"RoomID");
            json[i].href4=getUrlWithQueryStringAndID(href4,json[i].ID);
        }
        $scope.roomlist=json;
    });


});


ssts.controller('controller_courseList', function ($scope,$http,$location) {
    var url="/Medium/listCourse/";
    $.jxREST(url,null, function (json) {
        var length = json.length;
        for (var i=0; i < length; i++) {
            json[i].href=getUrlWithQueryStringAndID("replay.html",json[i].ID,"FDID");
        }
        $scope.list=json;
    });
});


function roomList_add(){
    alter("roomList_add");

}

function roomList_search(){
    alter("roomList_search");
    
}

function roomList_add(){
    gotoUrl("roomAdd.html")
}

function roomAdd_save(){
    var url="/Room/";
    var family=$("#room_Family").val();
    var room=$("#room_Room").val();
    var ip=$("#room_IP").val();
    var channel=$("#room_Channel").val();
    var user=$("#room_User").val();
    var passwd=$("#room_Passwd").val();
    var descr=$("#room_Descr").val();
    var param={Family:family,Room:room,DevIP:ip,DevChannelID:channel,DevUserName:user,DevPasswd:passwd,Descr:descr};
    alert(param.Family);
    $.jxInsert(url,param, function (json) {
        alert("设备添加成功");
            window.location.href=document.referrer;
    });
}

function roomEdit_init(){
    //alert("roomEdit_init ID:"+getQueryString("ID"));
    var url=getUrlWithQueryString("/Room/");
    //alert(url);
    $.jxGet(url,function (json) {
        $("#room_Family").val(json.Family);
        $("#room_Room").val(json.Name);
        $("#room_IP").val(json.DevIP);
        $("#room_Channel").val(json.DevChannelID);
        $("#room_User").val(json.DevUserName);
        $("#room_Passwd").val(json.DevPasswd);
        $("#room_Descr").val(json.Descr);
    });
}
function roomEdit_save(){
    var url="/Room/";
    var id=getQueryString("ID");
    var family=$("#room_Family").val();
    var room=$("#room_Room").val();
    var ip=$("#room_IP").val();
    var channel=$("#room_Channel").val();
    var user=$("#room_User").val();
    var passwd=$("#room_Passwd").val();
    var descr=$("#room_Descr").val();
    var param={ID:id,Family:family,Room:room,DevIP:ip,DevChannelID:channel,DevUserName:user,DevPasswd:passwd,Descr:descr};
    //alert(param.Family);
    $.jxUpdae(url,param, function (json) {
        alert("设备添加成功");
            window.location.href=document.referrer;
    });
}
var liveIP;
var liveID;
function live_init(){
    var url="/Medium/live/";
    $.jxREST(url,{RoomID:getQueryString("RoomID"),Type:"live_flv"}, function (json) {
        liveIP=json.ServerIP;
        liveID=json.SDP;
        var str="http://"+liveIP+":"+json.Port+"/"+json.Page;
        alert(str+",type:"+json.Type);
        play_videojs(str,json.Type);
        setInterval("infoLive_ID()",15000)
    });
}
var source;
var thePlayer;

function live_start(username,passwd,devip,channelid,html5){
    if(liveID){
        thePlayer.pause();
        live_stop();
    }
    var url="/Medium/liveplay/";
    noaudio=false;
    $.jxREST(url,{UserName:username,Passwd:passwd,DevIP:devip,ChannelID:channelid,NoAudio:noaudio,Type:"live_flv"}, function (json) {
        liveIP=json.ServerIP;
        liveID=json.SDP;
        var str="http://"+liveIP+":"+json.Port+"/"+json.Page;
                //alert(str+",type:"+json.Type);
            //if(html5)
               //play_html5(str,json.Type);
            //else
               play_videojs(str,json.Type);
        setInterval("infoLive_ID()",15000)
    });
}


function play_html5(url,type){
        thePlayer=document.getElementById("course_live");
        if(source)
            $('#course_live').prop('src', url);
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
function play_videojs(url,type){
    thePlayer = videojs('course_live');
    videojs("course_live").src({type:type,src:url});
    videojs("course_live").play();
}
function live_stop(){
    video_stop(liveID);
}
function video_stop(liveid,record){
    if(!liveid)
        return;
    //alert("video_stop");
    var url="/Medium/stop/";
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


function infoLive_ID(){
    var url="/Medium/keeplive";
    $.jxREST(url,{SDP:liveID});
}
function infoLive_9Grid(){
    //alert("infoLive_9Grid");
    var url="/Medium/keeplive";
    $.each(live_9_ID,function(k,v){
        //alert(v.SDP);
        $.jxRequest(url,{SDP:v.SDP});
    });

}

function record_start(){
    var url="/Medium/record";
    var name=$("#record_Name").val();
    var teacher=$("#record_Teacher").val();
    var duration=$("#record_Duration").val();
    var descr=$("#record_Descr").val();
    var param={RoomID:getQueryString("RoomID"),Name:name,Teacher:teacher,Duration:duration,Descr:descr};
    $.jxREST(url,param, function (json) {
        alert("课程开始录制");
            window.location.href=document.referrer;
    });
}
function record_stop(){
    video_stop(getQueryString("RoomID"),true);
}

function replay_init(){
    var url="/Medium/replay/";
    $.jxREST(url,{FDID:getQueryString("FDID"),Type:"replay_flv"}, function (json) {
        liveIP=json.ServerIP;
        liveID=json.SDP;
        var str="http://"+liveIP+":"+json.Port+"/"+json.Page;
        alert(str+",type:"+json.Type);
            //if(html5)
               //play_html5(str,json.Type);
            //else
        play_videojs(str,json.Type);
        setInterval("infoLive_ID()",15000)
    });
}



var live_9_ID={};
function live_play(videoel,username,passwd,devip,channelid){
    var url="/Medium/liveplay/";
    $.jxREST(url,{UserName:username,Passwd:passwd,DevIP:devip,ChannelID:channelid}, function (json) {
        var video=document.getElementById(videoel);
        var source = document.createElement('source');  
        //source.src = "/VideoRecord/replay/"+filedescid;  
        live_9_ID[videoel]=json;
        var str="http://"+json.DevIP+":"+json.Port+"/"+json.SDP;
        //alert(str)
        source.src = str;  
        //source.type= "video/mp4";  
        video.appendChild(source); 
        video.controls =true;
        video.play();
    });
}
function live_9gird_init(){
    live_play("live_1_1","admin","admin12345","172.16.1.15",1);
    live_play("live_1_2","admin","admin12345","172.16.1.10",2);
    live_play("live_1_3","admin","admin12345","172.16.1.10",3);
    live_play("live_2_1","admin","admin12345","172.16.1.10",4);
    live_play("live_2_2","admin","admin12345","172.16.1.10",5);
    live_play("live_3_1","admin","admin12345","172.16.1.10",6);
    live_play("live_3_2","admin","admin12345","172.16.1.10",7);
        setInterval("infoLive_9Grid()",30000)
}
function live_9gird_stop(){
    if(!live_9_ID)
        return;
    $.each(live_9_ID,function(k,v){
        video_stop(v.SDP);
    });
}

function live_OpenWin(elname){

        var json=live_9_ID[elname];
    window.open('live_play.html/?DevIP='+json.DevIP+'&Port='+json.Port+'&SDP='+json.SDP)
}
function live_play_init(){
    var devip=getQueryString("DevIP");
    var port=getQueryString("Port");
    var sdp=getQueryString("SDP");
    //alert(sdp);

    var video=document.getElementById("course_live");
        var source = document.createElement('source');  
        //source.src = "/VideoRecord/replay/"+filedescid;  
        var str="http://"+devip+":"+port+"/"+sdp;
        //alert(str)
        source.src = str;  
        //source.type= "video/mp4";  
        video.appendChild(source); 
        video.controls =true;
        video.play();
}

function test(){
    var thejwPlayer = jwplayer('course').setup({
        flashplayer:"/js/jwplayer/jwplayer.flash.swf",
        width: '720',          
        height: '576'
    });
}