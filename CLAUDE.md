# ege - Ewol Game Engine

Game engine built on top of ewol/gale. Provides entity-component system, cameras, physics integration, post-process effects, and 3D rendering pipeline.

## Resource URI Pattern

```java
new Uri("DATA", "path/to/resource.ext", "ege")  // 3rd param = library name
```

Without the library name, the URI resolver only searches the application classpath, not the ege library resources.

## Common Pitfalls

### OpenGL state in post-process effects
- **Always restore `blendFunc` after changing it.** If a shader pass uses non-standard blending (e.g., additive `GL_SRC_ALPHA, GL_ONE`), call `OpenGL.blendFuncAuto()` before disabling blend. Otherwise the blend function leaks into subsequent passes (text rendering, UI) and makes fonts invisible.
- **General rule:** any OpenGL state changed during a render pass (`blendFunc`, `depthFunc`, `stencilOp`, `cullFace`, etc.) must be restored before returning. OpenGL is global mutable state — treat every setter as requiring a matching restore.

### Build
- **Pre-existing test failures:** `TestTransformation3D` and other tests may fail. Use `-DskipTests` to build successfully.
