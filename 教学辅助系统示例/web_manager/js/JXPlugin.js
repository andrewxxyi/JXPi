
//
//操纵舵：
//左右移动则转向，长度变化则代表速度控制
//
;
var HelmSize=320;
var barWidth=8;

; (function ($) {
    $.fn.jxHelm = function (options) {
        if (typeof options == 'string') {
            //操作
            var method = options;
            if  (method == 'clear') {
            		var tobj = $(this).data("jxHelm");
						if (tobj) 		
							this.remove();
            }
            return;
        };
        	        //默认值 
        var defaultVal = {
	startOP:null,
	stopOP:null,
	moveFunc:null,
	Obj:null
        };
        var dual=false;
        var opt = $.extend(defaultVal, options);
	 var div = $("<div></div>");
	 //tobj = { opt: obj, div: div };
	 div.data("jxHelm", opt);
	 div.css({position: "absolute",'top':HelmSize/2+this.top,'left':HelmSize/2+this.left,'z-index':2});   
            	div.appendTo(this);
            	div.mousedown(function(){
            		//alert("mousedown")
            		dual=true;        
            		if(opt.startOP){
  				      	opt.startOP();
        				}
            	});
            	div.mouseup(function(){
            		//alert("mouseup")
            		dual=false;     
            		if(opt.stopOP){
  				      	opt.stopOP();
        				}
            	});
            	div.mousemove(function(e){
        	if(!dual)return;
            		//alert("mousemove")
        var e = e ? e : window.event;
        //if(!window.event) {e.preventDefault();}/* 阻止标注浏览器下拖动a,img的默认事件 */
        //alert(e.which)
        if(e.which == 1){
        	//$("#value_x").html(e.offsetX);
        	//$("#value_y").html(e.offsetY);
        	//计算鼠标所在位置相对操纵板中心位置的弧度角
							var dx=e.offsetX - HelmSize/2;
							var dy=e.offsetY  - HelmSize/2;
							//从canvas看到的弧度角
							//canvas的坐标系是以页面左上角为(0,0)，水平向右为x正轴，垂直向下为y正轴
							//但我们人的视角是以自己为原点，向右为x正轴，向前为y正轴，即在y轴两个坐标系是反向的
							//同时为了人的习惯，即在0度角时将操纵杆指向前方，所以anvas的坐标系也做了90度的旋转
        					var radian1=Math.atan2(dy,dx)+Math.PI/2;
        					var radian=Math.atan2(-dy,dx);
        	//$("#value_angle").html(angle1);
							var barLength=Math.pow((dx * dx + dy *dy), 0.5);
							//不能无限加速，所以设定了一个上限值
							if(barLength>HelmSize/2)
								barLength=HelmSize/2;								
        	//$("#value_length").html(barLength);
        	//根据鼠标动作重画操纵杆
        					div.jxCanvas("rotateRect",HelmSize/2,HelmSize/2,radian1,{width:barWidth,height:barLength});
        					//重画操纵板边框，rotateRect时会将整个画布清空
        					div.jxCanvas("strokeRect",1,1,HelmSize-1,HelmSize-1);
        if(opt.moveFunc){
        	//speed是【0，100】为范围的，所以barLength需要进行换算
        	opt.moveFunc(radian,barLength*2*100/HelmSize);
        }
        }
            	});

        	div.jxCanvas({Width: HelmSize,Height: HelmSize});
        	div.jxCanvas("setStrokeStyle","#0000ff");
        					div.jxCanvas("rotateRect",HelmSize/2,HelmSize/2,0,{width:barWidth,height:HelmSize/4});
        					div.jxCanvas("strokeRect",1,1,HelmSize-1,HelmSize-1);
        	return div;
    };
})(jQuery);



//
//家谱中的一个成员
//
;
var PedigreeWidth=160;
var PedigreeHeight=80;

; (function ($) {
    $.fn.jxPedigree = function (options, x, y,Width,Height) {
        if (typeof options == 'string') {
            //操作
            var method = options;
            if (method == 'toParent') {
            		var tobj = $(this).data("jxPedigree");
		if (tobj) {
			//向上画线
			var left=tobj.TotalWidth/2;
			//$(this).jxCanvas("drawLine",left,this.top,left,this.top+tobj.EdgeHeight);
			$(this).jxCanvas("drawLine",left,0,left,tobj.EdgeHeight);
		 }
            }
            else if (method == 'toSon') {
            		var tobj = $(this).data("jxPedigree");
		if (tobj) {
			//向下画线
			var left=tobj.TotalWidth/2;
			//$(this).jxCanvas("drawLine",left,this.top+tobj.TotalHeight-tobj.EdgeHeight,left,this.top+tobj.TotalHeight);
			$(this).jxCanvas("drawLine",left,tobj.TotalHeight-tobj.EdgeHeight,left,tobj.TotalHeight);
		 }
            }
            else if (method == 'toElder') {
            		var tobj = $(this).data("jxPedigree");
		if (tobj) {
			//向左画线
			//$(this).jxCanvas("drawLine",this.left,this.top,this.left+tobj.TotalWidth/2,this.top);
			$(this).jxCanvas("drawLine",0,0,tobj.TotalWidth/2,0);
		 }
            }
            else if (method == 'toYounger') {
            		var tobj = $(this).data("jxPedigree");
		if (tobj) {
			//向右画线
			//$(this).jxCanvas("drawLine",this.left+tobj.TotalWidth/2,this.top,this.left+tobj.TotalWidth,this.top);
			$(this).jxCanvas("drawLine",tobj.TotalWidth/2,0,tobj.TotalWidth,0);
		 }
            }
            else if (method == 'setName') {
                return this.each(function () {
                    var tobj = $(this).data("jxPedigree");
	if (tobj) {
				        $(this).jxCanvas("fillText",0,0,x);
		}
                });
            }
            else if (method == 'clear') {
            		var tobj = $(this).data("jxPedigree");
		if (tobj) {
			this.remove();
		 }
            }
            return;
        };
        	        //默认值 
        var defaultVal = {
	Name: "姓名",
	NameFontSize: 16,
	Pair: "妻子",
	PaireFontSize: 14,
	Desc: "说明一些其它情况",
	DescFontSize: 12,
	//总的大小
	TotalWidth: PedigreeWidth,
	TotalHeight: PedigreeHeight,
	//文字部分和总大小之间所留的边
	EdgeWidth: 20,
	EdgeHeight: 20,
	ClickFunc:null,
	Obj:null
        };
        var opt = $.extend(defaultVal, options);
	 var div = $("<div></div>");
	 //tobj = { opt: obj, div: div };
	 div.data("jxPedigree", opt);
	 div.css({position: "absolute",'top':y,'left':x,'z-index':2});   
            	div.appendTo(this);
            	div.click(function(){
            		if(opt.ClickFunc)
            			opt.ClickFunc(opt.Obj);
            	})

        	div.jxCanvas({Width: opt.TotalWidth,Height: opt.TotalHeight});
        	div.jxCanvas("strokeRect",opt.EdgeWidth,opt.EdgeHeight,opt.TotalWidth-opt.EdgeWidth*2,opt.TotalHeight-opt.EdgeHeight*2);
        	div.jxCanvas("fillText",opt.Name,opt.EdgeWidth+8,opt.EdgeHeight+16,opt.NameFontSize+"px Georgia");
        	div.jxCanvas("fillText",opt.Pair,opt.EdgeWidth+60,opt.EdgeHeight+16,opt.PaireFontSize+"px Georgia");
        	div.jxCanvas("fillText",opt.Desc,opt.EdgeWidth+8,opt.EdgeHeight+32,opt.DescFontSize+"px Georgia");
        	return div;
    };
})(jQuery);

