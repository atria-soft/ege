#!/usr/bin/python
import realog.debug as debug
import lutin.tools as tools
import realog.debug as debug
import lutin.image as image
import os
import lutin.multiprocess as lutinMultiprocess


def get_type():
	return "LIBRARY_DYNAMIC"

def get_desc():
	return "Ewol Tool Kit"

def get_licence():
	return "MPL-2"

def get_compagny_type():
	return "org"

def get_compagny_name():
	return "atria-soft"

#def get_maintainer():
#	return "authors.txt"

#def get_version():
#	return "version.txt"

def configure(target, my_module):

	my_module.add_src_file([
	    'src/entities/Camera.java',
	    'src/entities/Entity.java',
	    'src/entities/Light.java',
	    'src/entities/Player.java',
	    'src/guis/GuiRenderer.java',
	    'src/guis/GuiShader.java',
	    'src/guis/GuiTexture.java',
	    'src/models/RawModel.java',
	    'src/models/TexturedModel.java',
	    'src/org/atriasoft/eagle/CirclePointGenerator.java',
	    'src/org/atriasoft/eagle/SpaceGenerator.java',
	    'src/org/atriasoft/eagle/generator/Bridge.java',
	    'src/org/atriasoft/eagle/generator/Land.java',
	    'src/org/atriasoft/eagle/generator/LocalGenerator.java',
	    'src/org/atriasoft/eagle/generator/Montain.java',
	    'src/org/atriasoft/eagle/generator/River.java',
	    'src/org/atriasoft/eagle/generator/Road.java',
	    'src/org/atriasoft/eagle/model/MapChunk.java',
	    'src/org/atriasoft/eagle/model/MetaMap.java',
	    'src/org/atriasoft/eagle/model/TypeElement.java',
	    'src/org/atriasoft/eagle/model/VoxelMap.java',
	    'src/org/atriasoft/ege/Component.java',
	    'src/org/atriasoft/ege/ControlCameraPlayer.java',
	    'src/org/atriasoft/ege/ControlCameraPlayerFPS.java',
	    'src/org/atriasoft/ege/ControlCameraSimple.java',
	    'src/org/atriasoft/ege/ControlInterface.java',
	    'src/org/atriasoft/ege/CreatorEntity.java',
	    'src/org/atriasoft/ege/Ege.java',
	    'src/org/atriasoft/ege/Engine.java',
	    'src/org/atriasoft/ege/Entity.java',
	    'src/org/atriasoft/ege/EntityInteraction.java',
	    'src/org/atriasoft/ege/Environement.java',
	    'src/org/atriasoft/ege/GameStatus.java',
	    'src/org/atriasoft/ege/Light.java',
	    'src/org/atriasoft/ege/Material.java',
	    'src/org/atriasoft/ege/ResultNearestEntity.java',
	    'src/org/atriasoft/ege/Signal.java',
	    'src/org/atriasoft/ege/camera/Camera.java',
	    'src/org/atriasoft/ege/camera/ProjectionInterface.java',
	    'src/org/atriasoft/ege/camera/ProjectionOrthogonal.java',
	    'src/org/atriasoft/ege/camera/ProjectionPerspective.java',
	    'src/org/atriasoft/ege/celestial/CelestialBody.java',
	    'src/org/atriasoft/ege/celestial/CelestialBodyType.java',
	    'src/org/atriasoft/ege/celestial/CelestialSystem.java',
	    'src/org/atriasoft/ege/components/ComponentAI.java',
	    'src/org/atriasoft/ege/components/ComponentDynamicMeshs.java',
	    'src/org/atriasoft/ege/components/ComponentGravity.java',
	    'src/org/atriasoft/ege/components/ComponentGravityStatic.java',
	    'src/org/atriasoft/ege/components/ComponentLight.java',
	    'src/org/atriasoft/ege/components/ComponentLightSun.java',
	    'src/org/atriasoft/ege/components/ComponentMap.java',
	    'src/org/atriasoft/ege/components/ComponentMaterial.java',
	    'src/org/atriasoft/ege/components/ComponentMaterials.java',
	    'src/org/atriasoft/ege/components/ComponentMesh.java',
	    'src/org/atriasoft/ege/components/ComponentParticle.java',
	    'src/org/atriasoft/ege/components/ComponentPhysics.java',
	    'src/org/atriasoft/ege/components/ComponentPlayer.java',
	    'src/org/atriasoft/ege/components/ComponentPosition.java',
	    'src/org/atriasoft/ege/components/ComponentPositionPlayer.java',
	    'src/org/atriasoft/ege/components/ComponentPostProcess.java',
	    'src/org/atriasoft/ege/components/ComponentRender.java',
	    'src/org/atriasoft/ege/components/ComponentRenderColoredStaticMesh.java',
	    'src/org/atriasoft/ege/components/ComponentRenderMeshPalette.java',
	    'src/org/atriasoft/ege/components/ComponentRenderTexturedMaterialsDynamicMeshs.java',
	    'src/org/atriasoft/ege/components/ComponentRenderTexturedMaterialsStaticMesh.java',
	    'src/org/atriasoft/ege/components/ComponentRenderTexturedMaterialsStaticMeshs.java',
	    'src/org/atriasoft/ege/components/ComponentRenderTexturedStaticMesh.java',
	    'src/org/atriasoft/ege/components/ComponentStaticMesh.java',
	    'src/org/atriasoft/ege/components/ComponentStaticMeshs.java',
	    'src/org/atriasoft/ege/components/ComponentTexture.java',
	    'src/org/atriasoft/ege/components/ComponentTexturePack.java',
	    'src/org/atriasoft/ege/components/ComponentTexturePalette.java',
	    'src/org/atriasoft/ege/components/ComponentTextures.java',
	    'src/org/atriasoft/ege/components/GlLightIndex.java',
	    'src/org/atriasoft/ege/components/PhysicBodyType.java',
	    'src/org/atriasoft/ege/components/part/LightRender.java',
	    'src/org/atriasoft/ege/components/part/MaterialRender.java',
	    'src/org/atriasoft/ege/components/part/MaterialRenderBase.java',
	    'src/org/atriasoft/ege/components/part/MaterialsRender.java',
	    'src/org/atriasoft/ege/components/part/PartRenderInterface.java',
	    'src/org/atriasoft/ege/components/part/PositionningInterface.java',
	    'src/org/atriasoft/ege/components/part/RenderContext.java',
	    'src/org/atriasoft/ege/components/part/ShadowRender.java',
	    'src/org/atriasoft/ege/components/part/TransformRender.java',
	    'src/org/atriasoft/ege/engines/EngineAI.java',
	    'src/org/atriasoft/ege/engines/EngineDynamicMeshs.java',
	    'src/org/atriasoft/ege/engines/EngineGravity.java',
	    'src/org/atriasoft/ege/engines/EngineLight.java',
	    'src/org/atriasoft/ege/engines/EngineMap.java',
	    'src/org/atriasoft/ege/engines/EngineParticle.java',
	    'src/org/atriasoft/ege/engines/EnginePhysics.java',
	    'src/org/atriasoft/ege/engines/EnginePlayer.java',
	    'src/org/atriasoft/ege/engines/EnginePostProcess.java',
	    'src/org/atriasoft/ege/engines/EngineRender.java',
	    'src/org/atriasoft/ege/engines/EngineShadow.java',
	    'src/org/atriasoft/ege/geometry/AABB.java',
	    'src/org/atriasoft/ege/geometry/Geometry3D.java',
	    'src/org/atriasoft/ege/geometry/Line.java',
	    'src/org/atriasoft/ege/geometry/OBB.java',
	    'src/org/atriasoft/ege/geometry/Plane.java',
	    'src/org/atriasoft/ege/geometry/Plane____.java',
	    'src/org/atriasoft/ege/geometry/Ray.java',
	    'src/org/atriasoft/ege/geometry/Sphere.java',
	    'src/org/atriasoft/ege/geometry/Triangle.java',
	    'src/org/atriasoft/ege/map/ComponentDynamicMeshsVoxelMap.java',
	    'src/org/atriasoft/ege/map/ComponentRenderVoxelMesh.java',
	    'src/org/atriasoft/ege/map/MapVoxel.java',
	    'src/org/atriasoft/ege/map/Voxel.java',
	    'src/org/atriasoft/ege/map/VoxelChunk.java',
	    'src/org/atriasoft/ege/map/VoxelType.java',
	    'src/org/atriasoft/ege/postprocess/AdditiveOverlayEffect.java',
	    'src/org/atriasoft/ege/postprocess/OutlineEffect.java',
	    'src/org/atriasoft/ege/postprocess/PostProcessEffect.java',
	    'src/org/atriasoft/ege/postprocess/PostProcessResources.java',
	    'src/org/atriasoft/ege/postprocess/TintOverlayEffect.java',
	    'src/org/atriasoft/ege/shadow/ShadowCascade.java',
	    'src/org/atriasoft/ege/shadow/ShadowConfig.java',
	    'src/org/atriasoft/ege/shadow/ShadowMapResources.java',
	    'src/org/atriasoft/ege/tools/MeshGenerator.java',
	    'src/renderEngine/DisplayManager.java',
	    'src/renderEngine/DisplayManagerDraw.java',
	    'src/renderEngine/EntityRenderer.java',
	    'src/renderEngine/Loader.java',
	    'src/renderEngine/MasterRenderer.java',
	    'src/renderEngine/OBJLoader.java',
	    'src/renderEngine/TerrainRenderer.java',
	    'src/shaders/ShaderProgram.java',
	    'src/shaders/StaticShader.java',
	    'src/shaders/TerrainShader.java',
	    'src/skybox/SkyboxRenderer.java',
	    'src/skybox/SkyboxShader.java',
	    'src/terrains/Terrain.java',
	    'src/terrains/TerrainTexture.java',
	    'src/terrains/TerrainTexturePack.java',
	    'src/textures/ModelTexture.java',
	    'src/textures/TextureData.java',
	    'src/toolbox/Maths.java',
	    ])
	my_module.add_path('src/', type='java')
	
	my_module.add_depend([
	    'org-atriasoft-ewol',
	    'org-atriasoft-ephysics',
	    'org-atriasoft-loader3d',
	    ])
	
	#my_module.add_path([
	#    'lib/spotbugs-annotations-4.2.2.jar'
	#    ],
	#    type='java',
	#    export=True
	#);
	my_module.add_flag('java', "RELEASE_15_PREVIEW");
	
	return True

