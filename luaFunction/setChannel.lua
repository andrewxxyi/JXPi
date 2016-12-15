
require "cn.ijingxi.intelControl.jxLua"
require "cn.ijingxi.communication.USR88.luaUSR"

--本示例是系统直接调用了本脚本，所以参数可以确定化，如果通过/jxLua/call来调用则送入的是一个table的参数
--这是一个实际工作非常可靠，承担了多种控制功能的现场控制脚本
--所以未加改造的演示给使用者，以帮助使用者，尤其是初学者能更好的理解如何进行现场控制
function exec(devname,cmd,channel,lockname,inverserchannelid,delaysecond)
	--检查是否加锁，这个锁其实是一个虚拟设备，系统提供了加锁、解锁和检查锁等操作，这样就可以实现诸如
	--一键锁定功能：学生可对自己操作台上的小设备进行操控，但老师在讲解时，可以一键锁住，整个教室
	--所有学生就都不能再进行任何操作了，而这一功能在JXPi平台上不需要任何电路方面的设计就可以实现
	if lockname then
		--使用者可在某个输入的响应事件中或是在web前端通过REST接口直接调用脚本的方式：
		--使用jxLua.lock(lockname)来进行锁定以及jxLua.unlock(lockname)来进行解锁
		if jxLua.checkLock(lockname) then
			return "已加锁："..lockname
		end
	end
	
	--这是由于现在很多受控设备其实不是智能前端，只是一个提供了开关按钮的呆设备，为了确保操作意图的
	--准确实现，这里是首先将反向的（如准备开，则必须首先切断关）控制取消，否则就会出现开关同时给出信号
	--的情况，呆设备肯定会彻底呆住的
	if inverserchannelid and inverserchannelid>0 then
		--先反向操作
		if cmd=="Close" then
			luaUSR.openChannel(devname,inverserchannelid)
		else
			luaUSR.closeChannel(devname,inverserchannelid)
		end
	end
	--真正的操作
	if cmd=="Close" then
		luaUSR.closeChannel(devname,channel)
	else
		luaUSR.openChannel(devname,channel)
	end
	--如果呆设备还有现场的手动控制开关，则上面的操作就会阻止现场的手动控制，所以需要给予复位
	--需要注意的是：这里的示例是笔者所遇到的控制情况，使用方需根据自己的实际情况考虑如何复位
	if inverserchannelid and inverserchannelid>0 and delaysecond and delaysecond>0 then
		--延时一个指定的秒数之后再进行反向的复位动作，笔者所遇到的情况是要控制直流电机来升降吊杆，所以需要
		--根据整个吊杆的升降时间来控制复位的时间
		--需要注意的是，虽然delay是一个同步的函数，但lua控制脚本的调用在JXPi平台中却是可以异步的，所以
		--如果用户在短时间频繁点击就会造成多个lua控制脚本同时运行的情况，这样最终的状态是不可预知的，因此
		--使用者必须仔细定义自己的控制逻辑，同时不可以单凭自己想象系统现在应该是如何如何的，这是不可靠的！！
		--为了规避此种情况，在一开始，JXPi平台提供了同步的jxREST接口，而且还将系统对lua脚本的调用也使用
		--了同步的方式，结果造成客户前端锁死，必须等到delay结束之后用户的前端才恢复响应，结果用户的感觉又
		--不是很好，而且如果有多个用户同时操作，这种方案也仍然无法确保lua脚本的串行化调用，而如果在后台
		--来实现lua脚本的串行化执行无疑是过于复杂了。而且更严重的是：如果所有的控制都带有延迟，那么多个
		--用户同时操作时，串行化会导致某些用户没有反应，用户如果再一阵狂点（本能反应），那么，后果将是系统
		--可能在非常长（笔者的控制场景是电机运行单程在50多秒）的一个时间内自娱自乐，不理会任何的外部控制意图
		--所以，不管前端采用同步还是异步的方式来请求lua脚本的执行，系统都是以异步的方式进行lua脚本的调用
		--用户针对多用户的情况，可以利用前述的锁机制，也可以利用JXPi平台提供的状态机能力进行状态控制
		--复杂环境，多方参与的控制，建议一定应采用状态机的方案。JXPi平台的状态机非常稳定、可靠，笔者
		--认为要远比通过复杂的现场电路来进行控制要更为稳定、可靠，当然也会更为节约:)
		--状态机的示例请参考smsample.lua，笔者强烈建议使用者应当非常熟练的掌握状态机的编写
		jxLua.delay(delaysecond)
		--再次反向操作
		if cmd=="Close" then
			luaUSR.openChannel(devname,inverserchannelid)
		else
			luaUSR.closeChannel(devname,inverserchannelid)
		end
	end
end

