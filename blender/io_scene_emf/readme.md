# EGE Mesh File (EMF) - Blender Extension

Export Blender meshes to the EGE game engine EMF format.

Compatible with **Blender 4.2+** (including Blender 5.0).

## Installation

### As extension (Blender 4.2+)

1. Open Blender > Edit > Preferences > Add-ons
2. Click "Install from Disk..." and select the `io_scene_emf` folder
3. Enable the "EGE Mesh file format EMF" addon

### Manual install

Copy the `io_scene_emf` folder to your Blender extensions directory:
- Linux: `~/.config/blender/5.0/extensions/user_default/`
- Windows: `%APPDATA%\Blender Foundation\Blender\5.0\extensions\user_default\`
- macOS: `~/Library/Application Support/Blender/5.0/extensions/user_default/`

## Usage

### Interactive export

File > Export > EGE Mesh File (.emf)

### Batch export (headless)

```bash
blender --background -P ./exportEmf.py
```

## Physics shapes

To create compound physics collision shapes for a mesh:

1. Place the 3D cursor at the origin of the mesh object
2. Add > Empty, name it "physics"
3. Create a physics shape (Add > Mesh > Cube, UV Sphere, Cylinder, Cone, or arbitrary mesh for ConvexHull)
4. Parent the new shape to the "physics" Empty
5. The mesh name must start with: `Box`, `Sphere`, `Cylinder`, `Cone`, `Capsule`, or `ConvexHull`
6. Position and scale the shape object (do not modify vertices unless ConvexHull type)
7. Repeat steps 3-6 for compound shapes (1-level deep hierarchy only)

## Lights

To export lights with a mesh:

1. Create a collection named `lights` (or any name starting with "light") inside the root collection
2. Add light objects (Point, Sun, Spot, Area) into that collection
3. Position and configure the lights as desired

Exported light properties:
- **Point/Area**: color, position, attenuation (constant, linear, quadratic)
- **Sun**: color, direction, energy
- **Spot**: color, position, direction, angle, attenuation

EMF format example:
```
Lights:
	Point
		color:1.0 1.0 1.0
		position:0.0 0.0 3.5
		attenuation:1.0 0.0 0.1
	Sun
		color:1.0 0.9 0.8
		direction:0.0 0.0 -1.0
		energy:10.0
```
