
require "cn.ijingxi.communication.USR232.luaUSR232"

function exec(devname,cmd)
	print(devname,cmd)
	local rs=luaUSR232.send(devname,cmd)
	--print(rs)
	return
end
