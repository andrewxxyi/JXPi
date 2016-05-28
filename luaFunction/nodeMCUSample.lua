
require "cn.ijingxi.intelControl.jxLua"
require "cn.ijingxi.intelControl.luaStateMachine"
require "cn.ijingxi.communication.NodeMCU.luaNodeMCU"
require "cn.ijingxi.pi.gpio"

--状态机的名字，应当给状态机一个不会重复的名字
smName="nodeMCUSampleSM"
--状态机的工作状态，状态机的状态一般情况下很难立刻定义出来，所以一般都是先给出状态量，然后定义一个初始情况，然后从
--这个初始情况出发，逐个改变状态量，然后看在实际情况中是否会出现改变后的状态量取值的组合，如果存在可能就先确定为一个
--新的状态，这样先将所有可能的状态全部画出来，然后再进行合并和删去，这个过程需要进行多次的反复，图上作业无误后上机测试就简单了
sm_State_init="init"
sm_State_m1Start="m1Start"
sm_State_m2OK="m2OK"
sm_State_m3OK="m3OK"
sm_State_m1Stop="m1Stop"
sm_State_Warning="Warning"
--状态机的事件，本演示中有5个状态变量：m1、m2、m3、SW1、定时器，由于没有重复使用的定时器，而且定时器的时间也设定为一样的，所以定时器事件只需要一个，因此一共需9个事件，
--不过，看一下状态跃迁图其实就能看到，SW1按下后的松开事件并不需要关注，其实真正需要考虑的事件只有8个
sm_Event_m1Open="m1open"
sm_Event_m1Close="m1open"
sm_Event_m2Open="m2open"
sm_Event_m2Close="m2open"
sm_Event_m3Open="m3open"
sm_Event_m3Close="m3open"
sm_Event_SW1Open="sw1open"
sm_Event_SW1Close="sw1close"
sm_Event_Timeout="timeout"

--定时器名
timerName="nodeMCUSample_timer1"

--NodeMCU设备名
devName1="NodeMCU1"
devName2="NodeMCU2"

--管脚号
--m1、led1在NodeMCU1上
pinM1=1
pinLED1=2
--m2、m3在NodeMCU2上
pinM2=1
pinM3=2
--sw1、led2和告警灯都在树莓派上，wiringpi定义
pinSW1=25
pinLED2=23
pinLEDRed=24

--初始化函数，必须定义，系统自动调用
function init()
	--在脚本中可通过log、warn、error在系统log文件（./logs目录下）中写入相应的log信息
	jxLua.log("nodeMCUSample.lua init start")
	--nodeMCU的设备初始化
	--向系统注册一个该设备的初始化回调函数，这样设备只要注册进来。系统就会执行该函数来对本设备进行初始化
	luaNodeMCU.registerInitDual(devName1, function()
		jxLua.log("init:"..devName1)
		--如果是直接读，则需设置输入模块，而pinM1是本登记为输入事件触发，则可忽略该命令
		--luaNodeMCU.setInputMode(devName1,pinM1,luaNodeMCU.PULLUP)
		luaNodeMCU.setOutputMode(devName1,pinLED1)
		--注册pinM1端口的输入事件触发函数，事件为双边沿触发，即只要输入状态发生变化就会将变化后的状态送回
		luaNodeMCU.registerInputEvent(devName1,pinM1, function(state)
			--本函数的执行是回调函数注册的回调函数中执行，和init函数的启动线程是完全不同的线程
			jxLua.log("InputEvent:"..devName1..pinM1..state)
			if(state==luaNodeMCU.HIGH) then
				luaStateMachine.happen(smName,sm_Event_m1Open)
			else
				luaStateMachine.happen(smName,sm_Event_m1Close)
			end
		end)
	end)
	luaNodeMCU.registerInitDual(devName2, function()
		jxLua.log("init:"..devName2)
		--输入端口是和GND组成的输入电路
		luaNodeMCU.setInputMode(devName2,pinM2,luaNodeMCU.PULLUP)
		luaNodeMCU.setInputMode(devName2,pinM3,luaNodeMCU.PULLUP)
		luaNodeMCU.registerInputEvent(devName2,pinM2, function(state)
			jxLua.log("InputEvent:"..devName2..pinM2..state)
			if(state==luaNodeMCU.HIGH) then
				luaStateMachine.happen(smName,sm_Event_m2Open)
			else
				luaStateMachine.happen(smName,sm_Event_m2Close)
			end
		end)
		luaNodeMCU.registerInputEvent(devName2,pinM3, function(state)
			jxLua.log("InputEvent:"..devName2..pinM3..state)
			if(state==luaNodeMCU.HIGH) then
				luaStateMachine.happen(smName,sm_Event_m3Open)
			else
				luaStateMachine.happen(smName,sm_Event_m3Close)
			end
		end)
	end)

	--输入端口设置为上拉电阻。此处的输入开关是串入gpio口和GND之间来组成输入电路，此种情况下应配置上拉电阻；
	--反之，当输入电路是由Vcc和gpio口来组成时，则应配置下拉电阻：PULL_DOWN
	gpio.setInputMode(pinSW1,gpio.PULL_UP)

	--定义状态机，请参考《JXPi平台简明参考手册》中$6.4和附件三的说明。就是根据状态跃迁图将其中的每个跃迁一一定义出来即可
	luaStateMachine.addTrans(smName,sm_State_init,sm_Event_m1Close,sm_State_m1Start, function()
		jxLua.log("m1Close event")
		--定义一个3秒的定时器，超时将执行指定的回调函数来触发超时事件，最后一个参数是指明不重复执行，即只执行一次
			jxLua.setTimer(timerName,3000,function()
				jxLua.log("Timer timeout")
				luaStateMachine.happen(smName,sm_Event_Timeout)
			end, false)
		end)
	luaStateMachine.addTrans(smName,sm_State_init,sm_Event_m2Close,sm_State_Warning, function()
		jxLua.log("set pinLEDRed HIGH")
		gpio.write(pinLEDRed, gpio.HIGH)
	end)
	luaStateMachine.addTrans(smName,sm_State_init,sm_Event_m3Close,sm_State_Warning, function()
		jxLua.log("set pinLEDRed HIGH")
		gpio.write(pinLEDRed, gpio.HIGH)
	end)
	luaStateMachine.addTrans(smName,sm_State_m1Start,sm_Event_m2Close,sm_State_m2OK, function()
		jxLua.log("m2Close event")
		jxLua.clearTimer(timerName)
		luaNodeMCU.write(devName1,pinLED1,luaNodeMCU.HIGH)
	end)
	luaStateMachine.addTrans(smName,sm_State_m1Start,sm_Event_m3Close,sm_State_m3OK, function()
		jxLua.log("m3Close event")
		jxLua.clearTimer(timerName)
		gpio.write(pinLED2, gpio.HIGH)
	end)
	luaStateMachine.addTrans(smName,sm_State_m1Start,sm_Event_Timeout,sm_State_Warning, function()
		jxLua.log("set pinLEDRed HIGH")
		gpio.write(pinLEDRed, gpio.HIGH)
	end)
	luaStateMachine.addTrans(smName,sm_State_m2OK,sm_Event_m2Open,sm_State_m1Stop, function()
		jxLua.log("m2Open event")
		jxLua.setTimer(timerName,3000,function()
			jxLua.log("Timer timeout")
			luaStateMachine.happen(smName,sm_Event_Timeout)
		end, false)
		luaNodeMCU.write(devName1,pinLED1,luaNodeMCU.LOW)
	end)
	luaStateMachine.addTrans(smName,sm_State_m3OK,sm_Event_m3Open,sm_State_m1Stop, function()
		jxLua.log("m3Open event")
		jxLua.setTimer(timerName,3000,function()
			jxLua.log("Timer timeout")
			luaStateMachine.happen(smName,sm_Event_Timeout)
		end, false)
		gpio.write(pinLED2, gpio.LOW)
	end)
	luaStateMachine.addTrans(smName,sm_State_m1Stop,sm_Event_Timeout,sm_State_Warning, function()
		jxLua.log("set pinLEDRed HIGH")
		gpio.write(pinLEDRed, gpio.HIGH)
	end)
	luaStateMachine.addTrans(smName,sm_State_m1Stop,sm_Event_m1Close,sm_State_init, function()
		jxLua.log("m1Close event")
		jxLua.clearTimer(timerName)
	end)
	luaStateMachine.addTrans(smName,sm_State_Warning,sm_Event_SW1Close,sm_State_init, function()
		jxLua.log("set pinLEDRed LOW")
		gpio.write(pinLEDRed, gpio.LOW)
	end)

	--设置状态机初始状态，如果不设置初始状态，则状态机不会工作
	luaStateMachine.setInitState(smName,sm_State_init)

	--配置输入事件
	gpio.setInputEvent(pinSW1, function(state)
		jxLua.log(pinSW1.." state:"..state)
		--输入事件的回调函数会送回事件触发时的状态，也就是该gpio口是处于高电平（1）还是低电平（0）
		--由于该gpio口被配置为使用gpio和GND组成输入电路，所以如果state为1则开关处于断路状态，为0
		--则开关处于和GND导通的状态，也就是接通的状态，所以在0时应送出开关关闭的事件
		if(state==gpio.LOW)
		then
			luaStateMachine.happen(smName,sm_Event_SW1Close)
		end
	end)

end

function close()
	jxLua.log("nodeMCUSample.lua close")
	luaStateMachine.clear(smName)
	--不管是否在工作都清理一下定时器，以免形成干扰
	jxLua.clearTimer(timerName)
end
