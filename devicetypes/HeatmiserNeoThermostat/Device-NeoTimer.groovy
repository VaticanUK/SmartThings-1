/**
 *  Neo Timer
 *
 *  Copyright 2017 Richard Pope (VaticanUK)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Neo Timer", namespace: "VaticanUK", author: "Richard Pope") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
        capability "Sensor"
        capability "Switch"
		capability "Relay Switch"
        capability "Temperature Measurement"
        
        command "refresh"
        command "refreshinfo"
        command "deRefresh"
        
        command "increaseDuration"
		command "decreaseDuration"
        command "away"
        command "awayOff"
        command "on"
        command "off"
        command "setHoldOff"
        
        attribute "holdtime", "string"
        attribute "statusText", "string"
        attribute "switchStatus", "string"
        attribute "nextSetpointText", "string"
        attribute "away", "string"
        attribute "temperature","number" // Temperature Measurement
	}

	tiles(scale:2) {
		multiAttributeTile(name:"switch", type:"lighting", width: 6, height: 4, canChangeIcon: false){
        	tileAttribute ("device.switchStatus", key: "PRIMARY_CONTROL") {
            	attributeState "on", label: 'on', action: "off", icon: "st.thermostat.heating", backgroundColor: "#79b821"
                attributeState "off", label: 'off', action: "on", icon: "st.thermostat.heating-cooling-off", backgroundColor: '#ffffff'
                attributeState "holdOn", label: 'on (hold)', action: "off", icon: "st.thermostat.heating", backgroundColor: "#79b821"
                attributeState "holdOff", label: 'off (hold)', action: "on", icon: "st.thermostat.heating-cooling-off", backgroundColor: '#ffffff'
            }
            tileAttribute ("device.holdtime", key: "SECONDARY_CONTROL") {
            	attributeState "holdtime", label: 'Hold time: ${currentValue}', icon: "st.Office.office6"
            }
        }  
        valueTile("HoldTitle", "device.HoldTitle", width: 2, height: 1) {
        	state "default", label:'Hold:'
        }
        standardTile("increaseTime", "device.increaseTime", width: 1, height: 1, decoration: "flat") {
			state "default", action:"increaseDuration", label:'+30m'
		}
        standardTile("decreaseTime", "device.decreaseTime", width: 1, height: 1, decoration: "flat") {
			state "default", action:"decreaseDuration", label:'-30m'
		}
		standardTile("holdtime", "device.holdtime", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", action:"setHoldOff", label:'Cancel'
		}

        valueTile("nextSetpointText", "device.nextSetpointText", width: 3, height: 1) {
        	state "default", label: '${currentValue}'
        }
		valueTile("statusText", "statusText", decoration: "flat", width: 3, height: 1) {     //, inactiveLabel: false
			state "default", label:'${currentValue}'
		}
        standardTile("away", "away", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "off", action:"away", label:'Set Away', icon: 'st.nest.nest-away'
			state "on", action:"awayOff", label:'Away Activated', icon: 'st.nest.nest-home'
            state "...", label: '...'
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "default", action:"deRefresh", label: "Refresh", icon:"st.secondary.refresh"
		}
        standardTile("temperature", "device.temperature", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
        	state "default", label: '${currentValue}°', unit:"C",
					backgroundColors:[
							// Celsius
							[value: 0, color: "#153591"],
							[value: 9, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 22, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 32, color: "#d04e00"],
							[value: 36, color: "#bc2323"]
					]
        }
        main "switch"
        details ([
        	"switch",
        	"HoldTitle", "decreaseTime", "increaseTime", "holdtime",
            "nextSetpointText", "away",
            "statusText", "refresh", "temperature"
        ])
	}
}

def refresh() {}

def deRefresh() {
	log.debug "force refresh"
	state.commandInProgress = false
    state.on = false
    state.hold = false
    refreshinfo()
}

def refreshinfo() {
	def cmds = []
    if (!state.commandInProgress) {
        log.debug "Refreshing thermostat data from parent"
        //Get the parent to request a refresh but this should be the parent showing the actual refresh command
        cmds << sendEvent(name: "holdtime", value: "...")
        cmds << sendEvent(name: "nextSetpointText", value: "...")
        cmds << sendEvent(name: "away", value: "...")
        cmds << sendEvent(name: "statusText", value: "Refreshing...")
        parent.childRequestingRefresh(device.deviceNetworkId)
    } else {
    	log.debug "Not refreshing since command is in progress"
    }
    return cmds
}

def installed() {
	updated()
}

def updated() {
	//Here we have a new device so lets ensure that all the tiles are correctly filled with something
    def cmds = []
    refreshinfo()
    state.commandInProgress = false
    return cmds
}

def increaseDuration() {
	log.debug "increase duration"
	def cmds = []
    if (device.currentValue("holdtime")?.contains(':')) {
        def mins = timeStringToMins(device.currentValue("holdtime")) + 30
        if (mins > 300) mins = 0

        cmds = setHoldTime(mins)
    }
    
    return cmds
}

def decreaseDuration() {
	log.debug "descrease duration"
	def cmds = []
    if (device.currentValue("holdtime")?.contains(':')) {
        def mins = timeStringToMins(device.currentValue("holdtime")) - 30
        if (mins < 0 && mins > -30) mins = 0
        if (mins < 0) mins = 300

        cmds = setHoldTime(mins)
    }
    
    return cmds
}

def away() {
	//Set away mode on
	def cmds = setAway(true)
    return cmds
}

def awayOff() {
	//Set away mode off
	def cmds = setAway(false)
    return cmds
}

def setHoldOff() {
	def cmds = []
	cmds << sendEvent(name: "holdtime", value: "0:00", displayed: true)
    parent.childCancelHold(device.deviceNetworkId)
    return cmds
}

def processNeoResponse(result) {
	//Response received from Neo Hub so process it (mainly used for refresh, but could also process the success/fail messages)
	def cmds = []

	//log.debug result
    if (result.containsKey("relaydevice")) {
    	//If we have a relaydevice key then we have a response to a command we sent, so process it here
        if (result.relayresult.containsKey("result")) {
        	state.commandInProgress = false
            log.debug "success on last command: " + result.relayresult.result
            refreshinfo()
        }
    }
    else if (result.containsKey("device")) {
    	if (!state.commandInProgress) {
    		cmds << parseDeviceInfo(result)
        } else {
        	log.debug "not parsing device info, command in progress"
        }
	}
    return cmds
}

def parseDeviceInfo(deviceInfo) {
	def cmds = []
    
    //def on = false;
    //def holdEnabled = false;
    
	//If we have a device key then it is probably a refresh command
    //First store the update date/time
    def dateTime = new Date()
    def updateddatetime = dateTime.format("yyyy-MM-dd HH:mm", location.timeZone)
    cmds << sendEvent(name: "statusText", value: "Last refreshed info at ${updateddatetime}")

    //Now update the various fields on the thermostat
    if (deviceInfo.containsKey("CURRENT_TEMPERATURE")) {
    	log.debug "DeviceInfo setting temperature to ${deviceInfo.CURRENT_TEMPERATURE}"
        cmds << sendEvent(name: "temperature", value: deviceInfo.CURRENT_TEMPERATURE)
    }
    if (deviceInfo.containsKey("CURRENT_SET_TEMPERATURE")) {
        //Got a set temperature so update it if above 8 (otherwise Away will set this to 5° annoyingly)
        state.on = deviceInfo.CURRENT_SET_TEMPERATURE.toBigDecimal().toInteger() > 100
            log.debug "Reported Set Temperature: " + deviceInfo.CURRENT_SET_TEMPERATURE.toBigDecimal().toInteger()
            log.debug "Set Temp interpreted as: " + state.on
    }
    if (deviceInfo.containsKey("CURRENT_FLOOR_TEMPERATURE")) {
        def flrtempstring
        if (deviceInfo.CURRENT_FLOOR_TEMPERATURE == 255) {
            flrtempstring = "N/A"
        }
        else {
            flrtempstring = deviceInfo.CURRENT_FLOOR_TEMPERATURE
        }
        log.debug "DeviceInfo setting floortemp to ${flrtempstring}"
        cmds << sendEvent(name: "floortemp", value: flrtempstring)
    }
    if (deviceInfo.containsKey("DEMAND")) {
        //Update the tiles to show that it is currently heating (if desired)
    }
    if (deviceInfo.containsKey("HOLD_TIME")) {
    	def statusTextmsg = ""
        if (deviceInfo.HOLD_TIME == "0:00") {
            statusTextmsg = "Set to " + (on ? "ON" : "OFF") + " until "
            if (deviceInfo.NEXT_ON_TIME.reverse().take(3).reverse() == "255") {
                //If we see 255:255 in hh:mm field then it is set permanently
                statusTextmsg = statusTextmsg + "changed"
            }
            else {
                //Otherwise add on the time for next change
                statusTextmsg = statusTextmsg + deviceInfo.NEXT_ON_TIME.reverse().take(5).reverse()
            }
            state.hold = false
        }
        else {
            //Here we do have a hold time so display temp and time
            if (deviceInfo.containsKey("HOLD_TEMPERATURE")) {
                log.debug "HOLD_TEMPERATURE = " + deviceInfo.HOLD_TEMPERATURE
                on = (deviceInfo.HOLD_TEMPERATURE.toInteger() == 1)
                log.debug "Hold Temperature sets switch to " + on
            }

            state.hold = true
            statusTextmsg = "Holding " + (on ? "ON" : "OFF") + " for " + deviceInfo.HOLD_TIME
        }
        
        log.debug "DeviceInfo setting nextSetpointText to ${statusTextmsg}"
        log.debug "DeviceInfo setting holdtime to ${deviceInfo.HOLD_TIME}"
        
        cmds << sendEvent(name: "nextSetpointText", value: statusTextmsg)
        cmds << sendEvent(name: "holdtime", value: deviceInfo.HOLD_TIME)
    }
    
    if (state.hold) {
        if (state.on) {
            log.debug "Setting switch to HoldOn"
            cmds << sendEvent(name: "switchStatus", value: "holdOn")
        } else {
            log.debug "Setting switch to HoldOff"
            cmds << sendEvent(name: "switchStatus", value: "holdOff")
        }
    } else {
        if (state.on) {
            log.debug "Setting switch to On"
            cmds << sendEvent(name: "switchStatus", value: "on")
        } else {
            log.debug "Setting switch to Off"
            cmds << sendEvent(name: "switchStatus", value: "off")
        }
    }

    if (deviceInfo.containsKey("AWAY")) {
        //Update away status as this is the most important parameter for Neostats
        log.debug "DeviceInfo setting away to ${deviceInfo.AWAY}"
        if (deviceInfo.AWAY == false) {
            cmds << sendEvent(name: "away", value: "off")
        }
        else {
            cmds << sendEvent(name: "away", value: "on")
        }
    }
    
    return cmds
}

def on() {
	def cmds = []
	log.debug "Executing 'on'"
    if (state.hold) {
        def mins = timeStringToMins(device.currentValue("holdtime"))
        if (mins > 0) {
        	parent.childTimerHoldOn(mins, device.deviceNetworkId)
        }
    }
    else {
    	parent.childTimerOn(device.deviceNetworkId)
    }
    
    cmds << sendEvent(name: "holdtime", value: "...")
    cmds << sendEvent(name: "nextSetpointText", value: "...")
    cmds << sendEvent(name: "statusText", value: "Setting on...")
    
    return cmds
}

def off() {
	def cmds = []
	log.debug "Executing 'off'"
    if (state.hold) {
        def mins = timeStringToMins(device.currentValue("holdtime"))
        if (mins > 0) {
        	parent.childTimerHoldOff(mins, device.deviceNetworkId)
        }
    }
    else {
    	parent.childTimerOff(device.deviceNetworkId)
    }
    
    cmds << sendEvent(name: "holdtime", value: "...")
    cmds << sendEvent(name: "nextSetpointText", value: "...")
    cmds << sendEvent(name: "statusText", value: "Setting off...")
    
    return cmds
}

private setHoldTime(intMins) {
	def cmds = []
    if (!state.commandInProgress)
    {
    	log.debug "setHoldTime: setting commandInProgress to true"
        state.commandInProgress = true
        
        if (state.on) {
        	parent.childTimerHoldOn(intMins, device.deviceNetworkId)
        } else {
        	parent.childTimerHoldOff(intMins, device.deviceNetworkId)
        }

        cmds << sendEvent(name: "holdtime", value: "...")
        cmds << sendEvent(name: "nextSetpointText", value: "...")
        cmds << sendEvent(name: "statusText", value: "Setting hold...")
    } else {
    	log.debug "setHoldTime: Not invoking since command is in progress"
    }
    return cmds
}

private setAway(boolAway) {
	def cmds = []
    if (!state.commandInProgress)
    {
    	log.debug "setAway: setting commandInProgress to true"
        state.commandInProgress = true
        if (boolAway) {
            parent.childAwayOn(device.deviceNetworkId)
            cmds << sendEvent(name: "statusText", value: "Setting away...")
        } else {
            parent.childAwayOff(device.deviceNetworkId)
            cmds << sendEvent(name: "statusText", value: "Setting back...")
        }

        cmds << sendEvent(name: "away", value: "...", displayed: true)
    } else {
    	log.debug "setAway: Not invoking since command is in progress"
    }
    return cmds
}

private timeStringToMins(timeString){
	if (timeString?.contains(':')) {
    	def hoursandmins = device.currentValue("holdtime").split(":")
        def mins = hoursandmins[0].toInteger() * 60 + hoursandmins[1].toInteger()
        log.debug "${timeString} converted to ${mins}" 
        return mins
    }
}

private minsToTimeString(intMins) {
	def timeString =  "${(intMins/60).toInteger()}:${intMins%60}"
    log.debug "${intMins} converted to ${timeString}"
    return timeString
}

private minsToHoursMins(intMins) {
	def hoursMins = []
    hoursMins << (intMins/60).toInteger()
    hoursMins << intMins%60
    log.debug "${intMins} converted to ${hoursMins[0]}:${hoursMins[1]}" 
    return hoursMins
}

def ping() {
}

def poll() {
}
