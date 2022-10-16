package com.scheible.testgapanalysis.common;

import java.io.File;
import java.io.IOException;
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
