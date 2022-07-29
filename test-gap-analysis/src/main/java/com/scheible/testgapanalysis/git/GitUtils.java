package com.scheible.testgapanalysis.git;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

/**
 *
 * @author sj
 */
public abstract class GitUtils {

	private GitUtils() {
	}

	static Repository open(File currentDir) throws IOException {
		return new FileRepositoryBuilder().findGitDir(currentDir.getAbsoluteFile()).setMustExist(true).build();
	}

	static Map<String, String> getCommitedContents(File currentDir, String objectId, Set<String> files) {
		Map<String, String> fileLastCommitContentMapping = new HashMap<>();

		try (Repository repository = GitUtils.open(currentDir)) {
			try (Git git = new Git(repository)) {

				try (RevWalk walk = new RevWalk(repository)) {
					RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));

					for (String file : files) {
						fileLastCommitContentMapping.put(file, getContent(repository, git, commit, file));
					}
				}

			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		return Collections.unmodifiableMap(fileLastCommitContentMapping);
	}

	private static String getContent(Repository repository, Git git, RevCommit commit, String path) throws IOException {
		try (TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), path, commit.getTree())) {
			ObjectId blobId = treeWalk.getObjectId(0);
			try (ObjectReader objectReader = repository.newObjectReader()) {
				ObjectLoader objectLoader = objectReader.open(blobId);
				byte[] bytes = objectLoader.getBytes();
				return new String(bytes, StandardCharsets.UTF_8);
			}
		}
	}

	static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
		try (RevWalk walk = new RevWalk(repository)) {
			RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
			RevTree tree = walk.parseTree(commit.getTree().getId());

			CanonicalTreeParser treeParser = new CanonicalTreeParser();
			try (ObjectReader reader = repository.newObjectReader()) {
				treeParser.reset(reader, tree.getId());
			}

			walk.dispose();

			return treeParser;
		}
	}

	static List<String> getCommitHashes(File currentDir, int count) {
		try (Repository repository = GitUtils.open(currentDir)) {
			try (Git git = new Git(repository)) {
				Iterator<RevCommit> logs = git.log().call().iterator();
				List<String> result = new ArrayList<>(count);

				for (int i = 0; i < count; i++) {
					if (!logs.hasNext()) {
						break;
					} else {
						RevCommit commit = logs.next();
						result.add(commit.getId().getName());
					}
				}

				return result;
			} catch (GitAPIException ex2) {
				throw new IllegalStateException(ex2);
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}
