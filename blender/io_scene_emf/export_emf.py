# SPDX-License-Identifier: GPL-3.0-or-later

import os
import time

import bpy
import bmesh
from mathutils import Vector, Quaternion
from bpy_extras import io_utils, node_shader_utils

EXPORT_COLLISION_NAME = ""

#blender myscene.blend --background --python myscript.py

def getChildren(obj):
	children = []
	for ob in bpy.data.objects:
		if ob.parent == obj:
			children.append(ob)
	return children


"""
Usage Notes:
To create a compound physics collision shape for a mesh in blender:

1. place the 3D cursor at the origin of the mesh object.
2. Add > Empty, name it "physics"
3. Create a physics shape with Add > Mesh > Cube, UV Sphere, Cylinder, Cone or create an arbitrary mesh for a ConvexHull shape.
4. Parent the new shape to the "physics" Empty.
5. The mesh name must start with: Box, Sphere, Cylinder, Cone, Capsule, or ConvexHull, depending on the shape you want.
6. Position and scale the shape object, but do not modify the internal vertices, unless it is a ConvexHull type.
7. Repeat step 3-6 until your shape is complete. Shapes can only be a 1-level deep hierarchy.
8. IMPORTANT: Select the empty object you named "physics"
9. Click File > Export > Physics Shapes (.yaml)
"""

def out_point3( v ):
	return "%g %g %g" % ( v.x, v.y, v.z )
def out_scale3( s ):
	return "%g %g %g" % ( s.x, s.y, s.z )
def out_quaternion( q ):
	return "%g %g %g %g" % ( q.x, q.y, q.z, q.w )


def get_physics_shape(obj):
	shape = ""
	props = { }
	name = obj.name.lower()
	scale = Vector(( abs(obj.scale.x), abs(obj.scale.y), abs(obj.scale.z) ))

	# BOX
	if    name.startswith('box') \
	   or name.startswith('cube'):
		shape = "Box"
		props["half-extents"] = out_scale3( scale )
	# SPHERE
	elif name.startswith('sph'):
		shape = "Sphere"
		props["radius"] = obj.scale.x
	# CONE
	elif name.startswith('cone'):
		shape = "Cone"
		props["radius"] = (obj.scale.x + obj.scale.y)*0.5
		props["size"] = obj.scale.z * 2.0
	# CYLINDER
	elif name.startswith('cyl'):
		shape = "Cylinder"
		props["radius"] = (obj.scale.x + obj.scale.y)*0.5
		props["size"] = obj.scale.z * 2.0
	# CAPSULE
	elif name.startswith('cap'):
		shape = "Capsule"
		props["radius"] = (obj.scale.x + obj.scale.y)*0.5
		props["size"] = obj.scale.z * 2.0
	# CONVEX-HULL
	elif name.startswith('convex'):
		shape = "ConvexHull"
		depsgraph = bpy.context.evaluated_depsgraph_get()
		ob_eval = obj.evaluated_get(depsgraph)
		mesh = ob_eval.to_mesh()
		props["points"] = ""
		for v in mesh.vertices:
			props["points"] += "" + out_point3( v.co ) + "|"
		props["points"] = props["points"].rstrip("|")
		if scale != Vector((1,1,1)):
			props["scale"] = out_scale3( scale )
		ob_eval.to_mesh_clear()

	print("            shape type: '" + str(shape) + "' from element name:'" + str(obj.name) + "'")

	if obj.matrix_world.to_translation() != Vector((0,0,0)):
		props["origin"] = out_point3(obj.matrix_world.to_translation())

	qrot = obj.matrix_world.to_quaternion()
	if qrot != Quaternion((1,0,0,0)):
		props["rotate"] = out_quaternion(qrot)
	props["mass"] = obj.scale.x * obj.scale.y * obj.scale.z * 100.0
	if props["mass"] < 0.01:
		props["mass"] = 0.01

	return (shape, props)


def write_collision_shape(objects, file, offset):
	fw = file.write
	if len(objects)==0:
		# no physical shape ...
		return
	string_offset = ""
	for iii in range(offset):
		string_offset += "\t"
	fw(string_offset + 'Physics:\n')
	for subObj in objects:
		print("        element='" + subObj.name + "' type '" + str(subObj.type) + "'")
		if     subObj.type != 'MESH' \
		   and subObj.type != 'EMPTY':
			continue
		(shape, props) = get_physics_shape(subObj)
		if shape=="":
			print("error of shape detection type ...")
			continue
		fw(string_offset + "\t" + shape + "\n" )
		for (k,v) in props.items():
			fw(string_offset + "\t\t%s:%s\n" % (k, v) )



def write_lights(objects, file, offset):
	"""Export light objects from a collection into the EMF Lights: section.
	Only LIGHT type objects are exported. Supports POINT, SUN, and SPOT light types.
	"""
	fw = file.write
	lights = [obj for obj in objects if obj.type == 'LIGHT']
	if len(lights) == 0:
		return
	string_offset = ""
	for iii in range(offset):
		string_offset += "\t"
	fw(string_offset + 'Lights:\n')
	for light_obj in lights:
		light_data = light_obj.data
		light_type = light_data.type  # 'POINT', 'SUN', 'SPOT', 'AREA'
		pos = light_obj.matrix_world.to_translation()
		color = light_data.color
		energy = light_data.energy

		print("        light='" + light_obj.name + "' type='" + light_type + "'")

		if light_type == 'POINT':
			fw(string_offset + "\tPoint\n")
			fw(string_offset + "\t\tcolor:%.6f %.6f %.6f\n" % (color.r, color.g, color.b))
			fw(string_offset + "\t\tposition:%.6f %.6f %.6f\n" % (pos.x, pos.y, pos.z))
			fw(string_offset + "\t\tradius:%.6f\n" % light_data.shadow_soft_size)
			# Convert Blender energy (Watts) to attenuation factors
			# constant=1.0, linear=0.0, quadratic based on energy
			# Higher energy = less attenuation (larger effective range)
			if energy > 0:
				quadratic = 1.0 / energy
			else:
				quadratic = 1.0
			fw(string_offset + "\t\tattenuation:1.0 0.0 %.6f\n" % quadratic)
		elif light_type == 'SUN':
			fw(string_offset + "\tSun\n")
			fw(string_offset + "\t\tcolor:%.6f %.6f %.6f\n" % (color.r, color.g, color.b))
			# Sun direction is the negative Z axis of the light object in world space
			direction = light_obj.matrix_world.to_quaternion() @ Vector((0.0, 0.0, -1.0))
			fw(string_offset + "\t\tdirection:%.6f %.6f %.6f\n" % (direction.x, direction.y, direction.z))
			fw(string_offset + "\t\tenergy:%.6f\n" % energy)
		elif light_type == 'SPOT':
			fw(string_offset + "\tSpot\n")
			fw(string_offset + "\t\tcolor:%.6f %.6f %.6f\n" % (color.r, color.g, color.b))
			fw(string_offset + "\t\tposition:%.6f %.6f %.6f\n" % (pos.x, pos.y, pos.z))
			direction = light_obj.matrix_world.to_quaternion() @ Vector((0.0, 0.0, -1.0))
			fw(string_offset + "\t\tdirection:%.6f %.6f %.6f\n" % (direction.x, direction.y, direction.z))
			fw(string_offset + "\t\tangle:%.6f\n" % light_data.spot_size)
			fw(string_offset + "\t\tblend:%.6f\n" % light_data.spot_blend)
			fw(string_offset + "\t\tradius:%.6f\n" % light_data.shadow_soft_size)
			if energy > 0:
				quadratic = 1.0 / energy
			else:
				quadratic = 1.0
			fw(string_offset + "\t\tattenuation:1.0 0.0 %.6f\n" % quadratic)
		elif light_type == 'AREA':
			# Treat area lights as point lights at their center
			fw(string_offset + "\tPoint\n")
			fw(string_offset + "\t\tcolor:%.6f %.6f %.6f\n" % (color.r, color.g, color.b))
			fw(string_offset + "\t\tposition:%.6f %.6f %.6f\n" % (pos.x, pos.y, pos.z))
			fw(string_offset + "\t\tradius:%.6f\n" % light_data.size)
			if energy > 0:
				quadratic = 1.0 / energy
			else:
				quadratic = 1.0
			fw(string_offset + "\t\tattenuation:1.0 0.0 %.6f\n" % quadratic)
		else:
			print("        WARNING: unsupported light type '" + light_type + "' - skipping")


def name_compat(name):
	if name is None:
		return 'None'
	else:
		return name.replace(' ', '_')


def mesh_triangulate(me):
	bm = bmesh.new()
	bm.from_mesh(me)
	bmesh.ops.triangulate(bm, faces=bm.faces)
	bm.to_mesh(me)
	bm.free()


def write_mtl(scene, file, filepath, path_mode, copy_set, mtl_dict):
	fw = file.write
	source_dir = os.path.dirname(bpy.data.filepath)
	dest_dir = os.path.dirname(filepath)
	fw('\n')
	mtl_dict_values = list(mtl_dict.values())
	mtl_dict_values.sort(key=lambda m: m[0])
	# Write material/image combinations we have used.
	# Using mtl_dict.values() directly gives un-predictable order.
	for mtl_mat_name, mat, face_img in mtl_dict_values:
		if mtl_mat_name.lower().startswith("palette_"):
			fw('# Just for information:\nPalettes:%s\n' % mtl_mat_name[8:])
		else:
			fw('Materials:%s\n' % mtl_mat_name)

		mat_wrap = node_shader_utils.PrincipledBSDFWrapper(mat) if mat else None

		if mat_wrap:
			use_mirror = mat_wrap.metallic != 0.0
			use_transparency = mat_wrap.alpha != 1.0

			# Empirical conversion from Principled BSDF roughness to OBJ specular exponent
			spec = (1.0 - mat_wrap.roughness) * 30
			spec *= spec
			fw('	Ns %.6f\n' % spec)

			# Ambient
			if use_mirror:
				fw('	Ka %.6f %.6f %.6f\n' % (mat_wrap.metallic, mat_wrap.metallic, mat_wrap.metallic))
			else:
				fw('	Ka %.6f %.6f %.6f\n' % (1.0, 1.0, 1.0))
			fw('	Kd %.6f %.6f %.6f\n' % mat_wrap.base_color[:3])  # Diffuse
			fw('	Ks %.6f %.6f %.6f\n' % (mat_wrap.specular, mat_wrap.specular, mat_wrap.specular))  # Specular
			# Emission
			emission_strength = mat_wrap.emission_strength
			emission = [emission_strength * c for c in mat_wrap.emission_color[:3]]
			fw('	Ke %.6f %.6f %.6f\n' % tuple(emission))
			fw('	vNi %.6f\n' % mat_wrap.ior)  # Refraction index
			fw('	d %.6f\n' % mat_wrap.alpha)  # Alpha (obj uses 'd' for dissolve)

			# See http://en.wikipedia.org/wiki/Wavefront_.obj_file for whole list of values...
			if mat_wrap.specular == 0:
				fw('	illum 1\n')  # no specular.
			elif use_mirror:
				if use_transparency:
					fw('	illum 6\n')  # Reflection, Transparency, Ray trace
				else:
					fw('	illum 3\n')  # Reflection and Ray trace
			elif use_transparency:
				fw('	illum 9\n')  # Glass transparency
			else:
				fw('	illum 2\n')  # light normally

			#### And now, the image textures...
			image_map = {
					"map_Kd": "base_color_texture",
					"map_Ka": None,  # ambient...
					"map_Ks": "specular_texture",
					"map_Ns": "roughness_texture",
					"map_d": "alpha_texture",
					"map_Tr": None,  # transmission roughness?
					"map_Bump": "normalmap_texture",
					"disp": None,  # displacement...
					"refl": "metallic_texture",
					"map_Ke": "emission_color_texture" if emission_strength != 0.0 else None,
					}

			for key, mat_wrap_key in sorted(image_map.items()):
				if mat_wrap_key is None:
					continue
				tex_wrap = getattr(mat_wrap, mat_wrap_key, None)
				if tex_wrap is None:
					continue
				image = tex_wrap.image
				if image is None:
					continue

				filepath = io_utils.path_reference(image.filepath, source_dir, dest_dir,
				                                   path_mode, "", copy_set, image.library)
				options = []
				if key == "map_Bump":
					if mat_wrap.normalmap_strength != 1.0:
						options.append('-bm %.6f' % mat_wrap.normalmap_strength)
				if tex_wrap.translation != Vector((0.0, 0.0, 0.0)):
					options.append('-o %.6f %.6f %.6f' % tex_wrap.translation[:])
				if tex_wrap.scale != Vector((1.0, 1.0, 1.0)):
					options.append('-s %.6f %.6f %.6f' % tex_wrap.scale[:])
				if options:
					fw('\t%s %s %s\n' % (key, " ".join(options), repr(filepath)[1:-1]))
				else:
					fw('\t%s %s\n' % (key, repr(filepath)[1:-1]))

		else:
			# Write a dummy material here?
			fw('	Ns 500\n')
			fw('	Ka 0.8 0.8 0.8\n')
			fw('	Kd 0.8 0.8 0.8\n')
			fw('	Ks 0.8 0.8 0.8\n')
			fw('	d 1\n')  # No alpha
			fw('	illum 2\n')  # light normally


def veckey3d(v):
	return round(v.x, 6), round(v.y, 6), round(v.z, 6)

def veckey2d(v):
	return round(v[0], 6), round(v[1], 6)

def write_mesh(scene, file, object, depsgraph, mtl_dict):
	print("**************** '" + str(object.name) + "' *******************")
	fw = file.write
	# Initialize totals, these are updated each object
	totverts = 1
	totuvco = 1
	totno = 1
	globalNormals = {}
	face_vert_index = 1
	# Used to reduce the usage of matname_texname materials, which can become annoying in case of
	# repeated exports/imports, yet keeping unique mat names per keys!
	# mtl_name: (material.name, image.name)
	mtl_rev_dict = {}


	if object.type != 'MESH':
		print(object.name + 'is not a mesh type - ignoring type=' + object.type)
		fw('# can not export:"%s":type="%s"\n' % (object.name, str(object.type)))
		return
	print("generate Object name:'%s'" % object.name)
	# ignore dupli children
	if object.parent and object.parent.instance_type in {'VERTS', 'FACES'}:
		print(object.name, 'is a dupli child - ignoring')
		return
	obs = []
	if object.instance_type != 'NONE':
		print('******************** creating instance_type on', object.name)
	else:
		obs = [(object, object.matrix_world)]
	for ob, ob_mat in obs:
		# Get the evaluated object (with modifiers applied)
		ob_eval = ob.evaluated_get(depsgraph)
		try:
			me = ob_eval.to_mesh()
		except RuntimeError:
			me = None
		if me is None:
			continue
		me.transform(ob_mat)
		print("ploppp:" + str(ob_mat) )
		# _must_ do this first since it re-allocs arrays
		# triangulate all the mesh:
		mesh_triangulate(me)
		# Normals are automatically computed in Blender 4.0+
		# export UV mapping:
		faceuv = len(me.uv_layers) > 0
		if faceuv:
			uv_texture = me.uv_layers.active.data[:]
			uv_layer = me.uv_layers.active.data[:]
		me_verts = me.vertices[:]
		# Make our own list so it can be sorted to reduce context switching
		face_index_pairs = [(face, index) for index, face in enumerate(me.polygons)]
		edges = me.edges
		if not (len(face_index_pairs) + len(edges) + len(me.vertices)):  # Make sure there is something to write
			# clean up
			ob_eval.to_mesh_clear()
			continue  # dont bother with this mesh.

		materials = me.materials[:]
		material_names = [m.name if m else None for m in materials]
		# avoid bad index errors
		if not materials:
			materials = [None]
			material_names = [name_compat(None)]
		# Sort by Material
		face_index_pairs.sort(key=lambda a: (a[0].material_index))


		# Set the default mat to no material and no image.
		contextMat = 0, 0  # Can never be this, so we will label a new material the first chance we get.
		name1 = ob.name
		name2 = ob.data.name
		if name1 == name2:
			obnamestring = name_compat(name1)
		else:
			obnamestring = '%s_%s' % (name_compat(name1), name_compat(name2))
		fw('Mesh:%s\n' % obnamestring)  # Write Object name
		###########################################################
		## Vert
		###########################################################
		fw('\tVertex:%d\n\t\t' % len(me_verts))
		for v in me_verts:
			fw('%.6f %.6f %.6f|' % v.co[:])
		fw('\n')
		###########################################################
		## UV
		###########################################################
		if faceuv:
			fw('\tUV-mapping:\n\t\t')
			# in case removing some of these dont get defined.
			uv = uvkey = uv_dict = f_index = uv_index = None
			uv_face_mapping = [None] * len(face_index_pairs)
			uv_dict = {}  # could use a set() here
			for f, f_index in face_index_pairs:
				uv_ls = uv_face_mapping[f_index] = []
				for uv_index, l_index in enumerate(f.loop_indices):
					uv = uv_layer[l_index].uv
					uvkey = veckey2d(uv)
					try:
						uv_k = uv_dict[uvkey]
					except KeyError:
						uv_k = uv_dict[uvkey] = len(uv_dict)
						fw('%.6f %.6f|' % uv[:])
					uv_ls.append(uv_k)
			uv_unique_count = len(uv_dict)
			del uv, uvkey, uv_dict, f_index, uv_index, uv_ls, uv_k
			# Only need uv_unique_count and uv_face_mapping
			fw('\n')
		else:
			print("does not use UV-MAPPING")
		###########################################################
		## NORMAL
		###########################################################
		if len(face_index_pairs) > 0:
			if face_index_pairs[0][0].use_smooth:
				localIsSmooth = 'vertex'
			else:
				localIsSmooth = 'face'
		else:
			localIsSmooth = 'face'
		fw('\tNormal(%s):%d\n\t\t' % (localIsSmooth, len(face_index_pairs)) )
		for f, f_index in face_index_pairs:
			if f.use_smooth:
				for v_idx in f.vertices:
					v = me_verts[v_idx]
					noKey = veckey3d(v.normal)
					if noKey not in globalNormals:
						globalNormals[noKey] = totno
						totno += 1
						fw('%.6f %.6f %.6f|' % noKey)
			else:
				# Hard, 1 normal from the face.
				noKey = veckey3d(f.normal)
				if noKey not in globalNormals:
					globalNormals[noKey] = totno
					totno += 1
					fw('%.6f %.6f %.6f|' % noKey)

		fw('\n')
		if not faceuv:
			f_image = None
		###########################################################
		## faces
		###########################################################
		fw('\tFace:%d' % len(face_index_pairs))
		for f, f_index in face_index_pairs:
			f_image = None
			f_smooth = f.use_smooth
			f_mat = min(f.material_index, len(materials) - 1)
			tmp_faceuv = faceuv
			if tmp_faceuv:
				tface = uv_texture[f_index]
				if 'image' in dir(uv_texture[f_index]):
					f_image = tface.image
			# MAKE KEY
			if tmp_faceuv and f_image:  # Object is always true.
				key = material_names[f_mat], f_image.name
			else:
				key = material_names[f_mat], None  # No image, use None instead.
			# CHECK FOR CONTEXT SWITCH
			if key == contextMat:
				pass  # Context already switched, dont do anything
			else:
				if key[0] is None and key[1] is None:
					# inform the use of a material:
					fw("\n\t\t---:")  # mat, image
				else:
					mat_data = mtl_dict.get(key)
					if not mat_data:
						# First add to global dict so we can export to mtl
						# Then write mtl
						mtl_name = "%s" % name_compat(key[0])
						if mtl_rev_dict.get(mtl_name, None) not in {key, None}:
							if key[1] is None:
								tmp_ext = "_NONE"
							else:
								tmp_ext = "_%s" % name_compat(key[1])
							i = 0
							while mtl_rev_dict.get(mtl_name + tmp_ext, None) not in {key, None}:
								i += 1
								tmp_ext = "_%3d" % i
							mtl_name += tmp_ext
						mat_data = mtl_dict[key] = mtl_name, materials[f_mat], f_image
						mtl_rev_dict[mtl_name] = key
					# set the use of a material:
					fw("\n\t\t%s\n\t\t\t" % mat_data[0].replace("palette_", "palette:"))
			contextMat = key
			f_v = [(vi, me_verts[v_idx]) for vi, v_idx in enumerate(f.vertices)]
			if tmp_faceuv:
				# export the normals:
				if f_smooth:  # Smoothed, use vertex normals
					for vi, v in f_v:
						fw(" %d/%d/%d" %
						   (v.index + totverts-1,
						    totuvco + uv_face_mapping[f_index][vi]-1,
						    globalNormals[veckey3d(v.normal)]-1,
						   ))  # vert, uv, normal
				else:  # No smoothing, face normals
					no = globalNormals[veckey3d(f.normal)]
					for vi, v in f_v:
						fw(" %d/%d/%d" %
						   (v.index + totverts-1,
						    totuvco + uv_face_mapping[f_index][vi]-1,
						    no-1,
						   ))  # vert, uv, normal
				face_vert_index += len(f_v)
			else:  # No UV's
				# export the normals:
				if f_smooth:  # Smoothed, use vertex normals
					for vi, v in f_v:
						fw(" %d/%d" % (
								   v.index + totverts-1,
								   globalNormals[veckey3d(v.normal)]-1,
								   ))
				else:  # No smoothing, face normals
					no = globalNormals[veckey3d(f.normal)]
					for vi, v in f_v:
						fw(" %d/%d" % (v.index + totverts-1, no-1))
			fw('|')
		fw('\n')

		# Make the indices global rather then per mesh
		totverts += len(me_verts)
		if faceuv:
			totuvco += uv_unique_count
		# clean up
		ob_eval.to_mesh_clear()

	#####################################################################
	## Save collision shapes (for one object):
	#####################################################################
	for subObj in getChildren(object):
		print("     child:'%s' check if start with : '%s'" % (subObj.name, EXPORT_COLLISION_NAME))
		if subObj.name.lower().startswith(EXPORT_COLLISION_NAME):
			print("     find physics:'%s'" % (subObj.name))
			write_collision_shape(subObj, file, object.scale, 1)



"""
 " @brief Basic write function. The context and options must be already set.
"""
def write_file(filepath,
               collection,
               depsgraph,
               scene,
               EXPORT_PATH_MODE='AUTO',
               EXPORT_BINARY_MODE=False,
               EXPORT_COLLISION_NAME=""
               ):
	print('EMF Export path: %r' % filepath)

	time1 = time.time()

	mtlfilepath = os.path.splitext(filepath)[0] + ".mtl"

	file = open(filepath, "w", encoding="utf8", newline="\n")
	fw = file.write
	# Write Header
	fw('EMF(STRING)\n') # if binary:fw('EMF(BINARY)\n')
	fw('# Blender v%s EMF File: %r\n' % (bpy.app.version_string, os.path.basename(bpy.data.filepath)))

	# A Dict of Materials
	# (material.name, image.name):matname_imagename # matname_imagename has gaps removed.
	mtl_dict = {}

	copy_set = set()

	print("Find Mesh in collection: '" + str(collection.name) + "'")
	# Get all meshes
	for ob_main in collection.objects:
		if ob_main.type == 'MESH':
			write_mesh(scene, file, ob_main, depsgraph, mtl_dict)

	print("Find Physics and Lights in collection: '" + str(collection.name) + "'")
	for col in collection.children:
		print("    - name: " + str(col.name) + "/" + str(col.name_full) )
		if col.name.lower().startswith("physic"):
			write_collision_shape(col.objects, file, 0)
		elif col.name.lower().startswith("light"):
			write_lights(col.objects, file, 0)

	#####################################################################
	## Now we have all our materials, save them in the material section
	#####################################################################
	write_mtl(scene, file, mtlfilepath, EXPORT_PATH_MODE, copy_set, mtl_dict)

	#####################################################################
	## End of the file generation:
	#####################################################################
	file.close()

	# copy all collected files.
	io_utils.path_reference_copy(copy_set)

	print("EMF Export time: %.2f" % (time.time() - time1))


"""
 " @brief generate the requested object file ... with his material inside and ...
 "
"""
def _write(context,
           filepath,
           EXPORT_SEL_ONLY,
           EXPORT_PATH_MODE,
           EXPORT_BINARY_MODE,
           EXPORT_COLLISION_NAME,
           ):
	#
	base_name, ext = os.path.splitext(filepath)
	# create the output name:
	context_name = [base_name, '', '', ext]  # Base name, scene name, frame number, extension
	# get the current scene:
	scene = context.scene
	# Get the dependency graph for evaluated objects (modifiers, etc.)
	depsgraph = context.evaluated_depsgraph_get()
	# Exit edit mode before exporting, so current object states are exported properly.
	if bpy.ops.object.mode_set.poll():
		bpy.ops.object.mode_set(mode='OBJECT')
	# get the current frame selected:
	frame = scene.frame_current
	# Loop through all frames in the scene and export.
	scene.frame_set(frame, subframe=0.0)
	# get only the object that are selected or all...
	if EXPORT_SEL_ONLY:
		collection = bpy.context.collection
	else:
		print("collection auto detect 'root': ")
		collection = scene.collection
		if collection.name != "root":
			for col in collection.children:
				if col.name == "root":
					collection = col
					break
		if collection.name != "root":
			raise Exception("Can not detect collection 'root'")

	print("============================================================================================")
	draw_tree(scene.collection, 0, collection)
	print("============================================================================================")

	full_path = ''.join(context_name)

	write_file(full_path,
	           collection,
	           depsgraph,
	           scene,
	           EXPORT_PATH_MODE,
	           EXPORT_BINARY_MODE,
	           EXPORT_COLLISION_NAME,
	           )



##
## Display all the element in the collection tree (for help debug exporting)
##
def draw_tree(collection, offset, export_collection):
	string_offset = ""
	for iii in range(offset):
		string_offset += "\t"
	if export_collection == collection:
		print(string_offset + "- collection: '" + str(collection.name) + "'         !!!! exported node !!!!")
	else:
		print(string_offset + "- collection: '" + str(collection.name) + "'")
	for col in collection.children:
		draw_tree(col, offset+1, export_collection)
	if 'objects' in dir(collection):
		for obj in collection.objects:
			print(string_offset + "\t- objects: '" + str(obj.name) + "' type '" + str(obj.type) + "'")

"""
 " @brief Save the current element in the file requested.
 "
"""
def save(operator,
         context,
         filepath="",
         use_selection=True,
         use_binary=False,
         collision_object_name="",
         path_mode='AUTO'
         ):
	_write(context,
	       filepath,
	       EXPORT_SEL_ONLY=use_selection,
	       EXPORT_PATH_MODE=path_mode,
	       EXPORT_BINARY_MODE=use_binary,
	       EXPORT_COLLISION_NAME=collision_object_name,
	       )

	return {'FINISHED'}
