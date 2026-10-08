package org.atriasoft.ege.lab;

/**
 * Where the problems of a lab go (the info panel of its {@link LabView}):
 * each one under a name ({@code Proxies}, {@code reading the data}), shown
 * until the same thing succeeds again ({@link #clear}).
 */
public interface LabReporter {

	/** {@code what} failed with {@code error}. */
	void report(String what, Throwable error);

	/** Something is wrong with {@code what}: {@code message}. */
	void report(String what, String message);

	/** {@code what} succeeded: its problem, if any, is gone. */
	void clear(String what);
}
