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
 *  Neo Hub Bridge (Parent Device of Neo Thermostat)
 *
 *  Author: Chris Charles (cjcharles0)
 *  Date: 2017-04-26
 */

import groovy.json.JsonSlurper

metadata {
	definition (name: "Neo Hub Bridge", namespace: "cjcharles0", author: "Chris Charles") {
		capability "Refresh"
        capability "Configuration"
        capability "Polling"
        capability "Thermostat"

		command "refreshipaddresses"
        command "getthermostats"
        command "removethermostats"
        command "setAllAwayOn"
        command "setAllAwayOff"
        
        command "childRequestingRefresh"
        command "childAwayOn"
        command "childAwayOff"
        command "childHeat"
        command "childSetTemp"
        command "childHold"
        command "childCancelHold"
        command "childBoostOn"
        command "childBoostOff"
        command "childFrostOn"
        command "childFrostOff"
        command "childSetFrost"
                
        command "testcommand"
        
	}

	simulator {
	}
    
    preferences {
        input("password", "password", title:"Password", required:false, displayDuringSetup:false)
        input("bridgeip", "string", title:"Neo Bridge IP Address", description: "e.g. 192.168.1.10", required: true, displayDuringSetup: true)
        input("neohubip", "string", title:"NeoHub IP Address", description: "e.g. 192.168.1.11", required: true, displayDuringSetup: true)
        input("prestatname", "string", title:"Add before stat name", description: "e.g. 'Thermostat' would give 'Thermostat Kitchen'", required: false, displayDuringSetup: true)
        input("poststatname", "string", title:"Add after stat name", description: "e.g. 'Thermostat' would give 'Kitchen Thermostat'", required: false, displayDuringSetup: true)
	}

	tiles (scale: 2){
	
        valueTile("lastcommand", "lastcommand", decoration: "flat", width: 6, height: 2) {
			state "lastcommand", label:'${currentValue}', icon: "st.Home.home2"
		}
        
        valueTile("stip", "stip", decoration: "flat", width: 2, height: 1) {
    		state "stip", label:'ST IP Addr.\r\n${currentValue}'
		}
        valueTile("neoip", "neoip", decoration: "flat", width: 2, height: 1) {
    		state "neoip", label:'Neohub IP Addr.\r\n${currentValue}'
		}
        valueTile("brip", "brip", decoration: "flat", width: 2, height: 1) {
    		state "brip", label:'ESP Bridge IP Addr.\r\n${currentValue}'
		}
        
        standardTile("refreships", "device.refreships", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", label:"Refresh IPs", action:"refreshipaddresses", icon:"st.secondary.refresh"
		}
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "configure", label:'Bridge', action:"configure", icon:"st.secondary.configure"
		}
        standardTile("getthermostats", "device.getthermostats", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "getthermostats", label:'Create Thermostat Devices', action:"getthermostats", icon: "st.unknown.zwave.remote-controller"
		}
        standardTile("removethermostats", "device.removethermostats", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "removethermostats", label:'Remove Thermostat Devices', action:"removethermostats", icon: "st.samsung.da.washer_ic_cancel"
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "default", label:"Refresh Thermostat Info", action:"refresh", icon:"st.secondary.refresh"
		}
        standardTile("allaway", "device.allaway", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
        	state "off", label:'Set all Thermostats to Away', action:"setAllAwayOn"
			state "on", label:'Thermostats Away, Press to cancel', action:"setAllAwayOff"
		}
        standardTile("testcommand", "device.testcommand", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "testcommand", label:'Create Test Device', action:"testcommand"
		}
    }

	main(["lastcommand"])
    details(["lastcommand",
             "stip", "neoip", "brip",
             "refreships", "configure", "getthermostats", "removethermostats",
             "refresh", "allaway",
             "testcommand"
             ])
}


//This is a test command that can be used for whatever reason
def testcommand(){
	log.debug "Sometimes it refuses to create a device, adding one test device now to fix things"
    try {
        def curdevice = getChildDevices()?.find { it.deviceNetworkId == "visonictestdevice"}
        if (curdevice) {
            //Do nothing as we already have a test device
        }
        else {
            addChildDevice("cjcharles0", "Neo Thermostat", "neostatTemp", device.hub.id, [name: "neo tempt", label: name, completedSetup: true]) //"cjcharles0", //device.hub.id
        }
    } catch (e) {
        log.error "Couldnt find device, probably doesn't exist so safe to add a new one: ${e}"
        addChildDevice("cjcharles0", "Neo Thermostat", "neostatTemp", device.hub.id, [name: "neo tempt", label: name, completedSetup: true]) //"cjcharles0", //device.hub.id
    }
}


//These are the built in commands that ST requires for basic operations
def configure(){
	def cmds = []
	log.debug "Configuring NeoBridge"
    cmds << getAction("/config?ip_for_st=${device.hub.getDataValue("localIP")}&port_for_st=${device.hub.getDataValue("localSrvPortTCP")}&ip_for_neohub=${neohubip}&port_for_neohub=4242")
    return cmds
}
def refreshipaddresses() {
	def cmds = []
    log.debug "refreships()"
    //SendEvents should be before any getAction, otherwise getAction does nothing
    cmds << sendEvent(name: "stip", value: device.hub.getDataValue("localIP")+"    Port:"+device.hub.getDataValue("localSrvPortTCP"), displayed: false)
    cmds << sendEvent(name: "neoip", value: neohubip + "    Port:4242", displayed: false)
    cmds << sendEvent(name: "brip", value: bridgeip + "    Port:80", displayed: false)
    return cmds
}
def refresh() {
	log.debug "parent triggered refresh"
    childRequestingRefresh()
}
def installed() {
	log.debug "installed()"
    refreshipaddresses()
}
def uninstalled() {
    removethermostats()
}
def updated() {
	log.debug "updated()"
    refreshipaddresses()
}
def ping() {
    log.debug "ping()"
    refresh()
}
def poll() {
	refresh()
}

//These commands will either add or remove the child thermostats, surprisingly....
private getthermostats(){
    //Before sending request for thermostat list we should remove current thermostats
	def cmds = []
	removethermostats()
	log.debug "Requesting List of Thermostats"
    cmds << getAction("/neorelay?device=hubzonesetup&command={\"GET_ZONES\":0}")
    return cmds
}
private removethermostats(){
	log.debug "Removing Child Thermostats"
    try {
        getChildDevices()?.each {
            deleteChildDevice(it.deviceNetworkId)
        }
    } catch (e) {
        log.error "Error deleting devices, probably none exist so all actually ok: ${e}"
    }
}


//These commands might be useful for doing bulk changes on all thermostats
def setAllAwayOn(){
    log.debug "Setting all Thermostats to Away"
    sendEvent(name: "allaway", value: "on", isStateChange: true)
    getAction("/neorelay?device=hubcommand&command={\"AWAY_ON\":0}")
}
def setAllAwayOff(){
	//def cmds = []
	log.debug "Setting all Thermostats to Home"
    sendEvent(name: "allaway", value: "off", isStateChange: true)
    getAction("/neorelay?device=hubcommand&command={\"AWAY_OFF\":0}")
}


//These functions can be called by the children in order to request something from the bridge
def childRequestingRefresh(String dni) {
	//Send Refresh command - this will occur for all thermostats, not just the one which requested it
    log.debug "Requesting refreshed info for all children"
	getAction("/neorelay?device=hubzonerefresh&command={\"INFO\":0}")
    //Add it here to try and do it automatically in 10mins - still not working reliably so disabled
    //runEvery10Minutes(childRequestingRefresh())
}
def childAwayOn(String dni) {
	//Send Child Away on command
    def deviceid = dni.replaceAll("neostat", "").replaceAll("-", " ")
    log.debug "Requesting away mode on for child ${deviceid}"
	getAction("/neorelay?device=${dni}&command={\"AWAY_ON\":\"${deviceid}\"}")
}
def childAwayOff(String dni) {
	//Send Child Away off command
    def deviceid = dni.replaceAll("neostat", "").replaceAll("-", " ")
    log.debug "Requesting away mode off for child ${deviceid}"
	getAction("/neorelay?device=${dni}&command={\"AWAY_OFF\":\"${deviceid}\"}")
}
def childHeat(String dni) {
	//Send Child Heat mode
    def deviceid = dni.replaceAll("neostat", "").replaceAll("-", " ")
    log.debug "Requesting heat mode for child ${deviceid}"
	getAction("/neorelay?device=${dni}&command={\"HEAT\":\"${deviceid}\"}")
}
def childSetTemp(String temp, String dni) {
	//Send Child Set Temp command
    def deviceid = dni.replaceAll("neostat", "").replaceAll("-", " ")
    log.debug "Requesting ${temp} degrees for child ${deviceid}"
	getAction("/neorelay?device=${dni}&command={\"SET_TEMP\":[${temp},\"${deviceid}\"]}")
}
def childCancelHold(String dni) {
	//Send Child Set Temp command
    def deviceid = dni.replaceAll("neostat", "").replaceAll("-", " ")
    log.debug "Requesting revert to schedule for child ${deviceid}"
	getAction("/neorelay?device=${dni}&command={\"TIMER_HOLD_OFF\":[0,\"${deviceid}\"]}")
}
def childHold(String temp, String hours, String minutes, String dni) {
	//Send Child Hold command
    def deviceid = dni.replaceAll("neostat", "").replaceAll("-", " ")
    log.debug "Requesting Hold at ${temp} degrees for ${hours}h:${mins}m for child ${deviceid}"
	getAction("/neorelay?device=${dni}&command={\"HOLD\":[{\"temp\":${temp},\"id\":\"\",\"hours\":${hours},\"minutes\":${minutes}},\"${deviceid}\"]}")
}
def childBoostOn(String hours, String minutes, String dni) {
	//Send Child Boost On command
    def deviceid = dni.replaceAll("neostat", "").replaceAll("-", " ")
    log.debug "Requesting boost on for child ${deviceid}"
	getAction("/neorelay?device=${dni}&command={\"BOOST_ON\":[{\"hours\":${hours},\"minutes\":${minutes}},\"${deviceid}\"]}")
}
def childBoostOff(String hours, String minutes, String dni) {
	//Send Child Boost On command
    def deviceid = dni.replaceAll("neostat", "").replaceAll("-", " ")
    log.debug "Requesting boost off for child ${deviceid}"
	getAction("/neorelay?device=${dni}&command={\"BOOST_OFF\":[{\"hours\":${hours},\"minutes\":${minutes}},\"${deviceid}\"]}")
}
def childFrostOn(String dni) {
	//Send Child Frost On command
    def deviceid = dni.replaceAll("neostat", "").replaceAll("-", " ")
    log.debug "Requesting frost on for child ${deviceid}"
	getAction("/neorelay?device=${dni}&command={\"FROST_ON\":\"${deviceid}\"}")
}
def childFrostOff(String dni) {
	//Send Child Frost On command
    def deviceid = dni.replaceAll("neostat", "").replaceAll("-", " ")
    log.debug "Requesting frost off for child ${deviceid}"
	getAction("/neorelay?device=${dni}&command={\"FROST_OFF\":\"${deviceid}\"}")
}
def childSetFrost(String temp, String dni) {
	//Send Child Set Frost command
    def deviceid = dni.replaceAll("neostat", "").replaceAll("-", " ")
    log.debug "Requesting set frost at ${temp} degrees for child ${deviceid}"
	getAction("/neorelay?device=${dni}&command={\"SET_FROST\":[\"${temp}\",\"${deviceid}\"]}")
}

// This function receives the response from the NeoHub bridge and updates things, or passes the response to an individual thermostat
def parse(description) {
    def map = [:]
    def events = []
    def cmds = []
    
    if (description == "updated") return
    def descMap = parseDescriptionAsMap(description)

    def body = new String(descMap["body"].decodeBase64())

    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    
    log.debug result
    cmds << sendEvent(name: "lastcommand", value: "${result}", isStateChange: true)
    
    if (result.containsKey("relaydevice")) {
        //Received a device key, hence process the command (null devices will be ignored to make it easier to avoid bugs)
		if (result.relaydevice == "hubzonesetup") {
        	//Device is a fake device for setting up zones, so create new devices if they dont exist
        	for (def currentthermostat in result.relayresult) {
            	//Iterate through each of the items in the result to create a device
                for ( item in currentthermostat) {
                	def thisthermostatname = ""
                    def thisthermostatdni = ""
                    //Prepare Thermostat name - prepend and postpend of settings if they exist
                    if (prestatname != null) {thisthermostatname = thisthermostatname + prestatname + " "}
                    thisthermostatname = thisthermostatname + item.key
                    if (poststatname != null) {thisthermostatname = thisthermostatname + " " + poststatname}
                    //Prepare DNI - Remove spaces and replace with a hyphen to prevent problems with HTML requests
                    thisthermostatdni = "neostat${item.key.replaceAll(" ", "-")}"
                    log.debug "Adding child Name: ${thisthermostatname}, DNI: ${thisthermostatdni}, ID: ${item.value} to ${device.hub.id}"
                    addChildDevice("cjcharles0", "Neo Thermostat", thisthermostatdni, device.hub.id, [name: thisthermostatname]) //, , label: thisthermostatname, completedSetup: true //device.hub.id
                }
        	}
        //Finally send a refresh command to get the latest info for each thermostat
        //cmds << refresh()
        }
		if (result.relaydevice == "hubzonerefresh") {
        	//Device is a fake device for refreshing all zone info, so process responses out to each thermostat device
            log.debug "Received a refresh command from NeoHub"
            for (deviceinfo in result.relayresult.devices) {
                //If we have a devices key then it is an INFO command so process the events to update the DH
                if (deviceinfo.DEVICE_TYPE < 10) {
                	//Device type < 10 should exclude repeaters
                    def curdevicename = deviceinfo.device.replaceAll(" ", "-")
                    def curdevice = getChildDevices().find { it.deviceNetworkId == "neostat"+curdevicename}
                    curdevice?.processNeoResponse(deviceinfo)
                }
            }
        }
        if (result.relaydevice.contains("neostat")) {
        	//We got a command/response for an individual thermostat so send data to thermostat
            def resultdevice = getChildDevices().find { it.deviceNetworkId == result.relaydevice}
            resultdevice?.processNeoResponse(result)
        }
    }
    return cmds
}

//These functions are helper functions to talk to the NeoHub Bridge
def getAction(uri){ 
  log.debug "uri ${uri}"
  updateDNI()
  
  def userpass
  
  if(password != null && password != "") 
    userpass = encodeCredentials("admin", password)
    
  def headers = getHeader(userpass)
  
  def hubAction = new physicalgraph.device.HubAction(
    method: "GET",
    path: uri,
    headers: headers
  )
  return hubAction    
}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private getHeader(userpass = null){
    def headers = [:]
    headers.put("Host", getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    if (userpass != null)
       headers.put("Authorization", userpass)
    return headers
}

private encodeCredentials(username, password){
	def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    return userpass
}

private updateDNI() { 
    if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
       device.deviceNetworkId = state.dni
    }
}

private getHostAddress() {
    if(getDeviceDataByName("bridgeip") && getDeviceDataByName("port")){
        return "${getDeviceDataByName("bridgeip")}:${getDeviceDataByName("bridgeport")}"
    }
    else {
	    return "${bridgeip}:80"
    }
}
