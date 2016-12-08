print("JXPi/NodeMCU Started")

--
-- Created by IntelliJ IDEA.
-- User: andrew
-- Date: 16-2-4
-- Time: 下午7:30
-- To change this template use File | Settings | File Templates.
--
-----------------------------------------------------------------------------
-- 配置信息
-----------------------------------------------------------------------------
local server
local ssid
local ssidPwd
local myName
--local server = "172.18.18.1"-
--local ssidPwd="12345678"
--local myName="nodeMCU1"

local port = 10008
--local data={}

--local devType = "NodeMCU"

--local myVersion="1.0.0"
--local myIP
--local myMAC

--发送数据
local client

local function getPkg(cmd,mid)
    local pkg={}
    pkg.c=cmd
    pkg.mid=mid
    pkg.n=myName

    return pkg
end
local function sendPkg(pkg)

    local str=cjson.encode(pkg).."\n"

    --print("send:"..str)
    client:send(str)

end

local function getConfigFromFile(filename)
    if file.open(filename,"r") then
        local line=file.readline()
        --print(line)
        file.close()
        return string.sub(line,1,-2)
    end
    return nil
end

local function sentOK(mid)

    local pkg=getPkg("r",mid)
    pkg.m="OK"
    --pkg.msg="OK"
    sendPkg(pkg)
end

local function reportTo(pin,state)
    local rp=getPkg("i",0)
    rp.t="g"
    rp.p=pin
    rp.d=state
    sendPkg(rp)
end
--写入config
--启动时读入覆盖掉server等配置信息，以及端口设置，如果是读入端口，则还需设置是否监视、监视周期、报告门限等
local i2c_data={}
local timer_User=1
local function i2cRead(dev_addr,reg_addr,len)
    local s
    i2c.start(0)
    i2c.address(0,dev_addr,i2c.TRANSMITTER)
    i2c.write(0, reg_addr)
    i2c.stop(0)
    i2c.start(0)
    i2c.address(0,dev_addr,i2c.RECEIVER)
    s=i2c.read(0, len)
    i2c.stop(0)
    if len==1 then
        return string.byte(s)
    end
    local t={}
    for i=1,len do
        t[i]=string.byte(s,i)
    end
    return t
end
local function config(pkg)
    if pkg.t=="g" then
        if pkg.m==1 then
            gpio.mode(pkg.p,gpio.OUTPUT)
        elseif pkg.m==0 then
            if pkg.f==0 then
                gpio.mode(pkg.p,gpio.INPUT,gpio.FLOAT)
            else
                gpio.mode(pkg.p,gpio.INPUT,gpio.PULLUP)
            end
        elseif pkg.m==2 then
            pwm.setup(pkg.p,pkg.o,pkg.d)
        elseif pkg.m==3 then
            gpio.mode(pkg.p,gpio.INT,gpio.PULLUP)
            gpio.trig(pkg.p, "both",function(state)
                reportTo(pkg.p,state)
            end)
        end
    elseif pkg.t=="i" then
        if pkg.m==1 then
            i2c.setup(0,pkg.d,pkg.s,i2c.SLOW)
        elseif pkg.m==0 then
            for itr=2,timer_User
            do
                tmr.stop(itr)
                tmr.unregister(itr)
            end
            timer_User=1
        elseif pkg.m==2 then
            timer_User=timer_User+1
            tmr.alarm(timer_User, pkg.l, 1, function()
                local i2cd=i2cRead(pkg.a,pkg.p,pkg.n)
                reportTo(pkg.a.."_"..pkg.p,i2cd)
            end)
        end
    end

    sentOK(pkg.mid)
end

--保活
local serverdieNum=0

--执行命令，一般为输出端口动作
local function set(pkg)
    if pkg.t=="g" then
        local active=gpio.HIGH
        if pkg.a==0 then active=gpio.LOW end
        gpio.write(pkg.p,active)
    elseif pkg.t=="p" then
        pwm.setduty(pkg.p, pkg.d)
    elseif pkg.t=="ps" then
        pwm.start(pkg.p)
    elseif pkg.t=="po" then
        pwm.stop(pkg.p)
    elseif pkg.t=="a" then
        pwm.setduty(pkg.p,(0.5+(90+pkg.a)/90)/20*1023)
    end

    sentOK(pkg.mid)
end
local function get(pkg)
    local rp=getPkg("r",pkg.mid)
    if pkg.t=="g" then
        --rp.type="gpio"
        rp.t="g"
        rp.p=pkg.p
        rp.d=gpio.read(pkg.p)
    elseif pkg.t=="i" then
        rp.t="i"
        rp.a=pkg.a
        rp.p=pkg.p
        rp.d=i2cRead(rp.a,rp.p,1)
    elseif pkg.t=="a" then
        rp.t="a"
        rp.p=pkg.p
        _, rp.te, rp.hu, rp.ted, rp.hud = dht.read11(pkg.p)
    end
    sendPkg(rp)
end

local function connectServer()

    client=net.createConnection(net.TCP, 0)

    client:on("receive", function(sck, response)
        --print("r "..response)

        local pkg=cjson.decode(response)
        local c=pkg.c
        if not pkg then return end
        if(c=="l") then
            --保活
            serverdieNum=0
            --sentOK(pkg.mid)
        elseif(c=="s") then
            set(pkg)
        elseif(c=="g") then
            get(pkg)
        elseif(c=="c") then
            config(pkg,true)
        elseif(c=="reg") then
            local pkg=getPkg("reg")
            pkg.ID=node.chipid()
            --pkg.t=devType
            --pkg.IP=myIP
            --pkg.v=myVersion
            --pkg.devMAC=myMAC
            sendPkg(pkg)
            serverdieNum=0
            --elseif(c=="rs") then
            --    sentOK()
            --    node.restart()
        end
    end)

    client:connect(port, server)

end

local function init()
    --print("init")

    wifi.setmode(wifi.STATION)
    ssid = getConfigFromFile("ssid")
    print("ssid:"..ssid)
    server = getConfigFromFile("sip")
    print("sip:"..server)
    ssidPwd = getConfigFromFile("pwd")
    print("pwd:"..ssidPwd)
    myName = getConfigFromFile("dn")
    --if not ssid then return end
    wifi.sta.config(ssid,ssidPwd)   --路由器登陆名称和密码
    wifi.sta.connect()

    connectServer()

    tmr.alarm(0, 10000, 1, function()
        --print("tmr c")
        serverdieNum=serverdieNum+1
        if serverdieNum>10 then
                --print("CS:"..server.."/"..port)
            connectServer()
        end
    end)

end

init()

