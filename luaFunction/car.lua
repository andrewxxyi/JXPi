
require "cn.ijingxi.intelControl.jxLua"
require "cn.ijingxi.pi.wheel"
require "cn.ijingxi.pi.gpio"

--
function drive(paramTable)
    for k, v in pairs(paramTable) do 
        jxLua.log(k..":"..v) 
    end  
	wheel.speed(paramTable["Speed"])
	wheel.turnAround(paramTable["Radian"])
	return true
end

function stop(paramTable)
	wheel.stop()
	return true
end

--测试函数，在系统log文件中可以看到本函数的执行情况
function test(paramTable)
    jxLua.log("test函数开始执行") 
    for k, v in pairs(paramTable) do 
        jxLua.log("送入的参数："..k..":"..v) 
    end  
    jxLua.log("test函数执行完毕") 
	--通过REST接口'/jxLua/call'调用的lua函数一定需要返回一个值以指示函数执行的结果
	return true
end

valueName_LeftStopwatch="LeftStopwatch"
function getStopwatchValue(paramTable)
    --通过REST接口'/jxLua/call'调用的lua函数一定需要返回一个值以指示函数执行的结果
    return jxLua.getValue(valueName_LeftStopwatch)
end

--左码表，后台只负责计数，前端取得数据后自行计算转速
pinLeftStopwatch=12

function init()
    gpio.setInputMode(pinLeftStopwatch,gpio.PULL_UP)
    --gpio.setInputMode(pinRightStopwatch,gpio.PULL_UP)
    gpio.setInputEvent(pinLeftStopwatch, function(state)
        if (state==gpio.HIGH) then
            --取值，由于此处的值处理和前端读取值虽然是在同一个lua脚本中，但却是来自不同的线程，
            --系统为避免复杂处理，并未将同一个脚本的不同调用进行合一，也就是说不同的调用就是不同
            --的上下文，所以此处对值的处理在getStopwatchValue是看不到的，所以系统提供了一个简单
            --的值存储接口，用来在不同线程间共享值数据
            v=jxLua.getValue(valueName_LeftStopwatch)
            if (v) then
                v=v+1
            else
                v=1
            end
            jxLua.setValue(valueName_LeftStopwatch,v)
        end
    end)
end

function close()
    --清除值
    jxLua.clearValue(valueName_LeftStopwatch)
end
