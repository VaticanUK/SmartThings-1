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
        capability "Configuration"
		capability "Polling"
		capability "Refresh"
        capability "Sensor"
        capability "Switch"
		capability "Relay Switch"
        
        command "refresh"
        command "refreshinfo"
        
        command "increaseDuration"
		command "decreaseDuration"
        command "away"
        command "awayOff"
        command "switchOn"
        command "switchOff"
        command "setHoldOff"
        
        attribute "holdtime", "string"
        attribute "statusText", "string"
        attribute "switchStatus", "string"
	}

	tiles(scale:2) {
		multiAttributeTile(name:"switch", type:"lighting", width: 6, height: 4, canChangeIcon: true){
        	tileAttribute ("switchStatus", key: "PRIMARY_CONTROL") {
            	attributeState "on", label: 'on', action: "switchOff", icon: "st.thermostat.heating", backgroundColor: "#79b821"
                attributeState "off", label: 'off', action: "switchOn", icon: "st.thermostat.heating-cooling-off", backgroundColor: '#ffffff'
                attributeState "HoldOn", label: 'on (hold)', action: "switchOff", icon: "st.thermostat.heating", backgroundColor: "#79b821"
                attributeState "HoldOff", label: 'off (hold)', action: "switchOn", icon: "st.thermostat.heating-cooling-off", backgroundColor: '#ffffff'
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
			state "on", action:"awayOff", label:'Away Activated\r\nPress to cancel', icon: 'st.nest.nest-home'
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "default", action:"refreshinfo", label: "Refresh", icon:"st.secondary.refresh"
		}
        main "switch"
        details ([
        	"switch",
        	"HoldTitle", "decreaseTime", "increaseTime", "holdtime",
            "nextSetpointText", "away",
            "statusText", "refresh"
        ])
	}
}

def refresh() {}

def refreshinfo() {
	log.debug "Refreshing thermostat data from parent"
    //Get the parent to request a refresh but this should be the parent showing the actual refresh command
    parent.childRequestingRefresh(device.deviceNetworkId)
}

def ping() {}

def poll() {}

def installed() {
	updated()
}

def updated() {
	//Here we have a new device so lets ensure that all the tiles are correctly filled with something
    def cmds = []
    cmds << sendEvent(name: "holdtime", value: "0:00")
    return cmds
}

def increaseDuration() {
	log.debug "increase duration"
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
        
        def hours = device.currentValue("holdtime").take(1)
        def minutes = device.currentValue("holdtime").reverse().take(2).reverse()
        log.debug hours + " hours, " + minutes + "mins"
        parent.childHold(newtemp.toString(), hours, minutes, device.deviceNetworkId)
    }
    else {
    	//Here we should reset as it hasnt found
		cmds << sendEvent(name: "holdtime", value: "0:00", displayed: true)
        parent.childCancelHold(device.deviceNetworkId)
        return cmds
    }
    return cmds
}

def decreaseDuration() {
	log.debug "descrease duration"
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
        
        def hours = device.currentValue("holdtime").take(1)
    	def minutes = device.currentValue("holdtime").reverse().take(2).reverse()
    	log.debug hours + " hours, " + minutes + "mins"
    	parent.childHold(newtemp.toString(), hours, minutes, device.deviceNetworkId)
    }
    else {
    	//Here we should reset as it hasnt found
		cmds << sendEvent(name: "holdtime", value: "0:00", displayed: true)
        parent.childCancelHold(device.deviceNetworkId)
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

def setHoldOff() {
	def cmds = []
	cmds << sendEvent(name: "holdtime", value: "0:00", displayed: true)
    parent.childCancelHold(device.deviceNetworkId)
    return cmds
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'switch' attribute

}

def processNeoResponse(result) {
	//Response received from Neo Hub so process it (mainly used for refresh, but could also process the success/fail messages)
	def statusTextmsg = ""
	def cmds = []
	
    def on = false;
    def holdEnabled = false;

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
        if (result.containsKey("CURRENT_SET_TEMPERATURE")) {
        	//Got a set temperature so the current state - 12 = off, 238 = on!
            on = result.CURRENT_SET_TEMPERATURE.toBigDecimal().toInteger() > 100
            log.debug "Reported Set Temperature: " + result.CURRENT_SET_TEMPERATURE.toBigDecimal().toInteger()
            log.debug "Set Temp interpreted as: " + on
        }
        if (result.containsKey("HOLD_TIME")) {
        	if (result.HOLD_TIME == "0:00") {
            	log.debug "Hold Disabled"
            	//Here we have zero hold time so run until next on time
            	statusTextmsg = "Set to " + (on ? "ON" : "OFF") + " until "
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
                cmds << sendEvent(name: "holdtime", value: "0:00")
            }
            else {
            	//Here we do have a hold time so display temp and time
                log.debug "Hold Enabled"
                if (result.containsKey("HOLD_TEMPERATURE")) {
                	log.debug "HOLD_TEMPERATURE = " + result.HOLD_TEMPERATURE
                	on = (result.HOLD_TEMPERATURE.toInteger() == 1)
                    log.debug "Hold Temperature sets switch to " + on
                }
                
                holdEnabled = true;
            	statusTextmsg = "Holding " + (on ? "ON" : "OFF") + " for " + result.HOLD_TIME
            	cmds << sendEvent(name: "nextSetpointText", value: statusTextmsg)
                cmds << sendEvent(name: "holdtime", value: result.HOLD_TIME, displayed: true)
            }
        }
        
        if (holdEnabled) {
            if (on) {
            	log.debug "Setting switch to HoldOn"
                cmds << sendEvent(name: "switchStatus", value: "holdOn")
            } else {
                log.debug "Setting switch to HoldOff"
                cmds << sendEvent(name: "switchStatus", value: "holdOff")
            }
        } else {
            if (on) {
            	log.debug "Setting switch to On"
                cmds << sendEvent(name: "switchStatus", value: "on")
            } else {
            	log.debug "Setting switch to Off"
                cmds << sendEvent(name: "switchStatus", value: "off")
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
	}
    return cmds
}

// handle commands
def configure() {
	log.debug "Executing 'configure'"
	// TODO: handle 'configure' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}