package org.atriasoft.ege.lab;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.atriasoft.ege.components.part.ShadowRender;
import org.atriasoft.ege.engines.EngineShadow;
import org.atriasoft.ege.engines.ShadowCaster;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.Gale;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;
import org.atriasoft.gale.resource.ResourceProgram;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Draws a lab: the ground with its grid, the content ({@link LabMesh}) and
 * the human figure, lit by the sun of the lab with the cascaded shadows of
 * ege's shadow engine (the opaque triangles cast them, registered as a
 * {@link ShadowCaster}; everything lit receives them), then the translucent
 * triangles (blended, the depth tested but not written), the lines, the lines
 * on top. One vertex buffer per layer, filled again when the content changes:
 * nothing of OpenGL leaks from one content to the next; {@link #release()}
 * gives everything back. Every OpenGL state it changes is restored (blending
 * off with its default function, depth writes on, depth test as the caller
 * set it is left on). Created and drawn on the rendering thread.
 */
final class LabRenderer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LabRenderer.class);
	/** Unit direction towards the sun: east-south-east, 49 degrees high (shadows fall west, to the left). */
	static final Vector3f TO_SUN = new Vector3f(0.6f, 0.75f, 0.25f).normalize();
	static final Vector3f SUN_COLOR = new Vector3f(0.78f, 0.74f, 0.66f);
	static final Vector3f SKY_AMBIENT = new Vector3f(0.42f, 0.46f, 0.52f);
	static final Vector3f GROUND_AMBIENT = new Vector3f(0.25f, 0.24f, 0.20f);
	static final float SHADOW_STRENGTH = 0.8f;
	/** The ground: a square of this half side, metres, around the origin. */
	static final float GROUND_HALF = 2000.0f;
	static final Color GROUND = new Color(0x9A / 255.0f, 0xAE / 255.0f, 0x72 / 255.0f, 1.0f);

	/** A vertex array and its buffer, filled again for each content. */
	private static final class Layer {
		private final int floatsPerVertex;
		private int array = -1;
		private int buffer = -1;
		private int vertices;

		Layer(final int floatsPerVertex) {
			this.floatsPerVertex = floatsPerVertex;
		}

		/** Fill the buffer with the first {@code size} floats of {@code data} (made at the first fill). */
		void fill(final float[] data, final int size, final FloatBuffer[] upload) {
			if (this.array == -1) {
				this.buffer = GL15.glGenBuffers();
				this.array = GL30.glGenVertexArrays();
				GL30.glBindVertexArray(this.array);
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.buffer);
				final int stride = this.floatsPerVertex * Float.BYTES;
				GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0L);
				GL20.glEnableVertexAttribArray(0);
				if (this.floatsPerVertex == LabMesh.LIT_FLOATS) {
					GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, stride, 3L * Float.BYTES);
					GL20.glEnableVertexAttribArray(2);
					GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, stride, 6L * Float.BYTES);
				} else {
					GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, stride, 3L * Float.BYTES);
				}
				GL20.glEnableVertexAttribArray(3);
				GL30.glBindVertexArray(0);
			}
			if (upload[0].capacity() < size) {
				upload[0] = BufferUtils.createFloatBuffer(Math.max(size, upload[0].capacity() * 2));
			}
			final FloatBuffer floats = upload[0];
			floats.clear();
			floats.put(data, 0, size);
			floats.flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.buffer);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floats, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			this.vertices = size / this.floatsPerVertex;
		}

		void draw(final int mode) {
			if (this.vertices == 0 || this.array == -1) {
				return;
			}
			GL30.glBindVertexArray(this.array);
			GL11.glDrawArrays(mode, 0, this.vertices);
			GL30.glBindVertexArray(0);
		}

		int[] objects() {
			final int[] objects = { this.array, this.buffer };
			this.array = -1;
			this.buffer = -1;
			this.vertices = 0;
			return objects;
		}
	}

	private final EngineShadow shadowEngine;
	private final ResourceProgram litCreated;
	private final ResourceProgram lineCreated;
	/** The programs drawn with, {@code null} when one did not compile (nothing is drawn, the panel still is). */
	private final ResourceProgram lit;
	private final ResourceProgram line;
	private final ShadowRender shadows = new ShadowRender();
	private final int litProjection;
	private final int litView;
	private final int litEye;
	private final int litToSun;
	private final int litSunColor;
	private final int litSkyAmbient;
	private final int litGroundAmbient;
	private final int litShadowStrength;
	private final int litGrid;
	private final int lineProjection;
	private final int lineView;
	private final Layer ground = new Layer(LabMesh.LIT_FLOATS);
	private final Layer opaque = new Layer(LabMesh.LIT_FLOATS);
	private final Layer human = new Layer(LabMesh.LIT_FLOATS);
	private final Layer translucent = new Layer(LabMesh.LIT_FLOATS);
	private final Layer lines = new Layer(LabMesh.LINE_FLOATS);
	private final Layer linesOnTop = new Layer(LabMesh.LINE_FLOATS);
	private final FloatBuffer[] upload = { BufferUtils.createFloatBuffer(4096) };
	private final ShadowCaster caster = this::castShadows;
	/** What the layers hold. */
	private LabMesh shownContent;
	private LabMesh shownHuman;
	private boolean released;

	/** Compile the programs and register the caster of the shadows with {@code shadowEngine}. */
	LabRenderer(final EngineShadow shadowEngine) {
		this.shadowEngine = shadowEngine;
		this.litCreated = ResourceProgram.create(new Uri("DATA", "lab/labLit.vert", "ege"),
				new Uri("DATA", "lab/labLit.frag", "ege"));
		this.lineCreated = ResourceProgram.create(new Uri("DATA", "lab/labLine.vert", "ege"),
				new Uri("DATA", "lab/labLine.frag", "ege"));
		final boolean usable = usable(this.litCreated, "labLit") & usable(this.lineCreated, "labLine");
		this.lit = usable ? this.litCreated : null;
		this.line = usable ? this.lineCreated : null;
		final ResourceProgram l = this.litCreated;
		this.litProjection = l.getUniform("in_matrixProjection");
		this.litView = l.getUniform("in_matrixView");
		this.litEye = l.getUniform("in_eye");
		this.litToSun = l.getUniform("in_toSun");
		this.litSunColor = l.getUniform("in_sunColor");
		this.litSkyAmbient = l.getUniform("in_skyAmbient");
		this.litGroundAmbient = l.getUniform("in_groundAmbient");
		this.litShadowStrength = l.getUniform("in_shadowStrength");
		this.litGrid = l.getUniform("in_grid");
		this.lineProjection = this.lineCreated.getUniform("in_matrixProjection");
		this.lineView = this.lineCreated.getUniform("in_matrixView");
		if (this.lit == null) {
			LOGGER.error("The lab draws no 3D: its shaders are not usable");
			return;
		}
		this.shadows.init(this.lit);
		final LabMesh plane = new LabMesh();
		plane.quad(GROUND, new Vector3f(-GROUND_HALF, 0.0f, -GROUND_HALF), new Vector3f(-GROUND_HALF, 0.0f, GROUND_HALF),
				new Vector3f(GROUND_HALF, 0.0f, GROUND_HALF), new Vector3f(GROUND_HALF, 0.0f, -GROUND_HALF));
		this.ground.fill(plane.opaque().data(), plane.opaque().size(), this.upload);
		if (shadowEngine != null) {
			shadowEngine.addShadowCaster(this.caster);
		}
	}

	/**
	 * Whether {@code program} is linked with both its shaders (gale links a
	 * program whose shader did not compile with the other alone); its log is
	 * told otherwise.
	 */
	private static boolean usable(final ResourceProgram program, final String name) {
		if (program == null) {
			LOGGER.error("Shader program {} could not be created", name);
			return false;
		}
		program.use();
		final int id = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		final boolean usable = id != 0 && GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == GL11.GL_TRUE
				&& GL20.glGetProgrami(id, GL20.GL_ATTACHED_SHADERS) == 2;
		if (!usable) {
			LOGGER.error("Shader program {} is not usable: {}", name,
					id != 0 ? GL20.glGetProgramInfoLog(id, 500) : "no program");
		}
		program.unUse();
		return usable;
	}

	/** Show {@code content} and the human figure ({@code null}: hidden) from this frame on. */
	void show(final LabMesh content, final LabMesh figure) {
		if (this.lit == null || this.released) {
			return;
		}
		if (content != this.shownContent) {
			this.shownContent = content;
			final LabMesh mesh = content != null ? content : new LabMesh();
			this.opaque.fill(mesh.opaque().data(), mesh.opaque().size(), this.upload);
			this.translucent.fill(mesh.translucent().data(), mesh.translucent().size(), this.upload);
			this.lines.fill(mesh.lines().data(), mesh.lines().size(), this.upload);
			this.linesOnTop.fill(mesh.linesOnTop().data(), mesh.linesOnTop().size(), this.upload);
		}
		if (figure != this.shownHuman) {
			this.shownHuman = figure;
			final LabMesh mesh = figure != null ? figure : new LabMesh();
			this.human.fill(mesh.opaque().data(), mesh.opaque().size(), this.upload);
		}
	}

	/** Draw into the depth pass of the shadow engine: the opaque content and the human figure. */
	private void castShadows() {
		if (this.released) {
			return;
		}
		this.opaque.draw(GL11.GL_TRIANGLES);
		this.human.draw(GL11.GL_TRIANGLES);
	}

	/**
	 * Draw the scene: the projection and the view given, the depth test on
	 * (left on), the shadow maps of this frame drawn already.
	 */
	void draw(final Matrix4f projection, final Matrix4f view, final Vector3f eye) {
		if (this.lit == null || this.released) {
			return;
		}
		OpenGL.disable(Flag.flag_cullFace);
		OpenGL.disable(Flag.flag_blend);
		OpenGL.enable(Flag.flag_depthTest);
		OpenGL.updateAllFlags();
		this.lit.use();
		this.lit.uniformMatrix(this.litProjection, projection);
		this.lit.uniformMatrix(this.litView, view);
		this.lit.uniformVector(this.litEye, eye);
		this.lit.uniformVector(this.litToSun, TO_SUN);
		this.lit.uniformVector(this.litSunColor, SUN_COLOR);
		this.lit.uniformVector(this.litSkyAmbient, SKY_AMBIENT);
		this.lit.uniformVector(this.litGroundAmbient, GROUND_AMBIENT);
		this.lit.uniformFloat(this.litShadowStrength, SHADOW_STRENGTH);
		this.shadows.bindForRendering(this.lit, this.shadowEngine);
		this.lit.uniformInt(this.litGrid, 1);
		this.ground.draw(GL11.GL_TRIANGLES);
		this.lit.uniformInt(this.litGrid, 0);
		this.opaque.draw(GL11.GL_TRIANGLES);
		this.human.draw(GL11.GL_TRIANGLES);
		OpenGL.enable(Flag.flag_blend);
		OpenGL.updateAllFlags();
		OpenGL.blendFuncAuto();
		GL11.glDepthMask(false);
		this.translucent.draw(GL11.GL_TRIANGLES);
		GL11.glDepthMask(true);
		this.shadows.unBindForRendering();
		this.lit.unUse();
		this.line.use();
		this.line.uniformMatrix(this.lineProjection, projection);
		this.line.uniformMatrix(this.lineView, view);
		this.lines.draw(GL11.GL_LINES);
		OpenGL.disable(Flag.flag_depthTest);
		OpenGL.updateAllFlags();
		this.linesOnTop.draw(GL11.GL_LINES);
		OpenGL.enable(Flag.flag_depthTest);
		OpenGL.updateAllFlags();
		this.line.unUse();
		OpenGL.blendFuncAuto();
		OpenGL.disable(Flag.flag_blend);
		OpenGL.updateAllFlags();
	}

	/**
	 * Give everything back, once, from any thread: the caster leaves the
	 * shadow engine, the buffers are deleted on the rendering thread, the
	 * programs released.
	 */
	void release() {
		if (this.released) {
			return;
		}
		this.released = true;
		if (this.shadowEngine != null) {
			this.shadowEngine.removeShadowCaster(this.caster);
		}
		final List<int[]> objects = new ArrayList<>();
		for (final Layer layer : new Layer[] { this.ground, this.opaque, this.human, this.translucent, this.lines,
				this.linesOnTop }) {
			objects.add(layer.objects());
		}
		this.litCreated.release();
		this.lineCreated.release();
		Gale.getContext().getResourcesManager().runOnGlThread(() -> {
			for (final int[] object : objects) {
				if (object[1] != -1) {
					GL15.glDeleteBuffers(object[1]);
				}
				if (object[0] != -1) {
					GL30.glDeleteVertexArrays(object[0]);
				}
			}
		});
	}
}
