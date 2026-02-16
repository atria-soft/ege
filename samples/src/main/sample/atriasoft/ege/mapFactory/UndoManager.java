package sample.atriasoft.ege.mapFactory;

import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndoManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(UndoManager.class);
	private static final int MAX_HISTORY = 100;

	private final Deque<MapAction> undoStack = new ArrayDeque<>();
	private final Deque<MapAction> redoStack = new ArrayDeque<>();

	public void execute(final MapAction action) {
		action.redo();
		this.undoStack.push(action);
		this.redoStack.clear();
		if (this.undoStack.size() > MAX_HISTORY) {
			this.undoStack.removeLast();
		}
		LOGGER.debug("Execute action, undo stack size: {}", this.undoStack.size());
	}

	public void undo() {
		if (this.undoStack.isEmpty()) {
			LOGGER.debug("Nothing to undo");
			return;
		}
		final MapAction action = this.undoStack.pop();
		action.undo();
		this.redoStack.push(action);
		LOGGER.debug("Undo, undo stack: {}, redo stack: {}", this.undoStack.size(), this.redoStack.size());
	}

	public void redo() {
		if (this.redoStack.isEmpty()) {
			LOGGER.debug("Nothing to redo");
			return;
		}
		final MapAction action = this.redoStack.pop();
		action.redo();
		this.undoStack.push(action);
		LOGGER.debug("Redo, undo stack: {}, redo stack: {}", this.undoStack.size(), this.redoStack.size());
	}

	public boolean canUndo() {
		return !this.undoStack.isEmpty();
	}

	public boolean canRedo() {
		return !this.redoStack.isEmpty();
	}

	public void clear() {
		this.undoStack.clear();
		this.redoStack.clear();
		LOGGER.debug("Undo history cleared");
	}
}
