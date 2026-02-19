# SPDX-License-Identifier: GPL-3.0-or-later
# EGE Mesh file format EMF - Blender 5.0+ compatible


if "bpy" in locals():
    import importlib
    if "export_emf" in locals():
        importlib.reload(export_emf)


import bpy
from bpy.props import (
        BoolProperty,
        FloatProperty,
        StringProperty,
        )
from bpy_extras.io_utils import (
        ExportHelper,
        path_reference_mode,
        )


class ExportEMF(bpy.types.Operator, ExportHelper):
	"""Save an EGE Mesh File"""

	bl_idname = "export_scene.emf"
	bl_label = "Export EMF"
	bl_options = {'PRESET'}

	filename_ext = ".emf"
	filter_glob: StringProperty(
		default="*.emf",
		options={'HIDDEN'},
	)

	# context group
	use_selection: BoolProperty(
		name="Selection Only",
		description="Export selected objects only",
		default=False,
	)
	# generate binary file
	use_binary: BoolProperty(
		name="Binary",
		description="Export the file in binary mode",
		default=False,
	)

	global_scale: FloatProperty(
		name="Scale",
		description="Scale all data",
		min=0.01, max=1000.0,
		soft_min=0.01,
		soft_max=1000.0,
		default=1.0,
	)

	collision_object_name: StringProperty(
		name="Collision root name (start with)",
		description="The top-level name that will contain the physics shapes",
		default="phys",
	)

	path_mode: path_reference_mode

	check_extension = True

	def execute(self, context):
		from . import export_emf

		keywords = self.as_keywords(ignore=("global_scale",
		                                    "check_existing",
		                                    "filter_glob",
		                                    ))

		return export_emf.save(self, context, **keywords)


def menu_func_export(self, context):
	self.layout.operator(ExportEMF.bl_idname, text="EGE Mesh File (.emf)")


classes = (
    ExportEMF,
)


def register():
	for cls in classes:
		bpy.utils.register_class(cls)
	bpy.types.TOPBAR_MT_file_export.append(menu_func_export)


def unregister():
	bpy.types.TOPBAR_MT_file_export.remove(menu_func_export)
	for cls in classes:
		bpy.utils.unregister_class(cls)


if __name__ == "__main__":
	register()
