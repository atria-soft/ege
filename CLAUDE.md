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

## Lab kit (`org.atriasoft.ege.lab`)

A small kit to write a standalone lab window for a library (eFlora's trees: `eFlora/lab`; eArchi's buildings
later). Nothing in it knows about trees or buildings. Overview and example: `package-info.java`.

| Class | Role |
|-------|------|
| `Lab` | What a lab implements: `title()`, `start(view)` (declare controls, watch files, first build), `update(view, seconds)` (each frame: take results, `setContent`, `setInfo`), `close()` |
| `LabApplication.run(args, factory)` | Starts Gale/Ewol/Ege and opens the window (`LabWindow`: 3D view left, control panel right) |
| `LabView` | The 3D view: ground ruled 1/5/25 m (X red, Z blue), sky, sun with ege's cascaded shadows, human figure of 1.80 m (`placeHuman`), info panel (`setInfo`), F1 help, errors (`report`), file watching (`watch`), `frame()` |
| `LabControls` | The single source of the actions: `group(heading)`, `action`, `toggle`, `choice`, `stepper`; each gives a widget of the panel (`LabPanelWidgets`), a key (`LabKey`, refused when bound twice) and a help line |
| `LabMesh` / `LabShapes` | Content: opaque flat-shaded triangles (cast shadows), translucent ones (alpha < 1), lines hidden or on top; boxes, cylinders, polygons, wire boxes, crosses, the human |
| `LabCamera` | Orbit (drag turns, right/middle or Shift drag pans, wheel zooms) or flight (F3; arrows, Page up/down); `frame(box, fovX, aspect, leftShare)` fits a box beside a panel |
| `LabWorkshop<R, M>` | Build thread, newest request wins, a throwable becomes a `Result` that carries it |
| `LabWatcher` | Data files polled each second; a change is read once it stayed still for a poll |

Kit keys: `F` frame, `F1` help, `F2` human, `F3` free flight; the arrows and Page up/down drive the camera.
Labs must not bind those. **Tab never reaches the application** (AWT keeps it for focus traversal), and Escape
is left to ewol (it closes a drop-down list). `LabWindow.onEventShortCut` sees every key first, whatever widget
has the focus. `LabRenderer` draws with `data/lab/labLit.*` (the CSM code copied from dementia's `surface.frag`)
and `labLine.*`, casts through a `ShadowCaster`, refills its buffers per content and releases everything on the
GL thread. Tests: `src/test/org/atriasoft/ege/lab/` (headless). See it only through a virtual X server
(`eFlora/tools/lab-smoke.sh`), never on the real display.

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
