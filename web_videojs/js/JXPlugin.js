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

