
var ptzArr=new Array();
function setPTZ(id,cmd){
	$('#'+id).mousedown(function(){
		ptzArr[id]=true;
      	ptz_201(cmd,"start");
              });
      $('#'+id).mouseup(function(){
      	ptz_201(cmd,"stop");
      	ptzArr[id]=false;
              });
      $('#'+id).mouseout(function(){
      	if(ptzArr[id]){
      		ptz_201(cmd,"stop");
      		ptzArr[id]=false;
      	}
        });
}
function ptz_201(cmd,active){
	ptz("admin","admin12345","172.16.1.15",1,cmd,active);
}

function ptz(userName,passwd,dvrIP,channelID,cmd,active){
    var url="/HikVision/ptz/";
    $.jxRequest(url,{DvrIP:dvrIP,DevUserName:userName,DevPasswd:passwd,DevChannelID:channelID,PTZ:cmd,Active:active}, function (json) {
            //alert($.toJSON(json));
    });
               // alert("命令已执行");
}

function airControl_6D201(state){
    var url="/FrontUSR232/call/";
    $.jxRequest(url,{Function:"airControl",DevName:"6D201Air",Cmd:state}, function (json) {
            //alert($.toJSON(json));
    });
                alert("命令已执行");
}

function setChannel(cmd,channelid,lockname,inverserchannelid,delaysecond,tip){
    var url="/FrontUSR/call/";
    $.jxRequest(url,{Function:"setChannel",DevName:"USR1",Channel:channelid,Lock:lockname,Cmd:cmd,InverseChannel:inverserchannelid,DelaySecond:delaysecond,Async:true}, function (json) {
            //alert($.toJSON(json));
    });
                if(tip)
                    alert(tip)
                else
                    alert("命令已执行")
}

function openChannel(channelid,lockName,tip){
    setChannel(channelid,"Open",lockName);
    if(tip)
                alert(tip);
}
function closeChannel(channelid,lockName,tip){
    setChannel(channelid,"Close",lockName);
    if(tip)
                alert(tip);
}

function openPowerAll(){
    openChannel(1);
    openChannel(2);
    openChannel(3);
                alert("命令已执行");
}
function closePowerAll(){
    closeChannel(1);
    closeChannel(2);
    closeChannel(3);
                alert("命令已执行");
}

function openWithClose(channelid,delaySecond,firstInverseOpChannelID,lockName){
    opDelayInverseOp(channelid,"Open",delaySecond,firstInverseOpChannelID,lockName);
                alert("命令已执行");
}
function closeWithOpen(channelid,delaySecond,firstInverseOpChannelID,lockName){
    opDelayInverseOp(channelid,"Close",delaySecond,firstInverseOpChannelID,lockName);
                alert("命令已执行");
}


function lock301Table(msg){
    openChannel(5);
    openChannel(6);
    lock("301Table",msg)
}
function unlock301Table(msg){
    unlock("301Table",msg)
    closeChannel(5);
    closeChannel(6);
}
function lock(lockName,msg){
    var url="/Control/lock/";
    $.jxREST(url,{Lock:lockName}, function (json) {
            //alert($.toJSON(json));
            if(json){
                alert(msg);
            }
    });
}
function unlock(lockName,msg){
    var url="/Control/unlock/";
    $.jxREST(url,{Lock:lockName}, function (json) {
            //alert($.toJSON(json));
            if(json){
                alert(msg);
            }
    });
}


//firstInverseOpChannelID：执行动作前先对其进行反操作
function opDelayInverseOp(channelid,state,delaySecond,firstInverseOpChannelID,lockName){
    var url="/Control/opDelayInverseOp/";
    $.jxREST(url,{Channel:channelid,Cmd:state,Delay:delaySecond,FirstInverseOpChannelID:firstInverseOpChannelID,Lock:lockName}, function (json) {
            //alert($.toJSON(json));
            if(json){
                //alert("命令已执行");
            }
    });
}


//
//导航条
//
var ssts = angular.module('ssts', []);

ssts.controller('NavbarCtrl', function ($scope,$http,$location) {
    //alert("NavbarCtrl");
    //$http.get("/js/nav.json").success(function(json){
    //    $scope.navbar = json;
    //});

    var array=[
        {
            "label": "首页",
            "href": "main.html",
            "children": []
        },
         {
            "label": "在线课程",
            "href": "#",
            "children": [
                {
                    "label": "实时课程",
                    "href": "roomList.html"
                },
                {
                    "label": "录播课程",
                    "href": "courseList.html"
                }]
        },
         {
            "label": "6D201",
            "href": "6D201.html",
            "children": []
        },
         {
            "label": "6D301",
            "href": "6D301.html",
            "children": []
        },
         {
            "label": "6D303",
            "href": "6D303.html",
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
