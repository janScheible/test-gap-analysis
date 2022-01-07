package com.scheible.testgapanalysis.jacoco.resolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class CoverageResult {

	private final Map<ParsedMethod, MethodWithCoverageInfo> resolved = new HashMap<>();
	private final Set<ParsedMethod> unresolved = new HashSet<>();

	CoverageResult() {
	}

	public CoverageResult(final Map<ParsedMethod, MethodWithCoverageInfo> resolved,
			final Set<ParsedMethod> unresolved) {
		this.resolved.putAll(resolved);
		this.unresolved.addAll(unresolved);
	}

	void add(final CoverageResult result) {
		resolved.putAll(result.resolved);
		unresolved.addAll(result.unresolved);
	}

	public Map<ParsedMethod, MethodWithCoverageInfo> getResolved() {
		return Collections.unmodifiableMap(resolved);
	}

	public Set<ParsedMethod> getUnresolved() {
		return Collections.unmodifiableSet(unresolved);
	}
}
