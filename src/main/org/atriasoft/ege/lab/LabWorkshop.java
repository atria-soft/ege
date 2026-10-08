package org.atriasoft.ege.lab;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the models of a lab on a thread of its own, so that the picture
 * keeps moving while a row of trees grows or a building bakes. The newest
 * request wins: a request that has not started when a newer one comes is
 * skipped, and a result is published only when it is newer than the one
 * published ({@link #latest()}: a growing tree shows each step built). A
 * throwable of a build, whatever it is (an error of the data, a class that
 * does not load, no memory left), becomes a result that carries it, never
 * an exception past this thread.
 * <p>
 * Data that must be read again before a build (a file that changed) is best
 * read by the builder itself, from a token of the request (a reload counter):
 * the newest request carries the newest token, so skipping older requests
 * never skips a reading.
 *
 * @param <R> what is asked (an immutable selection)
 * @param <M> what is built
 */
public final class LabWorkshop<R, M> implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(LabWorkshop.class);

	/**
	 * What a request gave.
	 *
	 * @param generation the number of the request (they grow)
	 * @param request    what was asked
	 * @param model      what was built, {@code null} when it failed
	 * @param error      why it failed, {@code null} when it did not
	 */
	public record Result<R, M>(long generation, R request, M model, Throwable error) {

		/** Whether the build threw. */
		public boolean failed() {
			return this.error != null;
		}
	}

	private final Function<R, M> builder;
	private final ExecutorService executor;
	private final AtomicLong asked = new AtomicLong();
	private volatile Result<R, M> latest;

	/**
	 * @param name    name of the thread
	 * @param builder builds a model from a request, on the thread of the workshop
	 */
	public LabWorkshop(final String name, final Function<R, M> builder) {
		this.builder = builder;
		this.executor = Executors.newSingleThreadExecutor(work -> {
			final Thread thread = new Thread(work, name);
			thread.setDaemon(true);
			return thread;
		});
	}

	/** Build {@code request} (after the build running now, unless a newer request comes first). */
	public long request(final R request) {
		final long generation = this.asked.incrementAndGet();
		try {
			this.executor.execute(() -> build(generation, request));
		} catch (final RuntimeException e) {
			// Closed: nothing is built any more.
			LOGGER.debug("Lab workshop closed, request {} dropped", generation);
		}
		return generation;
	}

	private void build(final long generation, final R request) {
		if (generation != this.asked.get()) {
			return;
		}
		Result<R, M> result;
		try {
			result = new Result<>(generation, request, this.builder.apply(request), null);
		} catch (final Throwable e) {
			LOGGER.error("The lab cannot build {}: {}", request, e.toString(), e);
			result = new Result<>(generation, request, null, e);
		}
		publish(result);
	}

	private synchronized void publish(final Result<R, M> result) {
		final Result<R, M> shown = this.latest;
		if (shown == null || result.generation() > shown.generation()) {
			this.latest = result;
		}
	}

	/** The newest result, {@code null} before the first one. */
	public Result<R, M> latest() {
		return this.latest;
	}

	/** The number of the last request. */
	public long requested() {
		return this.asked.get();
	}

	/** Whether the last request is not built yet. */
	public boolean busy() {
		final Result<R, M> shown = this.latest;
		return shown == null || shown.generation() < this.asked.get();
	}

	/** Stop the thread (what it builds is dropped). */
	@Override
	public void close() {
		this.asked.incrementAndGet();
		this.executor.shutdownNow();
	}
}
