
function openChannel(channelid){
	setChannel(channelid,"Open");
}
function closeChannel(channelid){
	setChannel(channelid,"Close");
}

function setChannel(channelid,state){
    var url="/Control/setChannel/";
    $.jxREST(url,{Channel:channelid,Cmd:state}, function (json) {
            //alert($.toJSON(json));
            if(json){
                alert("命令已执行");
            }
    });
}

