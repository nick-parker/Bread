;start.gcode
G28 ; home all axes
G1 X160 Y160 F6000; Center
G90 ; use absolute coordinates
G92 X0 Y0 Z0
M104 S198 ; set temperature
M106 S255
M109 S198
; End start.gcode