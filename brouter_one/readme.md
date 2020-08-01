# BRouter working with waterway


This folder contains an idea for BRouter project to integrate variable values in the lookup table.
Please see [BRouter feature request ](https://github.com/abrensch/brouter/issues/233)

like:


```
--- lookups.dat
seamark:bridge:clearance_height_closed;0000000001 *
depth;0000000001 *

```

This definition save the values behind the variables into the BRouter segment files as a positive integer.
To have floating values the stored integer is build by value * 100.
The parsing und converting is only done for meter and feet at the moment.

To use the values later on in the profile they are marked as variable 'v:'
   e.g. v:seamark:bridge:clearance_height_closed
   
It can be only compared with the lesser, greater, equal logic

use like:


```
--- waterway.brf
assign boat_height	1.5  

assign waiting_height 
	if seamark:bridge:category=opening then 
	  switch  and not      seamark:bridge:clearance_height_closed=
	              lesser v:seamark:bridge:clearance_height_closed boat_height true 
	  false
	else false

assign initialcost 
	switch waiting_height      900
	0

```

This shows also a problem on using this variables.
The variable has to be controlled if it comes with a value or not. Otherwise if delivers -1 as result and can produce wrong results.
The WaterwayModel/WaterwayPath class are helper classes and can be used to collect e.g. litre per hour.

An other problem is the definition for 'boat_height'. It is s fix value in the profile, but needs an update for an individual value of the user.
This should be a change for BRouter too. In BRouter-web this is done by %boat_height%.

Changes on the original for RoutingContext, BExpression and BExpressionContext

Rules

* control draft CEMT or other waterway
* control bridge height, width
* control cable height on nodes
* control own boat and CEMT
* define max speed and flow
* define explicit speed limit
* define implicit speed limit
* define waiting time for locks, bridges, others 

This is not the end.