# BRouter working with waterway


This folder contains an idea for BRouter project to integrate variable values in the lookup table.
Please see [BRouter feature request ](https://github.com/abrensch/brouter/issues/233)

like:

seamark:bridge:clearance_height;0000000001 *
depth;0000000001 *

The problem of all is this is not visible inside the BRouter rules file.
So I worked around with a control command like this:

assign control_depth
	switch not depth= true 
	false

The final control is done in a WaterwayModel/WaterwayPath class

Rules

* control draft on CEMT=0 or other waterway
* control bridge height, width
* control cable height on nodes
* control own boat and CEMT
* define max speed and flow
* define explicit speed limit
* define implicit speed limit
* define waiting time for locks, bridges, others 

This is not the end.