package com.scheible.testgapanalysis.git;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheible.testgapanalysis.git.GitRepoChangeScanner.PreviousType;

/**
 *
 * @author sj
 */
class PreviousStateFinder {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	GitRepoState getPreviousState(Repository repository, PreviousType type, String regEx) {
		try {
			Set<TimestampedState> timestampedStates = getTimestampedStates(repository, type);

			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Found the following timestamped states [{}] with [{}] matching to the RegEx.",
						timestampedStates.stream().sorted(Comparator.comparing(TimestampedState::getTimestamp))
								.map(state -> state.getName() + " (" + state.getTimestamp() + ")")
								.collect(Collectors.joining(", ")),
						timestampedStates.stream().filter(timestampedState -> matches(timestampedState.name, regEx))
								.map(TimestampedState::getName).collect(Collectors.joining(", ")));
			}

			return new GitRepoState(findPrevious(timestampedStates, regEx).objectId.name());
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		} catch (GitAPIException ex) {
			throw new IllegalStateException(ex);
		}
	}

	static TimestampedState findPrevious(Set<TimestampedState> timestampedStates, String regEx) {
		List<TimestampedState> sorted = timestampedStates.stream()
				.filter(timestampedState -> matches(timestampedState.name, regEx))
				.sorted(Comparator.comparing(TimestampedState::getTimestamp)).collect(Collectors.toList());

		Optional<TimestampedState> headState = sorted.stream().filter(TimestampedState::isHead).findFirst();
		if (headState.isPresent()) {
			return sorted.get(Math.min(sorted.indexOf(headState.get()) + 1, sorted.size() - 1));
		} else {
			return sorted.get(sorted.size() - 1);
		}
	}

	static boolean matches(String name, String regEx) {
		return (name.contains("/") ? name.substring(name.lastIndexOf('/') + 1) : name).matches(regEx);
	}

	private static Set<TimestampedState> getTimestampedStates(Repository repository,
			GitRepoChangeScanner.PreviousType type) throws IOException, GitAPIException {
		Set<TimestampedState> result = new HashSet<>();

		RevWalk walk = new RevWalk(repository);

		RevCommit headCommit = walk.parseCommit(repository.resolve(Constants.HEAD));

		try (Git git = new Git(repository)) {
			List<Ref> refs = type == GitRepoChangeScanner.PreviousType.BRANCH
					? git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call()
					: git.tagList().call();
			for (Ref ref : refs) {
				// Peeled objectId is only relevant for annotated tags (for them objectId whould be the tag itself).
				ObjectId id = ref.getPeeledObjectId() != null ? ref.getPeeledObjectId() : ref.getObjectId();

				if (id != null) {
					RevCommit commit = walk.parseCommit(id);
					OffsetDateTime timestamp = commit.getCommitterIdent().getWhen().toInstant().atOffset(
							ZoneOffset.ofTotalSeconds(commit.getCommitterIdent().getTimeZone().getRawOffset() / 1000));

					result.add(new TimestampedState(ref.getName(), timestamp, ref.getObjectId(),
							commit.equals(headCommit)));
				}
			}
		}

		return result;
	}

	static class TimestampedState {

		private final String name;
		private final OffsetDateTime timestamp;
		private final ObjectId objectId;
		private final boolean head;

		TimestampedState(String name, OffsetDateTime timestamp, ObjectId objectId, boolean head) {
			this.name = name;
			this.timestamp = timestamp;
			this.objectId = objectId;
			this.head = head;
		}

		String getName() {
			return this.name;
		}

		OffsetDateTime getTimestamp() {
			return this.timestamp;
		}

		boolean isHead() {
			return this.head;
		}
	}
}
