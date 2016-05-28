print("JXPi/NodeMCU Started")
local e=0
if file.open("watch","r")then
local t=file.readline()
while t do
e=e+1
if e>3 then
file.close()
print("remove jx")
file.remove("watch")
file.remove("jx.lc")
file.remove("jx.lua")
t=nil
else
t=file.readline()
end
end
file.close()
end
file.open("watch","a+")
file.writeline("watch")
file.close()
if file.open("jx.lc")then
file.close()
tmr.alarm(0,5e3,0,function()
file.remove("watch")
print("do jx")
dofile("jx.lc")
end)
else
tmr.alarm(0,5e3,0,function()
if file.open("jx.lua")then
file.close()
print("Compile:jx.lua")
file.remove("jx.lc")
node.compile("jx.lua")
if file.open("jx.lc")then
file.close()
print("jx.lua compile ok")
node.restart()
else
print("jx.lua compile error")
end
else
print("jx.lua not exist")
end
end)
end
