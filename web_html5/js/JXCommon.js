

var sessionID;

//
//Ajax
//  以json方式传递参数并通过json方式获取返回值
//
; (function ($) {
    $.jxGetPage = function (URL, CallBackFunc) {
        //alert("jxGet:"+URL);
        $.ajax({
            type: "get",
            url: URL,
            //返回值为json格式
            //dataType: "json",
            //contentType: "application/x-www-form-urlencoded; charset=utf-8", 
            //将Object转换成json字符串post到后台
            //processData: false,
            //data: $.toJSON(Object),
            success: function (data) {
                if (CallBackFunc)
                    CallBackFunc(data);
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                //$.messager.alert("页面请求错误", XMLHttpRequest.responseText, "error");
                alert(XMLHttpRequest.responseText);
            },
            beforeSend: function(request) {
                if(sessionID)
                        request.setRequestHeader("jxSessionID", sessionID);
            }
        });
    };
    //将对象转换为json字符串以method发送到后台
    $.jxGet = function (URL, CallBackFunc) {
        ajax("get",true,URL, null, CallBackFunc);
    };    
    //将对象转换为json字符串以post发送到后台
    //由于$.toJSON(obj)是将对象内容转换，而不转换对象名，而象put、delete等方法中最好能知道对象名（即类名）这样就可以直接将json字符串转换为对象，然后就可以直接通过orm进行处理
    //所以这里的对象需象如下的ts这样进行处理：
    //var ts=new Object();
    //ts.TopSpace=new Object();
    //ts.TopSpace.Name="test";
    $.jxInsert = function (URL, obj, CallBackFunc) {
        ajax("post",true,URL, obj, CallBackFunc);
    };
    //同步方式
    $.jxREST = function (URL, obj, CallBackFunc) {
        ajax("post",false,URL, obj, CallBackFunc);
    };
    $.jxRequest = function (URL, obj, CallBackFunc) {
        ajax("post",true,URL, obj, CallBackFunc);
    };
    $.jxUpdae = function (URL, obj, CallBackFunc) {
        ajax("put",true,URL, obj, CallBackFunc);
    };
    $.jxDelete = function (URL, CallBackFunc) {
        ajax("delete",true,URL, null, CallBackFunc);
    };
    $.jxRequest_Sync = function (method, URL, obj, CallBackFunc) {
        ajax(method,false,URL, obj, CallBackFunc);
    };
    //不将对象转换为json进行传递
    $.jxUpoladFile = function (URL, obj, CallBackFunc) {
        $.ajax({
            type: "post",
            url: URL,
            //返回值为json格式
            //dataType: "json",
            //将Object转换成json字符串post到后台
            processData: false,
            contentType:false,
            data: obj,
            success: function (json) {
                if (json && !DualSystemReturn(json)) return;
                if (CallBackFunc)
                    CallBackFunc(json);
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                //$.messager.alert("页面请求错误", XMLHttpRequest.responseText, "error");
                alert("jxUpoladFile error:"+textStatus);
            },
            beforeSend: function(request) {
                if(sessionID)
                        request.setRequestHeader("jxSessionID", sessionID);
            }
        });
    };
        $.jxSetUpoladFile = function (URL,formID, fileControlID, CallBackFunc) {
         var form = document.getElementById(formID);
         var fc = document.getElementById(fileControlID);
         form.addEventListener('submit', function(evt) {
             evt.preventDefault();//组织页面刷新
             var data = new FormData();
             for (var i = 0, len = fc.files.length; i < len; i++) {
                 //file property: name, size, type, lastModifiedDate
                 var file = fc.files[i];
                 data.append(file.name, file);
             }
             $.jxUpoladFile(URL,data,CallBackFunc);
         }, false);
    };

    $.login=function(name,passwd,CallBackFunc){
    $.ajax({
            type: "post",
            url: "/Person/login/",
            //返回值为json格式
            dataType: "json",
            //将Object转换成json字符串post到后台
            processData: false,
            data: $.toJSON({Name:name,Passwd:passwd}),
            success: function (json, status, xhr) {
                if (json && !DualSystemReturn(json)) return;
                sessionID = xhr.getResponseHeader("jxSessionID");
                addQueryString("jxSessionID",sessionID);
                //alert(sessionID);
                if(CallBackFunc)
                    CallBackFunc(getReturn(json));
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                //$.messager.alert("页面请求错误", XMLHttpRequest.responseText, "error");
                alert("error:"+XMLHttpRequest.responseText);
            }
        });    
    }
    function getReturn(json){
        if(json)
            if(json.meta.type=="val"||json.meta.type=="obj")
                        return json.data;
            else if(json.meta.type=="arr")
                        return json.data.oList;
        return null;
    };

    function ajax(method, async, URL, obj, CallBackFunc){
        $.ajax({
            async: async,
            type: method,
            url: URL,
            //返回值为json格式
            dataType: "json",
            //将Object转换成json字符串post到后台
            processData: false,
            data: obj ? $.toJSON(obj) : null,
            success: function (json) {
                if (json && !DualSystemReturn(json)) return;
                if (CallBackFunc)
                    CallBackFunc(getReturn(json));
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                alert("error:"+XMLHttpRequest.responseText);
            },
            beforeSend: function(request) {
                if(sessionID)
                        request.setRequestHeader("jxSessionID", sessionID);
            }
        });
    };

    //对返回的值如果有系统信息要先进行处理，如果返回false则不再继续处理
    function DualSystemReturn(json) {
        if (json.reload) {
            //弹出到登录界面，终止执行
            //OpenLoginWindow();
            return false;
        }
        if (json.meta && json.meta.rc!=200) {
            //显示系统消息，终止执行
            alert(json.meta.rc+"：json.meta.msg");
            return false;
        }
        return true;
    };
})(jQuery);

;
function CheckMailAddress(text) {
    if (text) {
        var pattern = /\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*/;
        flag = pattern.test(text);
        if (flag)
            return true;
    };
    alert("不是合法的eMail地址格式");
    return false;
};

function inArray(attrName,arr,obj){
    var rs=-1;
    try{
        $.each(arr,function(i,value){
                if(value[attrName]==obj[attrName])
                {
                    rs=i;
                    return;
                }
            });
    }catch(e){};
    return rs;
}

//获取一个随机整数
function GetRandInt(IntMax) {
    var rand = Math.floor(Math.random() * IntMax);
    return rand;
};

String.prototype.LTrim = function () {
    return this.replace(/(^\s+)/g, "");
};

String.prototype.RTrim = function () {
    return this.replace(/(\s+$)/g, "");
};

String.prototype.Trim = function () {
    return this.replace(/(\s+)/g, "");
};

function InitUploadFileControl(ControlID, QueueID, URL, Func_Uploaded) {
    $("#" + ControlID).uploadify({
        auto: true,
        multi: false,
        swf: "/js/uploadify.swf",
        buttonText: "选择文件",
        //formData: Data,
        uploader: URL,
        queueID: QueueID,
        onUploadSuccess: function (file, data, response) {
            var json = null;
            if (data && data != "")
                json = eval('(' + data + ')');
            if (Func_Uploaded)
                Func_Uploaded(json);
        }
    });
};

function setRadioValue(radioName,value,defaultValue){
    var index=0;
     var robj = document.getElementsByName(radioName);
     if(robj)
        for(i=0;i<robj.length;i++){
            if(robj[i].value==value){
                robj[i].checked="checked";
                //robj[i].attr('checked',true);  
                return;
            }
            if(defaultValue&&robj[i].value==defaultValue){
                //alert(robj[i].value);
                index=i;
            }
        }
        if(defaultValue)
            robj[index].checked="checked";
}

function addString(str,splitor,wantAdd){
    var rs="";
    if(str&&str!="")
        rs=str+splitor+wantAdd;
    else
        rs=wantAdd;
    return rs;
}

function getItem(ObjArray,filedName,filedValue){
    if(ObjArray){
        var i = ObjArray.length;
        while (i--) {
            var item=ObjArray[i];
            var fv=item[filedName];
            if(fv&&fv==filedValue)
                return item;
        }
    }
    return null;
}

var urlQuery;
function getQueryString(name){
    var url=location.href;
    if(name==null&&name.Trim()=="") return null;
    if(url.indexOf("?") != -1){
        var ss=url.split("?"); 
        urlQuery={};
        var str = ss[1];
        //alert(str);
        var strs = str.split("&"); 
        for(var i = 0; i < strs.length; i ++) {
            var ps=strs[i].split("=");
            urlQuery[ps[0]]=unescape(ps[1]);
        }
        return urlQuery[name];
    }
    return null;
}

function addQueryString(name,param){
    if(!urlQuery)
        urlQuery={};
    urlQuery[name]=escape(param);
}
function getUrlWithQueryString(url){
    if(!urlQuery)
        return url;
    var objps=[];
    for(var k in urlQuery)
        if(urlQuery[k])
            objps.push(k+"="+urlQuery[k]);
    if(objps.length>0)
        return url+"?"+objps.join("&");
    return url;
}

function getUrlWithQueryStringAndID(url,param,paramName){
    var newurl;
    var added;
    if(urlQuery){
        var objps=[];
        for(var k in urlQuery)
            if(urlQuery[k])
                objps.push(k+"="+urlQuery[k]);
        if(objps.length>0){
            added=true;
            newurl=url+"?"+objps.join("&");
        }
    }
    else
        newurl=url;
    var pn=paramName?paramName:"ID";
    if(added)
        return newurl+"&"+pn+"="+param;
    else
        return newurl+"?"+pn+"="+param;
}

function checkSession(){
    var sid=getQueryString("jxSessionID");
    if(sid){
        sessionID=sid;
        addQueryString("jxSessionID",sid);
    }
}

function gotoUrl(url){
    //alert(url);
    window.location.href=getUrlWithQueryString(url);
}
