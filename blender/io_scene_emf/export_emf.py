
import os
import time

import bpy
import mathutils
import bpy_extras.io_utils

EXPORT_COLLISION_NAME = ""

#blender myscene.blend --background --python myscript.py

def getChildren(obj):
	children = []
	for ob in bpy.data.objects:
		if ob.parent == obj:
			children.append(ob)
	return children


import bpy
from bpy.props import *
import mathutils, math, struct
from mathutils import *
import os
from os import remove
import time
import bpy_extras
from bpy_extras.io_utils import ExportHelper 
import time
import shutil

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

"""
	use_y_up = BoolProperty(name="Convert To Y-Up",
							description="Converts the values to a Y-Axis Up coordinate system",
							default=True)
"""

def out_point3( v ):
	return "%g %g %g" % ( v.x, v.y, v.z )
def out_scale3( s ):
	return "%g %g %g" % ( s.x, s.y, s.z )
def out_quaternion( q ):
	return "%g %g %g %g" % ( q.x, q.y, q.z, q.w )


def get_physics_shape(obj, mainObjScale):
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
		props["radius"] = obj.scale.x * mainObjScale.x
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
		mesh = obj.to_mesh()
		props["points"] = ""
		for v in mesh.vertices:
			props["points"] += "" + out_point3( v.co ) + "|"
		props["points"] = props["points"].rstrip("|")
		if scale != Vector((1,1,1)):
			props["scale"] = out_scale3( scale )
		# remove mesh
	
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


def write_collision_shape(object, file, mainObjScale, offset):
	if len(getChildren(object))==0:
		# no phisical shape ...
		return
	string_offset = ""
	for iii in range(offset):
		string_offset += "\t"
	file.write(string_offset + 'Physics:\n')
	for subObj in getChildren(object):
		print("        element='" + subObj.name + "' type '" + str(subObj.type) + "'")
		if     subObj.type != 'MESH' \
		   and subObj.type != 'EMPTY':
			continue
		(shape, props) = get_physics_shape(subObj, mainObjScale)
		if shape=="":
			print("error of shape detection type ...");
			continue
		file.write(string_offset + "\t" + shape + "\n" )
		for (k,v) in props.items():
			file.write(string_offset + "\t\t%s:%s\n" % (k, v) )



def name_compat(name):
	if name is None:
		return 'None'
	else:
		return name.replace(' ', '_')


def mesh_triangulate(me):
	import bmesh
	bm = bmesh.new()
	bm.from_mesh(me)
	bmesh.ops.triangulate(bm, faces=bm.faces)#, use_beauty=False)
	bm.to_mesh(me)
	bm.free()


def write_mtl(scene, file, filepath, path_mode, copy_set, mtl_dict):
	from mathutils import Color
	world = scene.world
	#if world and world.ambient_color:
	#	world_amb = world.ambient_color
	#else:
	world_amb = Color((0.0, 0.0, 0.0))
	source_dir = os.path.dirname(bpy.data.filepath)
	dest_dir = os.path.dirname(filepath)
	file.write('\n')
	#file.write('\nMaterials:%i\n' % len(mtl_dict))
	mtl_dict_values = list(mtl_dict.values())
	mtl_dict_values.sort(key=lambda m: m[0])
	# Write material/image combinations we have used.
	# Using mtl_dict.values() directly gives un-predictable order.
	for mtl_mat_name, mat, face_img in mtl_dict_values:
		# Get the Blender data for the material and the image.
		# Having an image named None will make a bug, dont do it:)
		file.write('Materials:%s\n' % mtl_mat_name)  # Define a new material: matname_imgname
		if mat:
			# convert from blenders spec to 0 - 1000 range.
			if mat.specular_shader == 'WARDISO':
				tspec = (0.4 - mat.specular_slope) / 0.0004
			else:
				tspec = (mat.specular_hardness - 1) * 1.9607843137254901
			file.write('\tNs %.6f\n' % tspec)
			del tspec
			file.write('\tKa %.6f %.6f %.6f\n' % (mat.ambient * world_amb)[:])  # Ambient, uses mirror color,
			file.write('\tKd %.6f %.6f %.6f\n' % (mat.diffuse_intensity * mat.diffuse_color)[:])  # Diffuse
			file.write('\tKs %.6f %.6f %.6f\n' % (mat.specular_intensity * mat.specular_color)[:])  # Specular
			if hasattr(mat, "ior"):
				file.write('\tNi %.6f\n' % mat.ior)  # Refraction index
			else:
				file.write('\tNi %.6f\n' % 1.0)
			file.write('\td %.6f\n' % mat.alpha)  # Alpha (obj uses 'd' for dissolve)
			# 0 to disable lighting, 1 for ambient & diffuse only (specular color set to black), 2 for full lighting.
			if mat.use_shadeless:
				file.write('\tillum 0\n')  # ignore lighting
			elif mat.specular_intensity == 0:
				file.write('\tillum 1\n')  # no specular.
			else:
				file.write('\tillum 2\n')  # light normaly
		else:
			#write a dummy material here?
			file.write('\tNs 0\n')
			file.write('\tKa %.6f %.6f %.6f\n' % world_amb[:])  # Ambient, uses mirror color,
			file.write('\tKd 0.8 0.8 0.8\n')
			file.write('\tKs 0.8 0.8 0.8\n')
			file.write('\td 1\n')  # No alpha
			file.write('\tillum 2\n')  # light normaly
		# Write images!
		if face_img:  # We have an image on the face!
			filepath = face_img.filepath
			if filepath:  # may be '' for generated images
				# write relative image path
				filepath = bpy_extras.io_utils.path_reference(filepath,
				                                              source_dir,
				                                              dest_dir,
				                                              path_mode,
				                                              "",
				                                              copy_set,
				                                              face_img.library)
				file.write('\tmap_Kd %s\n' % filepath)  # Diffuse mapping image
				del filepath
			else:
				# so we write the materials image.
				face_img = None
		if mat:  # No face image. if we havea material search for MTex image.
			image_map = {}
			# backwards so topmost are highest priority
			for mtex in reversed(mat.texture_slots):
				if mtex and mtex.texture and mtex.texture.type == 'IMAGE':
					image = mtex.texture.image
					if image:
						# texface overrides others
						if(     mtex.use_map_color_diffuse
						    and (face_img is None)
						    and (mtex.use_map_warp is False)
						    and (mtex.texture_coords != 'REFLECTION')
						   ):
							image_map["map_Kd"] = image
						if mtex.use_map_ambient:
							image_map["map_Ka"] = image
						# this is the Spec intensity channel but Ks stands for specular Color
						if mtex.use_map_color_spec:  # specular color
							image_map["map_Ks"] = image
						if mtex.use_map_hardness:  # specular hardness/glossiness
							image_map["map_Ns"] = image
						if mtex.use_map_alpha:
							image_map["map_d"] = image
						if mtex.use_map_translucency:
							image_map["map_Tr"] = image
						if mtex.use_map_normal and (mtex.texture.use_normal_map is True):
							image_map["map_Bump"] = image
						if mtex.use_map_normal and (mtex.texture.use_normal_map is False):
							image_map["map_Disp"] = image
						if mtex.use_map_color_diffuse and (mtex.texture_coords == 'REFLECTION'):
							image_map["map_refl"] = image
						if mtex.use_map_emit:
							image_map["map_Ke"] = image
			for key, image in image_map.items():
				filepath = bpy_extras.io_utils.path_reference(image.filepath,
				                                              source_dir,
				                                              dest_dir,
				                                              path_mode,
				                                              "",
				                                              copy_set,
				                                              image.library)
				file.write('\t%s %s\n' % (key, repr(filepath)[1:-1]))

def veckey3d(v):
	return round(v.x, 6), round(v.y, 6), round(v.z, 6)

def veckey2d(v):
	return round(v[0], 6), round(v[1], 6)

def write_mesh(scene, file, object, mtl_dict):
	print("**************** '" + str(object.name) + "' *******************")
	
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
		file.write('# can not export:"%s":type="%s"\n' % (object.name, str(object.type)))
		return
	#print("name:'%s'" % object.name)
	#for plop in object.child:
	#	print("    child:'%s'" % plop.name)
	# ignore dupli children
	if object.parent and object.parent.instance_type in {'VERTS', 'FACES'}:
		# XXX
		print(object.name, 'is a dupli child - ignoring')
		return
	obs = []
	if object.instance_type != 'NONE':
		# XXX
		print('******************** creating instance_type on', object.name)
		"""
		object.dupli_list_create(scene)
		obs = [(dob.object, dob.matrix) for dob in object.dupli_list]
		# XXX debug print
		print(object.name, 'has', len(obs), 'dupli children')
		"""
	else:
		obs = [(object, object.matrix_world)]
	for ob, ob_mat in obs:
		try:
			# apply the mesh modifieur at the curent object:
			me = ob.to_mesh()
		except RuntimeError:
			me = None
		if me is None:
			continue
		me.transform(ob_mat)
		#print("ploppp:" + str(ob_mat) )
		# _must_ do this first since it re-allocs arrays
		# triangulate all the mesh:
		mesh_triangulate(me)
		# calculated normals:
		me.calc_normals()
		# export UV mapping:
		faceuv = len(me.uv_textures) > 0
		if faceuv:
			uv_texture = me.uv_textures.active.data[:]
			uv_layer = me.uv_layers.active.data[:]
		me_verts = me.vertices[:]
		# Make our own list so it can be sorted to reduce context switching
		face_index_pairs = [(face, index) for index, face in enumerate(me.polygons)]
		# faces = [ f for f in me.tessfaces ]
		edges = me.edges
		if not (len(face_index_pairs) + len(edges) + len(me.vertices)):  # Make sure there is somthing to write
			# clean up
			bpy.data.meshes.remove(me)
			continue  # dont bother with this mesh.
		
		materials = me.materials[:]
		material_names = [m.name if m else None for m in materials]
		# avoid bad index errors
		if not materials:
			materials = [None]
			material_names = [name_compat(None)]
		# Sort by Material, then images
		# so we dont over context switch in the obj file.
		if faceuv:
			face_index_pairs.sort(key=lambda a: (a[0].material_index, hash(uv_texture[a[1]].image), a[0].use_smooth))
		elif len(materials) > 1:
			face_index_pairs.sort(key=lambda a: (a[0].material_index, a[0].use_smooth))
		else:
			# no materials
			face_index_pairs.sort(key=lambda a: a[0].use_smooth)
		# Set the default mat to no material and no image.
		contextMat = 0, 0  # Can never be this, so we will label a new material the first chance we get.
		contextSmooth = None  # Will either be true or false,  set bad to force initialization switch.
		# use:blen obs ??? what is this ....
		name1 = ob.name
		name2 = ob.data.name
		if name1 == name2:
			obnamestring = name_compat(name1)
		else:
			obnamestring = '%s_%s' % (name_compat(name1), name_compat(name2))
		file.write('Mesh:%s\n' % obnamestring)  # Write Object name
		###########################################################
		## Vert
		###########################################################
		file.write('\tVertex:%d\n\t\t' % len(me_verts))
		for v in me_verts:
			file.write('%.6f %.6f %.6f|' % v.co[:])
		file.write('\n')
		###########################################################
		## UV
		###########################################################
		if faceuv:
			file.write('\tUV-mapping:\n\t\t')
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
					except:
						uv_k = uv_dict[uvkey] = len(uv_dict)
						file.write('%.6f %.6f|' % uv[:])
					uv_ls.append(uv_k)
			uv_unique_count = len(uv_dict)
			del uv, uvkey, uv_dict, f_index, uv_index, uv_ls, uv_k
			# Only need uv_unique_count and uv_face_mapping
			file.write('\n')
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
		file.write('\tNormal(%s):%d\n\t\t' % (localIsSmooth, len(face_index_pairs)) )
		for f, f_index in face_index_pairs:
			if f.use_smooth:
				for v_idx in f.vertices:
					v = me_verts[v_idx]
					noKey = veckey3d(v.normal)
					if noKey not in globalNormals:
						globalNormals[noKey] = totno
						totno += 1
						file.write('%.6f %.6f %.6f|' % noKey)
			else:
				# Hard, 1 normal from the face.
				noKey = veckey3d(f.normal)
				if noKey not in globalNormals:
					globalNormals[noKey] = totno
					totno += 1
					file.write('%.6f %.6f %.6f|' % noKey)
		
		file.write('\n')
		if not faceuv:
			f_image = None
		###########################################################
		## faces
		###########################################################
		file.write('\tFace:%d' % len(face_index_pairs))
		for f, f_index in face_index_pairs:
			f_smooth = f.use_smooth
			f_mat = min(f.material_index, len(materials) - 1)
			if faceuv:
				tface = uv_texture[f_index]
				f_image = tface.image
			# MAKE KEY
			if faceuv and f_image:  # Object is always true.
				key = material_names[f_mat], f_image.name
			else:
				key = material_names[f_mat], None  # No image, use None instead.
			# CHECK FOR CONTEXT SWITCH
			if key == contextMat:
				pass  # Context already switched, dont do anything
			else:
				if key[0] is None and key[1] is None:
					# inform the use of a material:
					file.write("\n\t\t---:")  # mat, image
				else:
					mat_data = mtl_dict.get(key)
					if not mat_data:
						# First add to global dict so we can export to mtl
						# Then write mtl
						# Make a new names from the mat and image name,
						# converting any spaces to underscores with name_compat.
						# If none image dont bother adding it to the name
						# Try to avoid as much as possible adding texname (or other things)
						# to the mtl name (see [#32102])...
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
					file.write("\n\t\t%s\n\t\t\t" % mat_data[0])  # can be mat_image or (null)
			contextMat = key
			f_v = [(vi, me_verts[v_idx]) for vi, v_idx in enumerate(f.vertices)]
			if faceuv:
				# export the normals:
				if f_smooth:  # Smoothed, use vertex normals
					for vi, v in f_v:
						file.write(" %d/%d/%d" %
						   (v.index + totverts-1,
						    totuvco + uv_face_mapping[f_index][vi]-1,
						    globalNormals[veckey3d(v.normal)]-1,
						   ))  # vert, uv, normal
				else:  # No smoothing, face normals
					no = globalNormals[veckey3d(f.normal)]
					for vi, v in f_v:
						file.write(" %d/%d/%d" %
						   (v.index + totverts-1,
						    totuvco + uv_face_mapping[f_index][vi]-1,
						    no-1,
						   ))  # vert, uv, normal
				face_vert_index += len(f_v)
			else:  # No UV's
				# export the normals:
				if f_smooth:  # Smoothed, use vertex normals
					for vi, v in f_v:
						file.write(" %d/%d" % (
								   v.index + totverts-1,
								   globalNormals[veckey3d(v.normal)]-1,
								   ))
				else:  # No smoothing, face normals
					no = globalNormals[veckey3d(f.normal)]
					for vi, v in f_v:
						file.write(" %d/%d" % (v.index + totverts-1, no-1))
			file.write('|')
		file.write('\n')
		# Write edges. ==> did not know what it is ...
		#file.write('Faces:%d' % len(edges))
		#for ed in edges:
		#	if ed.is_loose:
		#		file.write('%d %d\n' % (ed.vertices[0] + totverts, ed.vertices[1] + totverts))
		
		# Make the indices global rather then per mesh
		totverts += len(me_verts)
		if faceuv:
			totuvco += uv_unique_count
		# clean up
		bpy.data.meshes.remove(me)
	
	if object.dupli_type != 'NONE':
		object.dupli_list_clear()
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
               objects,
               scene,
               EXPORT_PATH_MODE='AUTO',
               EXPORT_BINARY_MODE=False,
               EXPORT_COLLISION_NAME=""
               ):
	print('EMF Export path: %r' % filepath)
	
	time1 = time.time()
	
	mtlfilepath = os.path.splitext(filepath)[0] + ".mtl"
	
	file = open(filepath, "w", encoding="utf8", newline="\n")
	
	# Write Header
	file.write('EMF(STRING)\n') # if binary:file.write('EMF(BINARY)\n')
	file.write('# Blender v%s EMF File: %r\n' % (bpy.app.version_string, os.path.basename(bpy.data.filepath)))
	
	# A Dict of Materials
	# (material.name, image.name):matname_imagename # matname_imagename has gaps removed.
	mtl_dict = {}
	
	copy_set = set()
	"""
	nb_total_mesh = 0
	nb_total_physic = 0
	for ob_main in objects:
		print("**************** '" + str(ob_main.name) + "' *******************")
		if ob_main.type == 'EMPTY':
			for sub_obj in getChildren(ob_main):
				print("     child:'" + str(sub_obj.name) + "' type=" + sub_obj.type)
				if sub_obj.type == 'MESH':
					nb_total_mesh += 1
				elif     sub_obj.type == 'EMPTY' \
				     and sub_obj.name.lower().startswith("physic"):
					nb_total_physic += 1
				for sub_obj_2 in getChildren(sub_obj):
					print("         child:'" + str(sub_obj_2.name) + "' type=" + sub_obj_2.type)
					for sub_obj_3 in getChildren(sub_obj_2):
						print("         child:'" + str(sub_obj_3.name) + "' type=" + sub_obj_3.type)
		if ob_main.type == 'MESH':
			nb_total_mesh += 1
	print("nb_total_mesh: " + str(nb_total_mesh))
	print("nb_total_physic: " + str(nb_total_physic))
	"""
	
	# Get all meshes
	for ob_main in objects:
		if ob_main.type == 'MESH':
			write_mesh(scene, file, ob_main, mtl_dict)
		elif ob_main.type == 'EMPTY':
			for sub_obj in getChildren(ob_main):
				print("     child:'" + str(sub_obj.name) + "' type=" + sub_obj.type)
				if sub_obj.type == 'MESH':
					write_mesh(scene, file, sub_obj, mtl_dict)
				elif     sub_obj.type == 'EMPTY' \
				     and sub_obj.name.lower().startswith("physic"):
					print("     child:'" + str(sub_obj.name) + "' type=" + sub_obj.type)
					#####################################################################
					## Save collision shapes (for one all):
					#####################################################################
					write_collision_shape(sub_obj, file, sub_obj.scale, 0)
				else:
					print("     child:'" + str(sub_obj.name) + "' type=" + sub_obj.type + " not parsed ...")
					
	#####################################################################
	## Now we have all our materials, save them in the material section
	#####################################################################
	write_mtl(scene, file, mtlfilepath, EXPORT_PATH_MODE, copy_set, mtl_dict)
	
	#####################################################################
	## End of the file generation:
	#####################################################################
	file.close()
	
	# copy all collected files.
	bpy_extras.io_utils.path_reference_copy(copy_set)
	
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
	# get the curent scene:
	scene = context.scene
	# Exit edit mode before exporting, so current object states are exported properly.
	if bpy.ops.object.mode_set.poll():
		bpy.ops.object.mode_set(mode='OBJECT')
	# get the curent frame selected:
	frame = scene.frame_current
	# Loop through all frames in the scene and export.
	scene.frame_set(frame, subframe=0.0)
	# get only the object that are selected or all...
	if EXPORT_SEL_ONLY:
		objects = context.selected_objects
	else:
		objects = scene.objects
	
	full_path = ''.join(context_name)
	
	write_file(full_path,
	           objects,
	           scene,
	           EXPORT_PATH_MODE,
	           EXPORT_BINARY_MODE,
	           EXPORT_COLLISION_NAME,
	           )
	


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
