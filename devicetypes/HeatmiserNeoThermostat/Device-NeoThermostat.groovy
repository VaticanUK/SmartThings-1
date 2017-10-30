/**
 *  Copyright 2017 Chris Charles
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
 *  Neo Thermostat (Child Device of Neo Hub Bridge)
 *
 *  Author: Chris Charles (cjcharles0)
 *  Date: 2017-04-26
 *  
 *  Important note, since each command is requesting something from the server, they cannot be void's otherwise
 *  nothing seems to come back from ST, even though it should receive the JSON anyway
 */

import groovy.json.JsonSlurper

metadata {
	definition (name: "Neo Thermostat", namespace: "cjcharles0", author: "Chris Charles")
    { 
    	capability "Refresh" //This is kept here so we could drive refreshes by the individual thermostats
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Thermostat"
		capability "Polling"
		capability "Sensor"
        capability "Configuration"
		
		command "refresh" // Refresh - this does nothing as otherwise there will be too many refresh commands being sent
        command "refreshinfo" //This is the actual refresh details command to update all thermostats
        
        command "setHeatingSetpoint" //Required by ST for thermostat type
        command "setCoolingSetpoint" //Required by ST for thermostat type
        command "off" //Required by ST for thermostat type
        command "heat" //Required by ST for thermostat type
        command "emergencyHeat" //Required by ST for thermostat type
        command "cool" //Required by ST for thermostat type
        command "setThermostatMode" //Required by ST for thermostat type
        command "fanOn" //Required by ST for thermostat type
        command "anAuto" //Required by ST for thermostat type
        command "fanCirculate" //Required by ST for thermostat type
        command "setThermostatFanMode" //Required by ST for thermostat type
        command "auto" //Required by ST for thermostat type
        command "ensureAlexaCapableMode"
        
        command "raiseSetpoint" // Custom
		command "lowerSetpoint" // Custom
		command "increaseDuration" // Custom
		command "decreaseDuration" // Custom
		command "setTempHoldOn" // Custom
		command "setTempHoldOff" // Custom
        command "away" // Custom
		command "awayOff" // Custom
        command "deRefresh"

		attribute "temperature","number" // Temperature Measurement
		attribute "heatingSetpoint","number" // Thermostat setpoint
		attribute "thermostatSetpoint","number" // Thermostat setpoint
        attribute "holdtime","string" // Custom for how long to hold for
		attribute "nextSetpointText", "string" // Custom for text display
		attribute "statusText", "string" // Custom for neohub response
	}
    
    preferences {
        section {
        	//Preferences here
        }
	}

//Thermostat Temp and State
	tiles(scale: 2) {

		// Main multi information tile
		multiAttributeTile(name:"temperature", type:"lighting", width:6, height:3) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue}°', unit:"C",
					backgroundColors:[
							// Celsius
							[value: 0, color: "#153591"],
							[value: 9, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 22, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 32, color: "#d04e00"],
							[value: 36, color: "#bc2323"]
					])
			}
            tileAttribute ("device.holdtime", key: "SECONDARY_CONTROL") {
            	attributeState "holdtime", label: 'Hold time: ${currentValue}', icon: "st.Office.office6"
            }
			// Operating State - used to get background colour when type is 'thermostat'.
            /*tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
                attributeState("heating", backgroundColor:"#e86d13")
                attributeState("idle", backgroundColor:"#00A0DC")
                attributeState("cooling", backgroundColor:"#00A0DC")
            }*/
            tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
                attributeState("auto", label:' ')
                //attributeState("heat", label:' ')
                //attributeState("off", label:' ')
                //attributeState("cool", label:' ')
            }
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("heatingSetpoint", label:'', backgroundColor:"#ffffff", defaultState: true)
			}
		}
       
		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 1, decoration: "flat") {
			state "default", label:'${currentValue}°C', backgroundColor:"#ffffff"
		}
		standardTile("raisethermostatSetpoint", "device.raisethermostatSetpoint", width: 1, height: 1, decoration: "flat") {
			state "default", action:"raiseSetpoint", label:'+1°C'//, icon:"st.thermostat.thermostat-up"
		}
		standardTile("lowerthermostatSetpoint", "device.lowerthermostatSetpoint", width: 1, height: 1, decoration: "flat") {
			state "default", action:"lowerSetpoint", label:'-1°C'//, icon:"st.thermostat.thermostat-down"
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
		standardTile("cancelHold", "device.cancelHold", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", action:"setTempHoldOff", label:'Cancel'
		}
        standardTile("holdTempTitle", "device.HoldTempTitle", width: 2, height: 1) {
        	state "default", label:'Temp:'
        }
		valueTile("nextSetpointText", "device.nextSetpointText", width: 3, height: 1) {
			state "default", label:'${currentValue}'
		}
        valueTile("statusText", "statusText", decoration: "flat", width: 3, height: 1) {     //, inactiveLabel: false
			state "default", label:'${currentValue}'
		}
        valueTile("floortemp", "floortemp", decoration: "flat", width: 1, height: 1) {
    		state "floortemp", label:'Floor Temp\r\n${currentValue}'
		}
		standardTile("away", "away", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "off", action:"away", label:'Set Away', icon: 'st.nest.nest-away'
			state "on", action:"awayOff", label:'Away Activated\r\nPress to cancel', icon: 'st.nest.nest-home'
            state "...", label:'...'
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", action:"deRefresh", label: "Refresh", icon:"st.secondary.refresh"
		}
        
		standardTile("hanges", "device.hanges", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", action:"ensureAlexaCapableMode", label: "changestate"
		}
        
		standardTile("off", "device.off", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "on", action:"off", icon:"st.thermostat.heating-cooling-off"
 			state "off", action:"on", label:'Turn On', icon:"st.thermostat.heating-cooling-off"
		}
		
		main "temperature"
		details(
				[
				"temperature",
                "HoldTitle", "decreaseTime", "increaseTime", "cancelHold",
                "holdTempTitle", "lowerthermostatSetpoint", "raisethermostatSetpoint", "heatingSetpoint",  
				"nextSetpointText", "away",
                "statusText", "floortemp", "refresh", "hanges"])
	}
}

def ensureAlexaCapableMode() {
	sendEvent(name: "thermostatMode", value: "auto")
    //sendEvent(name: "thermostatOperatingState", value: "idle")
}


def refresh() {
}

def deRefresh() {
	log.debug "force refresh"
	state.commandInProgress = false
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
        cmds << sendEvent(name: "heatingSetpoint", value: "...")
        cmds << sendEvent(name: "statusText", value: "Refreshing...")
        parent.childRequestingRefresh(device.deviceNetworkId)
    } else {
    	log.debug "Not refreshing since command is in progress"
    }
    return cmds
}

def ping() {
}

def poll() {
}

def installed() {
	updated()
}

def updated() {
	//Here we have a new device so lets ensure that all the tiles are correctly filled with something
    ensureAlexaCapableMode()
    def cmds = []
    refreshinfo()
    state.commandInProgress = false
    state.tempCommandPending = false
    state.holdCommandPending = false
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
        def settempint = deviceInfo.CURRENT_SET_TEMPERATURE.toBigDecimal().toInteger()
        if (settempint >= 8) {
            log.debug "DeviceInfo setting heatingSetpoint to ${settempint}"
            cmds << sendEvent(name: "heatingSetpoint", value: settempint)
        }
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
            statusTextmsg = deviceInfo.CURRENT_SET_TEMPERATURE + "C until "
            if (deviceInfo.NEXT_ON_TIME.reverse().take(3).reverse() == "255") {
                //If we see 255:255 in hh:mm field then it is set permanently
                statusTextmsg = statusTextmsg + "changed"
            }
            else {
                //Otherwise add on the time for next change
                statusTextmsg = statusTextmsg + deviceInfo.NEXT_ON_TIME.reverse().take(5).reverse()
            }
        }
        else {
            //Here we do have a hold time so display temp and time
            statusTextmsg = "Holding " + deviceInfo.HOLD_TEMPERATURE + "°C for " + deviceInfo.HOLD_TIME
        }
        log.debug "DeviceInfo setting nextSetpointText to ${statusTextmsg}"
        log.debug "DeviceInfo setting holdtime to ${deviceInfo.HOLD_TIME}"
        
        cmds << sendEvent(name: "nextSetpointText", value: statusTextmsg)
        cmds << sendEvent(name: "holdtime", value: deviceInfo.HOLD_TIME)
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
    if (deviceInfo.containsKey("HOLD_TEMPERATURE")) {
    }
    
    return cmds
}

def increaseDuration() {
	def cmds = []

    if (device.currentValue("holdtime")?.contains(':')) {
        def mins = timeStringToMins(device.currentValue("holdtime")) + 30
        if (mins > 300) mins = 0

        cmds = setHoldTime(mins)
    }
    
    return cmds
}

def decreaseDuration() {
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

def setTempHoldOff() {
	//Cancel the temp hold
	def cmds = []
    if (!state.commandInProgress)
    {
    	log.debug "increaseDuration: setting commandInProgress to true"
        state.commandInProgress = true;
        log.debug "cancel hold/temp - setTempHoldOff()"
        parent.childCancelHold(device.deviceNetworkId)
        cmds << sendEvent(name: "holdtime", value: "...")
        cmds << sendEvent(name: "nextSetpointText", value: "...")
        cmds << sendEvent(name: "statusText", value: "Cancelling hold...")
    } else {
    	log.debug "setTempHoldOff: Not invoking since command is in progress"
    }
    return cmds
}


def raiseSetpoint() {
	//Called by tile to increase set temp box
    def newtemp = device.currentValue("heatingSetpoint").toInteger() + 1
    def cmds = setTemp(newtemp)
    
    return cmds
}

def lowerSetpoint() {
    //called by tile to decrease set temp box
	def newtemp = device.currentValue("heatingSetpoint").toInteger() - 1
    def cmds = setTemp(newtemp)
    
    return cmds
}

//This is the command used by Amazon Alexa
def setHeatingSetpoint(number) {
	setTemp(number)
}

//This command is probably not used, but just another way of setting thermostat to away (may not work because of Alexa)
def off() {
	//In this case we'll just set the thermostat to be away
	away()
}

def setHoldTime(intMins) {
	def cmds = []
    if (!state.commandInProgress && !state.tempCommandPending) {
    	state.requestedHold = intMins
        state.holdCommandPending = true
        def timeString = minsToTimeString(intMins)
        cmds << sendEvent(name: "holdtime", value: timeString)
        // this runIn command will be overwritten by future runIn commands, effectively only sending the command if the user doesn't do anything for 5 seconds
        runIn(5, setHoldOnHub)
    } else {
    	log.debug "setTemp: Not invoking since command is in progress"
    }
    
    return cmds
}

def setHoldOnHub() {
	def cmds = []
    def intMins = state.requestedHold
    log.debug "setHoldOnHub: hold mins ${intMins}"
    log.debug "setHoldOnHub: setting commandInProgress to true"
	state.commandInProgress = true
    state.holdCommandPending = false
	if (intMins == 0) {
		log.debug "setHoldOnHub: hold time is 0 - cancelling hold"
		parent.childCancelHold(device.deviceNetworkId)
	} else {
		def hoursMins = minsToHoursMins(intMins)
		log.debug "setHoldOnHub setting hold. Time - ${hoursMins[0]}:${hoursMins[1]}, Temp - ${temp}"
		def temp = device.currentValue("heatingSetpoint").toInteger()
		parent.childHold(temp, hoursMins[0], hoursMins[1], device.deviceNetworkId)
	}

	cmds << sendEvent(name: "holdtime", value: "...")
	cmds << sendEvent(name: "nextSetpointText", value: "...")
	cmds << sendEvent(name: "statusText", value: "Setting hold...")
}

def setTemp(intTemp) {
	def cmds = []
    if (!state.commandInProgress && !state.holdCommandPending) {
        state.requestedTemp = intTemp
        state.tempCommandPending = true
        cmds << sendEvent(name: "heatingSetpoint", value: intTemp)
        // this runIn command will be overwritten by future runIn commands, effectively only sending the command if the user doesn't do anything for 5 seconds
        runIn(5, setTempOnHub)
    } else {
    	log.debug "setTemp: Not invoking since command is in progress"
    }

	return cmds
}

def setTempOnHub() {
	def cmds = []
    def intTemp = state.requestedTemp
	log.debug "setTemp: setting commandInProgress to true"
    state.commandInProgress = true;
    log.debug "setTemp setting temp - ${intTemp}"
    parent.childSetTemp(intTemp, device.deviceNetworkId)
    state.tempCommandPending = false
    cmds << sendEvent(name: "heatingSetpoint", value: "...")
    if (device.currentValue("holdtime")?.contains(':')) {
		def mins = timeStringToMins(device.currentValue("holdtime"))
		if (mins > 0) {
			def hoursMins = minsToHoursMins(mins)
			log.debug "setTemp setting hold. Time - ${hoursMins[0]}:${hoursMins[1]}, Temp - ${intTemp}"
			parent.childHold(intTemp, hoursMins[0], hoursMins[1], device.deviceNetworkId)
		}
	}

	cmds << sendEvent(name: "nextSetpointText", value: "...")
	cmds << sendEvent(name: "statusText", value: "Setting temperature...")
    
    return cmds
}

def setAway(boolAway) {
	def cmds = []
    if (!state.commandInProgress)
    {
    	log.debug "setAway: setting commandInProgress to true"
        state.commandInProgress = true;
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

//Dont use any of these yet as I havent worked out why they would be needed!
def heat() {
}

def emergencyHeat() {
}

def setThermostatMode(chosenmode) {
}

def fanOn() {
}

def fanAuto() {
}

def fanCirculate() {
}

def setThermostatFanMode(chosenmode) {
}

def cool() {
}

def setCoolingSetpoint(number) {
}

def auto() {
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
	def timeString =  "${(intMins/60).toInteger()}:${(intMins%60).toString().padLeft(2, "0")}"
    log.debug "${intMins} converted to ${timeString}"
    return timeString
}

private minsToHoursMins(intMins) {
	def hoursMins = []
    log.debug "minsToHoursMins - ${intMins}"
    hoursMins << (intMins/60).toInteger()
    hoursMins << intMins%60
    log.debug "${intMins} converted to ${hoursMins[0]}:${hoursMins[1]}" 
    return hoursMins
}
