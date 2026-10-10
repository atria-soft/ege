package org.atriasoft.ege.lab;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * What a lab remembers for its user between two runs, in a small file of its
 * own: {@code ~/.config/atriasoft/lab/<class of the lab>.properties} (the
 * width of its panel, {@value #PANEL_WIDTH}). One file per class of lab, so
 * two different labs open at once never write over each other (two windows of
 * the same lab share it: the last written wins). Written whole to a file
 * beside it, forced to the disk, then moved over it at once; whatever goes
 * wrong while reading or writing is ignored (a lab with no settings starts as
 * new). Pure Java.
 */
final class LabSettings {

	/** The width of the control panel, pixels. */
	static final String PANEL_WIDTH = "panel.width";

	private LabSettings() {}

	/** The folder of the settings of the labs, {@code null} when the user has no home. */
	static Path folder() {
		try {
			return folder(System.getProperty("user.home"));
		} catch (final RuntimeException e) {
			return null;
		}
	}

	/** The folder of the settings of the labs of a user whose home is {@code home}, {@code null} for none. */
	static Path folder(final String home) {
		if (home == null || home.isBlank()) {
			return null;
		}
		try {
			return Path.of(home, ".config", "atriasoft", "lab");
		} catch (final RuntimeException e) {
			return null;
		}
	}

	/** The settings file of the lab of class {@code lab} in {@code folder}, {@code null} when there is no folder. */
	static Path file(final Path folder, final Class<?> lab) {
		return folder != null ? folder.resolve(name(lab) + ".properties") : null;
	}

	/**
	 * The name of the file of a lab: its class, every character but letters, digits, {@code .} and {@code -} made
	 * {@code _}.
	 */
	static String name(final Class<?> lab) {
		final String name = lab.getName();
		final StringBuilder out = new StringBuilder(name.length());
		for (int i = 0; i < name.length(); i++) {
			final char c = name.charAt(i);
			final boolean plain = c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '.'
					|| c == '-';
			out.append(plain ? c : '_');
		}
		return out.toString();
	}

	/** The settings in {@code file}: none when it is missing, unreadable or {@code null}. */
	static Properties read(final Path file) {
		final Properties values = new Properties();
		if (file == null) {
			return values;
		}
		try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			values.load(reader);
		} catch (final IOException | RuntimeException e) {
			// No settings: the lab starts as new.
			values.clear();
		}
		return values;
	}

	/**
	 * Write {@code values} to {@code file} at once: whole to a file beside it, then moved over it (the folders made
	 * when missing).
	 *
	 * @return whether it was written (a failure is otherwise ignored)
	 */
	static boolean write(final Path file, final Properties values) {
		if (file == null || file.getParent() == null) {
			return false;
		}
		Path temporary = null;
		try {
			Files.createDirectories(file.getParent());
			temporary = Files.createTempFile(file.getParent(), file.getFileName().toString(), ".tmp");
			try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING);
					Writer writer = Channels.newWriter(channel, StandardCharsets.UTF_8)) {
				values.store(writer, "Settings of a lab (org.atriasoft.ege.lab)");
				writer.flush();
				channel.force(true);
			}
			try {
				Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (final AtomicMoveNotSupportedException e) {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
			}
			temporary = null;
			return true;
		} catch (final IOException | RuntimeException e) {
			return false;
		} finally {
			if (temporary != null) {
				try {
					Files.deleteIfExists(temporary);
				} catch (final IOException | RuntimeException e) {
					// Left behind: nothing reads it.
				}
			}
		}
	}

	/** The width of the panel remembered in {@code file}, {@code NaN} when there is none (or it is no number). */
	static float panelWidth(final Path file) {
		final String text = read(file).getProperty(PANEL_WIDTH);
		if (text == null) {
			return Float.NaN;
		}
		try {
			return Float.parseFloat(text.strip());
		} catch (final NumberFormatException e) {
			return Float.NaN;
		}
	}

	/**
	 * Remember {@code width} as the width of the panel in {@code file}, the other settings kept.
	 *
	 * @return whether it was written
	 */
	static boolean savePanelWidth(final Path file, final float width) {
		final Properties values = read(file);
		values.setProperty(PANEL_WIDTH, Integer.toString(Math.round(width)));
		return write(file, values);
	}
}
