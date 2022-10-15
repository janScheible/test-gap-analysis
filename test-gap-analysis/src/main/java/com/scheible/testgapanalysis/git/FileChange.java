package com.scheible.testgapanalysis.git;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author sj
 */
public class FileChange {

	private final String relativePath;

	private final Optional<String> previousContent;
	private final Optional<String> currentContent;

	public FileChange(String relativePath, Optional<String> previousContent, Optional<String> currentContent) {
		this.relativePath = relativePath;

		if (!previousContent.isPresent() && !currentContent.isPresent()) {
			throw new IllegalArgumentException(
					"It doesn't make sense to have a file change with no previous and no current content!");
		}
		this.previousContent = previousContent;
		this.currentContent = currentContent;
	}

	public String getRelativePath() {
		return this.relativePath;
	}

	public Optional<String> getPreviousContent() {
		return this.previousContent;
	}

	public Optional<String> getCurrentContent() {
		return this.currentContent;
	}

	public boolean isCreation() {
		return !this.previousContent.isPresent() && this.currentContent.isPresent();
	}

	public boolean isChange() {
		return this.previousContent.isPresent() && this.currentContent.isPresent();
	}

	public boolean isDeletion() {
		return this.previousContent.isPresent() && !this.currentContent.isPresent();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof FileChange) {
			FileChange other = (FileChange) obj;
			return Objects.equals(this.relativePath, other.relativePath)
					&& Objects.equals(this.previousContent, other.previousContent)
					&& Objects.equals(this.currentContent, other.currentContent);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.relativePath, this.previousContent, this.currentContent);
	}
}
