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
| `EngineShadow` | Orchestrates depth passes, manages cascades, renders debug thumbnails; `release()` gives the shadow maps back (no shadow afterwards) |
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

A small kit to write a standalone lab window for a library (eFlora's trees: `eFlora/lab`; eArchi's buildings:
`eArchi/lab`). Nothing in it knows about trees or buildings. Overview and example: `package-info.java`.

| Class | Role |
|-------|------|
| `Lab` | What a lab implements: `title()`, `start(view)` (declare controls, watch files, first build), `update(view, seconds)` (each frame: take results, `setContent`, `setInfo`), `close()` |
| `LabApplication.run(args, factory)` | Starts Gale/Ewol/Ege and opens the window (`LabWindow`: 3D view left, control panel right: the groups of the lab scroll, the View group and Quit stay in a footer under them, always in sight). `run(args, appClass, resources, factory)`: `DATA:` then names the lab's own files (the kit's are found by their library `ege`) |
| `LabView` | The 3D view: ground ruled 1/5/25 m (X red, Z blue), sky, sun with ege's cascaded shadows, human figure of 1.80 m (`placeHuman`, `showHuman(boolean)`: the F2 toggle shows the same state), info panel (`setInfo`), F1 help, file watching (`watch`), `frame()` (the content) or `frame(box)` (any box: the grid of an editor); `setOverlay(mesh)`: the lab's tools drawn over the content (a grid, a cursor; never framed, no shadow cast, its lines on top over everything); `setPointer(LabPointer)`: the mouse to the lab first (hover, leave, press: a press it takes makes the drag and the release its own, one it leaves goes to the camera; the wheel always zooms); a press on the view takes the focus (a text field gives the keys back); it is the `LabReporter`: `report(what, …)` shows a problem in red at the top of the info panel until `clear(what)` |
| `LabPointer` | What a lab does with the mouse over the view: `pointer(Event)` with the action (`HOVER`, `LEAVE`, `PRESS`, `DRAG`, `RELEASE`), the button, the pixel, the eye and the ray through the pointer (`LabCamera.ray`), Shift and Control; `ground()` / `at(height)`: the point under the pointer on a level |
| `LabControls` | The single source of the actions: `group(heading)`, `action`, `toggle`, `choice`, `stepper`, `text` (a text field under its label: what is typed goes to the control on Enter; no key: while it has the focus every key is its own, Escape or a click on the view gives them back), `palette` (one of a few items, each with its own key: a grid of buttons, two a row, the item chosen lit); each gives a widget of the panel (`LabPanelWidgets`: thin buttons; a choice is its title with Prev/Next, then the drop-down list alone on the row under, its items shown at most 34 characters, `...` after: the lab is told the index; every widget used gives the focus back to the view), a key (`LabKey.of(char)`, `LabKey.of(KeyKeyboard)`, `LabKey.ctrl(char)` for Ctrl+Z: gale hands the letter with Control set) and a help line (a palette a line per item). A key that cannot be bound is reported and dropped, the control kept: already bound, a kit key (reserved before `Lab.start`), or `refusal(key)` (arrows, Page up/down, F12, Tab, Escape). Callbacks and suppliers are guarded: a problem is reported under the control's label (a supplier under `label (state)`, `(items)`, `(item chosen)`, `(value)`, `(text)`) and cleared on its next success |
| `LabKeyRepeat` | Tells gale's auto-repeat (a release and a press at once) from a new press, as dementia's `KeyPressFilter`: a held key repeats steppers and choices, never actions nor toggles |
| `LabMesh` / `LabShapes` | Content: opaque flat-shaded triangles (cast shadows), translucent ones (alpha < 1), lines hidden or on top; boxes, cylinders, polygons, wire boxes, crosses, the human |
| `LabCamera` | Orbit (drag turns, right/middle or Shift drag pans, wheel zooms) or flight (F3; arrows, Page up/down); `frame(box, fovX, aspect, leftShare)` fits a box beside a panel; `setDirection(azimuth, elevation)` turns it (the orbit's clamp kept, 89 degrees at most: an editor looks straight down; around the eye in a flight); `lookAt(target, distance)`; `ray(x, y, width, height, fovX)`: the direction through a pixel |
| `LabWorkshop<R, M>` | Build thread, newest request wins, a throwable becomes a `Result` that carries it |
| `LabWatcher` | Data files polled each second; a change is read once it stayed still for a poll |
| `LabText` | Panel lines (`Line(text, kind[, mono])`; `Line.mono(text[, kind])` drawn in FreeMono, wrapped between characters, its columns lined up) and their kinds; `ascii`, `wrap` (between words, a long word between letters; always ends), `wrapChars`, `shorten` |

Kit keys: `F` frame, `F1` help, `F2` human, `F3` free flight; the arrows and Page up/down drive the camera.
**Tab never reaches the application** (AWT keeps it for focus traversal), Escape is left to the drop-down lists,
F12 is ewol's inspector. `LabWindow.onEventShortCut` sees every key first, whatever widget has the focus; while a
drop-down list is open (`popUpCount() > 0`) it lets the list have the keys, while a text field (`Entry`) has the
focus it lets the field have them (Escape gives the focus back to the view; a release still lets a camera key go). A
key with Control runs a `LabKey.ctrl` control, a key without it a plain one; the left Alt or Meta held runs none;
AltGr counts as no key held (it types `#`, `|`, `@` on a French keyboard; Windows hands it with Control).
gale tells nothing when the window loses the focus: a release lost then leaves a camera key held until it is
pressed again. Nothing a lab does while the window opens kills it (a factory, `start`, the controls of the view,
the first sync of the panel: all reported). The info and help boxes stop at the bottom of the view (`... N more
lines`). `LabRenderer` draws with `data/lab/labLit.*` (the CSM code copied from dementia's `surface.frag`) and
`labLine.*`, casts through a `ShadowCaster`, refills its buffers per content, leaves texture unit 0 active and
releases everything on the GL thread; `LabView.release()` also releases its `EngineShadow` (`release()`: cascade
framebuffers, depth textures, programs). A renderer that cannot be made is reported once, never retried. Tests:
`src/test/org/atriasoft/ege/lab/` (headless). See it only through a virtual X server
(`tools/lab-smoke.sh LAB_SCRIPT OUTDIR "ARGS" STEP...`, which compiles with no display and runs the window in Xvfb;
`eFlora/tools/lab-smoke.sh` and `eArchi/tools/lab-smoke.sh` wrap it), never on the real display. In the smoke steps
`click=4` is the wheel up (away from the user: the view comes closer, the panel scrolls up) and `click=5` the wheel
down; gale numbers them the other way round (5 up, 4 down), which `LabView.onEventInput` reads. Quit is in the
footer: `close=1220,667` at the default 1280x760.

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
