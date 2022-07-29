package com.scheible.testgapanalysis.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author sj
 */
public abstract class FilesUtils {

	private static final String UTF_8_CHARSET = StandardCharsets.UTF_8.name();

	private static final String BACKSLASH_PATTERN = "\\\\";

	private FilesUtils() {
	}

	public static String readUtf8(Class<?> anchor, String name) {
		return readUtf8(anchor.getResourceAsStream(name));
	}

	public static String readUtf8(InputStream inputStream) {
		try (InputStream tryCatchInputStream = Objects.requireNonNull(inputStream)) {

			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = tryCatchInputStream.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}

			return result.toString(UTF_8_CHARSET);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	public static String readUtf8(File file) {
		try {
			return new String(Files.readAllBytes(Objects.requireNonNull(file).toPath()), UTF_8_CHARSET);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	public static File toCanonical(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException ex) {
			return file;
		}
	}

	public static File getWorkingDir() {
		return toCanonical(new File("."));
	}

	public static Set<String> toRelative(File rootDir, Set<File> files) {
		Path rootDirAsPath = rootDir.toPath().toAbsolutePath();
		return files.stream().map(File::toPath).map(Path::toAbsolutePath).map(p -> p
				.subpath(rootDirAsPath.getNameCount(), p.getNameCount()).toString().replaceAll(BACKSLASH_PATTERN, "/"))
				.collect(Collectors.toSet());
	}
}
