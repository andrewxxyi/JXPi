import json
import sys
import os
import traceback
import picamera
from subprocess import Popen, PIPE
import wiringpi as gpio
from wiringpi import GPIO

class JX:
	def __init__(self):
		self.eventID=0
		gpio.wiringPiSetup()
		pass


	def sendEvent(self,event,msg,value):
		self.eventID=self.eventID+1
		jsonrs={"cmd":"event","resFor":event,"data":value,"msg":msg,"msgid":self.eventID}
		self.sendResponse_json(jsonrs)

	def startCamera_nc(self,paramJson):
		self.p = Popen(["/bin/nc","-l",str(paramJson["port"])], stdin=PIPE)
		self.camera=picamera.PiCamera()
		self.camera.start_recording(self.p.stdin,"h264")
		self.camera.wait_recording(0)
		self.sendResponse("startCamera_nc",paramJson["msgid"],"OK")
		return True

	def startCamera_pipe(self,paramJson):
		path="/tmp/"+paramJson["pipefile"]
		os.mkfifo(path)
		self.wp=open(path, 'w')
		self.camera=picamera.PiCamera()
		self.camera.start_recording(self.wp,"h264")
		self.camera.wait_recording(0)
		self.sendResponse("startCamera_pipe",paramJson["msgid"],"OK")
		return True

	def stopCamera(self,paramJson):
		self.camera.stop_recording()
		self.camera.close()
		if self.p:
			self.p.kill()
			self.p=None
		else:
			os.close(self.wp)
			self.wp=None
		self.sendResponse("stopCamera",paramJson["msgid"],"OK")
		return True

	def getResponse_json(self,cmd,msgid,msg):
		jsonrs={"cmd":"res","resFor":cmd,"msg":msg,"msgid":msgid}
		return jsonrs

	def sendResponse_json(self,jsonrs):
		str=json.dumps(jsonrs)
		sys.stdout.write(str+"\n")
		sys.stdout.flush()

	def sendResponse(self,cmd,msgid,msg):
		jsonrs={"cmd":"res","resFor":cmd,"msg":msg,"msgid":msgid}
		self.sendResponse_json(jsonrs)
	
	def openPipeWrite(self,paramJson):
		path="/tmp/"+paramJson["path"]
		os.mkfifo(path)
		self.wp=open(path, 'w')
		self.sendResponse("openPipeWrite",paramJson["msgid"],"OK")
		return True
	
	def openPipeRead(self,paramJson):
		path="/tmp/"+paramJson["path"]
		os.mkfifo(path)
		self.rp=open(path, 'r')
		self.sendResponse("openPipeRead",paramJson["msgid"],"OK")
		return True

	def close(self,paramJson):
		if hasattr(self,'camera'):
			self.camera.stop_recording()
			self.p.kill()
			self.camera.close()
		if hasattr(self,'wp'):
			self.wp.close()
		if hasattr(self,'rp'):
			self.rp.close()
		self.sendResponse("close",paramJson["msgid"],"OK")
		return False

	def default(self,paramJson):
		fun = getattr(self,paramJson["cmd"])
		fun(paramJson)
		return True

	def doCmd(self,cmd,paramJson):
		switch={
			"startCamera_pipe":lambda:self.startCamera_pipe(paramJson),
			"startCamera_nc":lambda:self.startCamera_nc(paramJson),
			"stopCamera":lambda:self.stopCamera(paramJson),
			"close":lambda:self.close(paramJson)
		}
		return switch.get(cmd,lambda:self.default(paramJson))()


if __name__=='__main__':
	jx=JX()
	run=True
	while run:
		bytesread = raw_input()
		if bytesread:
			try:
				decodejson=json.loads(bytesread);
				if decodejson:
					run=jx.doCmd(decodejson["cmd"],decodejson)
			except:
				traceback.print_exc(sys.stderr)  
    			sys.stderr.flush()

