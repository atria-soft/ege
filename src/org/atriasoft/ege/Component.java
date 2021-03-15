package org.atriasoft.ege;

public abstract class Component {
	public abstract String getType();
	/**
	 * @brief Evironement notify that a new component is added on the same Element
	 * @param component New component added
	 */
	public void addFriendComponent(Component component) {
		// nothing to do.
	}
	/**
	 * @brief Evironement notify that a component is removed on the same Element
	 * @param component Old component removed
	 */
	public void removeFriendComponent(Component component) {
		// nothing to do.
	}

}
