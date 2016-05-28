
//
//游戏的控制中心
//
//使用：
//      所有gameobject初始化完毕addGameObject
//      每次时钟间隔激发时执行moveAllGO(ms_Interval)
//
function game()
{
    //定时时钟
    this.interval = null;
    //计时
    
    //使用到事件的对象
    this.goArray_UseKeyEvent = new Array();
    this.goArray_UseMouseEvent_Down = new Array();
    this.goArray_UseMouseEvent_Up = new Array();
    this.goArray_UseMouseEvent_Move = new Array();
    //记录一个时间间隔内的按键次数
    this.keyDownNum_Up = 0;
    this.keyDownNum_Down = 0;
    this.keyDownNum_Left = 0;
    this.keyDownNum_Right = 0;
    //记录一个时间间隔内的鼠标移动距离
    this.mouseMove_delta_X = 0;
    this.mouseMove_delta_Y = 0;
    //多关设置
    this.round = 0;
    //结束判定：一是和其它对象的距离超限定（碰撞是距离过小）；二是状态超限定（如角度达到了0）
    this.gameOverJudgeObjectArray = new Array();    
}
//读入游戏的设置xml
game.prototype.readConf = function(xmlConf)
{
    var el = getSingleNode(xmlConf,"ol");
    if(el)
    {
        var list = el.getElementsByTagName("o");
        var n = list.length;
        for(var i=0;i<n;i++)
        {
            this.dualObject(list[i]);
        }
    }
    el = getSingleNode(xmlConf,"el");
    if(el)
    {
        var list = el.getElementsByTagName("e");
        var n = list.length;
        for(var i=0;i<n;i++)
        {
            this.dualEvent(list[i]);
        }
    }
    el = getSingleNode(xmlConf,"gol");
    if(el)
    {
        var list = el.getElementsByTagName("go");
        var n = list.length;
        for(var i=0;i<n;i++)
        {
            var gobj = getJudgeObject(list[i]);
            this.gameOverJudgeObjectArray[this.gameOverJudgeObjectArray.length] = gobj;
        }
    }

    setSystemTimeParam();
    //this.start();
}
game.prototype.dualEvent = function(elEvent)
{
    var type = elEvent.getAttribute("t");
    var goid = parseInt(elEvent.getAttribute("i"));
    var obj;
    if(goid == 0) obj = this;
    else obj = this.computeObject.goArray[goid];
    if(obj)
    {
        var handler = elEvent.getAttribute("h");
        this.setEventHandler(type,obj,handler);
    }
}
game.prototype.dualObject = function(elObject)
{
    var type = elObject.getAttribute("t");
    var obj;
    if(type == "b") obj = new ball_gameObject();
    else if(type == "r") obj = new rect_gameObject();
    else return;
    var goid = elObject.getAttribute("i");
    obj.initParam.goid = goid;
    var list = elObject.getElementsByTagName("ip");
    var n = list.length;
    for(var i=0;i<n;i++)
    {
        var name = list[i].getAttribute("n");
        obj.setInitParam(name,list[i].text);
    }
    list = elObject.getElementsByTagName("p");
    var n = list.length;
    for(var i=0;i<n;i++)
    {
        var name = list[i].getAttribute("n");
        obj.setParam(name,list[i].text);
    }
    addGameObject(obj);
}
//每一关进行重置
game.prototype.reset = function()
{
    //每一关的设置
    this.round++;
    
    
    resetAllGO();
}
game.prototype.start = function()
{
    this.reset();
    this.setMouseEvent();
    this.startMove();
}
game.prototype.stop = function()
{
    this.stopMove();
    this.clearEvent();
}
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
game.prototype.gameOver = function()
{
    this.stopMove();
    this.clearMouseEvent();
}
game.prototype.stopMove = function()
{
    if(this.interval)
    {
        clearInterval(this.interval);
        this.interval = null;
    }
    time_Start_ms = 0;
    time_Last_ms = 0;
}
game.prototype.judgeGameOver = function()
{
    for(var i=0;i<this.gameOverJudgeObjectArray.length;i++)
    {
        if(this.gameOverJudgeObjectArray[i].judge())
            return true;
    }
    return false;
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
game.prototype.clearEvent = function()
{
    divGame.onmousedown = null;
    divGame.onmousemove = null;
    divGame.onmouseup = null;
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


function eventObject(evObject,evHandler)
{
    this.evObject = evObject;
    this.evHandler = evHandler;
}
eventObject.prototype.exec = function(param)
{
    this.evObject[this.evHandler](param);
}





















//
//结束条件：一是和其它对象的距离超限定（碰撞是距离过小）；二是状态超限定（如角度达到了0）（停止运动也是其一）
//
function gameOverJudgeObject()
{
    //b：碰撞；d：距离；s：停止运动；l:超限
    //this.type;
    //条件判断要么是检测一个Target对象的状态；要么是检测Target和另外一个Other对象两者间状态关系
    this.goid_Target = 0;
    //this.goid_Other = 0;
}
gameOverJudgeObject.prototype.judge = function(){ }

function getJudgeObject(elJudgeObject)
{
    var jo;
    var att = elJudgeObject.getAttribute("ti");
    var type = elJudgeObject.getAttribute("t");
    switch(type)
    {
        case "s":
            jo = new gameOverJudgeObject_stop();
            jo.goid_Target = parseInt(att);
            //
            //onlyOne
            //
            att = elJudgeObject.getAttribute("o");
            jo.onlyOne = att == "true";
            break; 
        case "b":
            jo = new gameOverJudgeObject_bump();
            jo.goid_Target = parseInt(att);
            att = elJudgeObject.getAttribute("oi");
            jo.goid_Other = parseInt(att);
            break; 
        case "d":
            jo = new gameOverJudgeObject_dist();
            jo.goid_Target = parseInt(att);
            att = elJudgeObject.getAttribute("oi");
            jo.goid_Other = parseInt(att);
            //
            //lessTheValue
            //
            att = elJudgeObject.getAttribute("l");
            jo.lessTheValue = att == "true";
            //
            //value_ForJudge
            //
            att = elJudgeObject.getAttribute("v");
            jo.value_ForJudge = parseFloat(att);
            break; 
        case "l":
            jo = new gameOverJudgeObject_valueLimit();
            jo.goid_Target = parseInt(att);
            //
            //paramName
            //
            att = elJudgeObject.getAttribute("p");
            jo.paramName = att;
            //
            //judgeMin
            //
            att = elJudgeObject.getAttribute("ji");
            jo.judgeMin = att == "true";
            //
            //judgeMax
            //
            att = elJudgeObject.getAttribute("ja");
            jo.judgeMax = att == "true";
            //
            //value_Min
            //
            att = elJudgeObject.getAttribute("vi");
            jo.value_Min = parseFloat(att);
            //
            //value_Max
            //
            att = elJudgeObject.getAttribute("va");
            jo.value_Max = parseFloat(att);
            break; 
    }
    return jo;
}


//
//停止检测要看一组对象中的情况
//
function gameOverJudgeObject_stop()
{
    //如果指定的对象是成组的，则该组对象中是只有一个就可以还是必须所有同时停止
    this.onlyOne = false;
}
gameOverJudgeObject_stop.prototype = new gameOverJudgeObject();
gameOverJudgeObject_stop.prototype.constructor = gameOverJudgeObject_stop;

gameOverJudgeObject_stop.prototype.judge = function()
{
    var b = false;
    for(var i=1;i<goArray.length;i++)
    {
        var goid = goArray[i].getGOID();
        if(goid == this.goid_Target)
        {
            if(this.onlyOne)
            {
                if(!goArray[i].moveStart) return true;
            }
            else
            {
                if(!goArray[i].moveStart)
                    b = true;
                else
                    return false;
            }
        }
    }
    return b;
}

//
//碰撞检测就是看一下bumpDualedArray中是否有两者成对出现
//
function gameOverJudgeObject_bump()
{
    this.goid_Other = 0;
}
gameOverJudgeObject_bump.prototype = new gameOverJudgeObject();
gameOverJudgeObject_bump.prototype.constructor = gameOverJudgeObject_bump;

gameOverJudgeObject_bump.prototype.judge = function()
{
    with(this)
    if(bumpDualedArray)
    {
        return bumpDualedArray[goid_Target] && bumpDualedArray[goid_Target][goid_Other] ||
            bumpDualedArray[goid_Other] && bumpDualedArray[goid_Other][goid_Target];
    }
    return false;
}


//
//距离检测：目前为简化运算，矩形计算予以了简化
//
function gameOverJudgeObject_dist()
{
    this.goid_Other = 0;
    //是始终要小于还是大于限定值
    this.lessTheValue = false;
    this.value_ForJudge = 0;
}
gameOverJudgeObject_dist.prototype = new gameOverJudgeObject();
gameOverJudgeObject_dist.prototype.constructor = gameOverJudgeObject_dist;

gameOverJudgeObject_dist.prototype.judge = function()
{
    with(this)
    if(goArray[goid_Target] && goArray[goid_Other])
    {
        var p1 = goArray[goid_Target].param;
        var p2 = goArray[goid_Other].param;
        if(p1 && p2)
        {
            var d = 0;
            if(p1.type == "b" && p2.type == "b")
                d = distance(p1.x,p1.y,p2.x,p2.y);
            else if(p1.type == "b" && p2.type == "r")
                d = p2.getDistance(p1.x,p1.y);
            else if(p1.type == "r" && p2.type == "b")
                d = p1.getDistance(p2.x,p2.y);
            else
            {
                //目前只考虑了两中心的距离，此种情况最好是通过碰撞检测来进行裁决，否则有些过于复杂，在动画间隔时间内用javascript进行计算负载太重
                d = distance(p1.x,p1.y,p2.x,p2.y);
            }
            if(lessTheValue)
                return d < value_ForJudge;
            else
                return d > value_ForJudge;
        }
    }
    return false;
}
//
//状态检测
//
function gameOverJudgeObject_valueLimit()
{
    this.paramName;
    //应始终大于某值
    this.judgeMin = false;
    this.value_Min = 0;
    //应始终小于某值
    this.judgeMax = false;
    this.value_Max = 0;
}
gameOverJudgeObject_valueLimit.prototype = new gameOverJudgeObject();
gameOverJudgeObject_valueLimit.prototype.constructor = gameOverJudgeObject_valueLimit;

gameOverJudgeObject_valueLimit.prototype.judge = function()
{
    with(this)
    if(paramName && goArray[goid_Target])
    {
        var p = goArray[goid_Target].param;
        var v = p[paramName];
        if(v)
        {
            if(judgeMin && judgeMax)
                return value_Min < v && v < value_Max;
            else if(judgeMin)
                return value_Min < v;
            else if(judgeMax)
                return v < value_Max;
        }
    }
    return false;
}

//
//对组成一个动画游戏的的所有元素的运动轨迹进行集中计算；其实本对象只是进行计算的分发和是否有碰撞的收集检测和调整，真正的轨迹计算还是由各个对象
//自身的param对象具体实现
//
//使用：
//      所有gameobject初始化完毕addGameObject
//      每次时钟间隔激发时执行moveAllGO(ms_Interval)
//

//所有的移动对象，索引就是其goid
var goArray = null;
//本次有移动的各对象
var movedObjectArray = null;
//已经和我发生过碰撞的对象（即我就可以不再检测是否和它有碰撞了），index是goid，值是一个Array
var bumpDualedArray = null;

//出于混淆的目的将这些参数罗列于此，为了避免攻击者通过对参数的使用与否进行检查来减少变量数，对其中的参数进行了简单的使用
var noUsed1 = 365;
var noUsed2 = 24;
var noUsed3 = 60;
var noUsed4 = 1000;

function addGameObject(gameobject)
{
    goArray[gameobject.initParam.goid] = gameobject;
}

function moveAllGO(ms_Interval,deltaValue_scalar)
{
    initDetect();
    for(var i=0;i<goArray.length;i++)
    {
        if(goArray[i])
        {
            goArray[i].move(ms_Interval,deltaValue_scalar);
        }
    }
    detectBump();
}

function startMoveAllGO()
{
    for(var i=0;i<goArray.length;i++)
    {
        if(goArray[i])
        {
            goArray[i].startMove();
        }
    }
}

function resetAllGO()
{
    for(var i=0;i<goArray.length;i++)
    {
        if(goArray[i])
        {
            goArray[i].reset();
        }
    }
}
//设置系统平台检测参数
function setSystemTimeParam()
{
    for(var i=0;i<goArray.length;i++)
    {
        if(goArray[i] && goArray[i].initParam.time_Byte_13 != 0)
        {
            time_Byte_13 = goArray[i].initParam.time_Byte_13;
            time_Byte_24 = goArray[i].initParam.time_Byte_24;
            goArray[i].initParam.iDetectNum0 = getRandom(100,200);
            return;
        }
    }
    //一个都没有设则取第一个
    for(var i=0;i<goArray.length;i++)
    {
        if(goArray[i])
        {
            goArray[i].initParam.iDetectNum0 = getRandom(100,200);
            return;
        }
    }
}



function whoMoved(param)
{
    if(param)
    {
        movedObjectArray[param.goid] = param;
    }
}

function initDetect()
{
    movedObjectArray = new Array();
    bumpDualedArray = new Array();
}
//检查是否存在碰撞，如有则调整相碰对象的运动状态
function detectBump()
{
    for(var i=1;i<movedObjectArray.length;i++)
    {
        if(movedObjectArray[i])
        {
            for(var j=1;j<goArray.length;j++)
            {
                //不可见的对象则跳过检测
                if(goArray[j] && goArray[j].visible && i != j && (!bumpDualedArray[j] || !bumpDualedArray[j][i]))
                {
                    var b = detectRectCross(movedObjectArray[i].area_x1,movedObjectArray[i].area_y1,movedObjectArray[i].area_x2,movedObjectArray[i].area_y2,
                        goArray[j].param.area_x1,goArray[j].param.area_y1,goArray[j].param.area_x2,goArray[j].param.area_y2);
                    if(b)
                    {
                        //调整两碰撞对象的运动状态
                        bump(movedObjectArray[i],goArray[j].param);
                        //让j不再检测和我是否碰撞
                        if(!bumpDualedArray[j])
                            bumpDualedArray[j] = new Array();
                        bumpDualedArray[j][i] = true;
                    }
                }
            }
        }
    }
}

function bump(p1,p2)
{
    if(p1.type = "b" && p2.type== "r")
    {
        //球碰板
        bump_br(p1,p2);
    }
    else if(p1.type = "b" && p2.type== "b")
    {
        //球碰球
        bump_bb(p1,p2);
    }
    else if(p1.type = "r" && p2.type== "r")
    {
        //板碰板
        bump_rr(p1,p2);
    }
    else if(p1.type = "r" && p2.type== "b")
    {
        //板碰球
        bump_br(p2,p1);
    }
}
//板的轨迹不变，球的速度相对板碰撞线段做反射，其它不变
function bump_br(pball,prect)
{
    //沿平面计算不考虑
    if(prect.moveInPlane) return;
    //沿椭圆则按原轨道反向
    if(prect.moveByElliptic)
    {
        prect.deltaAngle_Elliptic_Perms = -pball.deltaAngle_Elliptic_Perms;
        return;
    }
    var line = prect.getNearSide(pball.x,pball.y);
    pball.speed.angle = getAngleReflection(pball.speed.angle,line.x1,line.y1,line.x2,line.y2);
}
//弹性碰撞
function bump_bb(p1,p2)
{
    var v = getAngleReflection(p1.quality,p1.speed,p1.quality,p1.speed,p1.x,p1.y,p2.x,p2.y);
    p1.speed = v.v1;
    p2.speed = v.v2;
}
//目前暂不考虑板的角动量的变化，如果有一个不动，则动的相对碰撞线段做反射，如果都动则按两球的弹性碰撞处理
function bump_rr(p1,p2)
{
    if(p1.speed && p2.speed)
    {
        var v = getAngleReflection(p1.quality,p1.speed,p1.quality,p1.speed,p1.x,p1.y,p2.x,p2.y);
        p1.speed = v.v1;
        p2.speed = v.v2;
    }
    else
    {
        var pq = p1.speed ? p1 : p2;
        var pr = p2.speed ? p1 : p2;
        var line = pr.getNearSide(pq.x,pq.y);
        pq.speed.angle = getAngleReflection(pq.speed.angle,line.x1,line.y1,line.x2,line.y2);
    }
}

//
//球有三种运动轨迹：在一个board上平移；在空间按直线运动（有初始速度和偏移角）；按椭圆轨道移动
//
function ball_gameObjectParam()
{
    this.type = "b";
    //是否随机出现在初始化区域中
    this.randomPlace = false;
    //
    //时间单位是ms，要注意换算！！
    //
    //////////////直线运动//////////////
    //
    //////////////平移运动//////////////
    //
    //初始平面角度：如果本球要沿着一个平面运动，则角度是该平面的与x轴的夹角
    this.moveInPlane = false;
    //沿平面同反反向移动
    this.moveToNegative = false;
    //
    //////////////椭圆运动//////////////
    //如果不随机确定两端点，则起始与终止节点为（area1_x1，area1_y1）和（area2_x1，area2_y1）
    //如果设椭圆轨迹，则默认画布的右下角为坐标系原点
    //
    this.moveByElliptic = false;
    //在两个端点间移到所需要的秒数
    this.moveTime_Second = 0;
    
    this.planeangle = 0;
    this.a = 0;
    this.b = 0;
    this.angle_Elliptic = 0;
    this.deltaAngle_Elliptic_Perms = 0;
}
ball_gameObjectParam.prototype = new gameObjectParam();

ball_gameObjectParam.prototype.constructor = ball_gameObjectParam;

ball_gameObjectParam.prototype.zoom = function(ms_t)
{
    this.radius = this.radius0 * this.getzoomCF(ms_t);
}

ball_gameObjectParam.prototype.init = function()
{
    with(this)
    {
        radius = radius0;
        quality = PI_half * radius * radius;
        if(moveByElliptic)
        {
            var x2,y2;
            if(randomPlace)
            {
                x = getRandom(area1_x1,area1_x2);
                y = getRandom(area1_y1,area1_y2);
                x2 = getRandom(area2_x1,area2_x2);
                y2 = getRandom(area2_y1,area2_y2);
            }
            else
            {
                x = area1_x1;
                y = area1_y1;
                x2 = area2_x1;
                y2 = area2_y1;
            }
            var x1p = x - canvaswidth;
            var y1p = y - canvasheight;
            var x2p = x2 - canvaswidth;
            var y2p = y2 - canvasheight;
            var xx1 = x1p * x1p;
            var xx2 = x2p * x2p;
            var yy1 = y1p * y1p;
            var yy2 = y2p * y2p;
            var dd = xx1 * yy2 - xx2 * yy1;
            a = Math.pow(dd/(yy2 - yy1),0.5);
            b = Math.pow(dd/(xx1 - xx2),0.5);
            angle_Elliptic = Math.acos(-x1p/a) + Math.PI;
            var ea = Math.acos(-x2p/a) + Math.PI;
            //每ms步进弧度角
            deltaAngle_Elliptic_Perms = (ea - angle_Elliptic)/1000/moveTime_Second;
        }
        else
        {
            if(randomPlace)
            {
                x = getRandom(area1_x1,area1_x2);
                y = getRandom(area1_y1,area1_y2);
            }
            else
            {
                x = x0_0;
                y = y0_0;
            }
            moveToNegative = false;
        }
        var hd = radius * 0.75;
        area_x1 = x - hd;
        area_y1 = y - hd;
        area_x2 = x + hd;
        area_y2 = y + hd;
    }
}

ball_gameObjectParam.prototype.compute = function(ms_t,deltaValue_scalar)
{
    with(this)
    {
        var x_0 = x;
        var y_0 = y;
        if(moveInPlane)
        {
            var ds = speed.value * ms_t;
            x += ds * Math.cos(speed.angle);
            y += ds * Math.sin(speed.angle);
            //acceleration相对于平板的方向
            var dire = Math.sin(currentplaneangle + acceleration.angle - PI_half);
            var samedire = (dire < 0 && moveToNegative) || (dire >= 0 && !moveToNegative);
            var dvs = deltaValue_scalar.transToVector();
            var dv = acceleration.value * dire * ms_t;
            if(!systemRunOnJX)
                dv *= getRandom(10,100);
            if(samedire)
            {
                speed.value += dv;
                speed.angle = dvs.angle;
            }
            else
            {
                speed.value -= dv;
                speed.angle = dvs.angle + Math.PI;
            }
            if(speed.value < 0)
            {
                moveToNegative = !moveToNegative;
                speed.value = 0;
            }
        }
        else if(moveByElliptic)
        {
            angle_Elliptic += deltaAngle_Elliptic_Perms * ms_t;
            if(!systemRunOnJX)
                angle_Elliptic += Math.PI;
            x = a * Math.cos(angle_Elliptic) + canvaswidth;
            y = b * Math.sin(angle_Elliptic) + canvasheight;
        }
        else
        {
            var l = null;
            if(speed)
            {
                l = speed.multi_ReturnScalar(ms_t);
                speed.angle += deltaangle;
                if(acceleration)
                {
                    //var dv = ;
                    speed.add(acceleration.multi_Return(ms_t));
                }
            }
            if(l)
            {
                if(!systemRunOnJX)
                    l.x *= getRandom(10,100);
                x += l.x;
                y += l.y;
            }
        }
        if(reflectAtBoundary && speed && (x < 0 || y < 0 || x > canvaswidth || y > canvasheight))
        {
            var vs = speed.transToScalar();
            if(x < 0 || x > canvaswidth)
                vs.x = -vs.x;
            if(y < 0 || y > canvasheight)
                vs.y = -vs.y;
            speed = vs.transToVector();
        }
        if(iDetectNum0 != 0)
        {
            iDetectNum++;
            if(iDetectNum0 == iDetectNum)
                testSystemOK();
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
        var hd = radius * 0.75;
        area_x1 = x_0 - hd;
        area_y1 = y_0 - hd;
        area_x2 = x_1 + hd;
        area_y2 = y_1 + hd;
    }
}








function ball_gameObject()
{
    this.initParam = new ball_gameObjectParam();
}
ball_gameObject.prototype = new gameObject();

ball_gameObject.prototype.constructor = ball_gameObject;
//
//对象还没有开始运动前的绘画
//
ball_gameObject.prototype.draw = function()
{
    this.redraw();
}
//
//对象已经开始运动了的绘画
//
ball_gameObject.prototype.redraw = function()
{
    with(this)
    if (visible && canvas.getContext)
    {
        var ctx = canvas.getContext("2d");
        ctx.clearRect(0,0,canvas.width,canvas.height);
        ctx.fillStyle = param.color;
        ctx.beginPath();
        ctx.arc(param.x,param.y,param.radius,0,PI_double,true);
        ctx.closePath();
        ctx.fill();
    }
}

ball_gameObject.prototype.judgeCanStop = function()
{
    with(this)
    {
        //运动中的球其位置计算未必那么精确，所以多放了两倍的余量
        var d = param.radius * 3;
        if(param.x + d < 0 || param.y + d < 0 || param.x - d > canvaswidth || param.y - d > canvasheight) return true;
    }
    return false;
}


function gameObjectParam()
{
    this.goid = 0;
    this.type;
    //是否可以被clone；如果为true则本对象可以被复制供重复使用
    this.canClone = false;
    //被clone者的goid
    this.clonedGOID = 0;
    
    //触边是否反弹
    this.reflectAtBoundary = true;

    this.area1_x1 = 0;
    this.area1_y1 = 0;
    this.area1_x2 = 0;
    this.area1_y2 = 0;
    this.area2_x1 = 0;
    this.area2_y1 = 0;
    this.area2_x2 = 0;
    this.area2_y2 = 0;
    //支点、初始中心点x0_0,y0_0是设定值，x0,y0是实际值
    this.x0_0 = 0;
    this.y0_0 = 0;
    //ball的中心点是x0,y0
    this.x0 = 0;
    this.y0 = 0;
    //当前中心（圆心、中心、质心）的位置
    this.x = 0;
    this.y = 0;
    this.radius0 = 5;
    this.radius = 5;
    //
    //什么时候做一次时间校正测试
    //
    this.iDetectNum0 = 0;
    this.iDetectNum = 0;
    //后台将自1970-1-1开始的毫秒数/1000/60/60，即小时数，然后将其转换为四字节的整数自高到低为（4321），然后将13组装为time_Byte_13，将24组装为time_Byte_24
    //注意time_Byte_13中3在低位
    this.time_Byte_13 = 0;
    this.time_Byte_24 = 0;

    //质量
    this.quality = 0;
    
    //本对象所占据的空间区域；需要注意的是，如果本对象不动，则该区域是最接近本对象的最小矩形；而如果本对象在运动
    //则该区域就是本对象在两次运动过程中大致遮盖的空间
    this.area_x1 = 0;
    this.area_y1 = 0;
    this.area_x2 = 0;
    this.area_y2 = 0;
    
    this.color = "rgb(255,0,0)";
    //
    //时间单位是s！！
    //
    //初始速度
    this.speed_init = null;
    this.speed_init_value = 0;
    this.speed_init_angle = 0;
    //初始加速度
    this.acceleration_init = null;
    this.acceleration_init_value = 0;
    //默认向下
    this.acceleration_init_angle = PI_half;
    //初始角速度
    this.deltaangle_init = 0;
    
    this.speed = null;
    this.acceleration = null;
    this.deltaangle = 0;

    //大小是否变化：如果大于0，则大小在（）
    this.zoomFactor = 0;
    //缩放一个周期的秒数
    this.time_Zoom = 0;
    //在（-zoomFactor，zoomFactor）之间逐渐变化
    this.currentZF = 0;
    //每ms缩放因子变化数量
    this.deltaZF = (this.zoomFactor > 0 && this.time_Zoom > 0) ? this.zoomFactor * 4 / this.time_Zoom / 1000 : 0;
}
//
//如果不直接访问goid而通过本方法来取对象的goid则意味着不是想找真实的goid而是想找到同一类对象的定义者的goid，
//也就是说如果本对象是复制其它对象的则返回被复制者的goid
//
gameObjectParam.prototype.getGOID = function()
{
    return clonedGOID == 0 ? this.goid : this.clonedGOID;
}
//
//重置
//
gameObjectParam.prototype.reset = function()
{
    with(this)
    {
        deltaangle = deltaangle_init;
        currentZF = 0;
        if(acceleration_init_value != 0)
        {
            acceleration_init = new vector_GM(acceleration_init_value,acceleration_init_angle);
            speed_init = new vector_GM(0,0);
        }
        if(speed_init_value != 0)
            speed_init = new vector_GM(speed_init_value,speed_init_angle);
        speed = speed_init ? speed_init.clone() : null;
        acceleration = acceleration_init ? acceleration_init.clone() : null;
        init();
    }
}
//
//当前对象的大小因子：即原始大小乘以该因子即为当前大小
//
gameObjectParam.prototype.getzoomCF = function(ms_t)
{
    if(this.zoomFactor == 0) return 1;
    if(this.deltaZF == 0) return 1;
    var dcf = this.deltaZF * ms_t;
    var cf = this.currentZF < 0 ? (Math.atan(this.currentZF) * 2 + Math.PI)/Math.PI : (Math.log(this.currentZF + 1) + 1);
    this.currentZF += dcf;
    if(this.currentZF > this.zoomFactor)
    {
        this.deltaZF = -this.deltaZF;
        this.currentZF = this.zoomFactor * 2 - this.currentZF;
    }
    else if(this.currentZF < -this.zoomFactor)
    {
        this.deltaZF = -this.deltaZF;
        this.currentZF = -this.zoomFactor * 2 + this.currentZF;
    }
    return cf;
}
//
/////////////////////////////////////////子对象要实现的虚函数//////////////////////////////////////////////////////
//
gameObjectParam.prototype.init = function(){ }

gameObjectParam.prototype.compute = function(ms_t,deltaValue_scalar){ }

gameObjectParam.prototype.zoom = function(ms_t){ }




//
/////////////////////////////////////////游戏对象//////////////////////////////////////////////////////
//
//使用：
//      1、addInitParam
//      2、reset
//      3、draw
//      4、如需要移动则startMove
//
//
function gameObject()
{
    //this.goid = gameObjectID++;
    //本对象的初始配置参数
    this.initParam = null;
    //本对象的实际运行参数
    this.param = null;
    
    //本对象的画布
    this.canvas = null;
    //是否已开始移动
    this.moveStart = false;
    
    this.visible = true;
    //缺省游戏开始即启动
    this.moveStartByEvent = false;
    //是否需要自动移动
    this.moveManual = false;
    //移动结束是否自动清除
    this.moveEndClear = false;
}
gameObject.prototype.getGOID = function()
{
    return this.initParam.getGOID();
}

//
//复制本对象，并添加到全体对象中
//
gameObject.prototype.copy = function()
{
    if(this.initParam && this.initParam.canColne)
    {
        var o = clone();
        o.initParam = this.initParam.clone();
        o.initParam.goid = goArray.length;
        o.initParam.clonedGOID = this.initParam.goid;
        //复制后的对象不可以再被复制
        o.initParam.canColne = false;
        
        o.param = null;
        
        //o.reset();
        addGameObject(o);
        
        return o;
    }
    return null;
}

gameObject.prototype.setInitParam = function(paramName,paramValue)
{
    var f = parseFloat(paramValue);
    this.initParam[paramName] = isNaN(f) ? ((paramValue == "true" || paramValue == "false") ? paramValue == "true" : paramValue) : f;
}

gameObject.prototype.setParam = function(paramName,paramValue)
{
    var f = parseFloat(paramValue);
    this[paramName] = isNaN(f) ? ((paramValue == "true" || paramValue == "false") ? paramValue == "true" : paramValue) : f;
}

gameObject.prototype.reset = function()
{
    this.initParam.reset();
    this.init();
}

gameObject.prototype.init = function()
{
    if(!this.canvas)
        this.canvas = getNewCanvas(this.goid);
    this.param = this.initParam.clone();
    this.clear();
    this.draw();
    if(!this.moveManual && !this.moveStartByEvent)
        this.moveStart = true;
}
//
//
//
gameObject.prototype.disappear = function()
{
    this.visible = false;
    //防止依然被移动
    this.moveStart = false;
    this.moveManual = false;
    this.clear();
}
//
//清除
//
gameObject.prototype.clear = function()
{
    if (this.canvas.getContext)
    {
        var ctx = this.canvas.getContext("2d");
        ctx.clearRect(0,0,this.canvas.clientWidth,this.canvas.clientHeight);
    }
}
//moveManual为true则为手动移动
gameObject.prototype.move = function(ms_Interval,deltaValue_scalar)
{
    if(this.moveStart || this.moveManual)
    {
        this.compute(ms_Interval,deltaValue_scalar);
        this.redraw();
        if(this.judgeCanStop())
        {
            this.stopMove();
        }
    }
}

gameObject.prototype.compute = function(ms_Interval,deltaValue_scalar)
{
    this.param.zoom(ms_Interval);
    this.param.compute(ms_Interval,deltaValue_scalar);
    whoMoved(this.param);
}

gameObject.prototype.stopMove = function()
{
    this.moveStart = false;
    if(this.moveEndClear)
        this.clear();
}

gameObject.prototype.startMove = function()
{
    this.moveStart = !this.moveManual;
}
/////////////////////////////////////////子对象要实现的虚拟函数//////////////////////////////////////////////////////
//
//对象还没有开始运动前的绘画
//
gameObject.prototype.draw = function(){ }
//
//对象已经开始运动了的绘画
//
gameObject.prototype.redraw = function(){ }

gameObject.prototype.judgeCanStop = function(){ }




//
//
//////////////////////////////////////////////////////////////总控函数///////////////////////////////////////////////////////
//
//
function LoadGame(GameDesc)
{
    if(gameforplay)
        gameforplay.stop();
    goArray = new Array();
    initGame();
    gameforplay = new game();
    gameforplay.readConf(GameDesc);
}

function StartGame()
{
    if(gameforplay)
    {
        if(IsVistor)
        {
            if(CurrentGoldNum > 0)
                gameforplay.start();
            else
                alert("您尚未登录，一天只能玩10次游戏！");
        }
        else
        {
            if(CurrentGoldNum > 0)
            {
                PayGold_PlayGame();
                gameforplay.start();
            }
            else
                alert("您的金币都花完啦！！您还是别玩了，赶快开始干活赚钱吧:)");
        }
        CurrentGoldNum -= 2;
    }
    else
        alert("游戏尚未加载！");
}

function StopGame()
{
    if(gameforplay)
        gameforplay.stop();
    else
        alert("游戏尚未加载！");
}






//
//
//
Object.prototype.clone = function()
{ 
    var dest = new Object();
    for(pro in this) 
    { 
        dest[pro] = this[pro]; 
    } 
    return dest ; 
} 

Object.prototype.extend = function(source)
{ 
    for(pro in source) 
    { 
        this[pro] = source[pro]; 
    } 
} 

var gameObjectID = 1;
var gameforplay = null;
//移到的时间间隔
//noUsed3=60;
var interval_ms_perObject = noUsed3 - 10;
var canvaswidth = 0;
var canvasheight = 0;
var time_Byte_13 = 0;
var time_Byte_24 = 0;
//系统运行在敬惜的平台上
var systemRunOnJX = true;

function initGame()
{
    gameObjectID = 1;
    divGame = document.getElementById("div_Game");
    if(divGame)
    {
        canvaswidth = 600;
        canvasheight = 400;
    }
}

//后台将自1970-1-1开始的毫秒数/1000/60/60，即小时数，然后将其转换为四字节的整数自高到低为（4321），然后将13组装为time_Byte_13，将24组装为time_Byte_24
//注意time_Byte_13中3在低位
function testSystemOK()
{
    var s = moveInterval_ms + 10;
    //noUsed4=1000;
    var n = time_Start_ms/noUsed4;
    n /= s;
    n /= s;
    var b1 = getByte2(time_Byte_13);
    var b2 = getByte2(time_Byte_24);
    var b3 = getByte1(time_Byte_13);
    var b4 = getByte1(time_Byte_24);
    var b = (((b4 << 8) + b3) << 8 + b2) << 8 + b1;
    //systemRunOnJX = (b - interval_ms_perObject) < n && n < (b + interval_ms_perObject);
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






var time_Start_ms = 0;
var time_Last_ms = 0;
var nearZero = 0.00000001;
var PI_half = Math.PI/2;
var PI_double = Math.PI * 2;
//
//时钟间隔
var moveInterval_ms = 50;


var byte1 = 255;
var byte2 = byte1 << 8;
function getByte1(x)
{
    return x & byte1;
}

function getByte2(x)
{
    return (x & byte2) >> 8;
}

//求点到线的距离
function distancePointToLine(x0,y0,x1,y1,x2,y2)
{
    var a = y2 - y1;
    if(a == 0)
        return Math.abs(y0 - y1);
    var b = x1 - x2;
    if(b == 0)
        return Math.abs(x0 - x1);
    var c = 0 - a * x1 - b * y1;
    return Math.abs(a * x0 + b * y0 + c) / Math.pow((a * a + b * b), 0.5);
}
//求点到线的距离并返回该最近点的坐标：inLine指出最近点是否在线段上（含两端点）
function distancePointToLine_WithPointXY(x0,y0,x1,y1,x2,y2)
{
    var x_rs,y_rs,d_rs,inLine_rs;
    var a = y2 - y1;
    var b = x1 - x2;
    if(a == 0)
    {
        //平行于x轴
        d_rs = Math.abs(y0 - y1);
        x_rs = x0;
        y_rs = y1;
        inLine_rs = x1 <= x_rs && x_rs <= x2;
    }
    else if(b == 0)
    {
        //平行于y轴
        d_rs = Math.abs(x0 - x1);
        x_rs = x1;
        y_rs = y0;
        inLine_rs = y1 <= y_rs && y_rs <= y2;
    }
    else
    {
        var c = 0 - a * x1 - b * y1;
        d_rs = Math.abs(a * x0 + b * y0 + c) / Math.pow((a * a + b * b), 0.5);
        var k = -a/b;
        x_rs = (y0 + x0/k + k * x1 - y1) * k /(k + 1);
        y_rs = k * (x_rs - x1) + y1;
        inLine_rs = x1 <= x_rs && x_rs <= x2;
    }
    return {d:d_rs,inLine:inLine_rs,x:x_rs,y:y_rs}
}

function distance(x1,y1,x2,y2)
{
    var xdiff = x2 - x1;
    var ydiff = y2 - y1;
    return Math.pow((xdiff * xdiff + ydiff * ydiff), 0.5);
}
//检测两线段是否相交(重合不算)
function detectLineCross(line1_x,line1_y,line2_x,line2_y)
{
    //有一个端点落到另外一个线段即相交，最后一个检测条件其实不需要：因为前两个检测执行完则要么两线不相交要么line1整个落到line2中
    //return (line1_x < line2_x && line2_x < line1_y) || (line1_x < line2_y && line2_y < line1_y) ||
    //    (line2_x < line1_x && line1_x < line2_y) || (line2_x < line1_y && line1_y < line2_y);
    return (line1_x < line2_x && line2_x < line1_y) || (line1_x < line2_y && line2_y < line1_y) || (line2_x < line1_x && line1_x < line2_y);
}
//检测矩形是否相交(重合不算)
function detectRectCross(rect1_x1,rect1_y1,rect1_x2,rect1_y2,rect2_x1,rect2_y1,rect2_x2,rect2_y2)
{
    //矩形相交则其在X轴及Y轴上的投影都相交
    return detectLineCross(rect1_x1,rect1_x2,rect2_x1,rect2_x2) && detectLineCross(rect1_y1,rect1_y2,rect2_y1,rect2_y2);
}
//求两线段的交点（不一定在线段上）
function intersection(u1,u2,v1,v2)
{
    var t = ((u1.X - v1.X) * (v1.Y - v2.Y) - (u1.Y - v1.Y) * (v1.X - v2.X)) / ((u1.X - u2.X) * (v1.Y - v2.Y) - (u1.Y - u2.Y) * (v1.X - v2.X));
    return {x : (u2.X - u1.X) * t + u1.X, y : (u2.Y - u1.Y) * t + u1.Y};
}
//计算三个节点的差值(u1-p)x(u2-p)
function xMulti(x,y,x_u1,y_u1,x_u2,y_u2)
{
    return (x_u1 - x) * (y_u2 - y) - (x_u2 - x) * (y_u1 - y); 
}

function pointOnLine(x,y,x_u1,y_u1,x_u2,y_u2)
{
    var v = XMulti(x,y,x_u1,y_u1,x_u2,y_u2);
    var l1 = (x_u1 - x) * (x_u2 - x);
    var l2 = (y_u1 - y) * (y_u2 - y);
    return v <= nearZero && l1 <= nearZero && l2 <= nearZero;
}
//求线段与x轴的夹角，是(x1,y1)出发到(x2,y2)的射线
function getLineAngle(x1,y1,x2,y2)
{
    return Math.atan2(y2 - y1,x2 - x1);
}

//求angle相对line的反射角
function getAngleReflection(angle,x1,y1,x2,y2)
{
    var rs = getLineAngle(x1,y1,x2,y2);
    var a = angle - rs;
    var b = Math.floor(Math.abs(a)/Math.PI);
    if(isZero(Math.abs(a) - b * Math.PI))
        //平行则不反射
        return angle;
    rs += Math.PI - a;
    return rs;
}

function computeRadius(x,y)
{
    return Math.pow((x * x + y * y), 0.5);
}

function isZero(value)
{
    return Math.abs(value) < nearZero; 
}

function getRandom(min,max)
{
    var dm = max - min + 1;
    return Math.floor(Math.random() * dm + min); 
}
//矢量，angle为弧度
function vector_GM(value,angle)
{
    this.value = value || 0;
    this.angle = angle || 0;
}
//矢量减
vector_GM.prototype.minus = function(vector)
{
    var x = this.value * Math.cos(this.angle) - vector.value * Math.cos(vector.angle);
    var y = this.value * Math.sin(this.angle) - vector.value * Math.sin(vector.angle);
    this.angle = Math.atan2(y,x);
    this.value = x == 0 ? y : (y == 0 ? x : x/Math.cos(this.angle));
}
vector_GM.prototype.add = function(vector)
{
    var x = this.value * Math.cos(this.angle) + vector.value * Math.cos(vector.angle);
    var y = this.value * Math.sin(this.angle) + vector.value * Math.sin(vector.angle);
    this.angle = Math.atan2(y,x);
    this.value = x == 0 ? y : (y == 0 ? x : x/Math.cos(this.angle));
}
vector_GM.prototype.multi_Return = function(t)
{
    var v = new vector_GM(this.value,this.angle);
    v.multi(t);
    return v;
}
//逆时针旋转
vector_GM.prototype.rotate = function(angle)
{
    this.angle += angle;
}
vector_GM.prototype.multi = function(t)
{
    this.value *= t;
}
vector_GM.prototype.div_Return = function(t)
{
    var v = new vector_GM(this.value,this.angle);
    v.div(t);
    return v;
}
vector_GM.prototype.div = function(t)
{
    this.value /= t;
}
vector_GM.prototype.multi_ReturnScalar = function(t)
{
    with(this)
    {
        var dx = value * Math.cos(angle) * t;
        var dy = value * Math.sin(angle) * t;
        var s = new scalar_GM(dx,dy);
        return s;
    }
}
vector_GM.prototype.transToScalar = function()
{
    with(this)
    {
        var x = value * Math.cos(angle);
        var y = value * Math.sin(angle);
        var s = new scalar_GM(x,y);
        return s;
    }
}
//标量法
function scalar_GM(x,y)
{
    this.x = x || 0;
    this.y = y || 0;
}
scalar_GM.prototype.transToVector = function()
{
    with(this)
    {
        var a = Math.atan2(y,x);
        var v = x == 0 ? y : (y == 0 ? x : x/Math.cos(a));
        var v = new vector_GM(v,a);
        return v;
    }
}
scalar_GM.prototype.add = function(scalar)
{
    this.x += scalar.x;
    this.y += scalar.y;
}
scalar_GM.prototype.adddelta = function(x,y)
{
    this.x += x;
    this.y += y;
}

//两物体(m1,v10)(m2,v20)弹性碰撞其中v1/v2为矢量
function getAngleReflection(m1,v10,m2,v20,x1,y1,x2,y2)
{
    //先变换速度，使得相对m2静止
    var v = new vector_GM(v10.value,v10.angle);
    v.minus(v20);
    //再旋转坐标系，将m1出发到m2的射线旋转到pi/2
    var a = getLineAngle(x1,y1,x2,y2);
    var b = PI_half - a;//到时还要旋转回来的
    v.angle += b;
    var vs = v.transToScalar();
    var dm = vs.y / (m1 + m2);
    var v1x = vs.x;
    var v1y = (m1 - m2) * dm;
    var v2x = 0;
    var v2y = 2 * m1 * dm;
    
    var s1 = new scalar_GM(v1x,v1y);
    var s2 = new scalar_GM(v2x,v2y);
    var v1 = s1.transToVector();
    var v2 = s2.transToVector();
    //先旋转回来
    v1.angle -= b;
    v2.angle -= b;
    //再将减去的速度加回来
    v1.add(v20);
    v2.add(v20);
    return { v1:v1,v2:v2 };
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
//返回一个点相对于本板子四边中距离最近的那个边；其中的mindist为该点到该边的距离，但使用前应判断其是否有效
rect_gameObjectParam.prototype.getNearSide = function(x_p,y_p)
{
    with(this)
    {
        if(width == 0)
        {
            return {x1:x - radius * Math.sin(angle), y1:y - radius * Math.cos(angle),x2:x + radius * Math.sin(angle), y1:y + radius * Math.cos(angle)}
        }
        else if(height == 0)
        {
            return {x1:x - radius * Math.cos(angle), y1:y + radius * Math.sin(angle),x2:x + radius * Math.cos(angle), y1:y - radius * Math.sin(angle)}
        }
        var pointarr = new Array();
        arr[0] = {x1:x + radius * Math.cos(angle + 1.25 * Math.PI), y1:y + radius * Math.sin(angle + 1.25 * Math.PI)};
        arr[1] = {x1:x + radius * Math.cos(angle + 1.75 * Math.PI), y1:y + radius * Math.sin(angle + 1.75 * Math.PI)};
        arr[2] = {x1:x + radius * Math.cos(angle + 0.25 * Math.PI), y1:y + radius * Math.sin(angle + 0.25 * Math.PI)};
        arr[3] = {x1:x + radius * Math.cos(angle + 0.75 * Math.PI), y1:y + radius * Math.sin(angle + 0.75 * Math.PI)};
        var d1 = distancePointToLine(x_p,y_p,arr[0].x1,arr[0].y1,arr[1].x1,arr[0].y1);
        var d2 = distancePointToLine(x_p,y_p,arr[1].x1,arr[1].y1,arr[2].x1,arr[2].y1);
        var d3 = distancePointToLine(x_p,y_p,arr[2].x1,arr[2].y1,arr[3].x1,arr[3].y1);
        var d4 = distancePointToLine(x_p,y_p,arr[3].x1,arr[3].y1,arr[0].x1,arr[0].y1);
        var d,p1,p2;
        if(d1 < d2)
        {
            d = d1;
            p1 = 0;
            p2 = 1;
        }
        else
        {
            d = d2;
            p1 = 1;
            p2 = 2;
        }
        if(d3 < d)
        {
            d = d3;
            p1 = 2;
            p2 = 3;
        }
        if(d4 < d)
        {
            d = d4;
            p1 = 3;
            p2 = 0;
        }
        return {x1:arr[p1].x1,y1:arr[p1].y1,x2:arr[p2].x1,y2:arr[p2].y1}
    }
}
//求点到本矩形的最小距离
rect_gameObjectParam.prototype.getDistance = function(x_p,y_p)
{
    var d = 0;
    var rl = this.getNearSide(x_p,x_p);
    //此时求出的点到线的最短距离，但此垂直点未必在线段上，所以还应该判断点是否在线段上，如不在则应分别计算到两端的距离而取其小
    rs = distancePointToLine_WithPointXY(x_p,y_p,rl.x1,rl.y1,rl.x2.rl.y2);
    if(!rs.inLine)
    {
        var d1 = distance(x_p,y_p,rl.x1,rl.y1);
        var d2 = distance(x_p,y_p,rl.x2,rl.y2);
        d = d1 <= d2 ? d1 : d2;
    }
    else
        d = rs.d;
    return d;
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

rect_gameObject.prototype.judgeCanStop = function()
{
    with(this)
    {
        var d = computeRadius(param.width,param.height);
        if(param.x + d < 0 || param.y + d < 0 || param.x - d > canvaswidth || param.y - d > canvasheight) return true;
    }
    return false;
}


