Bread
==========

This is an experimental slicer which allows users to print with 3D layers on conventional FDM machines.

Getting Started
==========
At this time, I can make absolutely no guarantees about the behavior of this Slicer. Most hardware was not designed with constant rapid Z motion in mind. There are definitely still bugs in this code, some of which may cause bed crashes, jerky movement that causes skipped steps, etc. No Guarantees.

To simply use this Slicer:


1. Place TreeSlicerCmd.jar, ClipperLib2.dll, config.txt, start.gcode, and end.gcode in a directory of your choice.

2. Change the relevant settings in config.txt for your machine, and configure start.gcode and end.gcode to your liking. Set very conservative values for speeds on your first print. 

3. Choose a part to print, and model the layer shape you want to use in a second .stl. /Prints/wave.stl and /Prints/wavesurface.stl is one example. Alignment between part and surface currently must be done in your design software, assuming you're using the command line interface.

4. In command line or shell, navigate to your chosen directory and execute TreeSlicerCmd.jar [path to part.stl] [path to surface.stl] [path to config.txt] [path to write to]

Contributing
==========
While people are free to jump on this now, I'll be doing some pretty significant restructuring over the Summer. If you're eager to make a lasting contribution, the best way to do so is to optimize or improve some of the core components. For example, an octree implementation of Mesh3D.overlap could be dramatically faster than the current radix check, but I haven't gotten around to writing one. Surface3D.project(Point2D p) could benefit from similar optimizations.

Lastly, please submit issues if you find any. There are plenty.

License
=========
This project is licensed under GPL v3. For more information, see https://www.gnu.org/copyleft/gpl.html
