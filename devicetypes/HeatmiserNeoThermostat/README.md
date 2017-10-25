Instructions for setting up a Heatmiser Neo Thermostat (skip to step 4 if you have a pre-programmed NodeMCU)

1) Connect the Node MCU to your computer and ensure that it appears in Windows Device Manager as a COM port (serial/USB bridge) - you will need a driver though hopefully it will automatically find it. Take a note of this COM port.
2) Download the bin file and ESPtool.exe into the same folder and open a command prompt to this folder. (once the folder is open in Windows Explorer then hold shift and right click the background - that will allow you to 'Open a command prompt here')
3) Run the following command to flash the Node MCU chip (change COMXX to be COM3 if the COM port identified above is #3): "esptool.exe -vv -cd nodemcu -cb 115200 -cp COMXX -ca 0x00000 -cf Heatmiser_Neostat.ino.nodemcu.bin"
4) After the NodeMCU reboots (or turn it on by plugging in micro USB) you will see a WiFi Access Point called "NeoHubRelayBridge" (it might take a minute to appear), then connect to this access point (no password needed) and navigate to 192.168.4.1 (should be automatic on most phones). Here you should select your own WiFi network (2.4GHz only) by choosing from the menus.
5) After this is complete you should see the NodeMCU connect to your router. Now update the router so that this device is always given the same IP address, this is a DHCP static IP reservation and without it you may find things stop working after IP addresses change.
6) Navigate to the IP address that the Node MCU has been given (take note of it) and you should see a VERY simple dashboard. Also take note of the MAC address listed on this page.
7) Install the two device handlers (Bridge and Thermostat) into SmartThings. Copy and paste as code, nothing else should need to be changed.
8) Create a device in SmartThings and give it a name of "Neostat Bridge", plus a DNI (Device Network ID) of the MAC address from above. Ensure it uses the Device Handler called "Neo Hub Bridge" and other settings as required (e.g. set the 'Location' to be the correct Hub).
9) Open the new Bridge device on your phone and go into settings (top right). Here you should add the IP address for your NeoHub (get it from your router and make it a fixed DHCP reservation as with the NodeMCU), and the IP address of your NodeMCU (the IP address noted down above). I also suggest adding a Pre stat name of "Neostat" in the settings, this will ensure that your thermostats all start with Neostat (or similar) or finish with something if using post stat name - this will make them easier to identify in your device list.
10) Inside the Hub device you can now press Refresh IPs button and you should see the IP address boxes all populate. Now you can press Configure and this will send all information to the NodeMCU so the NodeMCU should now start working.
11) Finally you can now press Create Thermostat devices and it will create the required thermostats for each Neostat, using the naming rules from step 9.
12) Pressing refresh on any thermostat or the bridge will now show information for all devices and refresh status.


If you have enjoyed/benefited from this Integration, donations would be much appreciated though not required at http://paypal.me/cjcharles . It has taken a fair while to pull together (and expense of buying new Thermostats!!)! :)
