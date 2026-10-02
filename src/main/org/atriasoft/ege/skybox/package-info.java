/**
 * Skybox rendering for the EGE game engine.
 *
 * <h2>Overview</h2>
 * <p>
 * A skybox renders a cubemap texture on a cube that surrounds the camera,
 * providing a distant background (sky, mountains, space, etc.) to the 3D scene.
 * The skybox is always drawn behind all scene geometry.
 *
 * <h2>Quick Start</h2>
 * <pre>{@code
 * // 1. Create a SkyboxConfig with 6 face images
 * SkyboxConfig config = new SkyboxConfig(
 *     new Uri("RES", "skybox/right.png"),   // +X face
 *     new Uri("RES", "skybox/left.png"),    // -X face
 *     new Uri("RES", "skybox/top.png"),     // +Y face
 *     new Uri("RES", "skybox/bottom.png"),  // -Y face
 *     new Uri("RES", "skybox/front.png"),   // +Z face
 *     new Uri("RES", "skybox/back.png"));   // -Z face
 *
 * // 2. Apply to the environment
 * env.setSkybox(config);
 *
 * // 3. (Optional) Enable slow rotation
 * config.setRotationSpeed(0.02f);  // radians per second around Y axis
 * }</pre>
 * <p>
 * The rotation is driven by {@link org.atriasoft.ege.Environement#periodicCall()}
 * (game time): the sky turns counter-clockwise seen from above for a positive
 * speed, and the speed can be changed on the configuration at any time.
 * Setting another configuration starts again from an unrotated sky.
 *
 * <h2>Cubemap Face Order</h2>
 * <p>
 * The 6 faces follow the standard OpenGL cubemap convention:
 * <table>
 *   <tr><th>Parameter</th><th>OpenGL Target</th><th>Direction</th></tr>
 *   <tr><td>right</td><td>GL_TEXTURE_CUBE_MAP_POSITIVE_X</td><td>+X</td></tr>
 *   <tr><td>left</td><td>GL_TEXTURE_CUBE_MAP_NEGATIVE_X</td><td>-X</td></tr>
 *   <tr><td>top</td><td>GL_TEXTURE_CUBE_MAP_POSITIVE_Y</td><td>+Y</td></tr>
 *   <tr><td>bottom</td><td>GL_TEXTURE_CUBE_MAP_NEGATIVE_Y</td><td>-Y</td></tr>
 *   <tr><td>front</td><td>GL_TEXTURE_CUBE_MAP_POSITIVE_Z</td><td>+Z</td></tr>
 *   <tr><td>back</td><td>GL_TEXTURE_CUBE_MAP_NEGATIVE_Z</td><td>-Z</td></tr>
 * </table>
 *
 * <h2>Render Pipeline Integration</h2>
 * <p>
 * The skybox is rendered by {@link org.atriasoft.ege.engines.EngineSkybox},
 * which runs in the engine list after shadow map generation but before
 * scene geometry rendering:
 * <ol>
 *   <li>EngineShadow — generates shadow maps</li>
 *   <li><strong>EngineSkybox — renders skybox</strong></li>
 *   <li>EngineRender — renders scene geometry</li>
 *   <li>EnginePostProcess — post-processing effects</li>
 * </ol>
 * <p>
 * The skybox uses the {@code pos.xyww} trick in the vertex shader to write
 * depth = 1.0 (maximum), combined with {@code GL_LEQUAL} depth function,
 * so all scene geometry naturally renders in front of it.
 *
 * <h2>Performance</h2>
 * <p>
 * The skybox is drawn once per frame as a single 36-vertex cube with a
 * cubemap texture lookup. It has negligible performance impact.
 *
 * @see org.atriasoft.ege.skybox.SkyboxConfig
 * @see org.atriasoft.ege.engines.EngineSkybox
 * @see org.atriasoft.ege.Environement#setSkybox(SkyboxConfig)
 */
package org.atriasoft.ege.skybox;
