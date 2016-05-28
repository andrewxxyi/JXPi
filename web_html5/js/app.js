

function test(){
    var url="/jxLua/call";
    $.jxREST(url,{Action:"car",Func:"test",Radian:78,Speed:1.23,test:"ok"},function(){
    		alert("ok")
    	});
}

function stopdrive(){
    var url="/jxLua/call";
    $.jxREST(url,{Action:"car",Func:"stop"});
}

function drive(radian,speed){
    var url="/jxLua/call";
    $.jxREST(url,{Action:"car",Func:"drive",Radian:radian,Speed:speed});
}

var dura=1000;
var count=0;
var speedComputeCallback;
function getStopwatch(){
    var url="/jxLua/call";
    $.jxREST(url,{Action:"car",Func:"getStopwatchValue"},function(json){
    		var e=json.Result-count;
    		count=json.Result;
    		var speed=Math.round(e*1000/dura);
    		if(speedComputeCallback)
    			speedComputeCallback(speed)
    	});
}
function setSpeedCompute(dual){
	speedComputeCallback=dual;
	setInterval("getStopwatch()",dura)	
}

var liveID;
function live_start(){
    if(liveID){
        thePlayer.pause();
        live_stop();
    }
    var url="/Medium/play/";
    $.jxREST(url,{Active:"live_ogg"}, function (json) {
    	  if(json.noPlay){
    	  	alert("视频流播放地址："+json.ServerIP+":"+json.Port);
    	  	return;
    	  }
        liveID=json.SDP;
        var str="http://"+json.ServerIP+":"+json.Port+"/"+json.SDP;
                //alert(str+",type:"+json.Type);
            //if(html5)
               //play_html5(str,json.Type);
            //else
               play_html5(str,json.Type);
        setInterval("infoLive_ID()",15000)
    });
}
function live_start_HikVison(devip,username,passwd,channelid){
    if(liveID){
        thePlayer.pause();
        live_stop();
    }
    var url="/Medium/play/";
    $.jxREST(url,{Active:"live_HikVison_ogg",UserName:username,Passwd:passwd,DevIP:devip,ChannelID:channelid}, function (json) {
    	  if(json.noPlay)return;
        liveID=json.SDP;
        var str="http://"+json.ServerIP+":"+json.Port+"/"+json.Page;
                //alert(str+",type:"+json.Type);
            //if(html5)
               //play_html5(str,json.Type);
            //else
               play_html5(str,json.Type);
        setInterval("infoLive_ID()",15000)
    });
}

var source;
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
    if(!liveID)
        return;
    //alert("video_stop");
    var url="/Medium/stop/";
    $.jxREST(url,{LiveID:liveID});
}

function infoLive_ID(){
    if(!liveID)
        return;
    var url="/Medium/keeplive";
    $.jxREST(url,{LiveID:liveID});
}


//
//导航条
//
var pi = angular.module('pi', []);

pi.controller('NavbarCtrl', function ($scope,$http,$location) {

    var array=[
        {
            "label": "首页",
            "href": "main.html",
            "children": []
        }]

        // for 循环
        var length = array.length;
        for (var i=0; i < length; i++) {
            array[i].href=getUrlWithQueryString(array[i].href)
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

//
//roomlist
//
pi.controller('controller_roomList', function ($scope,$http,$location) {
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
