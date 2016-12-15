print("JXPi/NodeMCU Started")
local d
local h
local r
local m
local f=10008
local n
local function i(a,t)
local e={}
e.c=a
e.mid=t
e.n=m
return e
end
local function o(e)
local e=cjson.encode(e).."\n"
n:send(e)
end
local function s(e)
if file.open(e,"r")then
local e=file.readline()
file.close()
return string.sub(e,1,-2)
end
return nil
end
local function c(e)
local e=i("r",e)
e.m="OK"
o(e)
end
local function u(t,a)
local e=i("i",0)
e.t="g"
e.p=t
e.d=a
o(e)
end
local e={}
local t=1
local function l(a,o,t)
local e
i2c.start(0)
i2c.address(0,a,i2c.TRANSMITTER)
i2c.write(0,o)
i2c.stop(0)
i2c.start(0)
i2c.address(0,a,i2c.RECEIVER)
e=i2c.read(0,t)
i2c.stop(0)
if t==1 then
return string.byte(e)
end
local a={}
for t=1,t do
a[t]=string.byte(e,t)
end
return a
end
local function y(e)
if e.t=="g"then
if e.m==1 then
gpio.mode(e.p,gpio.OUTPUT)
elseif e.m==0 then
if e.f==0 then
gpio.mode(e.p,gpio.INPUT,gpio.FLOAT)
else
gpio.mode(e.p,gpio.INPUT,gpio.PULLUP)
end
elseif e.m==2 then
pwm.setup(e.p,e.o,e.d)
elseif e.m==3 then
gpio.mode(e.p,gpio.INT,gpio.PULLUP)
gpio.trig(e.p,"both",function(t)
u(e.p,t)
end)
end
elseif e.t=="i"then
if e.m==1 then
i2c.setup(0,e.d,e.s,i2c.SLOW)
elseif e.m==0 then
for e=2,t
do
tmr.stop(e)
tmr.unregister(e)
end
t=1
elseif e.m==2 then
t=t+1
tmr.alarm(t,e.l,1,function()
local t=l(e.a,e.p,e.n)
u(e.a.."_"..e.p,t)
end)
end
end
c(e.mid)
end
local a=0
local function w(e)
if e.t=="g"then
local t=gpio.HIGH
if e.a==0 then t=gpio.LOW end
gpio.write(e.p,t)
elseif e.t=="p"then
pwm.setduty(e.p,e.d)
elseif e.t=="ps"then
pwm.start(e.p)
elseif e.t=="po"then
pwm.stop(e.p)
elseif e.t=="a"then
pwm.setduty(e.p,(.5+(90+e.a)/90)/20*1023)
end
c(e.mid)
end
local function u(t)
local e=i("r",t.mid)
if t.t=="g"then
e.t="g"
e.p=t.p
e.d=gpio.read(t.p)
elseif t.t=="i"then
e.t="i"
e.a=t.a
e.p=t.p
e.d=l(e.a,e.p,1)
elseif t.t=="a"then
e.t="a"
e.p=t.p
_,e.te,e.hu,e.ted,e.hud=dht.read11(t.p)
end
o(e)
end
local function l()
n=net.createConnection(net.TCP,0)
n:on("receive",function(t,e)
local e=cjson.decode(e)
local t=e.c
if not e then return end
if(t=="l")then
a=0
elseif(t=="s")then
w(e)
elseif(t=="g")then
u(e)
elseif(t=="c")then
y(e,true)
elseif(t=="reg")then
local e=i("reg")
e.ID=node.chipid()
o(e)
a=0
end
end)
n:connect(f,d)
end
local function e()
wifi.setmode(wifi.STATION)
h=s("ssid")
print("ssid:"..h)
d=s("sip")
print("sip:"..d)
r=s("pwd")
print("pwd:"..r)
m=s("dn")
wifi.sta.config(h,r)
wifi.sta.connect()
l()
tmr.alarm(0,1e4,1,function()
a=a+1
if a>10 then
l()
end
end)
end
e()
