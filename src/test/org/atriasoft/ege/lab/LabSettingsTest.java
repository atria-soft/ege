package org.atriasoft.ege.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The settings a lab remembers: the width of its panel, one file per lab, written at once, failures ignored. */
class LabSettingsTest {

	@TempDir
	Path folder;

	@Test
	void theWidthIsRememberedPerLab() throws IOException {
		final Path file = LabSettings.file(this.folder.resolve("deep/lab"), LabSettingsTest.class);
		assertEquals("org.atriasoft.ege.lab.LabSettingsTest.properties", file.getFileName().toString());
		assertTrue(Float.isNaN(LabSettings.panelWidth(file)), "nothing remembered yet");
		assertTrue(LabSettings.savePanelWidth(file, 452.4f));
		assertEquals(452.0f, LabSettings.panelWidth(file));
		assertTrue(LabSettings.savePanelWidth(file, 500.0f));
		assertEquals(500.0f, LabSettings.panelWidth(file));
		// Written whole beside it, then moved over it: nothing else is left in the folder.
		try (Stream<Path> files = Files.list(file.getParent())) {
			assertEquals(List.of(file), files.toList());
		}
		// Another lab has its own file.
		final Path other = LabSettings.file(file.getParent(), LabPanelSize.class);
		assertTrue(LabSettings.savePanelWidth(other, 300.0f));
		assertEquals(500.0f, LabSettings.panelWidth(file));
		assertEquals(300.0f, LabSettings.panelWidth(other));
	}

	@Test
	void theOtherSettingsAreKept() throws IOException {
		final Path file = this.folder.resolve("lab.properties");
		Files.writeString(file, "colour=red\npanel.width=310\n", StandardCharsets.UTF_8);
		assertEquals(310.0f, LabSettings.panelWidth(file));
		assertTrue(LabSettings.savePanelWidth(file, 420.0f));
		final Properties values = LabSettings.read(file);
		assertEquals("red", values.getProperty("colour"));
		assertEquals("420", values.getProperty(LabSettings.PANEL_WIDTH));
	}

	@Test
	void aBrokenFileOrAFailureIsIgnored() throws IOException {
		final Path file = this.folder.resolve("lab.properties");
		Files.writeString(file, "panel.width=wide\n", StandardCharsets.UTF_8);
		assertTrue(Float.isNaN(LabSettings.panelWidth(file)));
		Files.write(file, new byte[] { (byte) 0xff, (byte) 0xfe, 0, 1, '\\', 'u', 'z', 'z' });
		assertTrue(Float.isNaN(LabSettings.panelWidth(file)));
		assertTrue(LabSettings.read(file).isEmpty());
		// A folder where the file should be, a file where its folder should be: not written, nothing thrown.
		final Path folderInstead = this.folder.resolve("a-folder.properties");
		Files.createDirectories(folderInstead);
		assertFalse(LabSettings.savePanelWidth(folderInstead, 400.0f));
		assertTrue(Float.isNaN(LabSettings.panelWidth(folderInstead)));
		final Path fileInstead = this.folder.resolve("plain");
		Files.writeString(fileInstead, "x", StandardCharsets.UTF_8);
		assertFalse(LabSettings.savePanelWidth(fileInstead.resolve("lab.properties"), 400.0f));
		// No file at all (no home): nothing read, nothing written.
		assertTrue(Float.isNaN(LabSettings.panelWidth(null)));
		assertFalse(LabSettings.savePanelWidth(null, 400.0f));
		assertNull(LabSettings.file(null, LabSettingsTest.class));
	}

	@Test
	void theFolderIsUnderTheHomeOfTheUser() {
		assertEquals(this.folder.resolve(".config/atriasoft/lab"), LabSettings.folder(this.folder.toString()));
		assertNull(LabSettings.folder(""));
		assertNull(LabSettings.folder(null));
		assertEquals(Path.of(System.getProperty("user.home"), ".config", "atriasoft", "lab"), LabSettings.folder());
	}

	@Test
	void theNameOfTheFileIsTheClassInPlainLetters() {
		assertEquals("org.atriasoft.ege.lab.LabSettingsTest", LabSettings.name(LabSettingsTest.class));
		final Object anonymous = new Object() {};
		assertEquals("org.atriasoft.ege.lab.LabSettingsTest_1", LabSettings.name(anonymous.getClass()));
	}
}
