

var sessionID;

//
//Ajax
//  以json方式传递参数并通过json方式获取返回值
//
; (function ($) {
    $.jxGetPage = function (URL, CallBackFunc, errorFunc) {
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
                if (errorFunc)
                    errorFunc(textStatus);
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
    $.jxGet = function (URL, CallBackFunc, errorFunc) {
        ajax("get",true,URL, null, CallBackFunc, errorFunc);
    };    
    //将对象转换为json字符串以post发送到后台
    //由于$.toJSON(obj)是将对象内容转换，而不转换对象名，而象put、delete等方法中最好能知道对象名（即类名）这样就可以直接将json字符串转换为对象，然后就可以直接通过orm进行处理
    //所以这里的对象需象如下的ts这样进行处理：
    //var ts=new Object();
    //ts.TopSpace=new Object();
    //ts.TopSpace.Name="test";
    $.jxInsert = function (URL, obj, CallBackFunc, errorFunc) {
        ajax("post",true,URL, obj, CallBackFunc, errorFunc);
    };
    //async
    $.jxREST = function (URL, obj, CallBackFunc, errorFunc) {
        ajax("post",true,URL, obj, CallBackFunc, errorFunc);
    };
    //sync
    $.jxRequest = function (URL, obj, CallBackFunc, errorFunc) {
        ajax("post",false,URL, obj, CallBackFunc, errorFunc);
    };
    $.jxUpdae = function (URL, obj, CallBackFunc, errorFunc) {
        ajax("put",true,URL, obj, CallBackFunc, errorFunc);
    };
    $.jxDelete = function (URL, CallBackFunc, errorFunc) {
        ajax("delete",true,URL, null, CallBackFunc, errorFunc);
    };
    $.jxRequest_Sync = function (method, URL, obj, CallBackFunc, errorFunc) {
        ajax(method,false,URL, obj, CallBackFunc, errorFunc);
    };
    //不将对象转换为json进行传递
    $.jxUpoladFile = function (URL, obj, CallBackFunc, errorFunc) {
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
                if (errorFunc)
                    errorFunc(textStatus);
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

    $.login=function(name,passwd,CallBackFunc, errorFunc){
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
                if (errorFunc)
                    errorFunc(textStatus);
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

    function ajax(method, async, URL, obj, CallBackFunc, errorFunc){
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
                if (errorFunc)
                    errorFunc(textStatus);
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

function dispTime_Chinese(seconds,noSecond) {
	var s=seconds%60;
	var dt;
	if(noSecond)
		dt='';
	else
		dt=s+"秒";
	var t=(seconds-s)/60;
	if(t<=0)return dt;
	var m=t%60;
	dt = m+"分"+dt;
	t=(t-m)/60;
	if(t<=0)return dt;
	var h=t%24;
	dt = h+"小时"+dt;
	t=(t-h)/24;
	if(t<=0)return dt;
	return t+"天"+dt;
}

function getNowFormatTime() {
    var date = new Date();
    var seperator1 = "-";
    var seperator2 = ":";
    var currentdate =date.getHours() + seperator2 + date.getMinutes()
            + seperator2 + date.getSeconds();
    return currentdate;
}

function getNowFormatDate() {
    var date = new Date();
    var seperator1 = "-";
    var seperator2 = ":";
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var currentdate = year + seperator1 + month + seperator1 + strDate;
    return currentdate;
}

function getNowFormatDateTime() {
    var currentdate = getNowFormatDate + " " + getNowFormatTime;
    return currentdate;
}

function getUrlPage(){
    var url=location.href;
    if(name==null&&name.Trim()=="") return null;
    var str;
    if(url.indexOf("?") != -1){
        var ss=url.split("?"); 
        str = ss[0];
    }
    	else 
    		str=url;
        //alert(str);
        var strs = str.split("/"); 
        var ps=strs[strs.length-1];
        //alert(ps);
        var us=ps.split(".");
        return us[0];
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
    if(!urlQuery){
    	//alert("no QueryString")
        return url;
     }
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
//从QueryString中查询paramName的值拼入到url中
function getUrlWithQueryStringName(url,paramName){
    var param=getQueryString(paramName);
    if(param)
   	 return getUrlAddParam(url,param,paramName);
    alert("参数不存在："+paramName);
    return url;
}
//给url增加sessionid和param
function getUrlAddParamWithSessionID(url,paramName,param){
    var newurl=getUrlWithQueryStringName(url,"jxSessionID");
    return getUrlAddParam(newurl,param,paramName);
}
function getUrlWithQueryStringNameWithSessionID(url,paramName){
    var newurl=getUrlWithQueryStringName(url,"jxSessionID");
    return getUrlWithQueryStringName(newurl,paramName);
}
//
function getUrlAddParam(url,param,paramName){
    if(url.indexOf("?") == -1)
        return url+"?"+paramName+"="+param;
    else 
        return url+"&"+paramName+"="+param;
}


function checkSession(){
    var sid=getQueryString("jxSessionID");
    //alert(sid)
    if(sid){
        sessionID=sid;
        addQueryString("jxSessionID",sid);
    }
}

function gotoUrl(url){
    //alert(url);
    window.location.href=getUrlWithQueryString(url);
}

function addCheckBox(parentID,id,text,checked,dual){
	
	var checkBox=document.createElement("input");
        checkBox.setAttribute("type","checkbox");
        checkBox.setAttribute("id",id);
        checkBox.setAttribute("name", text);
        var c=$(checkBox);
        if(checked)
        c.attr("checked",'checked');
        if(dual)
        c.click(dual);
        categoryCheckBoxArr.push(c);

        var li=document.createElement("li");
        li.appendChild(checkBox);       
        li.appendChild(document.createTextNode(text));
        var l=$(li);

	var parent=$("#"+parentID);
	l.appendTo(parent);
}
var categoryArr;
var categoryCheckBoxArr;
function listCategory(parentID,name){
	categoryArr=new Array();
	categoryCheckBoxArr=[];
		var url="/system/listCategory";
    $.jxREST(url,{Name:name},function (json) {
    	//alert($.toJSON(json));
    	$.each(json, function(k, v) {
    	//alert(k+":"+$.toJSON(v));
    		addCheckBox(parentID,'listCategory_'+k,v.category,false,function () {
				    		categoryArr[k]={name:v.category,checked:$('#listCategory_'+k).is(':checked')};
				    		//alert($.toJSON(categoryArr));
    		});
    });
    });
}
function getCategory() {
		var ca;
	if(categoryArr)
	$.each(categoryArr, function(k, v) {
		if(v.checked){
			if(ca)
				ca=ca + " "+v.name;
			else
				ca=v.name;
		}
	});
	return ca;
}
function categorySelectAll(select) {
	if(categoryCheckBoxArr){
		$.each(categoryCheckBoxArr, function(k, v) {
			v.click();
		});
	}
	//alert($.toJSON(categoryArr));
}

function closePaste(controlID) {
	$('#'+controlID).on("paste", function(e) { 
			alert("不允许粘贴！");
			e.preventDefault();
        }); 
}

function inhibitInput(controlID) {
	$('#'+controlID).on("keypress", function(e) { 
			alert("不允许修改！");
			e.preventDefault();
        }); 
}

function inhibitCut(controlID) {
	$('#'+controlID).on("cut", function(e) { 
			alert("不允许剪切！");
			e.preventDefault();
        }); 
}
function inhibitFocus(controlID) {
	$('#'+controlID).on("focus", function(e) { 
			//alert("不允许！");
			e.preventDefault();
        }); 
}

function inhibitSelect(controlID) {
	$('#'+controlID).on("select", function(e) { 
			alert("不允许选择！");
			e.preventDefault();
        }); 
}

function inhibitMouse(controlID) {
	$('#'+controlID).on("mousedown", function(e) { 
			//alert("不允许选择！");
			e.preventDefault();
        }); 
}
//监听是否按下了回车
function onCR(controlID,dual) {
	$('#'+controlID).on("keypress", function(e) { 
	var key=e.keyCode?e.keyCode:e.which;
	if(key==13)
		if(dual)
			dual();
        }); 
}

function inhibitRightClick() {
	$('body').bind("contextmenu", function()
    {
        return false;
    });

    $("body").bind("selectstart", function()
    {
        return false;
    });
}
//用正则表达式将空白符全部去掉：由于是富文本编辑器，所以回车是<br>
function  zip_reserveSpace(str) {
	var re1 = /\<br\>/gmi;
	var re2 = /\&nbsp\;/gmi;
	if(str)
		return str.replace(re1,'\n').replace(re2,'');
	return str;
}
function  unzip_enter(str) {
	var re1 = /\n/gmi;
	if(str)
		return str.replace(re1,'\<br\>');
	return str;
}
//用正则表达式将空白符全部去掉：由于是富文本编辑器，所以回车是<br>
function  zip(str) {
	var re = /\s/gmi;
	var re1 = /\<br\>/gmi;
	var re2 = /\&nbsp\;/gmi;
	if(str)
		return str.replace(re,'').replace(re1,'').replace(re2,'');
	return str;
}

//显示富文本编辑器
function dispACEEditor (controlID) {
		$('#'+controlID).ace_wysiwyg({
		toolbar:
		[
			'font',
			null,
			'fontSize',
			null,
			{name:'bold', className:'btn-info'},
			{name:'italic', className:'btn-info'},
			{name:'strikethrough', className:'btn-info'},
			{name:'underline', className:'btn-info'},
			null,
			{name:'insertunorderedlist', className:'btn-success'},
			{name:'insertorderedlist', className:'btn-success'},
			{name:'outdent', className:'btn-purple'},
			{name:'indent', className:'btn-purple'},
			null,
			{name:'justifyleft', className:'btn-primary'},
			{name:'justifycenter', className:'btn-primary'},
			{name:'justifyright', className:'btn-primary'},
			{name:'justifyfull', className:'btn-inverse'},
			null,
			{name:'createLink', className:'btn-pink'},
			{name:'unlink', className:'btn-pink'},
			null,
			{name:'insertImage', className:'btn-success'},
			null,
			'foreColor',
			null,
			{name:'undo', className:'btn-grey'},
			{name:'redo', className:'btn-grey'}
		]
	}).prev().addClass('wysiwyg-style2');
}

//
//显示代码编辑器
//
function dispCodeEditor_ace (controlID,lang) {
	var editor = ace.edit(controlID);
	//editor.height(1200);
	editor.$blockScrolling = Infinity;
	document.getElementById(controlID).style.fontSize='16px';
	editor.setTheme("ace/theme/twilight");
    editor.session.setMode("ace/mode/"+lang);
	editor.session.setTabSize(4);
	editor.session.setUseWrapMode(true);
	return editor;
}

//根据函数名获取函数，然后执行
//var func=getFunc('test');
//new func(param1,param2);
function getFunc(funcname) {
	if(funcname&&funcname!="")
		return eval(funcname);
}
function callFunc(funcname) {
	var func = eval(funcname);
	if(func)
		new func();
}

