package com.scheible.testgapanalysis.git;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 * Class to find all changes between to repo Git trees.
 * 
 * @author sj
 */
class TreeDiffer {

	Set<FileChange> scan(Repository repository, ObjectId previous, ObjectId current, TreeFilter treeFilter)
			throws IOException {
		AbstractTreeIterator previousTree = createTreeIterator(repository, previous);

		AbstractTreeIterator currentTree = current != null
				? createTreeIterator(repository, current)
				: new FileTreeIterator(repository);

		Set<FileChange> result = new HashSet<>();

		// There was no other way to access the RawText. Getting them this way might be pretty hacky but allows
		// to reuse all the file reading code of DiffFormatter...
		DiffFormatter formatter = new DiffFormatter(new NoopOutputStream()) {
			@Override
			public void format(FileHeader head, RawText a, RawText b) throws IOException {
				if (a == null || b == null) {
					return;
				}

				String path = "/dev/null".equals(head.getOldPath())
						? head.getNewPath()
						: "/dev/null".equals(head.getNewPath()) ? head.getOldPath() : head.getNewPath();
				result.add(new FileChange(path,
						RawText.EMPTY_TEXT.equals(a) ? Optional.empty() : Optional.of(a.getString(0, a.size(), false)),
						RawText.EMPTY_TEXT.equals(b)
								? Optional.empty()
								: Optional.of(b.getString(0, b.size(), false))));
			}
		};

		formatter.setRepository(repository);
		formatter.setPathFilter(treeFilter);

		for (DiffEntry diffEntry : formatter.scan(previousTree, currentTree)) {
			formatter.format(diffEntry);
		}

		return Collections.unmodifiableSet(result);
	}

	private static AbstractTreeIterator createTreeIterator(Repository repository, ObjectId objectId)
			throws IOException {
		CanonicalTreeParser treeParser = new CanonicalTreeParser();

		try (ObjectReader curs = repository.newObjectReader()) {
			treeParser.reset(curs, new RevWalk(repository).parseTree(objectId));
		}

		return treeParser;
	}

	private static class NoopOutputStream extends OutputStream {

		@Override
		public void write(int b) throws IOException {
		}
	}
}
