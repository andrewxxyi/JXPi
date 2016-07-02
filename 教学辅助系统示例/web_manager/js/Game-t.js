
//
//游戏的控制中心
//
//使用：
//      所有gameobject初始化完毕addGameObject
//      每次时钟间隔激发时执行moveAllGO(ms_Interval)
//

game.prototype.setEventHandler = function(eventType,gameobject,HandlerName)
{
    var eo;
    switch(eventType)
    {
        case "mm":
            eo = new eventObject(gameobject,HandlerName);
            this.goArray_UseMouseEvent_Move[this.goArray_UseMouseEvent_Move.length] = eo;
            break;
        case "md":
            eo = new eventObject(gameobject,HandlerName);
            this.goArray_UseMouseEvent_Down[this.goArray_UseMouseEvent_Down.length] = eo;
            break;
        case "mu":
            eo = new eventObject(gameobject,HandlerName);
            this.goArray_UseMouseEvent_Up[this.goArray_UseMouseEvent_Up.length] = eo;
            break;
    }
}
game.prototype.getMouseMove = function()
{
    var s = new scalar_GM(this.mouseMove_delta_X,this.mouseMove_delta_Y);
    this.mouseMove_delta_X = 0;
    this.mouseMove_delta_Y = 0;
    return s;
}
game.prototype.mouseEvent_Down = function()
{
    for(var i=0;i<this.goArray_UseMouseEvent_Down.length;i++)
        this.goArray_UseMouseEvent_Down[i].exec();
}
game.prototype.mouseEvent_Up = function()
{
    for(var i=0;i<this.goArray_UseMouseEvent_Up.length;i++)
        this.goArray_UseMouseEvent_Up[i].exec();
}
game.prototype.mouseEvent_Move = function(ds_scalar)
{
    for(var i=0;i<this.goArray_UseMouseEvent_Move.length;i++)
        this.goArray_UseMouseEvent_Move[i].exec(ds_scalar);
}

game.prototype.startMove = function()
{
    this.stopMove();
    resetAllGO();
    startMoveAllGO();
    
    var gameobject = this;
    gameobject.interval = setInterval(
    function()
    {
        //收集时间间隔，s为单位
        var msold = time_Last_ms;
        var dt = new Date();
        time_Last_ms = dt.getTime();
        var t = 0;
        //noUsed4=1000;
        if(msold == 0)
            time_Start_ms = time_Last_ms;
        else
            t = (time_Last_ms - msold)/noUsed4;
        //收集鼠标移动位移
        var ds = gameobject.getMouseMove();
        moveAllGO(t,ds);
        //判定游戏是否结束
        if(gameobject.judgeGameOver())
        {
            gameobject.gameOver();
        }
    },moveInterval_ms);
}
game.prototype.setMouseEvent = function()
{
    var gameobject = this;
    var x_old = 0;
    var y_old = 0;
    divGame.onmousedown = function(e)
    {
        var e = e ? e : window.event;
        if(e.button == 1)
        {
            x_old = e.clientX;
            y_old = e.clientY;
            gameobject.mouseEvent_Down();
        }
    }
    divGame.onmousemove = function(e)
    {
        var e = e ? e : window.event;
        //if(!window.event) {e.preventDefault();}/* 阻止标注浏览器下拖动a,img的默认事件 */
        if(e.button == 1)
        {
            var xoff = e.clientX - x_old;
            gameobject.mouseMove_delta_X += xoff;
            x_old = e.clientX;
            var yoff = e.clientY - y_old;
            gameobject.mouseMove_delta_Y += yoff;
            y_old = e.clientY;
            var ds = new scalar_GM(xoff,yoff);
            //鼠标的移动既有可能在没有动画时需要捕获，也有可能在动画开始后捕获，mouseEvent_Move处理的是无动画时的事件
            //动画开始后的鼠标移动的处理通过moveAllGO再分发到需要的对象中
            gameobject.mouseEvent_Move(ds);
        }
    }
    divGame.onmouseup = function(e)
    {
        var e = e ? e : window.event;
        if(e.button == 1)
        {
            x_old = 0;
            y_old = 0;
            gameobject.mouseEvent_Up();
        }
    }
} 


function getNewCanvas(goid)
{
    var canvasid = "canvas_Game_" + goid;
    var div = document.createElement("div");
    div.style.position = "absolute";
    var divGame = document.getElementById("div_Game");
    divGame.appendChild(div);
    var canvas = document.createElement("canvas");
    canvas.id = canvasid;
    canvas.width = canvaswidth;
    canvas.height = canvasheight;
    div.appendChild(canvas);
    canvas = window.G_vmlCanvasManager.initElement(canvas);
    return canvas;
}

function rect_gameObjectParam()
{
    this.type = "r";
    //（x0,y0）是支点
    //（x，y）是方块的中心
    //area1是中心初始化所在区域，为一个点则不随机初始化
    //area2是限制区域，只在该区域中移动
    //支点不在中心
    this.originNotSame = false;
    this.width0 = 0;
    this.height0 = 0;

    this.width = 0;
    this.height = 0;
    //当前倾斜角
    this.angle = 0;
    //在运动过程中是否接受手动控制运动（主要是旋转）
    this.canMove = false;
}
rect_gameObjectParam.prototype = new gameObjectParam();

rect_gameObjectParam.prototype.constructor = rect_gameObjectParam;

rect_gameObjectParam.prototype.zoom = function(ms_t)
{
    var cf = this.getzoomCF(ms_t);
    this.width = this.width0 * cf;
    this.height = this.height0 * cf;
    this.radius = computeRadius(this.width,this.height);
}

rect_gameObjectParam.prototype.init = function()
{
    with(this)
    {
        x = getRandom(area1_x1,area1_x2);
        y = getRandom(area1_y1,area1_y2);
        width = width0;
        height = height0;
        radius0 = computeRadius(width0,height0);
        radius = radius0;
        quality = width * height;
        area_x1 = x - width/2;
        area_y1 = y - height/2;
        area_x2 = x + width/2;
        area_y2 = y + height/2;
        if(!originNotSame)
        {
            x0 = x0_0;
            y0 = y0_0;
            angle = getLineAngle(x0,y0,x,y);
        }
    }
}

rect_gameObjectParam.prototype.compute = function(ms_t,deltaValue_scalar)
{
    with(this)
    {
        var x_0 = x;
        var y_0 = y;
        if(speed)
        {
            if(originNotSame)
            {
                //如果此种情况下angle_delta不为空，则为外部送入的支点位移量（标量）：在ms_t时间内(x0,y0)移动了（dx，dy）而又假定(x,y)的移动由于惯性
                //是由speed决定的，所以此时的角度是从(x0,y0)到(x,y)
                //在支点由外部决定时
                x0 += deltaValue_scalar.x;
                y0 += deltaValue_scalar.y;
                //由于支点的存在会导致运动速度永远和加速度垂直，所以需要计算angle_delta相当于加速度方向的位移进行补偿
                var dv = deltaValue_scalar.transToVector();
                dv.angle -= acceleration.angle;
                var ds = dv.transToScalar();
                //由于支点的存在，所以加速度在板的方向上被抵消，所以需要计算板垂直向的加速度
                var ac = new vector_GM(acceleration.value,acceleration.angle);
                //切向加速度的计算为先计算加速度在板垂直向上的投影，再计算该投影在加速度垂直向上的投影
                var b = acceleration.angle - angle;
                var b1 = b - acceleration.angle;
                //ac是加速度在其法向上的投影（因为支点其实会产生沿板的支撑力，所以不是0）
                ac.value *= Math.abs(Math.sin(b) * Math.sin(b1));
                ac.angle += PI_half;
                //所以实际速度只有加速度垂直向上的运动
                speed.add(ac.multi_Return(ms_t));
                var l = speed.multi_ReturnScalar(ms_t);
                if(!systemRunOnJX)
                    l.x *= getRandom(10,100);
                x += l.x + ds.x;
                y += l.y + ds.y;
                angle = getLineAngle(x0,y0,x,y);
            }
            else
            {
                var l = speed.multi_ReturnScalar(ms_t);
                if(!systemRunOnJX)
                    l.x *= getRandom(10,100);
                x += l.x;
                y += l.y;
                if(acceleration)
                    speed.add(acceleration.multi_Return(ms_t));
            }
            if(deltaangle != 0)
                angle += deltaangle;
            //如果出了限制区域则调整speed
            if(x < area2_x1 || y < area2_y1 || x > area2_x2 || y > area2_y2)
            {
                var vs = speed.transToScalar();
                if(x < area2_x1 || x > area2_x2)
                    vs.x = -vs.x;
                if(y < area2_y1 || y > area2_y2)
                    vs.y = -vs.y;
                speed = vs.transToVector();
            }
            if(reflectAtBoundary && (x < 0 || y < 0 || x > canvaswidth || y > canvasheight))
            {
                var vs = speed.transToScalar();
                if(x < 0 || x > canvaswidth)
                    vs.x = -vs.x;
                if(y < 0 || y > canvasheight)
                    vs.y = -vs.y;
                speed = vs.transToVector();
            }
        }
        else if(canMove)
        {
            //手工移动了angle_delta偏角
            var vs = deltaValue_scalar.transToVector();
            angle += vs.angle;
        }
        var x_1 = x;
        var y_1 = y;
        if(x_0 > x_1) 
        {
            var tx = x_0;
            x_0 = x_1;
            x_1 = tx;
        }
        if(y_0 > y_1) 
        {
            var ty = y_0;
            y_0 = y_1;
            y_1 = ty;
        }
        var hw = width * 0.75;
        var hh = height * 0.75;
        area_x1 = x_0 - hw;
        area_y1 = y_0 - hh;
        area_x2 = x_1 + hw;
        area_y2 = y_1 + hh;
    }
}




function rect_gameObject()
{
    this.initParam = new rect_gameObjectParam();
}
rect_gameObject.prototype = new gameObject();

rect_gameObject.prototype.constructor = rect_gameObject;

//
//对象还没有开始运动前的绘画
//
rect_gameObject.prototype.draw = function()
{
    this.redraw();
}
//
//对象已经开始运动了的绘画
//
rect_gameObject.prototype.redraw = function()
{
    with(this)
    if (visible && canvas.getContext)
    {
        var ctx = canvas.getContext("2d");
        ctx.clearRect(0,0,canvas.width,canvas.height);
        ctx.save();
        ctx.translate(param.x0,param.y0);
        if(param.angle != 0)
            ctx.rotate(param.angle);
        ctx.fillStyle = param.color;
        var xoff = 0;
        var yoff = 0;
        if(param.originNotSame)
        {
            xoff = param.x - param.x0;
            yoff = param.y - param.y0;
        }
        ctx.fillRect(-param.width/2 + xoff, -param.height/2 + yoff, param.width, param.height);
        ctx.restore();
    }
}

