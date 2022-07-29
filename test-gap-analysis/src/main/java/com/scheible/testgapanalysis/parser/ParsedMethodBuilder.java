package com.scheible.testgapanalysis.parser;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.scheible.testgapanalysis.parser.ParsedMethod.MethodType;

/**
 *
 * @author sj
 */
public abstract class ParsedMethodBuilder {

	private ParsedMethodBuilder() {
	}

	public interface MethodTypeStep {

		TopLevelTypeFqnStep setMethodType(MethodType methodType);
	}

	public interface TopLevelTypeFqnStep {

		ScopeStep setTopLevelTypeFqn(String topLevelTypeFqn);
	}

	public interface ScopeStep {

		NameStep setScope(List<String> scope);
	}

	public interface NameStep {

		RelevantCodeStep setName(String name);
	}

	public interface RelevantCodeStep {

		CodeLinesStep setRelevantCode(String relevantCode);
	}

	public interface CodeLinesStep {

		CodeColumnStep setCodeLines(List<Integer> codeLines);
	}

	public interface CodeColumnStep {

		EmptyStep setCodeColumn(int codeColumn);
	}

	public interface EmptyStep {

		ArgumentCountOrArgumentTypesStep setEmpty(boolean empty);
	}

	public interface ArgumentCountOrArgumentTypesStep {

		BuildStep setArgumentCount(int argumentCount);

		TypeParametersStep setArgumentTypes(List<String> argumentTypes);
	}

	public interface TypeParametersStep {

		OuterDeclaringTypeStep setTypeParameters(Map<String, String> typeParameters);
	}

	public interface OuterDeclaringTypeStep {

		BuildStep setOuterDeclaringType(Optional<String> outerDeclaringType);
	}

	public interface BuildStep {

		ParsedMethod build();
	}

	static class BuilderImpl
			implements
				MethodTypeStep,
				TopLevelTypeFqnStep,
				ScopeStep,
				NameStep,
				RelevantCodeStep,
				CodeLinesStep,
				CodeColumnStep,
				EmptyStep,
				ArgumentCountOrArgumentTypesStep,
				TypeParametersStep,
				OuterDeclaringTypeStep,
				BuildStep {

		MethodType methodType;
		String topLevelTypeFqn;
		List<String> scope;
		String name;
		String relevantCode;
		List<Integer> codeLines;
		int codeColumn;
		boolean empty;

		int argumentCount;

		List<String> argumentTypes;
		Map<String, String> typeParameters;
		Optional<String> outerDeclaringType = Optional.empty();

		BuilderImpl() {
		}

		@Override
		public TopLevelTypeFqnStep setMethodType(MethodType methodType) {
			this.methodType = methodType;
			return this;
		}

		@Override
		public ScopeStep setTopLevelTypeFqn(String topLevelTypeFqn) {
			this.topLevelTypeFqn = topLevelTypeFqn;
			return this;
		}

		@Override
		public NameStep setScope(List<String> scope) {
			this.scope = scope;
			return this;
		}

		@Override
		public RelevantCodeStep setName(String name) {
			this.name = name;
			return this;
		}

		@Override
		public CodeLinesStep setRelevantCode(String relevantCode) {
			this.relevantCode = relevantCode;
			return this;
		}

		@Override
		public CodeColumnStep setCodeLines(List<Integer> codeLines) {
			this.codeLines = codeLines;
			return this;
		}

		@Override
		public EmptyStep setCodeColumn(int codeColumn) {
			this.codeColumn = codeColumn;
			return this;
		}

		@Override
		public ArgumentCountOrArgumentTypesStep setEmpty(boolean empty) {
			this.empty = empty;
			return this;
		}

		@Override
		public BuildStep setArgumentCount(int argumentCount) {
			this.argumentCount = argumentCount;
			return this;
		}

		@Override
		public TypeParametersStep setArgumentTypes(List<String> argumentTypes) {
			this.argumentTypes = argumentTypes;
			return this;
		}

		@Override
		public OuterDeclaringTypeStep setTypeParameters(Map<String, String> typeParameters) {
			this.typeParameters = typeParameters;
			return this;
		}

		@Override
		public BuildStep setOuterDeclaringType(Optional<String> outerDeclaringType) {
			this.outerDeclaringType = outerDeclaringType;
			return this;
		}

		@Override
		public ParsedMethod build() {
			return new ParsedMethod(this);
		}
	}
}
