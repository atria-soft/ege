# add this folder in ~/.config/blender/2.66/scripts/addons


bl_info = {
    "name": "EGE Mesh file format emf",
    "author": "Edouard DUPIN",
    "blender": (2, 81, 6),
    "location": "File > Import-Export",
    "description": "Import-Export emf, Import EMF mesh, UV's, materials and textures",
    "category": "Import-Export"}

if "bpy" in locals():
    import imp
    if "import_emf" in locals():
        imp.reload(import_emf)
    if "export_emf" in locals():
        imp.reload(export_emf)


import bpy
from bpy.props import (
        BoolProperty,
        FloatProperty,
        StringProperty,
        EnumProperty,
        )
from bpy_extras.io_utils import (
        ImportHelper,
        ExportHelper,
        orientation_helper,
        path_reference_mode,
        axis_conversion,
        )


class ImportEMF(bpy.types.Operator, ImportHelper):
    """Load a Wavefront EMF File"""
    bl_idname = "import_scene.emf"
    bl_label = "Import EMF"
    bl_options = {'PRESET', 'UNDO'}

    filename_ext = ".emf"
    filter_glob = StringProperty(
            default="*.emf",
            options={'HIDDEN'},
            )

    def execute(self, context):
        # print("Selected: " + context.active_object.name)
        from . import import_obj

        keywords = self.as_keywords(ignore=("filter_glob",
                                            "split_mode",
                                            ))

        return import_obj.load(self, context, **keywords)

    def draw(self, context):
        layout = self.layout

        row = layout.row(align=True)

        row = layout.split(percentage=0.67)
        row.prop(self, "global_clamp_size")

        layout.prop(self, "use_image_search")


#@orientation_helper(axis_forward='-Z', axis_up='Y')
class ExportEMF(bpy.types.Operator, ExportHelper):
	"""Save a Wavefront EMF File"""
	
	bl_idname = "export_scene.emf"
	bl_label = 'Export EMF'
	bl_options = {'PRESET'}
	
	filename_ext = ".emf"
	filter_glob = StringProperty(
	    default="*.emf",
	    options={'HIDDEN'},
	    )
	
	# context group
	use_selection = BoolProperty(
	    name="Selection Only",
	    description="Export selected objects only",
	    default=False,
	    )
	# generate binary file
	use_binary = BoolProperty(
	    name="Binary",
	    description="Export the file in binary mode",
	    default=False,
	    )
	
	global_scale = FloatProperty(
	    name="Scale",
	    description="Scale all data",
	    min=0.01, max=1000.0,
	    soft_min=0.01,
	    soft_max=1000.0,
	    default=1.0,
	    )
	
	collision_object_name = StringProperty(
	    name="Collision root name (strat with)",
	    description="The top-level name that will contain the physics shapes",
	    default="phys"
	    )
	
	path_mode = path_reference_mode
	
	check_extension = True
	
	def execute(self, context):
		from . import export_emf
		
		from mathutils import Matrix
		keywords = self.as_keywords(ignore=("global_scale",
		                                    "check_existing",
		                                    "filter_glob",
		                                    ))
		
		global_matrix = Matrix()
		
		global_matrix[0][0] = \
		global_matrix[1][1] = \
		global_matrix[2][2] = self.global_scale
		
		return export_emf.save(self, context, **keywords)


def menu_func_import(self, context):
	self.layout.operator(ImportEMF.bl_idname, text="Ewol mesh file (.emf)")


def menu_func_export(self, context):
	self.layout.operator(ExportEMF.bl_idname, text="Ewol mesh File (.emf)")


classes = (
    ImportEMF,
    ExportEMF,
)


def register():
    for cls in classes:
        bpy.utils.register_class(cls)

    bpy.types.TOPBAR_MT_file_import.append(menu_func_import)
    bpy.types.TOPBAR_MT_file_export.append(menu_func_export)


def unregister():
    bpy.types.TOPBAR_MT_file_import.remove(menu_func_import)
    bpy.types.TOPBAR_MT_file_export.remove(menu_func_export)

    for cls in classes:
        bpy.utils.unregister_class(cls)


if __name__ == "__main__":
   print("Registering.");
   register();
   #print("Executing.");
   #bpy.ops.export_scene.emf();


