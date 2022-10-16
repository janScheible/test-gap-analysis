package com.scheible.testgapanalysis.jacoco.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.scheible.testgapanalysis.common.JavaMethodUtils;
import com.scheible.testgapanalysis.jacoco.InstrumentedMethod;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class CoverageResolver {

	private final Map<TopLevelType, Set<InstrumentedMethod>> instrumentedMethods;

	CoverageResolver(Map<TopLevelType, Set<InstrumentedMethod>> instrumentedMethods) {
		this.instrumentedMethods = instrumentedMethods;
	}

	/**
	 * A coverage resolver is created with fitting coverage report.
	 */
	public static CoverageResolver with(Set<InstrumentedMethod> instrumentedMethods) {
		return new CoverageResolver(groupCoverageByType(instrumentedMethods));
	}

	/**
	 * Resolves the coverage info for the given methods (could be all methods passed at initialization time or
	 * only some of them). The complete set of methods passed at initialization time is used to tackle special
	 * cases like initializers.
	 */
	public CoverageResult resolve(Set<ParsedMethod> parsedMethods) {
		CoverageResult result = CoverageResult.ofEmptyMethods(parsedMethods);

		for (Entry<TopLevelType, Set<ParsedMethod>> typeMethodsEntry : groupMethodsByType(
				parsedMethods.stream().filter(m -> !m.isEmpty()).collect(Collectors.toSet())).entrySet()) {
			result.add(resolveType(typeMethodsEntry.getKey(), typeMethodsEntry.getValue()));
		}

		return result;
	}

	CoverageResult resolveType(TopLevelType type, Set<ParsedMethod> parsedMethods) {
		CoverageResult result = new CoverageResult();

		Set<InstrumentedMethod> instrumentedMethods = this.instrumentedMethods.getOrDefault(type,
				Collections.emptySet());

		result.add(resolveInitializers(filter(parsedMethods, ParsedMethod::isInitializer), instrumentedMethods));
		result.add(resolveStaticInitializers(filter(parsedMethods, ParsedMethod::isStaticInitializer),
				instrumentedMethods));
		result.add(resolveConstructor(filter(parsedMethods, ParsedMethod::isAnyConstructor), instrumentedMethods));
		result.add(resolveNonLambdaMethods(filter(parsedMethods, ParsedMethod::isAnyNonLambdaMethod),
				instrumentedMethods));
		result.add(resolveLambdaMethods(filter(parsedMethods, ParsedMethod::isLambdaMethod), instrumentedMethods));

		return result;
	}

	/**
	 * Initializers don't have dedicated coverage equivalent but are part of the constructor coverage (the Java
	 * compiler injects all initializers into the beginning of each of the constructors). That means initializers
	 * have to be resolved to a constructor (to a code covered one in best case).
	 */
	private CoverageResult resolveInitializers(Set<ParsedMethod> parsedInitializers,
			Set<InstrumentedMethod> instrumentedMethods) {
		List<InstrumentedMethod> coverageConstructors = instrumentedMethods.stream()
				.filter(InstrumentedMethod::isConstructor).collect(Collectors.toList());

		if (parsedInitializers.size() == 1 && coverageConstructors.size() == 1) {
			Map<ParsedMethod, InstrumentedMethod> resolvedInitializers = new HashMap<>();
			resolvedInitializers.put(parsedInitializers.iterator().next(), coverageConstructors.get(0));
			return new CoverageResult(resolvedInitializers, Collections.emptySet());
		} else {
			// try to find a constructor with covered code, if there is none use any non-covered constructor
			Optional<InstrumentedMethod> firstConstructorCoverage = coverageConstructors.stream()
					.sorted(Comparator.comparing(InstrumentedMethod::getCoveredInstructionCount).reversed())
					.findFirst();

			if (firstConstructorCoverage.isPresent()) {
				Map<ParsedMethod, InstrumentedMethod> resolvedInitializers = parsedInitializers.stream()
						.collect(Collectors.toMap(Function.identity(), initilaizer -> firstConstructorCoverage.get()));
				return new CoverageResult(resolvedInitializers, Collections.emptySet());
			}
		}

		return new CoverageResult();
	}

	/**
	 * All static initializers are concatenated by the Java compiler to a single special static method. Therefore
	 * all static initializers are resolved to that single static initializer special method.
	 */
	private CoverageResult resolveStaticInitializers(Set<ParsedMethod> parsedStaticInitializers,
			Set<InstrumentedMethod> instrumentedMethods) {
		List<InstrumentedMethod> coverageStaticInitializers = instrumentedMethods.stream()
				.filter(InstrumentedMethod::isStaticInitializer).collect(Collectors.toList());

		if (!coverageStaticInitializers.isEmpty()) { // make sure that a coverage report is there
			Map<ParsedMethod, InstrumentedMethod> resolvedStaticInitializers = parsedStaticInitializers.stream()
					.collect(Collectors.toMap(Function.identity(),
							staticInitilaizer -> coverageStaticInitializers.get(0)));
			return new CoverageResult(resolvedStaticInitializers, Collections.emptySet());
		}

		return new CoverageResult();
	}

	/**
	 * Constructors might loose there line number in the JaCoCo report. Could be caused by a initializer or a
	 * member variable that is initialized. In JaCoCo line of the constructor is always the first line of code of
	 * the initalizer or member variable that is initialized instead of the real constructor code line. Therefore
	 * parameter matching is performed instead. Enum constructors and inner class constructors are also special
	 * cases.
	 */
	private CoverageResult resolveConstructor(Set<ParsedMethod> parsedConstructors,
			Set<InstrumentedMethod> instrumentedMethods) {
		Map<ParsedMethod, InstrumentedMethod> resolved = new HashMap<>();
		Set<ParsedMethod> unresolved = new HashSet<>();

		for (ParsedMethod parsedConstructor : parsedConstructors) {
			List<String> normalizedConstructorParameters = new ArrayList<>(JavaMethodUtils.normalizeMethodParameters(
					parsedConstructor.getParameterTypes(), parsedConstructor.getTypeParameters()));

			// The constructors of enums have two additional parameter of type String and int. Most likely the name and
			// index of the enum const is passed via this parameter.
			if (parsedConstructor.isEnumConstructor()) {
				normalizedConstructorParameters.add(0, "int");
				normalizedConstructorParameters.add(0, "String");
			} else // Non-static nested classes (called inner classes) have and addition first constructor parameter.
			if (parsedConstructor.isInnerClassConstructor()) {
				normalizedConstructorParameters.add(0,
						parsedConstructor.getOuterDeclaringType().orElseThrow(() -> new IllegalStateException(
								"Inner class constructor " + parsedConstructor + " has no outer declaring type!")));
			}

			List<InstrumentedMethod> instrumentedConstructors = instrumentedMethods.stream()
					.filter(InstrumentedMethod::isConstructor)
					.filter(mwci -> mwci.getEnclosingSimpleName().equals(parsedConstructor.getEnclosingSimpleName()))
					.filter(mwci -> normalizedConstructorParameters.equals(JavaMethodUtils.normalizeMethodParameters(
							JavaMethodUtils.convertParameterDescriptor(mwci.getDescription()), Collections.emptyMap())))
					.collect(Collectors.toList());
			if (instrumentedConstructors.size() == 1) {
				resolved.put(parsedConstructor, instrumentedConstructors.get(0));
			} else {
				unresolved.add(parsedConstructor);
			}
		}

		return new CoverageResult(resolved, unresolved);
	}

	/**
	 * For non lambda methods the JaCoCo line numbers are reliable. In addition to the line number methods names
	 * and parameter count are used to resolve.
	 */
	private CoverageResult resolveNonLambdaMethods(Set<ParsedMethod> parsedMethods,
			Set<InstrumentedMethod> instrumentedMethods) {
		Map<ParsedMethod, InstrumentedMethod> resolved = new HashMap<>();
		Set<ParsedMethod> unresolved = new HashSet<>();

		List<InstrumentedMethod> candidates = new ArrayList<>();
		for (ParsedMethod parsedMethod : parsedMethods) {
			for (InstrumentedMethod coverageMethod : instrumentedMethods.stream()
					.filter(InstrumentedMethod::isNonLambdaMethod).collect(Collectors.toSet())) {
				if (parsedMethod.containsLine(coverageMethod.getLine())
						&& parsedMethod.getName().equals(coverageMethod.getName())
						&& parsedMethod.getParameterCount() == JavaMethodUtils
								.convertParameterDescriptor(coverageMethod.getDescription()).size()) {
					candidates.add(coverageMethod);
				}
			}

			if (candidates.size() == 1) {
				resolved.put(parsedMethod, candidates.get(0));
			} else {
				unresolved.add(parsedMethod);
			}

			candidates.clear();
		}

		return new CoverageResult(resolved, unresolved);
	}

	/**
	 * Lambda methods have no name and an un-relaiable number of parameters (only a smaller than sanity check can
	 * be made). But line numbers can be used and the lambda identifiers (e.g. lambda$1) allows column sorting.
	 */
	private CoverageResult resolveLambdaMethods(Set<ParsedMethod> parsedLambdas,
			Set<InstrumentedMethod> instrumentedMethods) {
		CoverageResult result = new CoverageResult();

		// mappings based on line number overlap
		Map<ParsedMethod, Set<InstrumentedMethod>> mapping = new HashMap<>();
		Map<InstrumentedMethod, Set<ParsedMethod>> inverseMapping = new HashMap<>();

		for (ParsedMethod parsedLambda : parsedLambdas) {
			for (InstrumentedMethod lambdaCoverage : instrumentedMethods.stream()
					.filter(InstrumentedMethod::isLambdaMethod).collect(Collectors.toSet())) {
				if (parsedLambda.containsLine(lambdaCoverage.getLine())) {
					mapping.computeIfAbsent(parsedLambda, key -> new HashSet<>()).add(lambdaCoverage);
					inverseMapping.computeIfAbsent(lambdaCoverage, key -> new HashSet<>()).add(parsedLambda);
				}
			}
		}

		// resolve each cluster individually
		for (Entry<ParsedMethod, Set<InstrumentedMethod>> entry : mapping.entrySet()) {
			if (!result.contains(entry.getKey())) {
				result.add(resolveLambdaMethodsCluster(entry, mapping, inverseMapping));
			}
		}

		return result;
	}

	/**
	 * Resolves a cluster of lambda methods. A cluster consists of parsed methods and methods with coverage info
	 * that overlapp in terms of code lines.
	 */
	private CoverageResult resolveLambdaMethodsCluster(Entry<ParsedMethod, Set<InstrumentedMethod>> entry,
			Map<ParsedMethod, Set<InstrumentedMethod>> mapping,
			Map<InstrumentedMethod, Set<ParsedMethod>> inverseMapping) {
		List<ParsedMethod> lambdas = entry.getValue().stream().flatMap(mwci -> inverseMapping.get(mwci).stream())
				.sorted(Comparator.comparing(ParsedMethod::getFirstCodeLine).thenComparing(ParsedMethod::getCodeColumn))
				.distinct().collect(Collectors.toList());
		List<InstrumentedMethod> lambdaCoverage = lambdas.stream().flatMap(pm -> mapping.get(pm).stream())
				.sorted(Comparator.comparing(InstrumentedMethod::getLine)
						.thenComparing(Comparator.comparing(mwci -> mwci.getLambdaIndex().get())))
				.distinct().collect(Collectors.toList());

		// Unfortunately we can't compare parameter counts because the compiler adds a parameter for every
		// (effective) final variable used in the scope of the lambda. And those aren't visible in the source code...
		if (lambdaCoverage.size() == lambdas.size()) {
			Map<ParsedMethod, InstrumentedMethod> currentResolved = new HashMap<>();
			boolean withoutErrors = true;

			for (int i = 0; i < lambdaCoverage.size(); i++) {
				if (lambdas.get(i).getParameterCount() <= JavaMethodUtils
						.convertParameterDescriptor(lambdaCoverage.get(i).getDescription()).size()) {
					currentResolved.put(lambdas.get(i), lambdaCoverage.get(i));
				} else {
					withoutErrors = false;
					break;
				}
			}

			if (withoutErrors) {
				return new CoverageResult(currentResolved, Collections.emptySet());
			}
		}

		return new CoverageResult(Collections.emptyMap(), new HashSet<>(lambdas));
	}

	private static Map<TopLevelType, Set<ParsedMethod>> groupMethodsByType(Set<ParsedMethod> methods) {
		return methods.stream().collect(
				Collectors.groupingBy(TopLevelType::of, Collectors.mapping(Function.identity(), Collectors.toSet())));
	}

	private static Map<TopLevelType, Set<InstrumentedMethod>> groupCoverageByType(Set<InstrumentedMethod> methods) {
		return methods.stream().collect(
				Collectors.groupingBy(TopLevelType::of, Collectors.mapping(Function.identity(), Collectors.toSet())));
	}

	private static Set<ParsedMethod> filter(Set<ParsedMethod> methods, Predicate<ParsedMethod> predicate) {
		return methods.stream().filter(predicate).collect(Collectors.toSet());
	}
}
