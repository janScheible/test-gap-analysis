package com.scheible.testgapanalysis.git;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author sj
 */
public class TreeDifferTest {

	private static final TreeFilter JAVA_SOURCE_FILTER = AndTreeFilter.create(PathFilter.create("main/java"),
			PathSuffixFilter.create(".java"));

	@TempDir
	public static File temporaryFolder;

	private static Git git;
	private final TreeDiffer treeDiffer = new TreeDiffer();

	@BeforeAll
	public static void beforeClass() throws GitAPIException {
		git = Git.init().setDirectory(temporaryFolder).call();
	}

	@BeforeEach
	public void beforeEach(TestInfo testInfo) throws IOException, GitAPIException {
		if (git.getRepository().resolve("HEAD") == null) {
			createFile("unrelated", "initial commit");
		} else {
			git.checkout().setName("master").call();
		}

		String testCaseBranch = testInfo.getTestMethod().get().getName();
		git.branchCreate().setName(testCaseBranch).call();
		git.checkout().setName(testCaseBranch).call();
	}

	@AfterAll
	public static void afterClass() {
		if (git != null) {
			git.close();
		}
	}

	@Test
	public void testFileCreation() throws GitAPIException, IOException {
		createFile("test/readme.txt", "readme");

		Set<FileChange> diff = treeDiffer.scan(git.getRepository(), git.getRepository().resolve("HEAD^"),
				git.getRepository().resolve("HEAD"), TreeFilter.ALL);
		assertThat(find(diff, "test/readme.txt").isCreation()).isTrue();
	}

	@Test
	public void testFileModification() throws GitAPIException, IOException {
		createFile("test/readme.txt", "readme");
		modifyFile("test/readme.txt", "changed content");

		Set<FileChange> diff = treeDiffer.scan(git.getRepository(), git.getRepository().resolve("HEAD^"),
				git.getRepository().resolve("HEAD"), TreeFilter.ALL);
		assertThat(find(diff, "test/readme.txt").isChange()).isTrue();
	}

	@Test
	public void testFileMove() throws GitAPIException, IOException {
		createFile("test/readme.txt", "readme");
		moveFile("test/readme.txt", "test/src/readme.txt");

		Set<FileChange> diff = treeDiffer.scan(git.getRepository(), git.getRepository().resolve("HEAD^"),
				git.getRepository().resolve("HEAD"), TreeFilter.ALL);
		assertThat(find(diff, "test/readme.txt").isDeletion()).isTrue();
		assertThat(find(diff, "test/src/readme.txt").isCreation()).isTrue();

	}

	@Test
	public void testFileDeletion() throws GitAPIException, IOException {
		createFile("test/readme.txt", "readme");
		deleteFile("test/readme.txt");

		Set<FileChange> diff = treeDiffer.scan(git.getRepository(), git.getRepository().resolve("HEAD^"),
				git.getRepository().resolve("HEAD"), TreeFilter.ALL);
		assertThat(find(diff, "test/readme.txt").isDeletion()).isTrue();
	}

	@Test
	public void testFilterOutsideOfPath() throws GitAPIException, IOException {
		createFile("test/readme.txt", "readme");

		Set<FileChange> diff = treeDiffer.scan(git.getRepository(), git.getRepository().resolve("HEAD^"),
				git.getRepository().resolve("HEAD"), JAVA_SOURCE_FILTER);
		assertThat(diff).isEmpty();
	}

	@Test
	public void testFilterInsideOfPathWithJavaFile() throws GitAPIException, IOException {
		createFile("main/java/Test.java", "some Java fake code...");

		Set<FileChange> diff = treeDiffer.scan(git.getRepository(), git.getRepository().resolve("HEAD^"),
				git.getRepository().resolve("HEAD"), JAVA_SOURCE_FILTER);
		assertThat(find(diff, "main/java/Test.java").isCreation()).isTrue();

		createFile("main/java/org/Test.java", "some Java fake code...");
		diff = treeDiffer.scan(git.getRepository(), git.getRepository().resolve("HEAD^"),
				git.getRepository().resolve("HEAD"), JAVA_SOURCE_FILTER);
		assertThat(find(diff, "main/java/org/Test.java").isCreation()).isTrue();
	}

	@Test
	public void testFilterInsideOfPathWithNonJavaFile() throws GitAPIException, IOException {
		createFile("main/java/pom.xml", "<project...>");
		Set<FileChange> diff = treeDiffer.scan(git.getRepository(), git.getRepository().resolve("HEAD^"),
				git.getRepository().resolve("HEAD"), JAVA_SOURCE_FILTER);
		assertThat(diff).isEmpty();
	}

	private static void createFile(String relativePath, String contents) throws IOException, GitAPIException {
		Path file = temporaryFolder.toPath().resolve(relativePath);
		File parentDir = file.toFile().getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}

		Files.write(file, contents.getBytes());

		git.add().addFilepattern(relativePath).call();
		git.commit().setMessage("comitted '" + contents + "' to '" + relativePath + "'").call();
	}

	private void moveFile(String fromRelativePath, String toRelativePath) throws IOException, GitAPIException {
		Path fromFile = temporaryFolder.toPath().resolve(fromRelativePath);
		Path toFile = temporaryFolder.toPath().resolve(toRelativePath);
		File toFileparentDir = toFile.toFile().getParentFile();
		if (!toFileparentDir.exists()) {
			toFileparentDir.mkdirs();
		}

		Files.copy(fromFile, toFile);
		fromFile.toFile().delete();

		git.rm().addFilepattern(fromRelativePath).call();
		git.add().addFilepattern(toRelativePath).call();
		git.commit().setMessage("moved '" + fromRelativePath + "' to '" + toRelativePath + "'").call();
	}

	private void deleteFile(String relativePath) throws GitAPIException {
		Path file = temporaryFolder.toPath().resolve(relativePath);
		if (!file.toFile().exists()) {
			throw new IllegalStateException("'" + relativePath + "' does not exist!");
		}

		file.toFile().delete();

		git.rm().addFilepattern(relativePath).call();
		git.commit().setMessage("delteted '" + relativePath + "'").call();
	}

	private void modifyFile(String relativePath, String newContents) throws IOException, GitAPIException {
		createFile(relativePath, newContents);
	}

	private static FileChange find(Set<FileChange> fileChanges, String relativePath) {
		return fileChanges.stream().filter(fileChange -> fileChange.getRelativePath().equals(relativePath)).findFirst()
				.get();
	}
}
