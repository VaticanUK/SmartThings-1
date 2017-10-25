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
		multiAttributeTile(name:"temperature", type:"thermostat", width:6, height:3) {
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
       
		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 1, height: 1, decoration: "flat") {
			state "default", label:'${currentValue}°C', backgroundColor:"#ffffff"
		}
		standardTile("raisethermostatSetpoint", "device.raisethermostatSetpoint", width: 1, height: 1, decoration: "flat") {
			state "default", action:"raiseSetpoint", label:'+1°C'//, icon:"st.thermostat.thermostat-up"
		}
		standardTile("lowerthermostatSetpoint", "device.lowerthermostatSetpoint", width: 1, height: 1, decoration: "flat") {
			state "default", action:"lowerSetpoint", label:'-1°C'//, icon:"st.thermostat.thermostat-down"
		}
        
		standardTile("increaseTime", "device.increaseTime", width: 1, height: 1, decoration: "flat") {
			state "default", action:"increaseDuration", label:'+30m'
		}
        standardTile("decreaseTime", "device.decreaseTime", width: 1, height: 1, decoration: "flat") {
			state "default", action:"decreaseDuration", label:'-30m'
		}
		valueTile("holdtime", "device.holdtime", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", label:'${currentValue}' // icon TBC
		}
        
        standardTile("setTempHold", "setTempHold", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "settemp", action:"setTempHoldOn", label:'Set Temp' // icon TBC
			state "sethold", action:"setTempHoldOn", label:'Set Hold' // icon TBC
            state "tempwasset", action:"setTempHoldOn", label:'Temp Was Set' // icon TBC
            state "cancelhold", action:"setTempHoldOff", label:'Cancel Hold' // icon TBC
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
        standardTile("away", "away", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "off", action:"away", label:'Set Away' // icon TBC
			state "on", action:"awayOff", label:'Away Activated\r\nPress to cancel' // icon TBC
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", action:"refreshinfo", label: "Refresh", icon:"st.secondary.refresh"
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
				"lowerthermostatSetpoint", "heatingSetpoint", "raisethermostatSetpoint", "decreaseTime", "holdtime", "increaseTime", 
				"nextSetpointText", "statusText", "setTempHold",
				"floortemp", "away", "refresh", "hanges"])
	}
}

def ensureAlexaCapableMode() {
	sendEvent(name: "thermostatMode", value: "auto")
    //sendEvent(name: "thermostatOperatingState", value: "idle")
}


def refresh() {
}

def refreshinfo() {
	log.debug "Refreshing thermostat data from parent"
    //Get the parent to request a refresh but this should be the parent showing the actual refresh command
    parent.childRequestingRefresh(device.deviceNetworkId)
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
    cmds << sendEvent(name: "heatingSetpoint", value: 15)
    cmds << sendEvent(name: "holdtime", value: "0:00")
    return cmds
}


def processNeoResponse(result) {
	//Response received from Neo Hub so process it (mainly used for refresh, but could also process the success/fail messages)
	def statusTextmsg = ""
	def cmds = []

	//log.debug result
    if (result.containsKey("relaydevice")) {
    	//If we have a relaydevice key then we have a response to a command we sent, so process it here
        if (result.relayresult.containsKey("result")) {
    		//We have a success result from a command so process it here by pasting in the response and updating tile
            log.debug "success on last command: " + result.relayresult.result
            //Would love to refresh information at this point, but it will fail as the Neostats take a while to update
            //refreshinfo()
        	cmds << sendEvent(name: "statusText", value: result.relayresult)
        }
    }
    else if (result.containsKey("device")) {
    	//If we have a device key then it is probably a refresh command
        //First store the update date/time
        def dateTime = new Date()
        def updateddatetime = dateTime.format("yyyy-MM-dd HH:mm", location.timeZone)
        cmds << sendEvent(name: "statusText", value: "Last refreshed info at ${updateddatetime}")
        
        //Now update the various fields on the thermostat
        if (result.containsKey("CURRENT_TEMPERATURE")) {
        	//Got a temperature so update the tile
            //log.debug "got a temp = " + result.CURRENT_TEMPERATURE
            cmds << sendEvent(name: "temperature", value: result.CURRENT_TEMPERATURE)
        }
        if (result.containsKey("CURRENT_SET_TEMPERATURE")) {
        	//Got a set temperature so update it if above 8 (otherwise Away will set this to 5° annoyingly)
        	def settempint = result.CURRENT_SET_TEMPERATURE.toBigDecimal().toInteger() + 0 //.toBigDecimal().toInteger()
            if (settempint >= 8) {
            	cmds << sendEvent(name: "heatingSetpoint", value: settempint)
            }
        }
        if (result.containsKey("CURRENT_FLOOR_TEMPERATURE")) {
        	//Update the floor temperature in case anybody cares!
            def flrtempstring
            if (result.CURRENT_FLOOR_TEMPERATURE == 255) {
            	flrtempstring = "N/A"
            }
            else {
            	flrtempstring = result.CURRENT_FLOOR_TEMPERATURE
            }
        	cmds << sendEvent(name: "floortemp", value: flrtempstring)
        }
        if (result.containsKey("DEMAND")) {
        	//Update the tiles to show that it is currently heating (if desired)
        }
        if (result.containsKey("HOLD_TIME")) {
        	if (result.HOLD_TIME == "0:00") {
            	//Here we have zero hold time so run until next on time
            	statusTextmsg = "Set to " + result.CURRENT_SET_TEMPERATURE + "C until "
                if (result.NEXT_ON_TIME.reverse().take(3).reverse() == "255") {
                	//If we see 255:255 in hh:mm field then it is set permanently
                	statusTextmsg = statusTextmsg + "changed"
                }
                else {
                	//Otherwise add on the time for next change
                	statusTextmsg = statusTextmsg + result.NEXT_ON_TIME.reverse().take(5).reverse()
                }
                //Now send the update
            	cmds << sendEvent(name: "nextSetpointText", value: statusTextmsg)
                //Lastly if we are here then there should be not holds in place and hence lets update the set button text
                if (device.currentValue("holdtime") == "0:00") {
                	cmds << sendEvent(name: "setTempHold", value: "settemp")
                }
                else {    
                	cmds << sendEvent(name: "setTempHold", value: "setHold")
                }
            }
            else {
            	//Here we do have a hold time so display temp and time
            	statusTextmsg = "Holding " + result.HOLD_TEMPERATURE + "°C for " + result.HOLD_TIME
            	cmds << sendEvent(name: "nextSetpointText", value: statusTextmsg)
            }
        }
        
        if (result.containsKey("AWAY")) {
        	//Update away status as this is the most important parameter for Neostats
            if (result.AWAY == false) {
                cmds << sendEvent(name: "away", value: "off")
            }
            else {
                cmds << sendEvent(name: "away", value: "on")
            }
        }
        if (result.containsKey("HOLD_TEMPERATURE")) {
        }
	}
    return cmds
}


def increaseDuration() {
	def cmds = []
    if (device.currentValue("holdtime")?.contains(':')) {
        def hoursandmins = device.currentValue("holdtime").split(":")
        //Now we calculate and update hours and minutes and send it to the holdtime tile
        if (hoursandmins[1] == "00") {
            hoursandmins[1] = "30"
        }
        else {
        	hoursandmins[1] = "00"
            hoursandmins[0] = (hoursandmins[0].toInteger() + 1).toString()
        }
        //Reset hours if 5 or more
        if (hoursandmins[0].toInteger() >= 5) {
             hoursandmins[0] = "0"
             if (device.currentValue("setTempHold") != "cancelhold") {
             	cmds << sendEvent(name: "setTempHold", value: "settemp")
             }
         }
        else {
        	if (device.currentValue("setTempHold") != "cancelhold") {
            	cmds << sendEvent(name: "setTempHold", value: "setHold")
            }
        }
        cmds << sendEvent(name: "holdtime", value: "${hoursandmins[0]}:${hoursandmins[1]}", displayed: true)
    }
    else {
    	//Here we should reset as it hasnt found
		cmds << sendEvent(name: "holdtime", value: "0:00", displayed: true)
        return cmds
    }
    return cmds
}

def decreaseDuration() {
	def cmds = []
    if (device.currentValue("holdtime")?.contains(':')) {
        def hoursandmins = device.currentValue("holdtime").split(":")
        //Now we calculate and update hours and minutes and send it to the holdtime tile
        if ((hoursandmins[0] == "0") && (hoursandmins[1] == "00")) {
            hoursandmins[0] = "5"
            if (device.currentValue("setTempHold") != "cancelhold") {
            	cmds << sendEvent(name: "setTempHold", value: "setHold")
            }
        }
        if (hoursandmins[1] == "30") {
            hoursandmins[1] = "00"
        }
        else {
        	hoursandmins[1] = "30"
            hoursandmins[0] = (hoursandmins[0].toInteger() - 1).toString()
        }
        if ((hoursandmins[0] == "0") && (hoursandmins[1] == "00")) {
        	if (device.currentValue("setTempHold") != "cancelhold") {
            	cmds << sendEvent(name: "setTempHold", value: "settemp")
        	}
        }
        cmds << sendEvent(name: "holdtime", value: "${hoursandmins[0]}:${hoursandmins[1]}", displayed: true)
    }
    else {
    	//Here we should reset as it hasnt found
		cmds << sendEvent(name: "holdtime", value: "0:00", displayed: true)
        return cmds
    }
    return cmds
}

def away() {
	//Set away mode on
	def cmds = []
	if (state.debug) log.debug "${device.label}: away()"
	parent.childAwayOn(device.deviceNetworkId)
    cmds << sendEvent(name: "away", value: "on", displayed: true)
    return cmds
}
def awayOff() {
	//Set away mode off
	def cmds = []
	if (state.debug) log.debug "${device.label}: awayOff()"
	parent.childAwayOff(device.deviceNetworkId)
    cmds << sendEvent(name: "away", value: "off", displayed: true)
    return cmds
}

def setTempHoldOn(chosentemp) {
	def cmds = []
    def newtemp = device.currentValue("heatingSetpoint")
    
    if (state.debug) log.debug "${device.label}: set temp hold to ${newtemp} for ${device.currentValue("holdtime")} - setTempOn()"

    if (device.currentValue("holdtime") == "0:00") {
    	parent.childSetTemp(newtemp.toString(), device.deviceNetworkId)
		cmds << sendEvent(name: "setTempHold", value: "tempwasset")
    }
    else {
     	def hours = device.currentValue("holdtime").take(1)
    	def minutes = device.currentValue("holdtime").reverse().take(2).reverse()
        log.debug hours + " hours, " + minutes + "mins"
		parent.childHold(newtemp.toString(), hours, minutes, device.deviceNetworkId)
    	cmds << sendEvent(name: "setTempHold", value: "cancelhold")
	}
    return cmds
}

def setTempHoldOff() {
	//Cancel the temp hold
	def cmds = []
	if (state.debug) log.debug "${device.label}: cancel hold/temp - setTempHoldOff()"
	parent.childCancelHold(device.deviceNetworkId)
    if (device.currentValue("holdtime") == "0:00") {
    	//We have 0:00 as hh:mm, so the next command will be a set_temp
        cmds << sendEvent(name: "setTempHold", value: "settemp")
    }
    else {
    	cmds << sendEvent(name: "setTempHold", value: "setHold")
    }
    return cmds
}


def raiseSetpoint() {
	//Called by tile to increase set temp box
	def cmds = []
	def newtemp = device.currentValue("heatingSetpoint").toInteger() + 1
    cmds << sendEvent(name: "heatingSetpoint", value: newtemp)
    return cmds
}

def lowerSetpoint() {
    //called by tile to decrease set temp box
	def cmds = []
	def newtemp = device.currentValue("heatingSetpoint").toInteger() - 1
    cmds << sendEvent(name: "heatingSetpoint", value: newtemp)
    return cmds
}

//This is the command used by Amazon Alexa
def setHeatingSetpoint(number) {
	parent.childSetTemp(number.toString(), device.deviceNetworkId)
}

//This command is probably not used, but justan audio way of setting thermostat to away (may not work because of Alexa)
def off() {
	//In this case we'll just set the thermostat to be away
	away()
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
