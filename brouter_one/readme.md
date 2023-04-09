# BRouter working with waterway

## How it starts

This folder contains the idea for BRouter project to integrate variable values in the lookup table.
Please see [BRouter feature request ](https://github.com/abrensch/brouter/issues/233)
The work is done with the update [PR](https://github.com/abrensch/brouter/pull/340).

So the sources here are removed.

Anyway the BRouter project is mainly a biker project. The data we talk about here are not present in normal BRouter data.

But you can play with the water routing data on [this page](https://brouter.grade.de/#map=17/52.94108/5.63231/CARTO&lonlats=5.658196,53.029342;5.630579,52.939754)
It contains routing for boat and canoe. 
The data are only generated for Europe and North-America.

## What it looks like:

The definition is done in a lookups.dat file. And it has small rule changes:
an asterisk symbolizes a placeholder for variable values
```
--- lookups.dat

maxheight;0000000001 default no_sign unsigned
maxheight;0000000001 none
maxheight;0000000001 *

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
	  switch lesser v:seamark:bridge:clearance_height_closed boat_height true 
	  false
	else false

assign initialcost 
	switch waiting_height      900
	0

```

The variable don't need to be controlled if it comes with a value or not. 
It can also have a string value as usual. But this should be place before the variable part.

The WaterwayModel/WaterwayPath class are helper classes and can be used to collect e.g. litre per hour.

An other problem is the definition for 'boat_height'. It is s fix value in the profile, but needs an update for an individual value of the user.
This should be a change for BRouter too. In BRouter-web this is done by %boat_height%.


Rules

* control draft CEMT or other waterway
* control bridge height, width
* control cable height on nodes
* control own boat and CEMT
* define max speed and flow
* define explicit speed limit
* define implicit speed limit
* define waiting time for locks, bridges, others 

See also samples waterway.brf and truck.brf (truck will need an own data pool)
