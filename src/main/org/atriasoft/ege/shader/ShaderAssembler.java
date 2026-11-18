package org.atriasoft.ege.shader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.function.Function;

import org.atriasoft.etk.Uri;

/**
 * Include mechanism for the GLSL sources of ege (gale's shader loader has none).
 * <p>
 * A line {@code #include "shadow/csm.glsl"} of a shader is replaced by the text of the file of ege's data
 * ({@code DATA:shadow/csm.glsl} of the library {@code ege}), before the shader is compiled. The included text
 * goes where the directive stood, so a {@code #version} line stays first. {@link #assemble(Uri)} does it for a
 * shader that is about to be given to {@code ResourceProgram.create}: a source without a directive is returned
 * unchanged (same {@link Uri}), otherwise the assembled text is written to a file of the temporary directory
 * named after its content, and its {@code FILE} {@link Uri} is returned. {@link #expand} is the pure part.
 */
public final class ShaderAssembler {
	/** Directive recognised at the start of a line (after blanks). */
	private static final String INCLUDE = "#include";
	private static final int MAX_DEPTH = 8;

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
		for (final String line : source.split("\n", -1)) {
			final String trimmed = line.strip();
			if (!trimmed.startsWith(INCLUDE)) {
				out.add(line);
				continue;
			}
			final int open = trimmed.indexOf('"');
			final int close = trimmed.lastIndexOf('"');
			if (open < 0 || close <= open) {
				throw new IllegalArgumentException("malformed include: " + trimmed);
			}
			final String name = trimmed.substring(open + 1, close);
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
			out.add(expanded);
		}
		return String.join("\n", out);
	}

	/**
	 * The shader {@code uri} ready to compile: itself when it holds no include, else a file of the temporary
	 * directory holding the assembled text. Includes name files of ege's data.
	 *
	 * @throws IllegalArgumentException when the shader or an included file is not found
	 */
	public static Uri assemble(final Uri uri) {
		final String source = Uri.getAllDataString(uri);
		if (source == null) {
			// Left to the loader, which reports the missing file its own way.
			return uri;
		}
		if (!source.contains(INCLUDE)) {
			return uri;
		}
		final String text = expand(source, name -> Uri.getAllDataString(new Uri("DATA", name, "ege")));
		final String path = uri.getPath();
		final String file = path.substring(path.lastIndexOf('/') + 1);
		try {
			final byte[] digest = MessageDigest.getInstance("SHA-1").digest(text.getBytes(StandardCharsets.UTF_8));
			final Path directory = Path.of(System.getProperty("java.io.tmpdir"), "ege-shaders",
					HexFormat.of().formatHex(digest));
			final Path target = directory.resolve(file);
			if (!Files.exists(target)) {
				Files.createDirectories(directory);
				final Path temporary = Files.createTempFile(directory, "part", ".tmp");
				Files.writeString(temporary, text, StandardCharsets.UTF_8);
				Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
			}
			return new Uri("FILE", target.toAbsolutePath().toString());
		} catch (final IOException e) {
			throw new UncheckedIOException("Cannot write the assembled shader " + uri, e);
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
