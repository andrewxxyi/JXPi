
//
//Canvas：画图
//
; (function ($) {
    $.fn.jxCanvas = function (options, x1,y1,x2,y2) {
        if (typeof options == 'string') {
            //操作
            var method = options;
            if (method == 'clear') {
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj)
                        tobj.canvas.remove();
                    $(this).data("jxCanvas", null);
                });
            }
            else if (method == 'save') {
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj)
                        tobj.ctx.save();
                });
            }
            else if (method == 'restore') {
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj)
                        tobj.ctx.restore();
                });
            }
            else if (method == 'translate') {
                //将坐标原点移动到(x, y)处
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj)
                        tobj.ctx.translate(x1, y1);
                });
            }
            else if (method == 'scale') {
                //设置给定的x轴、y轴缩放因子
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj)
                        tobj.ctx.scale(x1, y1);
                });
            }
            else if (method == 'drawImage') {
                //将图像显示在(x, y)处,参数顺序为imgUrl, x, y
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj) {
                        var img = new Image();
                        var ctx = tobj.ctx;
                        img.onload = function () {
                            ctx.drawImage(img, y1, x2);
                        }
                        img.src = x1;
                    }
                });
            }
            else if (method == 'gray') {
                //将图像转为灰度图
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj) {
                        //alert("width:"+tobj.canvas.width+",height:"+tobj.canvas.height);
                        var canvasData = tobj.ctx.getImageData(0, 0, tobj.canvas.width, tobj.canvas.height);

                        //alert("width:"+canvasData.width+",height:"+canvasData.height);
                        for(var x=0;x<canvasData.width;x++)
                            for(var y=0;y<canvasData.height;y++){
                                var idx = (x + y * canvasData.width) * 4;
                                var r = canvasData.data[idx + 0];
                                var g = canvasData.data[idx + 1];
                                var b = canvasData.data[idx + 2];
                                var gray = .299 * r + .587 * g + .114 * b;
                                canvasData.data[idx + 0] = gray; // Red channel
                                canvasData.data[idx + 1] = gray; // Green channel
                                canvasData.data[idx + 2] = gray; // Blue channel
                                canvasData.data[idx + 3] = 255; // Alpha channel
                            }
                        tobj.ctx.putImageData(canvasData, 0, 0);
                    }
                });
            }
            else if (method == 'drawLine') {
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj) {
                        tobj.ctx.beginPath();
                        tobj.ctx.moveTo(x1, y1);
                        tobj.ctx.lineTo(x2, y2);
                        tobj.ctx.stroke();
                    }
                });
            }
            else if (method == 'drawCircle') {
                //画圆,参数顺序为x, y, Radius
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj) {
                        tobj.ctx.beginPath();
                        tobj.ctx.arc(x1, y1, x2, 0, PI_double);
                        tobj.ctx.fill();
                    }
                });
            }
            else if (method == 'drawRect') {
                //画矩形,参数顺序为Left, Top, Width, Height
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj)
                        tobj.ctx.fillRect(x1, y1, x2, y2);
                });
            }
            else if (method == 'clearRect') {
                //画矩形,参数顺序为Left, Top, Width, Height
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj)
                        tobj.ctx.clearRect(x1, y1, x2, y2);
                });
            }
            else if (method == 'rotateRect') {
                //旋转一个矩形,参数顺序为坐标原点，角度，和一个包括矩形大小的对象（高、宽）
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj){
                    		tobj.ctx.clearRect(0,0,tobj.canvas.width,tobj.canvas.height);
        						tobj.ctx.save();
        						tobj.ctx.translate(x1, y1);
        						tobj.ctx.rotate(x2-Math.PI);
        						tobj.ctx.fillRect(-y2.width/2,0,y2.width,y2.height);
        						tobj.ctx.restore();
                    }
                });
            }
            else if (method == 'strokeRect') {
                //画矩形,参数顺序为Left, Top, Width, Height
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj)
                        tobj.ctx.strokeRect(x1, y1, x2, y2);
                });
            }
            else if (method == 'setLineWidth') {
                //画圆,参数顺序为x, y, Radius
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj)
                        tobj.ctx.lineWidth = x1;
                });
            }
            else if (method == 'setFillStyle') {
                //画圆,参数顺序为x, y, Radius
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj)
                        tobj.ctx.fillStyle = x1;
                });
            }
            else if (method == 'setStrokeStyle') {
                //画圆,参数顺序为x, y, Radius
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj)
                        tobj.ctx.strokeStyle = x1;
                });
            }
            else if (method == 'fillText') {
                //,参数顺序为x, y, Radius
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj)
                    {
                        if(y2)
                            tobj.ctx.font=y2;
                        tobj.ctx.fillText(x1,y1,x2);
                    }
                });
            }
            else if (method == 'drawLineWithArrow') {
                //画带箭头的直线
                return this.each(function () {
                    var tobj = $(this).data("jxCanvas");
                    if (tobj) {
                        var dx = x1 - x0
                        var dy = y1 - y0;
                        var radius = computeRadius(dx, dy);
                        var angle = Math.atan2(dy, dx);
                        tobj.ctx.save();
                        tobj.ctx.translate(x0, y0);
                        tobj.ctx.rotate(angle);
                        tobj.ctx.fillRect(0, -3, radius - 5, 6);
                        tobj.ctx.beginPath();
                        tobj.ctx.moveTo(radius - 5, -5);
                        tobj.ctx.lineTo(radius, 0);
                        tobj.ctx.lineTo(radius - 5, 5);
                        tobj.ctx.lineTo(radius - 5, -5);
                        tobj.ctx.closePath();
                        tobj.ctx.fill();
                        tobj.ctx.restore();
                    }
                });
            }
            return;
        }
        //默认值 
        var defaultVal = {
            Width: 800,
            Height: 600
        };
        var obj = $.extend(defaultVal, options);
        return this.each(function () {
            var tobj = $(this).data("jxCanvas");
            if (!tobj) {
                var canvas = document.createElement("canvas");
                $(canvas).appendTo(this);
                canvas.width = obj.Width;
                canvas.height = obj.Height;
                var ctx = canvas.getContext('2d');
                ctx.strokeStyle="#0000ff";
                ctx.clearRect(0, 0, canvas.width, canvas.height);
                tobj = { opt: obj, canvas: canvas, ctx: ctx };
                $(this).data("jxCanvas", tobj);
            }
        });
    }
})(jQuery);

