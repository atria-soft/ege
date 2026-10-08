package org.atriasoft.ege.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/** The build thread of a lab: the newest request wins, a throwable becomes a result. */
class LabWorkshopTest {

	private static <R, M> LabWorkshop.Result<R, M> await(final LabWorkshop<R, M> workshop, final long generation)
			throws InterruptedException {
		final long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
		while (System.nanoTime() < end) {
			final LabWorkshop.Result<R, M> latest = workshop.latest();
			if (latest != null && latest.generation() >= generation) {
				return latest;
			}
			Thread.sleep(5);
		}
		throw new AssertionError("request " + generation + " never built");
	}

	@Test
	void theNewestRequestWinsAndTheOnesBetweenAreSkipped() throws InterruptedException {
		final CountDownLatch release = new CountDownLatch(1);
		final List<Integer> built = new CopyOnWriteArrayList<>();
		try (LabWorkshop<Integer, String> workshop = new LabWorkshop<>("test-workshop", request -> {
			if (request == 1) {
				try {
					release.await(10, TimeUnit.SECONDS);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			built.add(request);
			return "model " + request;
		})) {
			assertNull(workshop.latest());
			workshop.request(1);
			// Let the first build start and block.
			Thread.sleep(50);
			workshop.request(2);
			final long last = workshop.request(3);
			assertTrue(workshop.busy());
			release.countDown();
			final LabWorkshop.Result<Integer, String> result = await(workshop, last);
			assertEquals("model 3", result.model());
			assertFalse(result.failed());
			assertFalse(workshop.busy());
			assertEquals(List.of(1, 3), built);
		}
	}

	@Test
	void aThrowableBecomesAResultAndTheThreadGoesOn() throws InterruptedException {
		try (LabWorkshop<String, String> workshop = new LabWorkshop<>("test-workshop", request -> {
			if (request.equals("bad")) {
				throw new StackOverflowError("too deep");
			}
			return request.toUpperCase();
		})) {
			final LabWorkshop.Result<String, String> failed = await(workshop, workshop.request("bad"));
			assertTrue(failed.failed());
			assertNull(failed.model());
			assertTrue(failed.error() instanceof StackOverflowError);
			final LabWorkshop.Result<String, String> good = await(workshop, workshop.request("good"));
			assertEquals("GOOD", good.model());
		}
	}
}
