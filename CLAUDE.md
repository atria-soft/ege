# ege - Ewol Game Engine

Game engine built on top of ewol/gale. Provides entity-component system, cameras, physics integration, post-process effects, and 3D rendering pipeline.

## Resource URI Pattern

```java
new Uri("DATA", "path/to/resource.ext", "ege")  // 3rd param = library name
```

Without the library name, the URI resolver only searches the application classpath, not the ege library resources.

## Cascaded Shadow Maps (CSM)

The engine implements cascaded shadow mapping for directional lights (celestial bodies like the sun).

### Architecture

| Component | Role |
|-----------|------|
| `EngineShadow` | Orchestrates depth passes, manages cascades, renders debug thumbnails |
| `ShadowConfig` | Cascade count (1-4), resolution, shadow distance, PCF kernel size, split lambda |
| `ShadowCascade` | One cascade: computes light-space matrix from camera frustum slice |
| `ShadowMapResources` | GPU resources: depth FBO, depth texture with hardware shadow comparison |
| `ShadowRender` | Binds shadow maps + light-space matrices to shader uniforms during main pass |

### Rendering pipeline

1. **Depth pass** (`EngineShadow.render()`): for each celestial body × cascade, render all meshes into a depth-only FBO using `depthPass.vert/frag`
2. **Main pass**: `ShadowRender.bindForRendering()` sends shadow maps (texture units 2+) and matrices to the fragment shader
3. **Fragment shader**: selects cascade by distance, samples `sampler2DShadow` with PCF, blends between adjacent cascades

### Key files

- Depth shaders: `src/resources/resources/ege/data/shadow/depthPass.vert`, `depthPass.frag`
- Debug visualization: `src/resources/resources/ege/data/shadow/debugDepth.frag`
- Built-in lit shaders: `src/main/shaders/static.vert/frag`, `terrain.vert/frag`
- Sample shaders: `samples/src/resources/resources/shadowTest/data/shadowMaterial.vert/frag`, `basicPalette.vert/frag`
- Tests: `src/test/org/atriasoft/ege/shadow/TestShadowCascadeMatrix.java`

### Shadow-receiving render components

Any `ComponentRender` that wants shadows must:
1. Lazy-init a `ShadowRender` when `context.getEngineShadow() != null`
2. Call `renderShadow.bindForRendering()` / `unBindForRendering()` around draw calls
3. Have vertex/fragment shaders with CSM uniforms (`in_shadowMap[]`, `in_lightSpaceMatrix[]`, `in_cascadeSplits[]`, etc.)

Currently supported: `ComponentRenderTexturedMaterialsStaticMesh`, `ComponentRenderMeshPalette`.

## Common Pitfalls

### Shadow mapping
- **Hardware shadow comparison**: Depth textures use `GL_COMPARE_R_TO_TEXTURE` + `GL_LEQUAL`. Shaders must use `sampler2DShadow`, not `sampler2D`. To visualize raw depth (debug thumbnails), temporarily set `GL_TEXTURE_COMPARE_MODE = GL_NONE`.
- **Polygon offset**: Enabled during depth pass (`glPolygonOffset(1.0, 1.0)`) to prevent shadow acne. Keep values low to avoid visible gap between objects and their shadows.
- **Shader bias**: Use adaptive slope-scaled bias `max(0.001 * (1.0 - cosTheta), 0.0002)`. Higher values cause objects to appear floating.
- **Z range extension**: `ShadowCascade` extends the light-space AABB Z range beyond the frustum to capture shadow casters behind the camera. `maxZ += max(zRange * 2.0, 50.0)` (toward light), `minZ -= max(zRange * 0.5, 10.0)` (away from light).
- **LookAt matrix**: Use `ShadowCascade.buildLookAtMatrix()`, not `Matrix4f.createMatrixLookAt()` (which is rotation-only and produces incorrect translation for shadow mapping).

### OpenGL state in render passes
- **Always restore `blendFunc` after changing it.** If a shader pass uses non-standard blending (e.g., additive `GL_SRC_ALPHA, GL_ONE`), call `OpenGL.blendFuncAuto()` before disabling blend. Otherwise the blend function leaks into subsequent passes (text rendering, UI) and makes fonts invisible.
- **General rule:** any OpenGL state changed during a render pass (`blendFunc`, `depthFunc`, `stencilOp`, `cullFace`, etc.) must be restored before returning. OpenGL is global mutable state — treat every setter as requiring a matching restore.

### Build
- **Pre-existing test failures:** `TestTransformation3D` and other tests may fail. Use `-DskipTests` to build successfully.
