An extension for OpenRocket that allows simulating different wind speeds and directions for different altitudes
==============================================

This is an example simulation extension plugin for OpenRocket.  It defines a simulation extension that multiplies the motor thrust by a provided value.


Compiling and usage
-------------------

Compile by running `ant`.  This creates the file `MultiLevelWind.jar`.  Copy this to the OpenRocket plugin directory (`~/.openrocket/Plugins/` on Linux, `~/Library/Application Support/OpenRocket/Plugins/` on Mac, `...\Application Data\OpenRocket\ThrustCurves\Plugins\` on Windows).  Then restart OpenRocket.

You can add and configure it in the simulation edit dialog under Simulation options -> Add extension -> Flight -> MultiLevelWind
