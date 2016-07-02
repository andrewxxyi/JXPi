
require "cn.ijingxi.intelControl.jxLua"
require "cn.ijingxi.intelControl.luaStateMachine"
require "cn.ijingxi.pi.gpio"

--状态机的名字，应当给状态机一个不会重复的名字
smName="smtest"
--状态机的工作状态，状态机的状态一般情况下很难立刻定义出来，所以一般都是先给出状态量，然后定义一个初始情况，然后从
--这个初始情况出发，逐个改变状态量，然后看在实际情况中是否会出现改变后的状态量取值的组合，如果存在可能就先确定为一个
--新的状态，这样先将所有可能的状态全部画出来，然后再进行合并和删去，这个过程需要进行多次的反复，图上作业无误后上机测试就简单了
sm_State_Stop="stop"
sm_State_Prepare="prepare"
sm_State_Work="work"
--状态机的事件，本演示中有两个状态变量：SW1和SW2，而这两个开关每个又都有2个状态（开和关），所以需要定义四个状态变量
sm_Event_SW1Open="sw1open"
sm_Event_SW1Close="sw1close"
sm_Event_SW2Open="sw2open"
sm_Event_SW2Close="sw2close"
--管脚号，wiringpi定义
pinLed=23
pinSW1=24
pinSW2=25

--状态机的定义一般不会通过REST由外部调用，一般是以系统虚拟设备的方式通过./conf/front目录下的配置脚本来
--启动，以这种方案启动的lua脚本其入口是init函数，./conf目录下的是配置脚本，其入口是exec，返回一个table，
--供系统进行相应的配置，而虚拟设备是需要执行的初始化脚本，其入口是init，需要注意其间的区别
function init()
	--在脚本中可通过log、warn、error在系统log文件（./logs目录下）中写入相应的log信息
	jxLua.log("smsample.lua init start")
	--输入端口设置为上拉电阻。此处的输入开关是串入gpio口和GND之间来组成输入电路，此种情况下应配置上拉电阻；
	--反之，当输入电路是由Vcc和gpio口来组成时，则应配置下拉电阻：PULL_DOWN
	gpio.setInputMode(pinSW1,gpio.PULL_UP)
	gpio.setInputMode(pinSW2,gpio.PULL_UP)

	--定义状态机，请参考《JXPi平台简明参考手册》中附件三的说明
	luaStateMachine.addTrans(smName,sm_State_Stop,sm_Event_SW1Close,sm_State_Prepare)
	luaStateMachine.addTrans(smName,sm_State_Prepare,sm_Event_SW2Close,sm_State_Work, function()
			gpio. write(pinLed, gpio.HIGH)
		end)
	luaStateMachine.addTrans(smName,sm_State_Prepare,sm_Event_SW1Open,sm_State_Stop, function()
			gpio. write(pinLed, gpio.LOW)
		end)
	luaStateMachine.addTrans(smName,sm_State_Work,sm_Event_SW2Open,sm_State_Prepare, function()
			gpio. write(pinLed, gpio.LOW)
		end)
	luaStateMachine.addTrans(smName,sm_State_Work,sm_Event_SW1Open,sm_State_Stop, function()
			gpio. write(pinLed, gpio.LOW)
		end)
	--设置状态机初始状态
	luaStateMachine.setInitState(smName,sm_State_Stop)

	--配置输入事件
	gpio.setInputEvent(pinSW1, function(state)
		jxLua.log(pinSW1.." state:"..state)
		--输入事件的回调函数会送回事件触发时的状态，也就是该gpio口是处于高电平（1）还是低电平（0）
		--由于该gpio口被配置为使用gpio和GND组成输入电路，所以如果state为1则开关处于断路状态，为0
		--则开关处于和GND导通的状态，也就是接通的状态，所以在0时应送出开关关闭的事件
		if(state==gpio.LOW)
		then
			luaStateMachine.happen(smName,sm_Event_SW1Close)
		else
			luaStateMachine.happen(smName,sm_Event_SW1Open)
		end
	end)

	gpio.setInputEvent(pinSW2, function(state)
		jxLua.log(pinSW2.." state:"..state)
		if(state==gpio.LOW)
		then
			luaStateMachine.happen(smName,sm_Event_SW2Close)
		else
			luaStateMachine.happen(smName,sm_Event_SW2Open)
		end
	end)
end

function andriodClick(paramTable)
	jxLua.log("paramTable:"..paramTable)
	if(paramTable.State)
	then
		luaStateMachine.happen(smName,sm_Event_SW2Close)
	then
		luaStateMachine.happen(smName,sm_Event_SW2Open)
	end
	luaStateMachine.clear(smName)
end

function close()
	luaStateMachine.clear(smName)
end
