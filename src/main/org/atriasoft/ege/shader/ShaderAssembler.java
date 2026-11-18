package org.atriasoft.ege.shader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL.ShaderType;
import org.atriasoft.gale.resource.ResourceProgram;
import org.atriasoft.gale.resource.ShaderText;

/**
 * Include mechanism for the GLSL sources of ege (gale's shader loader has none).
 * <p>
 * A line {@code #include "shadow/csm.glsl"} of a shader is replaced by the text of the file of ege's data
 * ({@code DATA:shadow/csm.glsl} of the library {@code ege}), before the shader is compiled. The included text
 * goes where the directive stood, so a {@code #version} line stays first, between {@code #line 1} and
 * {@code #line N+1} directives so that a compile error points at the real line of the real source. Nothing is
 * written to disk: {@link #assemble(Uri)} gives the assembled text (cached per source {@link Uri} until
 * {@link #invalidate()}), {@link #shader(Uri, ShaderType)} a gale {@link ShaderText} that assembles again at each
 * reload of the resource (live edits of the shader or of an included file show up) and
 * {@link #program(Uri, Uri)} the program. {@link #expand} is the pure part.
 * <p>
 * Directive: {@code #include} (after blanks) followed by blanks and a quoted name; only the first quoted token
 * counts, whatever follows is ignored. A line inside a block comment is never a directive.
 */
public final class ShaderAssembler {
	private static final String INCLUDE = "#include";
	private static final int MAX_DEPTH = 8;
	private static final ConcurrentHashMap<String, String> CACHE = new ConcurrentHashMap<>();

	private ShaderAssembler() {
	}

	/**
	 * Replace the {@code #include "name"} lines of {@code source}, recursively.
	 *
	 * @param source the shader text
	 * @param resolver gives the text of an included file by its name, {@code null} when it does not exist
	 * @return the text without any directive
	 * @throws IllegalArgumentException on a directive that is malformed, a file not found, or a cycle
	 */
	public static String expand(final String source, final Function<String, String> resolver) {
		return expand(source, resolver, new ArrayList<>());
	}

	private static String expand(final String source, final Function<String, String> resolver,
			final List<String> stack) {
		if (!source.contains(INCLUDE)) {
			return source;
		}
		final List<String> out = new ArrayList<>();
		final String[] lines = source.split("\n", -1);
		boolean inComment = false;
		for (int index = 0; index < lines.length; index++) {
			final String line = lines[index];
			final boolean startsInComment = inComment;
			inComment = endsInComment(line, inComment);
			final String name = startsInComment ? null : includeName(line);
			if (name == null) {
				out.add(line);
				continue;
			}
			if (stack.contains(name) || stack.size() >= MAX_DEPTH) {
				throw new IllegalArgumentException("include cycle or too deep: " + stack + " -> " + name);
			}
			final String text = resolver.apply(name);
			if (text == null) {
				throw new IllegalArgumentException("include not found: " + name);
			}
			stack.add(name);
			String expanded = expand(text, resolver, stack);
			stack.remove(stack.size() - 1);
			if (expanded.endsWith("\n")) {
				expanded = expanded.substring(0, expanded.length() - 1);
			}
			out.add("#line 1");
			out.add(expanded);
			out.add("#line " + (index + 2));
		}
		return String.join("\n", out);
	}

	/**
	 * The name of the include directive that {@code line} holds, {@code null} when it is not one.
	 *
	 * @throws IllegalArgumentException when it is one but has no quoted name
	 */
	private static String includeName(final String line) {
		final String trimmed = line.strip();
		if (!trimmed.startsWith(INCLUDE)) {
			return null;
		}
		final String rest = trimmed.substring(INCLUDE.length());
		if (!rest.isEmpty() && !Character.isWhitespace(rest.charAt(0))) {
			return null; // another word, such as #includes
		}
		final String argument = rest.strip();
		if (argument.isEmpty() || argument.charAt(0) != '"') {
			throw new IllegalArgumentException("malformed include: " + trimmed);
		}
		final int close = argument.indexOf('"', 1);
		if (close < 0) {
			throw new IllegalArgumentException("malformed include: " + trimmed);
		}
		final String name = argument.substring(1, close);
		if (name.isEmpty()) {
			throw new IllegalArgumentException("malformed include: " + trimmed);
		}
		return name;
	}

	/** Whether a block comment is still open at the end of {@code line}, which starts in the state given. */
	private static boolean endsInComment(final String line, final boolean startsInComment) {
		boolean inComment = startsInComment;
		int position = 0;
		while (position < line.length()) {
			if (inComment) {
				final int end = line.indexOf("*/", position);
				if (end < 0) {
					return true;
				}
				inComment = false;
				position = end + 2;
			} else {
				final int block = line.indexOf("/*", position);
				final int lineComment = line.indexOf("//", position);
				if (block < 0 || (lineComment >= 0 && lineComment < block)) {
					return false;
				}
				inComment = true;
				position = block + 2;
			}
		}
		return inComment;
	}

	/**
	 * The assembled text of the shader {@code uri} (its includes name files of ege's data), cached per source
	 * Uri until {@link #invalidate()}.
	 *
	 * @throws IllegalArgumentException when the shader or an included file is not found, or an include is malformed;
	 *         the message names the shader
	 */
	public static String assemble(final Uri uri) {
		final String key = uri.toString();
		final String cached = CACHE.get(key);
		if (cached != null) {
			return cached;
		}
		return assembleFresh(uri);
	}

	/** Forget every cached text: the next {@link #assemble(Uri)} reads the files again. */
	public static void invalidate() {
		CACHE.clear();
	}

	private static String assembleFresh(final Uri uri) {
		final String source = Uri.getAllDataString(uri);
		if (source == null) {
			throw new IllegalArgumentException("Cannot assemble the shader " + uri + ": source not found");
		}
		final String text;
		try {
			text = expand(source, name -> Uri.getAllDataString(new Uri("DATA", name, "ege")));
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("Cannot assemble the shader " + uri + ": " + e.getMessage(), e);
		}
		CACHE.put(uri.toString(), text);
		return text;
	}

	/**
	 * The shader {@code uri} as a gale {@link ShaderText}: the first text comes from the cache, each reload of
	 * the resource assembles it again from the files (and refreshes the cache).
	 *
	 * @throws IllegalArgumentException as {@link #assemble(Uri)}
	 */
	public static ShaderText shader(final Uri uri, final ShaderType type) {
		final AtomicBoolean first = new AtomicBoolean(true);
		return ShaderText.of(uri.toString(), type, () -> first.getAndSet(false) ? assemble(uri) : assembleFresh(uri));
	}

	/**
	 * The program of a vertex and a fragment shader, assembled.
	 *
	 * @throws IllegalArgumentException when a shader cannot be assembled; the message names its Uri
	 */
	public static ResourceProgram program(final Uri vertexShader, final Uri fragmentShader) {
		return ResourceProgram.create(shader(vertexShader, ShaderType.VERTEX),
				shader(fragmentShader, ShaderType.FRAGMENT));
	}
}
