package org.atriasoft.ege.shader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL.ShaderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The assembly of the GLSL sources (headless): the include directive and the shaders that use it. */
class ShaderAssemblerTest {
	private static final Path EGE_DATA = Path.of("src/resources/resources/ege/data");
	private static final Path[] RECEIVERS = {
			Path.of("src/resources/resources/ege/data/lab/labLit.frag"),
			Path.of("samples/src/resources/resources/shadowTest/data/basicPalette.frag"),
			Path.of("samples/src/resources/resources/shadowTest/data/shadowMaterial.frag") };

	private static String read(final Path path) {
		try {
			return Files.readString(path, StandardCharsets.UTF_8);
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static String assembled(final Path shader) {
		return ShaderAssembler.expand(read(shader), name -> read(EGE_DATA.resolve(name)));
	}

	@Test
	void anIncludeIsReplacedWhereItStands() {
		final Map<String, String> files = Map.of("a.glsl", "float a;\n", "b.glsl", "#include \"a.glsl\"\nfloat b;");
		final String out = ShaderAssembler.expand("#version 400\n  #include \"b.glsl\"\nvoid main() {}\n", files::get);
		assertEquals("#version 400\n#line 1\n#line 1\nfloat a;\n#line 2\nfloat b;\n#line 3\nvoid main() {}\n", out);
	}

	@Test
	void lineDirectivesPointBackAtTheRealLines() {
		final String out = ShaderAssembler.expand("a\nb\n#include \"x\"\nc", name -> "x1\nx2\n");
		assertEquals("a\nb\n#line 1\nx1\nx2\n#line 4\nc", out);
	}

	@Test
	void onlyTheFirstQuotedTokenCounts() {
		final String out = ShaderAssembler.expand("#include \"a\" \"b\" // \"c\"\n", name -> "[" + name + "]");
		assertEquals("#line 1\n[a]\n#line 2\n", out);
	}

	@Test
	void theDirectiveMustBeExactlyInclude() {
		final String source = "#includes \"a\"\n#include_next \"a\"\n";
		assertEquals(source, ShaderAssembler.expand(source, name -> {
			throw new AssertionError(name);
		}));
		assertThrows(IllegalArgumentException.class, () -> ShaderAssembler.expand("#include\n", name -> ""));
		assertEquals("#line 1\nz\n#line 2", ShaderAssembler.expand("\t#include\t\"a\"", name -> "z"));
	}

	@Test
	void anIncludeInsideABlockCommentIsIgnored() {
		final String source = "/*\n#include \"nope\"\n*/\n// #include \"nope\"\nfloat a; /* x\n#include \"nope\" */\n";
		assertEquals(source, ShaderAssembler.expand(source, name -> {
			throw new AssertionError(name);
		}));
		final String after = "/* c */ float a;\n#include \"a\"";
		assertEquals("/* c */ float a;\n#line 1\nz\n#line 3", ShaderAssembler.expand(after, name -> "z"));
	}

	@Test
	void theAssembledTextIsCachedUntilInvalidated(@TempDir final Path directory) throws IOException {
		final Path file = directory.resolve("t.frag");
		Files.writeString(file, "one", StandardCharsets.UTF_8);
		final Uri uri = new Uri("FILE", file.toAbsolutePath().toString());
		ShaderAssembler.invalidate();
		assertEquals("one", ShaderAssembler.assemble(uri));
		Files.writeString(file, "two", StandardCharsets.UTF_8);
		assertEquals("one", ShaderAssembler.assemble(uri));
		ShaderAssembler.invalidate();
		assertEquals("two", ShaderAssembler.assemble(uri));
		assertEquals("two", ShaderAssembler.shader(uri, ShaderType.FRAGMENT).firstText());
		Files.delete(file);
		ShaderAssembler.invalidate();
		final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ShaderAssembler.assemble(uri));
		assertTrue(error.getMessage().contains(uri.toString()), error.getMessage());
	}

	@Test
	void aSourceWithoutIncludeIsUntouched() {
		final String source = "#version 400\nvoid main() {}";
		assertEquals(source, ShaderAssembler.expand(source, name -> null));
	}

	@Test
	void badIncludesAreRefused() {
		assertThrows(IllegalArgumentException.class, () -> ShaderAssembler.expand("#include \"x\"\n", name -> null));
		assertThrows(IllegalArgumentException.class, () -> ShaderAssembler.expand("#include x\n", name -> ""));
		assertThrows(IllegalArgumentException.class,
				() -> ShaderAssembler.expand("#include \"x\"\n", name -> "#include \"x\"\n"));
	}

	@Test
	void everyReceiverSharesTheCascadeCodeOnce() {
		for (final Path shader : RECEIVERS) {
			final String source = read(shader);
			assertFalse(source.contains("int selectCascade("), shader + " no longer copies the cascade selection");
			assertFalse(source.contains("float computeBias("), shader + " no longer copies the bias");
			final String out = assembled(shader);
			assertTrue(out.startsWith("#version 400 core\n"), shader + ": #version stays first");
			assertFalse(out.contains("#include"), shader.toString());
			assertEquals(1, out.split("int selectCascade\\(", -1).length - 1, shader.toString());
			assertEquals(1, out.split("float computeBias\\(", -1).length - 1, shader.toString());
			assertEquals(1, out.split("uniform sampler2DShadow in_shadowMap", -1).length - 1, shader.toString());
		}
	}

	@Test
	void theAssembledReceiversCompile(@TempDir final Path directory) throws IOException, InterruptedException {
		final Process probe = startOrNull("glslangValidator", "--version");
		assumeTrue(probe != null, "glslangValidator is not installed");
		probe.waitFor();
		for (final Path shader : RECEIVERS) {
			final Path file = directory.resolve(shader.getFileName().toString());
			Files.writeString(file, assembled(shader), StandardCharsets.UTF_8);
			final Process process = new ProcessBuilder("glslangValidator", "-S", "frag", file.toString())
					.redirectErrorStream(true).start();
			final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			assertEquals(0, process.waitFor(), shader + ":\n" + output);
		}
	}

	private static Process startOrNull(final String... command) {
		try {
			return new ProcessBuilder(command).redirectErrorStream(true).start();
		} catch (final IOException e) {
			return null;
		}
	}
}
