package org.atriasoft.ege.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.engines.EnginePostProcess;
import org.atriasoft.ege.postprocess.PostProcessEffect;

/**
 * Component that holds an ordered list of post-process effects for an entity.
 * <p>
 * Attach this component to an entity to apply screen-space visual effects
 * (outline, tint, glow, etc.) that are rendered after the main scene pass.
 * Effects are applied in order and share the same silhouette mask.
 * <p>
 * The component discovers its sibling {@link ComponentMesh} and
 * {@link ComponentPosition} via the friend-component mechanism,
 * which are needed by the engine to render the silhouette pass.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Add selection highlight to an entity
 * entity.addComponent(new ComponentPostProcess()
 *     .addEffect(new OutlineEffect(6.0f, Color.BLACK))
 *     .addEffect(new AdditiveOverlayEffect(new Color(1, 1, 1, 0.25f))));
 *
 * // Modify effects at runtime
 * ComponentPostProcess pp = (ComponentPostProcess) entity.getComponent("postprocess");
 * pp.clearEffects();
 *
 * // Remove all post-processing
 * entity.removeComponent("postprocess");
 * }</pre>
 *
 * @see PostProcessEffect
 * @see EnginePostProcess
 */
public class ComponentPostProcess extends Component {
	/** Component type identifier used for engine routing. */
	public static final String COMPONENT_NAME = "postprocess";

	private final List<PostProcessEffect> effects = new ArrayList<>();
	private ComponentMesh mesh = null;
	private ComponentPosition position = null;

	@Override
	public String getType() {
		return COMPONENT_NAME;
	}

	@Override
	public void addFriendComponent(final Component component) {
		if (component instanceof ComponentMesh m) {
			this.mesh = m;
		}
		if (component instanceof ComponentPosition p) {
			this.position = p;
		}
	}

	@Override
	public void removeFriendComponent(final Component component) {
		if (component == this.mesh) {
			this.mesh = null;
		}
		if (component == this.position) {
			this.position = null;
		}
	}

	// --- Fluent effect management API ---

	/**
	 * Add an effect at the end of the effect list.
	 *
	 * @param effect The effect to add
	 * @return This component for method chaining
	 */
	public ComponentPostProcess addEffect(final PostProcessEffect effect) {
		this.effects.add(effect);
		return this;
	}

	/**
	 * Insert an effect at the given index in the effect list.
	 *
	 * @param index  Position to insert at (0 = first)
	 * @param effect The effect to insert
	 * @return This component for method chaining
	 */
	public ComponentPostProcess addEffect(final int index, final PostProcessEffect effect) {
		this.effects.add(index, effect);
		return this;
	}

	/**
	 * Remove an effect by reference.
	 *
	 * @param effect The effect to remove
	 * @return {@code true} if the effect was found and removed
	 */
	public boolean removeEffect(final PostProcessEffect effect) {
		return this.effects.remove(effect);
	}

	/**
	 * Remove and return the effect at the given index.
	 *
	 * @param index Index of the effect to remove
	 * @return The removed effect
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	public PostProcessEffect removeEffect(final int index) {
		return this.effects.remove(index);
	}

	/**
	 * Remove all effects from this component.
	 */
	public void clearEffects() {
		this.effects.clear();
	}

	/**
	 * Get an unmodifiable view of the current effects in render order.
	 *
	 * @return Unmodifiable list of effects
	 */
	public List<PostProcessEffect> getEffects() {
		return Collections.unmodifiableList(this.effects);
	}

	/**
	 * Get the number of effects currently attached.
	 *
	 * @return Number of effects
	 */
	public int getEffectCount() {
		return this.effects.size();
	}

	/**
	 * Check if this component has any effects.
	 *
	 * @return {@code true} if at least one effect is attached
	 */
	public boolean hasEffects() {
		return !this.effects.isEmpty();
	}

	/**
	 * Get the sibling mesh component discovered via friend-component mechanism.
	 *
	 * @return The sibling mesh component, or {@code null} if not present
	 */
	public ComponentMesh getMesh() {
		return this.mesh;
	}

	/**
	 * Get the sibling position component discovered via friend-component mechanism.
	 *
	 * @return The sibling position component, or {@code null} if not present
	 */
	public ComponentPosition getPosition() {
		return this.position;
	}
}
