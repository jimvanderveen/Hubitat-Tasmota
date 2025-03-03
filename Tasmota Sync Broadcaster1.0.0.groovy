/**
*  Tasmota Sync Broadcaster
*  Version: v1.0.0
*  Download: See importUrl in definition
*  Description: An app that utilizes the tasmotaCustomCommand function present in the Tasmota Sync family of drivers to sent a single Tasmota command to multiple Tasmota devices making certain operations much easier.
*  In addition to simplifying the distribution of commands it is also a reference point for all those commands that you can't quite remember as well as some that you never knew.
*
*  Copyright 2022 Gary J. Milne  
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation.
*
*  Authors Notes:
*  For more information on Tasmota Sync drivers check out these resources:
*  Original posting on Hubitat Community forum.  https://community.hubitat.com/t/tasmota-sync-drivers-native-and-real-time-synchronization-between-hubitat-and-tasmota-11/93651
*  How to upgrade from Tasmota 8.X to Tasmota 11.X  https://github.com/GaryMilne/Hubitat-Tasmota/blob/main/How%20to%20Upgrade%20from%20Tasmota%20from%208.X%20to%2011.X.pdf
*  Tasmota Sync Installation and Use Guide https://github.com/GaryMilne/Hubitat-Tasmota/blob/main/Tasmota%20Sync%20Documentation.pdf
*  Tasmota Sync Universal Sensor Driver - Hubitat Thread https://community.hubitat.com/t/released-tasmota-sync-universal-sensor-driver/95474
*  Tasmota Sync Universal Sensor Driver documentation: https://github.com/GaryMilne/Hubitat-Tasmota/blob/main/Tasmota%20Sync%20Sensor%20Documentation.pdf
*
*
*  BROADCASTER - CHANGELOG
*  Version 1.0.0 - Initial Release
*
*  Gary Milne - September 8th, 2022
*
**/
import groovy.transform.Field

@Field static final commandMap = [
    //Each record looks like this "Short description" : "Tasmota Command|Long Description|Category1,Category2...|Reboot|WiFi|Refresh",
    //Common Commands
    "*** Select a Command ***" : " |None. |All,Common,Switch,Bulb,Configuration,Rules,Tasmota Upgrade,WiFi,Fan,Dimmer,Energy,Web UI|No|No|No",
    "Turn on a switch\\device." : "Power On|Turns on Switch\\Switch1 (if present). |Common,Switch,Bulb|No|No|No",
    "Turn off a switch\\device." : "Power Off|Turns Off Switch\\Switch 1 (if present). |Common,Switch|No|No|No",
    "Toggle the power to a switch\\device." : "Power Toggle|Changes the state of Switch\\Switch 1 (if present). |Common,Switch|No|No|No",
    "Restart a device." : "Restart 1|Restarts the Tasmota device.|Common|Yes|No|No",
    "Enable Fade on a bulb." : "Fade 1|Bulbs will fade during on\\off\\changes. 0 = do not use fade, 1 = use fade. Use FadeSpeed for rate of fade. |Common,Bulb|No|No|No",
    "Set Fade speed." : "Speed 10|Set fade speed from fast 1 to very slow 40, + = increase speed, - = decrease speed. The Speed value represents the time in 0.5s to fade from 0 to 100% (or the reverse). Example: Speed 4 takes 2.0s to fade from full brightness to black, or 0.5s to move from 75% to 100%. |Common,Bulb|No|No|No",
    
    //Configuration Commands
    "Set Power on state." : "PowerOnState 3|Control power state when the device is powered up. 0 = OFF after power up, 1 = ON after power up, 2 = toggle power from last saved state, 3 = use last saved state (default)," +\
                        "4 = turn power(s) ON and disable further power control, 5 = after a PulseTime period turn power(s) ON (acts as inverted PulseTime mode). |Configuration|No|No|No",
    "Set Led Power state." : "LedPower 0|Control the state of the default status LED. 0 = turn LED OFF and set LedState 0, 1 = turn LED ON and set LedState 8, 2 = toggle LED and set LedState 0, (Use Backlog LedPower 0; SetOption31 1 to disable LED even when Wi-Fi or MQTT is not connected)|Configuration|No|No|No",
    "Set Led Power <X> state." : "LedPower1 0|Control the state of connected to LedLink(i). 0 = turn LED OFF and set LedState 0, 1 = turn LED ON and set LedState 0, 2 = toggle LED and set LedState 0. Enabled only when LedLink(i) is configured.|Configuration|No|No|No",
    "Enable Fade at power up." : "PowerOnFade 1|0 = don't Fade at startup (default), 0 = don't Fade at startup (default). By default fading is not enabled at boot because of stuttering caused by wi-fi connection. Same as SetOption91. |Bulb|No|No|No",
    
    //Rules
    "Enable a Rule." : "Rule1 on|Enables rule1. |Rules|No|No|No",
    "Disable a Rule." : "Rule1 off|Disables rule1. |Rules|No|No|No",
    "Clear a Rule." : " Rule1 ''|Clears rule1. Note: <u>Single quotes will be replaced with double quotes before command is sent.</u> |Rules|No|No|No",
    "Enable OneShot on a rule." : " Rule1 5|Enable OneShot detection on Rule1. Something like 'ENERGY#Power>60' would only fire once time the power rises above the 60 Watt threshold.|Rules|No|No|No",
    "Disable OneShot on a rule." : " Rule1 4|Disable OneShot detection on Rule1. Something like 'ENERGY#Power>60' would fire repeatedly while the power remains above the 60 Watt threshold.|Rules|No|No|No",
    "Reset the color of a bulb at power up." : "Rule1 ON power1#boot DO color 000000FF60 ENDON|Resets the color of a bulb to a default value on power up. <b>You must enable the rule with 'rule1 on'.</b> |Rules,Bulb|No|No|No",
    "Turn a switch back on if it is turned off." : "rule1 ON Power1#state=0 do Backlog Delay 10; Power1 1 ENDON|Turns a relay back on after a 10 second delay. Remember to activate the rule with 'rule1 on'.|Rules|No|No|No",
    "Turn a switch back on X seconds after it has been turned off." : "rule1 ON Power1#state=0 do Backlog Delay 60; Power1 1 ENDON|Turns a relay back on after a 60 second delay. Remember to activate the rule with 'rule1 on'.|Rules|No|No|No",
    "Turn a switch off X seconds after it has been turned on" : "rule1 ON Power1#state=1 do Backlog Delay 60; Power1 0 ENDON|Turns a relay back off after a 60 second delay. Remember to activate the rule with 'rule1 on'.|Rules|No|No|No",
    "Keep the power state of two devices in sync." : "rule1 ON Power1#state DO WebSend [192.168.0.120] POWER1 %value% ENDON.|Sets the power state on device B to match the power state to device A bypassing Hubitat. Remember to activate the rule with 'rule1 on'.|Rules|No|No|No",
    "Change TelePeriod based on Power" : "backlog ; rule1 on ; rule1 5 ; rule1 ON ENERGY#Power<5 do TelePeriod 60 ENDON ON ENERGY#Power>10 do TelePeriod 10 ENDON|Changes the TelePeriod of a device automatically based upon it's power consumption. In this example the rule is enabled and uses OneShot detection.|Rules|No|No|No",
    "Change TelePeriod based on Temperature" : "backlog ; rule1 on ; rule1 5 ; rule1 ON DS18B20-1#Temperature > 80 do TelePeriod 10 ENDON ON DS18B20-1#Temperature < 80 do TelePeriod 60 ENDON|Changes the TelePeriod of a device automatically based upon it's temperature. In this example the rule is enabled and uses OneShot detection.|Rules|No|No|No",
        
    //Tasmota Upgrade
    "*Configure Over The Air Upgrade location for Standard Version." : "OtaUrl http://ota.tasmota.com/tasmota/release/tasmota.bin.gz|<b>Standard Version:</b> Sets the OTAURL on the Tasmota device for over the air upgrades. Upgrades initiated by <b>Upgrade 1</b>. |Tasmota Upgrade|No|No|No",
    "Configure Over The Air Upgrade location for Sensors Version." : "OtaUrl http://ota.tasmota.com/tasmota/release/tasmota-sensors.bin.gz|Sets the OTAURL on the Tasmota device for over the air upgrades using the <b>Sensor Version</b>. Upgrades initiated by <b>Upgrade 1</b>. |Tasmota Upgrade|No|No|No",
    "Configure Over The Air Upgrade location for Lite Version." : "OtaUrl http://ota.tasmota.com/tasmota/release/tasmota-lite.bin.gz|Sets the OTAURL on the Tasmota device for over the air upgrades using the <b>Lite Version</b>. Upgrades initiated by <b>Upgrade 1</b>. |Tasmota Upgrade|No|No|No",
    "Configure Over The Air Upgrade location for Minimal Version." : "OtaUrl http://ota.tasmota.com/tasmota/release/tasmota-minimal.bin.gz|Sets the OTAURL on the Tasmota device for over the air upgrades using the <b>Minimal Version</b>. Upgrades initiated by <b>Upgrade 1</b>. |Tasmota Upgrade|No|No|No",              
    "Configure Over The Air Upgrade location for IR Version." : "OtaUrl http://ota.tasmota.com/tasmota/release/tasmota-ir.bin.gz|Sets the OTAURL on the Tasmota device for over the air upgrades using the <b>IR Version</b>. Upgrades initiated by <b>Upgrade 1</b>. |Tasmota Upgrade|No|No|No",
    "Configure Over The Air Upgrade location for Display Version." : "OtaUrl http://ota.tasmota.com/tasmota/release/tasmota-display.bin.gz|Sets the OTAURL on the Tasmota device for over the air upgrades using the <b>Display Version</b>. Upgrades initiated by <b>Upgrade 1</b>. |Tasmota Upgrade|No|No|No",
    "Initiate OTA upgrade." : "Upgrade 1|Initiates an OTA upgrade if one is available. Process will load Tasmota Minimal version first followed by the version specified by OtaURL. Multiple device reboots. Expect entire process to take 2-5 minutes. |Tasmota Upgrade|Yes|No|No",
    "Configure OTA compatibility check." : "SetOption78 0|OTA compatibility check, 0 = enabled (default), 1 = disabled. |Tasmota Upgrade|No|No|No",
    
    //WiFi
    "Configure WiFi SSID and Password." : "Backlog SSID1 <myssid>; Password1 <mypassword>; SSID2 <myssid2>; Password2 <mypassword2>|Sets the primary and secondary WiFi SSID and password. Edit command to match your environment. <b>Be careful, incorrect use may render your device unable to connect to the network.</b>|WiFi|Yes|Yes|No",
    "Switch WiFi Access Point." : "AP 0|0 = switch to other Wi-Fi Access Point, 1 = select Wi-Fi Access Point 1, 2 = select Wi-Fi Access Point 2. |WiFi|No|Yes|No",
    "Configure WiFi LED Status." : "SetOption31 0|Set status LED blinking during Wi-Fi and MQTT connection problems. LedPower must be set to 0 for this feature to work. 0 = Enabled (default), 1 = Disabled.|WiFi|No|No|No",
    "WiFi Network Scan." : "SetOption56 1|Wi-Fi network scan to select strongest signal on restart (network has to be visible), 0 = disable (default), 1 = enable.|WiFi|No|No|No",
    
    //Fan
    "Set Fan Speed." : "FanSpeed 1|Applies to iFan02/iFan03/iFan04. 0 = turn fan OFF, 1..3 = set fan speed, + = increase fan speed,- = decrease fan speed. |Fan|No|No|No",
    "Configure speed change buzzer." : "BuzzerActive 0|iFan03/iFan04 Buzzer Control on speed change. 0 = Disabled, 1 = Enabled. This is the same as SetOption 67. |Fan|No|No|No",
    
    //Dimmers
    "Set Dimmer level." : "Dimmer 75|0..100 set dimmer value from 0 to 100%, + = increase by DimmerStep value (default = 10), - = decrease by DimmerStep value (default=10). |Dimmer,Bulb|No|No|No",
    "Set the dimming range." : "Set the DimmerRange 25,255|Change dimming range. (dimmerMin),(dimmerMax) = set the internal dimming range from minimum to maximum value (0..255, 0..255). Does not change Dimmer command behavior. " +
        "\nMany dimmers do not dim smoothly across the range from 1 - 100. This command addresses that.|Dimmer|No|No|No",
    "Set the step increments for the dimmer +//- commands." : "DimmerStep 5|1..50 - set Dimmer +/- step value. (default =10). |Dimmer,Bulb|No|No|No",

    //Configuration
    "Change Wemo & Hue Bridge emulation." : "Emulation 0|Disables emulation (default). 1 = enable Belkin WeMo emulation for Alexa. 2 = enable Hue Bridge emulation for Alexa.|Configuration|Yes|No|No",
    "Clear all volatile variables." : "Backlog var1 ''; var2 ''; var3 ''; var4 ''; var5 ''; var6 ''; var7 ''; var8 ''; var9 ''; var10 ''; var11 ''; var12 ''; var13 ''; var14 ''; var15 ''; var16 '' |Clears all Var variables on a Tasmota device. Note: <u>Single quotes will be replaced with double quotes before command is sent.</u> |Configuration|No|No|No",
    "Clear all non-volatile variables." : "Backlog mem1 ''; mem2 ''; mem3 ''; mem4 ''; mem5 ''; mem6 ''; mem7 ''; mem8 ''; mem9 ''; mem10 ''; mem11 ''; mem12 ''; mem13 ''; mem14 ''; mem15 ''; mem16 '' |Clears all mem veriables on a Tasmota device. Note: <u>Single quotes will be replaced with double quotes before command is sent.</u> |Configuration|No|No|No",
    "Set Power State after restart." : "SetOption0 1|Save power state and use after restart (=SaveState). 0 = disable (see note below), 1 = enable (default). Note: Note: Power state means on/off state of eg. relays or lights. Other parameters like color, color temperature, brightness, dimmer, etc. are still saved when changed. To disable saving other parameters see SaveData.|Configuration|No|No|No",
    "Allow Update of Dimmer/Color/CT without turning power on." : "SetOption20 1|Allows the update of Dimmer/Color/CT values without turning on power to the device. Allows for 'silent' changes to some bulb settings.on, 0 = disable (default), 1 = enable|Configuration,Bulb|No|No|No",
    "Change CT range for Alexa compatibility." : "SetOption82 1|Reduce the CT range from 153..500 to 200.380 to accommodate with Alexa range, 0 = CT ranges from 153 to 500 (default), 1 = CT ranges from 200 to 380 (although you can still set in from 153 to 500).|Configuration,Bulb|No|No|No", 
    "Change Friendly Name." : "FriendlyName1 Dryer Plug|1 = Reset friendly name to firmware default, (value) = set friendly name (32 char limit). Example <b>FriendlyName1 Dryer Plug</b> Tasmota supports Friendly Names 1-8.|Configuration|No|No|No",
    "Reset\\Change Hostname." : "Hostname NewHost|1 = reset hostname to MQTT_TOPIC-(4digits) and restart; (value) = set hostname (32 char limit) and restart. If hostname contains a percent symbol it will be reset to the default instead. See FAQ for allowed characters.|Configuration,WiFi|Yes|No|No",
    
     //Sensors
    "Set resolution for Pressure values." : "PressRes 3|Pressure resolution. 0..3 = maximum number of decimal places|Sensors|No|No|No",
    "Set resolution for Temperature values." : "TempRes 3|Temperature resolution. 0..3 = maximum number of decimal places|Sensors|No|No|Yes",
    "Set Temperature units C or F." : "SetOption8 1|Show temperature in 0 = Celsius (default), 1 = Fahrenheit.|Sensors|No|No|Yes",
    "Set Pressure units hPa or mmHg." : "SetOption24 1|Set pressure units 0 = hPa (default), 1 = mmHg. See also SetOption139.|Sensors|No|No|Yes",
    "Set Sensor Name Seperator, hyphen or underscore." : "SetOption64 1|Switch between - or _ as sensor name separator, 0 = sensor name index separator is - (hyphen) (default), 1 = sensor name index separator is _ (underscore). Affects DS18X20, DHT, BMP and SHT3X sensor names in tele messages. |Sensors|No|No|Yes",
    "Set Pressure switch units to mm or inches." : "SetOption139 1|When SetOption24 1 switch pressure unit to: 0 = mmHg (default), 1 = inHg|Sensors|No|No|Yes",
          
    //Energy
    "Set Current resolution." : "AmpRes 3|Current sensor resolution. 0..3 = maximum number of decimal places|Energy|No|No|No",
    "Calibrate Current to known value." : "CurrentSet 833|Calibrate current to target value in mA. 100W bulb and 120V supply. myvalue = 100/120 * 1000 = 833ma. <b>CurrentSet 833</b>|Energy|No|No|No",
    "Calibrate Power to known value." : "PowerSet 100|Calibrate power to a target value in watts. Use 'Kill-a-Watt' meter if you have one or a filament bulb with a high known wattage such as 100W. <b>PowerSet 100</b>|Energy|No|No|No",
    "Set Voltage resolution." : "VoltRes 3|Voltage resolution. 0..3 = maximum number of decimal places|Energy|No|No|No",
    "Calibrate Voltage to known value." : "VoltageSet 120|Calibrate voltage to a target value in volts. Usually set to match the nominal voltage of the household such as 120V or 240V. <b>VoltageSet 120</b>|Energy|No|No|No",
    "Set Wattage resolution." : "WattRes 3|Wattage resolution. 0..3 = maximum number of decimal places|Energy|No|No|No",
    "Energy Monitoring when power off." : "SetOption21 1|Energy monitoring when power is off, 0 = disable (default), 1 = enable.|Energy|No|No|No",
    "Over Power Time Limit." : "SetOption33 5|Number of seconds for which the maximum power limit can be exceeded before the power is turned off, 1..250 = set number of seconds (default = 5)|Energy|No|No|No",
                  
    //Web UI
    "Change label on a WebUI button." : "WebButton1 Pump 1|Changes the default label on the Tasmota WebUI for Relay1 from 1 to Pump 1. Especially usefully when you have a device with a large number of relays like a sprinkler controller example:<b>WebButton4 Front Lawn</b>.|Web UI|No|No|Yes",
    "Display Hostname and IP in WebUI." : "SetOption53 1|Display hostname and IP address in GUI, 0 = disable (default), 1 = enable.|Web UI|No|No|Yes",
    "Disable display of model name in Web UI" : "SetOption141 1|Disable display of model name in webUI header, 0 = enable display (default), 1 = disable display.|Web UI|No|No|Yes",
    "Change WebUI to use color scheme Dark." : "WebColor {'WebColor':['#eaeaea','#252525','#4f4f4f','#000000','#dddddd','#65c115','#1f1f1f','#ff5661','#008000','#faffff','#1fa3ec','#0e70a4','#d43535','#931f1f','#47c266','#5aaf6f','#faffff','#999999','#eaeaea']}|" + 
        "Changes the Web UI to Color Scheme - Dark (default). Note: <u>Single quotes will be replaced with double quotes before command is sent.</u>|Web UI|No|No|Yes",
    "Change WebUI to use color scheme Light." : "WebColor {'WebColor':['#000000','#ffffff','#f2f2f2','#000000','#ffffff','#000000','#ffffff','#ff0000','#008000','#ffffff','#1fa3ec','#0e70a4','#d43535','#931f1f','#47c266','#5aaf6f','#ffffff','#999999','#000000']}|" +
        "Changes the Web UI to Color Scheme - Light (default until 6.7.1). Note: <u>Single quotes will be replaced with double quotes before command is sent.</u>|Web UI|No|No|Yes",
    "Change WebUI to use color scheme Halloween." : "WebColor {'WebColor':['#cccccc','#2f3133','#3d3f41','#dddddd','#293134','#ffb000','#293134','#ff5661','#008000','#ffffff','#ec7600','#bf5f00','#d43535','#931f1f','#47c266','#5aaf6f','#ffffff','#999999','#bc4d90']}|" + 
        "Changes the Web UI to Color Scheme - Halloween. Note: <u>Single quotes will be replaced with double quotes before command is sent.</u>|Web UI|No|No|Yes",
    "Change WebUI to use color scheme Navy." : "WebColor {'WebColor':['#e0e0c0','#000033','#4f4f4f','#000000','#dddddd','#a7f432','#1e1e1e','#ff0000','#008000','#ffffff','#1fa3ec','#0e70a4','#d43535','#931f1f','#47c266','#5aaf6f','#ffffff','#999999','#eedd77']}|" + 
        "Changes the Web UI to Color Scheme - Navy. Note: <u>Single quotes will be replaced with double quotes before command is sent.</u>|Web UI|No|No|Yes",
    "Change WebUI to use color scheme Purple Rain." : "WebColor {'WebColor':['#e0e0c0','#000033','#4f4f4f','#000000','#dddddd','#a7f432','#1e1e1e','#ff0000','#008000','#ffffff','#1fa3ec','#0e70a4','#d43535','#931f1f','#47c266','#5aaf6f','#ffffff','#999999','#eedd77']}|" + 
        "Changes the Web UI to Color Scheme - Purple Rain. Note: <u>Single quotes will be replaced with double quotes before command is sent.</u>|Web UI|No|No|Yes",
    "Change WebUI to use color scheme Solarized Dark." : "WebColor {'WebColor':['#eaeaea','#252525','#282531','#eaeaea','#282531','#d7ccff','#1d1b26','#ff5661','#008000','#faffff','#694fa8','#4d3e7f','#b73d5d','#822c43','#1f917c','#156353','#faffff','#716b7f','#eaeaea']}|" + 
        "Changes the Web UI to Color Scheme - Solarized Dark. Note: <u>Single quotes will be replaced with double quotes before command is sent.</u>|Web UI|No|No|Yes",
    "Change WebUI device name." : "DeviceName Living Room Lamp|Device name displayed in the webUI and used for HA autodiscovery.(default = FriendlyName1 value).|Web UI|No|No|Yes",
    "Display Data\\Time on WebUI." : "WebTime 0,24| WebTime <start_pos>,<end_pos> = show part of date and/or time in WebUI based on '2017-03-07T11:08:02-07:00'  Date 0,10; DateTime 0,19; Time 11,19; DateTimeUTC 0,24; None 0,0.|Web UI|No|No|No",
    "Change Global text color" : "WebColor1 #ffffff|Change the color of the global text on the Web UI|Web UI|No|No|Yes",
    "Change Global background color" : "WebColor2 #ffffff|Change the color of the global background on the Web UI|Web UI|No|No|Yes",
    "Change Form background color" : "WebColor3 #ffffff|Change the color of the form background on the Web UI|Web UI|No|No|Yes",
    "Change Input background color" : "WebColor4 #ffffff|Change the color of the input background on the Web UI|Web UI|No|No|Yes",
    "Change Input text color" : "WebColor5 #ffffff|Change the color of the input text in the Web UI|Web UI|No|No|Yes",
    "Change Console text color" : "WebColor6 #ffffff|Change the color of the input text in the Web UI|Web UI|No|No|Yes",
    "Change Console background color" : "WebColor7 #ffffff|Change the color of the console background in the Web UI|Web UI|No|No|Yes",
    "Change Warning text color" : "WebColor8 #ffffff|Change the color of the warning text in the Web UI|Web UI|No|No|Yes",
    "Change Success text color" : "WebColor9 #ffffff|Change the color of the success text in the Web UI|Web UI|No|No|Yes",
    "Change Button text color" : "WebColor10 #ffffff|Change the color of the button text in the Web UI|Web UI|No|No|Yes",
    "Change Button color" : "WebColor11 #ffffff|Change the color of the button in the Web UI|Web UI|No|No|Yes",
    "Change Button hovered over color" : "WebColor12 #ffffff|Change the color of the button hovered over in the Web UI|Web UI|No|No|Yes",
    "Change Restart/Reset/Delete button color" : "WebColor13 #ffffff|Change the color of the Restart/Reset/Delete button in the Web UI|Web UI|No|No|Yes",
    "Change Restart/Reset/Delete button hover over color" : "WebColor14 #ffffff|Change the color of the Restart/Reset/Delete hover over button in the Web UI|Web UI|No|No|Yes",
    "Change Save button color" : "WebColor15 #ffffff|Change the color of the Save button in the Web UI|Web UI|No|No|Yes",
    "Change Save button hover over color" : "WebColor16 #ffffff|Change the color of the Save button hover over in the Web UI|Web UI|No|No|Yes",
    "Change Config timer tab text color" : "WebColor17 #ffffff|Change the color of the Config timer tab text in the Web UI|Web UI|No|No|Yes",
    "Change Config timer tab background color" : "WebColor18 #ffffff|Change the color of the Config timer tab background in the Web UI|Web UI|No|No|Yes",
    "Change Module title and FriendlyName text color" : "WebColor19 #ffffff|Change the color of Module title and FriendlyName text in the Web UI|Web UI|No|No|Yes"
    ]


definition(
    name: "Tasmota Sync Broadcaster",
    namespace: "garyjmilne",
    author: "Gary J. Milne",
    description: "Allows for mass operations across multiple Tasmota devices using the Tasmota Sync family of drivers",
    category: "Tasmota Sync",
    importUrl: "https://raw.githubusercontent.com/GaryMilne/Hubitat-Tasmota/main/Broadcaster.groovy",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    singleThreaded: true
)


preferences {
	page (name: "mainPage")
    page (name: "devicePage")
}

def mainPage() {
    if (state.flags == null ) initialize()
    
	dynamicPage(name: "mainPage", install: true, uninstall: true ) {

        //Check the state of the controls to figure out what changed and then refresh the screen
        isUseListChanged()
        isCommandListChanged()
        isFilterChanged()
        isCommandTextChanged()
        isColorChanged()
        refreshUI()
        
        section(title: titleise("Step 1 - Select Devices") ){  //Step1
                //paragraph "<div style='background-color:#000080; height: 0.5px; margin-top:0em; margin-bottom:0em ; border: 0;'></div>"    //Horizontal Line
                href "devicePage", title: "Populate Device Lists", width:6,  description: "Devices Selected?", state: selectOk?.devicePage ? "complete" : null
                
                //Invisible horizontal line to prevent the two controls being presented on the same line.
                paragraph "<div style='background-color:#FFFFFF; height: 0.5px; margin-top:-1em; margin-bottom:0em ; border: 0;'></div>"    //Horizontal Line
                
                //UseList selection
                input (name: "useList", title: "<b>Select Device List to Use:</b>", type: "enum", options: getLists(), required:false, submitOnChange:true, width:3)

                } //End Step 1

        section(title: titleise("Step 2 - Select Commands") ){ //Step 2
                input (name: "commandListItem", title: "<b>Select Tasmota Command:</b>", type: "enum", options: getCommands(), required:false, submitOnChange:true, width:6, defaultValue: "*** Select a Command ***")
                //Drop down list of Tasmota command descriptions..    
                input(name: "filter", title: "<b>Filter by Category:</b>", type: "enum", options: filters(), defaultValue: "All", required:false, submitOnChange:true, width:4)            
                
                paragraph "<div style='color:#17202A;text-align:left; margin-top:0em; font-size:14px'><b>Description:</b> ${state.info.description}</div>"  
                line = "<div style='color:#17202A;text-align:left; margin-top:-1em; font-size:14px'><b>Reboots Device:</b> ${state.info.reboot}  <b>Network Loss:</b> ${state.info.network}  <b>Browser Refresh Required:</b> ${state.info.refresh}</div>" 
                line = line.replace("Yes","<b><font color = 'red'>Yes</font color = 'black'></b>")
                paragraph line

                //OR statement.
                paragraph "<div style='color:#0000FF;text-align:left; margin-top:0em; margin-bottom:0em; font-size:16px'><b> - OR - </b></div>"
                
                //commandText box which is what gets executed           
                input("commandText", "text", title:"<b>Enter\\Edit Tasmota command line:</b> (<b>Tab or Enter</b> to save changes after edit.).", description: "", required: false, submitOnChange: true, width: 8)
            
                //Show the color picker if the filter is set to webUI.
                if (filter == "Web UI") {
                    input "myColor", "color", title: "<b>Selected Color: ${myColor}</b>", required: false, submitOnChange: true, width: 2, defaultValue: '#ffffff'
                    }
                }

        
        section(title: titleise("Step 3 - Execute Commands") ){ //Step 3
                //Button to initiate sending command to Tasmota devices.
                input(name: "runCommand", type: "button", title: "Run Command", backgroundColor: "Green", textColor: "white", submitOnChange: true, width: 3)
                //paragraph "<div style='background-color:#D5D8DC; height: 1.5px; margin-top:2em; border: 0;'></div>"    //Horizontal Line
                }    //End of section
        
        section() { //Debug Area
                    input "isDebug", "bool", title: "Enable Debug", required: false, multiple: false, defaultValue: false, submitOnChange: true, width: 4
                    input "isVerbose", "bool", title: "Enable Verbose Logging", required: false, multiple: false, defaultValue: false, submitOnChange: true, width: 4
                    //input "myColor", "color", title: "Select Color", required: false, submitOnChange: true, width: 4
                    
                    if (isDebug == true){
                        //Show info regarding control changes
                        paragraph "<div style='background-color:#000080; height: 0.5px; margin-top:3em; margin-bottom:0em ; font-size:12px'></div>"    //Horizontal Line
                        //input (name: "logLevel", title: "<b>Logging Level:</b>", type: "enum", options: ["0","1","2","3"], required:false, submitOnChange:true, width:2, defaultValue:1)
                        line = "<div style='color:#17202A;text-align:left; margin-top:0em; margin-bottom:0em ; font-size:12px'><b>useListChanged:</b>${state.flags.useListChanged}    <b>commandListChanged:</b>${state.flags.commandListChanged}    <b>filterChanged:</b>${state.flags.filterChanged}    <b>commandTextChanged:</b>${state.flags.commandTextChanged}    <b>colorChanged:</b>${state.flags.colorChanged}    <b>commandListItem:</b> ${commandListItem}    <br><b>commandText:</b> ${commandText}</div>"
                        line = line.replace("true","<b><font color = 'green'> True</font color = 'black'></b>")
                        line = line.replace("false","<b><font color = 'grey'> False</font color = 'grey'></b>")
                        paragraph line
                        
                        //Show Device Selection Info
                        myDevices = activeList()
                        paragraph "<div style='color:#17202A;text-align:left; margin-top:0em; margin-bottom:0em ; font-size:12px'><b>Selected Devices:</b> ${myDevices}    <b>Quantity:</b> ${state.deviceCount}    <b>Color Selected:</b> ${myColor}    <b>Filter Category:</b> ${filter}</div>"
                        paragraph "<div style='color:#17202A;text-align:left; margin-top:0em; margin-bottom:0em ; font-size:12px'><b>state.info.reboot:</b> ${state.info.reboot}  <b>state.info.network:</b> ${state.info.network}  <b>state.info.refresh:</b> ${state.info.refresh}    <b>state.info.description:</b> ${state.info.description}</div>" 
                        paragraph "<b>Tasmota Commands: </b><a href='https://tasmota.github.io/docs/Commands/'>https://tasmota.github.io/docs/Commands/</a>"
                    }
                }
        //Mark the execution of the runCommand complete
        state.flags.isRunCommand = false
        }  
}


//We have to split the string into a Command and Parameters to be compatible with the Tasmota Sync Drivers.
def runCommand(){
    state.flags.isRunCommand = true
    log("runCommand", "Tasmota command is ${commandText}", 1)
    
    if (commandText == null ) {
        log("runCommand", "Nothing done, commandText is null.", 0)
        return
    }
    
    if (state.deviceCount <=0 ) {
        log("runCommand", "The device count is zero. Check you have selected a Device List to use.", 0)
        return
    }
    
    newCommandText = commandText.replace("'","\"") 
    spacePos = commandText.indexOf(" ")
    length = commandText.length()
    
    try {
        command = newCommandText.substring(0, spacePos).trim()
        parameters = newCommandText.substring(spacePos).trim()        
        paramLen = parameters.trim().length() 
        
        //If there are no parameters send a null value as the second parameter as tasmotaCustomCommand requires two values.
        if (paramLen == 0) { parameters = " " }
        }
        
    catch (Exception e) {
            log("runCommand", "Exception $e in runCommand", 0)
            log.debug 
        }
    
    data = newCommandText.tokenize(' ')

    devs = activeList()
    log("runCommand", "Selected devices: ${devs}", 1)
    
    devs.each { 
        it.tasmotaCustomCommand(command, parameters)
        log("runCommand", "Sent tasmotaCustomCommand('${command}','${parameters}') to device ${it}.", 0)
        }
}


//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//**************
//**************  UI Functions.
//**************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************

//Makes the neccessary changes to the UI to keep things in sync based upon which controls have changed values.
void refreshUI(){
    
    log("refreshUI", "Initiated Refresh.", 2)
    
    //Handle the filterChanged event. If the filter changes we reset the commandList, the commandText and clear the description fields as we don't have a command selected.
    if (state.flags.filterChanged == true ) {
        log("refreshUI", "Processing filterChanged UI logic", 3)
        //Clears the commandText and commandListItem controls.
        app.updateSetting("commandListItem", [value:"*** Select a Command ***", type:"enum"])  //Works
        app.updateSetting("commandText", "")
        clearDescription()
        return
    }
    
    if (state.flags.colorChanged == true && commandText.toLowerCase().contains("color") == true){
        details = settings.commandText.tokenize(' ')
        app.updateSetting("commandText", details[0] + " ${myColor}")
        log("refreshUI", "New color selected:  ${myColor}", 1)
        return
    }
    
    //If the command selected from the commandList has not changed but the commandText has changed then it can only be because of an edit.
    //In that case we do not need to lookup the info and we do not want to clear the description fields so we just exit early.
    if ( state.flags.commandTextChanged == true && state.flags.commandListChanged == false && state.flags.isRunCommand == false) {
        log("refreshUI", "User has edited the commandText to: '${commandText}'", 0)
        return
    }

    //Clear the values if we find nothing in the lookup.
    if (commandListItem == null) {
        log("refreshUI", "No matching command entry found. commandListItem was null.", 2)
        app.updateSetting("commandText", "")
        clearDescription()
        return
    }
    
    //Find the entry that matches the description
    def myitem = commandMap.find{ it.key == commandListItem.toString() }
    value = myitem.value.toString()
            
    //Parse it into its constituent values, details[0] is the Tasmota command, details[1] is the info description, details[2] is the category, details[3] is reboot, details[4] is network, details[5] is refresh 
    details = value.tokenize('|')
    if ( state.flags.isRunCommand == false ) app.updateSetting("commandText", details[0])
    log("refreshUI", "Retrieved command data for ${details[0]}.", 2)
    state.info.description = details[1]
    state.info.reboot = details[3]
    state.info.network = details[4]
    state.info.refresh = details[5]
    
    log("refreshUI", "Refresh Complete.", 3)
}

//This is the device list selection page
def devicePage() {
	dynamicPage(name: "devicePage") {
        section ("<b>Populate Tasmota Device Lists</b>") {
            input "devices0", "capability.*", title: "All Tasmota Devices" , multiple: true, required: false, defaultValue: null
		    input "devices1", "capability.bulb", title: "Tasmota Bulbs" , multiple: true, required: false, defaultValue: null
            input "devices2", "capability.switch", title: "Tasmota Plugs" , multiple: true, required: false, defaultValue: null
            input "devices3", "capability.switch", title: "Tasmota Switches" , multiple: true, required: false, defaultValue: null
        	input "devices4", "capability.fanControl", title: "Tasmota Fans" , multiple: true, required: false, defaultValue: null
        	input "devices5", "capability.powerMeter", title: "Tasmota Power Meters" , multiple: true, required: false, defaultValue: null
            input "devices6", "capability.sensor", title: "Tasmota Sensor Devices" , multiple: true, required: false, defaultValue: null
            input "devices7", "capability.*", title: "Custom Device List" , multiple: true, required: false, defaultValue: null
            input "devices8", "capability.*", title: "Single Device" , multiple: false, required: false, defaultValue: null
            paragraph "<div style='color:#17202A;text-align:left; margin-top:0em; font-size:12px'><b>You can use these device list to target specific types of devices for given commands. The Custom Device List is nominally for Ad-Hoc selections.</b></div>"  //Line with Description of the selected command.
            //input "devices", "device.", title: "Tasmota Devices" , multiple: true, required: true, defaultValue: null
            //Declaring devices here makes it App global and can be accessed later.
            //devices = devices0
		}
	}
}

//Set the titles to a consistent style.
def titleise(title){
    title = "<div style='color:#0000FF;text-align:left; margin-top:0em; font-size:20px'><b><u>${title}</u></b></div>"
}

//Clears the description variables.
def clearDescription(){
    state.info.description = ""
    state.info.reboot = ""
    state.info.network = ""
    state.info.refresh = ""
    log("clearDescription", "Description cleared!", 2)
}

//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//**************
//**************  Miscellaneous Functions.
//**************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************

//Count the number of items in a (Device) List by searching for the commas.
int itemCount(itemList){
    myString = itemList.toString()
    count = myString.count(",")
    if ( itemList == null ) { return 0 }
    else { return count + 1 }
}

def getSelectOk()
{
    def status = [ devicePage: devices0 ?: devices1 ?: devices2 ?: devices3 ?: devices4 ?: devices5 ?: devices6 ?: devices7 ?: devices8 ]
	status << [all: status.devicePage]
}

//This is the standard button handler that receives the click of any button control.
def appButtonHandler(btn) {
    switch(btn) {
        case "runCommand":
            runCommand()
        break
    }
}

//Log status messages
private log(name, message, int loglevel){
    threshold = 0
    if ( isDebug == true ) threshold = 1
    if ( isVerbose == true ) threshold = 2
    if ( isDebug == true && isVerbose == true ) threshold = 3
    
    //This is a quick way to filter out messages based on loglevel
	if ( loglevel > threshold) {return}
    if ( loglevel <= 1 ) { log.info ( message )  }
    if ( loglevel >= 2 ) { log.debug ( message ) }
}



//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//**************
//**************  List Generation.
//**************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************

//Returns a list of device lists that are populated with at least on device.
def getLists(){
    log("getLists", "Preparing list of populated device lists.", 3)
    def list = []
    if ( devices0 != null && devices0.size() > 0 ) list.add("All Tasmota Devices")
    if ( devices1 != null && devices1.size() > 0 ) list.add("Tasmota Bulbs")
    if ( devices2 != null && devices2.size() > 0 ) list.add("Tasmota Plugs")
    if ( devices3 != null && devices3.size() > 0 ) list.add("Tasmota Switches")
    if ( devices4 != null && devices4.size() > 0 ) list.add("Tasmota Fans")
    if ( devices5 != null && devices5.size() > 0 ) list.add("Tasmota Power Meters")
    if ( devices6 != null && devices6.size() > 0 ) list.add("Tasmota Sensor Devices")
    if ( devices7 != null && devices7.size() > 0 ) list.add("Custom Device List")
    if ( devices8 != null ) list.add("Single Device")
    return list
}


//Returns the Active Device List based on the user selection.
def activeList(){     
    if (useList == "All Tasmota Devices") devices = devices0
    if (useList == "Tasmota Bulbs") devices = devices1
    if (useList == "Tasmota Plugs") devices = devices2
    if (useList == "Tasmota Switches") devices = devices3
    if (useList == "Tasmota Fans") devices = devices4
    if (useList == "Tasmota Power Meters") devices = devices5
    if (useList == "Tasmota Sensor Devices") devices = devices6
    if (useList == "Custom Device List") devices = devices7
    if (useList == "Single Device") devices = devices8

    state.deviceCount = itemCount(devices)
    log("activeList", "Selected Device List is: ${devices.toString()}", 3)
    return devices
}


//Returns a list of unique Categories that are extracted from the commandMap.  These categories can be used to filter the command list.
def filters(){
    def myfilter = []
    myfilter.add("All")
    //Note: There is no way to exit an each loop before it has run to completion. Not an issue when the lists are small.
    commandMap.each{
        key = it.key.toString()
        value = it.value.toString()
        details = value.tokenize('|')
        //details[0] is the Tasmota command, details[1] is the info description, details[2] is the category, details[3] is reboot, details[4] is network, details[5] is refresh 
        categories = details[2].tokenize(',')
        categories.each { myfilter.add (it) }
        }
    log("filters", "Filters List is: ${myfilter.unique().sort().toString()}", 3)
    return myfilter.unique().sort()
}

//Returns a list of commands that is usable by the drop down control. A filter is applied if requested.
def getCommands(){
    //Note: There is no way to exit an each loop before it has run to completion. Not an issue when the lists are small.
    def commandList = []
    commandMap.each { 
        key = it.key.toString()
        value = it.value.toString()
        //Split the value into two strings, before the | and after.  
        details = value.tokenize('|')
        //details[0] is the Tasmota command, details[1] is the info description, details[2] is the category, details[3] is reboot, details[4] is network, details[5] is refresh 
        categories = details[2].tokenize(',')
        categories.each { category ->
            if ( category == filter || filter == "All") {
                commandList.add(key) 
            }
        }
    }
    log("getCommands", "commandList is: ${return commandList.unique().sort()}", 3)
    return commandList.unique().sort()
}




//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//**************
//**************  Test functions to control logic flow.
//**************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************

//Determine if something has changed in the filter list.
def isFilterChanged(){
    if (state.filterHistory.new.toString() != settings.filter.toString() ) {
        state.filterHistory.old = state.filterHistory.new
        state.filterHistory.new = settings.filter
        state.flags.filterChanged = true
        log("isFilterChanged", "Filter changed to: ${settings.filter}", 1)
    }
    else state.flags.filterChanged = false
}

//Determine if something has changed in the command list.
def isCommandListChanged(){
    if (state.commandListHistory.new != commandListItem) {
        state.commandListHistory.old = state.commandListHistory.new
        state.commandListHistory.new = settings.commandListItem
        state.flags.commandListChanged = true
        log("isCommandListChanged", "Command Selected is: ${settings.commandListItem}", 1)
    } 
    else state.flags.commandListChanged = false
}

//Determine if something has changed in the commandText text box.
def isCommandTextChanged(){
    if (state.commandTextHistory.new.toString() != settings.commandText.toString() ) {
        state.commandTextHistory.old = state.commandTextHistory.new
        state.commandTextHistory.new = settings.commandText
        state.flags.commandTextChanged = true
       }
    else state.flags.commandTextChanged = false
}

//Determine if something has changed in the filter list.
def isUseListChanged(){
    if (state.useListHistory.new != settings.useList ) {
        state.useListHistory.old = state.useListHistory.new
        state.useListHistory.new = settings.useList
        state.flags.useListChanged = true
        log("isUseListChanged", "Selected Device List Changed to : ${settings.useList}", 1)
    }
    else state.flags.useListChanged = false
}

//Determine if the selected color has changed.
def isColorChanged(){
    if (state.colorHistory.new != settings.myColor ) {
        state.colorHistory.old = state.colorHistory.new
        state.colorHistory.new = settings.myColor
        state.flags.colorChanged = true
        log("isColorChanged", "Selected Color to : ${settings.myColor}", 1)
    }
    else state.flags.colorChanged = false
}



//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//**************
//**************  Installation and update routines.
//**************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************
//************************************************************************************************************************************************************************************************************************

// Initialize the states only when first installed...
void installed() {
    log.info "Running Install."    
	initialize()
	app.clearSetting("debugOutput")	// app.updateSetting() only updates, won't create.
	app.clearSetting("descTextEnable")
	if (descTextEnable) log.info "Installed with settings: ${settings}"
    //Populate the Command list
    getCommands()
}


void updated() {
    log.info "Running Updated."    
	unschedule()
	initialize()
}


void initialize() {
    log.info "Running Initialize."        
    state.info = [description: " ", reboot: " ", network: " ", refresh: " "]
    state.flags = [filterChanged: false, commandListChanged: false, commandTextChanged: false, useListChanged: false, isRunCommand: false]
    state.commandTextHistory = [new: "seed", old: ""]
    state.filterHistory = [new: "seed", old: ""]
    state.commandListHistory = [new: "seed", old: ""]
    state.useListHistory = [new: "seed", old: ""]
    state.colorHistory = [new: "seed", old: ""]
    state.deviceCount = 0
    
//	app.clearSetting("debugOutput")	// app.updateSetting() only updates, won't create.

}



def setDebug(dbg, inf) {
	app.updateSetting("debugOutput",[value:dbg, type:"bool"])
	app.updateSetting("descTextEnable",[value:inf, type:"bool"])
	if (descTextEnable) log.info "debugOutput: $debugOutput, descTextEnable: $descTextEnable"
}


