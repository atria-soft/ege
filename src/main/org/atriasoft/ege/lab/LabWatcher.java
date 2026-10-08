package org.atriasoft.ege.lab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

/**
 * Watches the data files of a lab: polled (once a second by the view), it
 * tells when one of them changed (its time, its size, or it came or went)
 * since they were read, and stayed the same for one poll (a file being
 * written is not read half way). Pure Java, tested headless.
 */
public final class LabWatcher {

	private final Supplier<List<Path>> files;
	/** Stamp of the files when they were read last, and at the last poll. */
	private long readStamp;
	private long polledStamp;

	/** @param files the files read now (asked at every poll: a file may come or go) */
	public LabWatcher(final Supplier<List<Path>> files) {
		this.files = files;
		markRead();
	}

	/**
	 * A number that changes when one of {@code files} changes (its time, its
	 * size), comes or goes.
	 */
	public static long stamp(final List<Path> files) {
		long stamp = 17L;
		for (final Path file : files) {
			long time = 0L;
			long size = -1L;
			try {
				time = Files.getLastModifiedTime(file).toMillis();
				size = Files.size(file);
			} catch (final IOException | RuntimeException e) {
				// Missing: counted as a change when it comes back.
			}
			stamp = stamp * 31L + file.hashCode();
			stamp = stamp * 31L + time;
			stamp = stamp * 31L + size;
		}
		return stamp;
	}

	private long stamp() {
		try {
			return stamp(this.files.get());
		} catch (final RuntimeException e) {
			return this.polledStamp;
		}
	}

	/** The files were read now (F5, or after {@link #poll} said so): a change counts from here. */
	public void markRead() {
		this.readStamp = stamp();
		this.polledStamp = this.readStamp;
	}

	/**
	 * One poll: whether the files changed since they were read and stayed the
	 * same since the poll before. Then they count as read: read them now.
	 */
	public boolean poll() {
		final long stamp = stamp();
		if (stamp == this.readStamp) {
			this.polledStamp = stamp;
			return false;
		}
		if (stamp == this.polledStamp) {
			this.readStamp = stamp;
			return true;
		}
		this.polledStamp = stamp;
		return false;
	}
}
