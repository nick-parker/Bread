Bread
==========

This is an experimental slicer which allows users to print with 3D layers on conventional FDM machines.

Why 3D Layers?
==========
In short, why not? We have these machines that can move in 3 dimensions, we should probably make them move in 3 dimensions. In terms of actual tangible benefits, adjusting your layer shape can:

-Eliminate the stairstep pattern typically seen on angled surfaces

-Reduce and manipulate the directional weakness caused by layer-layer bonds

-Print steeper overhangs without support material

-Manipulate which parts get printed first to reduce the number of material switches in multimaterial printing (future feature)

Getting Started
==========
At this time, I can make absolutely no guarantees about the behavior of this Slicer. Most hardware was not designed with constant rapid Z motion in mind. There are definitely still bugs in this code, some of which may cause bed crashes, jerky movement that causes skipped steps, etc. No Guarantees.

To simply use this Slicer:


1. Make sure you're running a 32 bit installation of Java.

2. Place Bread.jar, ClipperLib2.dll, config.txt, start.gcode, and end.gcode in a directory of your choice.

3. Change the relevant settings in config.txt for your machine, and configure start.gcode and end.gcode to your liking. Set very conservative values for speeds on your first print. 

4. Choose a part to print, and model the layer shape you want to use in a second .stl. /Prints/wave.stl and /Prints/wavesurface.stl is one example. Alignment between part and surface currently must be done in your design software, assuming you're using the command line interface.

5. In command line or shell, navigate to your chosen directory and execute Bread.jar [path to part.stl] [path to surface.stl] [path to config.txt] [path to write to]

Some important notes on making it play nice:
Your layer shape needs to cover everywhere the head is going to go in X and Y, sort of like a height function over the plane. I think there's a little hack which makes it OK for travels to go outside the layer shape, but you should really avoid that. This includes the initial travel from (0,0), so if possible get the origin inside your layer shape too.

I haven't really characterized the effects of NozzleOffset. It essentially tries to make up for the fact that parts of the code treat the disk-like nozzle as if it's a point. You can enable or disable it by simply setting TipRadius to 0, or your nozzle radius respectively. Enabling it will cause a lot of stress on your Z axis, but may improve surface finish.

You may notice that with steep layer shapes the surface finish gets awful very quickly. This is caused by the leading edge of your nozzle colliding with the previous layer. In general, don't angle your surface up above the XY plane by more than arctan(layer height / nozzle diameter). If you aren't using a NozzleOffset (TipRadius set to 0), this angle limit is lower.

Contributing
==========
While people are free to jump on this now, I'll be doing some pretty significant restructuring over the Summer. If you're eager to make a lasting contribution, the best way to do so is to optimize or improve some of the core components. For example, an octree implementation of Mesh3D.overlap could be dramatically faster than the current radix check, but I haven't gotten around to writing one. Surface3D.project(Point2D p) could benefit from similar optimizations.

Lastly, please submit issues if you find any. There are plenty.

License
=========
This project is licensed under GPL v3. For more information, see https://www.gnu.org/copyleft/gpl.html
