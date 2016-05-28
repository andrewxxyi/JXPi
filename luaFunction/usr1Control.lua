
require "cn.ijingxi.intelControl.jxLua"
require "cn.ijingxi.communication.USR88.luaUSR"

--对USR1设备的输入进行控制
function init()
	--在脚本中可通过log、warn、error在系统log文件（./logs目录下）中写入相应的log信息
	jxLua.log("usr1Control.lua init start")
	--USR1设备的1号输入端口上升沿触发时，关闭1号输出端口
	jxLua.setInputEvent("USR1",1,jxLua.RISING,function()
			luaUSR.closeChannel("USR1",1)
		end)
	--USR1设备的2号输入端口上升沿触发时，打开1号输出端口
	jxLua.setInputEvent("USR1",2,jxLua.RISING,function()
			luaUSR.openChannel("USR1",1)
		end)
end

function close()

end
