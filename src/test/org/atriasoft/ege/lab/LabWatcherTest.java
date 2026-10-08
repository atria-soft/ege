package org.atriasoft.ege.lab;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** A change of a data file is read once it stayed the same for a poll. */
class LabWatcherTest {

	@TempDir
	Path folder;

	@Test
	void aChangeIsReadOnceItStaysStill() throws IOException {
		final Path file = this.folder.resolve("species.json");
		Files.writeString(file, "{}");
		final LabWatcher watcher = new LabWatcher(() -> List.of(file));
		assertFalse(watcher.poll(), "nothing changed");
		Files.writeString(file, "{ \"species\": [] }");
		Files.setLastModifiedTime(file, FileTime.fromMillis(Files.getLastModifiedTime(file).toMillis() + 2000));
		assertFalse(watcher.poll(), "being written: wait for a still poll");
		assertTrue(watcher.poll(), "still: read it");
		assertFalse(watcher.poll(), "read already");
		// Still moving at each poll: never read half way.
		Files.writeString(file, "{ \"species\": [ {} ] }");
		assertFalse(watcher.poll());
		Files.writeString(file, "{ \"species\": [ {}, {} ] }");
		assertFalse(watcher.poll());
		assertTrue(watcher.poll());
	}

	@Test
	void aFileThatGoesOrComesIsAChangeAndF5StartsAgain() throws IOException {
		final Path file = this.folder.resolve("layer.json");
		final LabWatcher watcher = new LabWatcher(() -> List.of(file));
		Files.writeString(file, "{}");
		assertFalse(watcher.poll());
		watcher.markRead();
		assertFalse(watcher.poll(), "read on demand: no change left");
		Files.delete(file);
		assertFalse(watcher.poll());
		assertTrue(watcher.poll());
	}
}
