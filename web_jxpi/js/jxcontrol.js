
//
//按钮控件
//
;
(function($) {

	var methods = {
		init: function(options) {
 					var defaults = {
 					    Type:"jxUIButton",
						Text: '姓名',
						Tip: null,
						//原始文本
						oScript: null,
						Script: null,
						Top: 0,
						Left: 0,
						Run: false,
						onRun: function() {},
						onDispAttr: function() {},
						onOpenScriptEdit: function() {}
					}
 					var settings = $.extend({}, defaults, options);
 					var div = $('<button type="button" class="btn btn-primary">'+settings.Text+'</button>');
                                div.appendTo($(this));
                                var down=false;
                                var ox=0,oy=0;
                                div.mousedown(function(event){
                                    if(!settings.Run){
                                        var e = event ? event : window.event;
                                        if(e.which == 1){
                                            down = true;
                                            ox=settings.Left-e.offsetX;
                                            oy=settings.Top-e.offsetY;
                                        }
                                    }
                                });
                                div.mouseup(function(){
                                    if(!settings.Run){
        //div.css({position: "absolute",'top':settings.Top,'left':settings.Left,'z-index':2});
                                        down = false;
                                        ox = 0;
                                        oy = 0;
                                    }
                                });

                                div.mouseleave(function(){
                                    if(!settings.Run&&down){
        //div.css({position: "absolute",'top':settings.Top,'left':settings.Left,'z-index':2});
                                        down = false;
                                        ox = 0;
                                        oy = 0;
                                    }
                                });

                                div.mousemove(function(event){
                                    if(!settings.Run&&down){
                                        var e = event ? event : window.event;
                                        if(e.which == 1){
                                            settings.Left=ox+e.offsetX;
                                            settings.Top=oy+e.offsetY;
                                            div.css("top",settings.Top);
                                            div.css("left",settings.Left);
                                        }
                                    }
                                });
                                div.click(function(event){
                                    if(!settings.Run){
                                        //显示属性
                                        if(settings.onDispAttr)
                                            settings.onDispAttr(settings);
                                    }else{
                                        //运行脚本
                                        if(settings.Script){
                                            if(settings.onRun){
                                                settings.onRun(settings.Script,settings.Tip);
                                            }
                                        }
                                        else
                                            alert("代码不能为空！");
                                    }
                                });
                                div.dblclick(function(event){
                                    if(!settings.Run){
                                        //打开编辑窗口
                                        if(settings.onOpenScriptEdit)
                                            settings.onOpenScriptEdit(settings.ID,settings.oScript);
                                    }
                                });
 					div.data("jxUIButton", settings);
        div.css({position: "absolute",'top':settings.Top,'left':settings.Left,'z-index':2});
        return div;
		},
		setParam: function(paramName,paramValue) {
		    var tobj = $(this).data("jxUIButton");
            var f = parseFloat(paramValue);
            tobj[paramName] = isNaN(f) ? ((paramValue == "true" || paramValue == "false") ? paramValue == "true" : paramValue) : f;
            $(this).css({position: "absolute",'top':tobj.Top,'left':tobj.Left,'z-index':2});
            $(this).html(tobj.Text);
		},
		val: function(options) {
			var someValue = this.eq(0).html();
			return someValue;
		},
		toJson: function(script) {
		    var tobj = $(this).data("jxUIButton");
			return {Type:tobj.Type,ID:tobj.ID,Text:tobj.Text,Tip:tobj.Tip,Top:tobj.Top,Left:tobj.Left,Script:tobj.Script};
		},
		setScript: function(script) {
		    var tobj = $(this).data("jxUIButton");
		    tobj['oScript']=script;
			//tobj['Script']=$.base64.btoa(zip_reserveSpace(script),true);
			tobj['Script']=$.base64.btoa(script,true);
		}
	};

	$.fn.jxUIButton = function() {
		var method = arguments[0];
 		if(methods[method]) {
			method = methods[method];
			arguments = Array.prototype.slice.call(arguments, 1);
		} else if( typeof(method) == 'object' || !method ) {
			method = methods.init;
		} else {
			$.error( 'Method ' +  method + ' does not exist on plugin:'+"jxUIButton" );
			return this;
		}
 		return method.apply(this, arguments);
 	}
 })(jQuery);

//
//文本控件
//
;
(function($) {
	var methods = {
		init: function(options) {
 					var defaults = {
 					    Type:"jxUIText",
 					    ID: 0,
						Text: '姓名',
						FontSize: 1,
						Top: 0,
						Left: 0,
						Run: false,
						onDispAttr: function() {}
					}
 					var settings = $.extend({}, defaults, options);
 		var div = $("<div></div>");
        div.appendTo($(this));
 		div.data("jxUIText", settings);
        var down=false;
        var ox=0,oy=0;
        div.mousedown(function(event){
            if(!settings.Run)
                var e = event ? event : window.event;
                if(e.which == 1){
                    down = true;
                    ox=settings.Left-e.offsetX;
                    oy=settings.Top-e.offsetY;
                }
        });
        div.mouseup(function(){
            if(!settings.Run){
        //div.css({position: "absolute",'top':settings.Top,'left':settings.Left,'z-index':2});
                down = false;
                ox = 0;
                oy = 0;
            }
        });
        div.mouseleave(function(){
            if(!settings.Run&&down){
        //div.css({position: "absolute",'top':settings.Top,'left':settings.Left,'z-index':2});
                down = false;
                ox = 0;
                oy = 0;
            }
        });
        div.mousemove(function(event){
            if(!settings.Run&&down){
                var e = event ? event : window.event;
                if(e.which == 1){
                    settings.Left=ox+e.offsetX;
                    settings.Top=oy+e.offsetY;
                    div.css("top",settings.Top);
                    div.css("left",settings.Left);
                }
            }
        });
        div.click(function(event){
            if(!settings.Run){
                //显示属性
                if(settings.onDispAttr)
                    settings.onDispAttr(settings);
            }
        });
        div.css({position: "absolute",'top':settings.Top,'left':settings.Left,'z-index':2});
        div.html('<H'+settings.FontSize+' id="ui_text_'+settings.ID+'" >'+settings.Text+'</H'+settings.FontSize+'>');
        $("#ui_text_"+settings.ID).click(function(){
            if(!settings.Run){
                //显示属性
                if(settings.onDispAttr)
                     settings.onDispAttr(settings);
            }
        });
            	return div;
		},
		setParam: function(paramName,paramValue) {
		    var tobj = $(this).data("jxUIText");
            var f = parseFloat(paramValue);
            tobj[paramName] = isNaN(f) ? ((paramValue == "true" || paramValue == "false") ? paramValue == "true" : paramValue) : f;
        $(this).css({position: "absolute",'top':tobj.Top,'left':tobj.Left,'z-index':9999});
        $(this).html('<H'+tobj.FontSize+' id="ui_text_'+tobj.ID+'" >'+tobj.Text+'</H'+tobj.FontSize+'>');
                $("#ui_text_"+tobj.ID).click(function(){
                    if(!tobj.Run){
                        //显示属性
                        if(tobj.onDispAttr)
                             tobj.onDispAttr(tobj);
                    }
                });
		},
		val: function(options) {
			var someValue = this.eq(0).html();
			return someValue;
		},
		toJson: function(options) {
		    var tobj = $(this).data("jxUIText");
			return {Type:tobj.Type,ID:tobj.ID,Text:tobj.Text,FontSize:tobj.FontSize,Top:tobj.Top,Left:tobj.Left};
		}
	};

	$.fn.jxUIText = function() {
		var method = arguments[0];
 		if(methods[method]) {
			method = methods[method];
			arguments = Array.prototype.slice.call(arguments, 1);
		} else if( typeof(method) == 'object' || !method ) {
			method = methods.init;
		} else {
			$.error( 'Method ' +  method + ' does not exist on plugin:'+"jxUIText" );
			return this;
		}
 		return method.apply(this, arguments);
 	}
 })(jQuery);


//
//灯泡控件
//
;
(function($) {
	var methods = {
		init: function(options) {
 					var defaults = {
 					    Type:"jxUILight",
 					    ID: 0,
						Text: '灯泡',
						Top: 0,
						Left: 0,
						Run: false,
						onDispAttr: function() {}
					}
 					var settings = $.extend({}, defaults, options);
 		var div = $("<div></div>");
        div.appendTo($(this));
 		div.data("jxUILight", settings);
 		div.jxCanvas({Width:120,Height:130});
 		div.jxCanvas("drawImage","/images/light.jpg",0,0);
 		div.jxCanvas("scale",0.277,0.2);
 		settings.div=div;
        var down=false;
        var ox=0,oy=0;
        div.mousedown(function(event){
            if(!settings.Run)
                var e = event ? event : window.event;
                if(e.which == 1){
                    down = true;
                    ox=settings.Left-e.offsetX;
                    oy=settings.Top-e.offsetY;
                }
        });
        div.mouseup(function(){
            if(!settings.Run){
        //div.css({position: "absolute",'top':settings.Top,'left':settings.Left,'z-index':2});
                down = false;
                ox = 0;
                oy = 0;
            }
        });
        div.mouseleave(function(){
            if(!settings.Run&&down){
        //div.css({position: "absolute",'top':settings.Top,'left':settings.Left,'z-index':2});
                down = false;
                ox = 0;
                oy = 0;
            }
        });
        div.mousemove(function(event){
            if(!settings.Run&&down){
                var e = event ? event : window.event;
                if(e.which == 1){
                    settings.Left=ox+e.offsetX;
                    settings.Top=oy+e.offsetY;
                    div.css("top",settings.Top);
                    div.css("left",settings.Left);
                }
            }
        });
        div.click(function(event){
            if(!settings.Run){
                //显示属性
                if(settings.onDispAttr)
                    settings.onDispAttr(settings);
            }
        });
        div.css({position: "absolute",'top':settings.Top,'left':settings.Left,'z-index':2});
            	return div;
		},
		setParam: function(paramName,paramValue) {
		    var tobj = $(this).data("jxUIText");
            var f = parseFloat(paramValue);
            tobj[paramName] = isNaN(f) ? ((paramValue == "true" || paramValue == "false") ? paramValue == "true" : paramValue) : f;
        $(this).css({position: "absolute",'top':tobj.Top,'left':tobj.Left,'z-index':9999});
		},
		val: function(options) {
			var someValue = this.eq(0).html();
			return someValue;
		},
		open: function(options) {
		    var tobj = $(this).data("jxUILight");
 		    tobj.div.jxCanvas("drawImage","/images/light.jpg",0,0);
		},
		close: function(options) {
		    var tobj = $(this).data("jxUILight");
			tobj.div.jxCanvas("gray");
		},
		toJson: function(options) {
		    var tobj = $(this).data("jxUILight");
			return {Type:tobj.Type,ID:tobj.ID,Text:tobj.Text,Top:tobj.Top,Left:tobj.Left};
		}
	};

	$.fn.jxUILight = function() {
		var method = arguments[0];
 		if(methods[method]) {
			method = methods[method];
			arguments = Array.prototype.slice.call(arguments, 1);
		} else if( typeof(method) == 'object' || !method ) {
			method = methods.init;
		} else {
			$.error( 'Method ' +  method + ' does not exist on plugin:'+"jxUILight" );
			return this;
		}
 		return method.apply(this, arguments);
 	}
 })(jQuery);





