package com.scheible.testgapanalysis.parser;

import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.LAMBDA_METHOD;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.METHOD;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.STATIC_METHOD;
import static com.scheible.testgapanalysis.parser.TestClassSourceJavaParser.parseJavaTestSource;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.function.BiFunction;

import org.assertj.core.api.Condition;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class ParserUtilsTest {

	public static class TryCatchFirstCodeLine {

		public void doIt() {
			try {
				"".trim();
			} finally {
			}
		}
	}

	@Test
	public void testTryCatchFirstCodeLine() throws IOException {
		assertThat(parseJavaTestSource(TryCatchFirstCodeLine.class, METHOD)) //
				.first().satisfies(hasFirstCodeLineOffset(2));
	}

	public static class DoLoopFirstCodeLine {

		public void doIt() {
			do {
				"".trim();
			} while (false);
		}
	}

	@Test
	public void testDoLoopFirstCodeLine() throws IOException {
		assertThat(parseJavaTestSource(DoLoopFirstCodeLine.class, METHOD)) //
				.first().satisfies(hasFirstCodeLineOffset(2));
	}

	public static class LambdaMultiLineParametersFirstCodeLine {

		public void doItLambda() {
			BiFunction<String, String, Integer> ss = (first, //
					second) -> 42;
			ss.apply("a", "b");
		}
	}

	@Test
	public void testLambdaMultiLineParametersFirstCodeLine() throws IOException {
		assertThat(parseJavaTestSource(LambdaMultiLineParametersFirstCodeLine.class, LAMBDA_METHOD)) //
				.first().satisfies(hasFirstCodeLineOffset(1));
	}

	public static class CommentAtMethodStartFirstCodeLine {

		public void doIt() {
			// asdf
			// asdf
			// asdf
			"".trim();
		}
	}

	@Test
	public void testCommentAtMethodStartFirstCodeLine() throws IOException {
		assertThat(parseJavaTestSource(CommentAtMethodStartFirstCodeLine.class, METHOD)) //
				.first().satisfies(hasFirstCodeLineOffset(4));
	}

	public static class AnnotationAtMethodStartFirstCodeLine {

		private static <T> T[] doIt() {
			@SuppressWarnings("unchecked")
			final T[] array = (T[]) Array.newInstance(Object.class, 1);
			return array;
		}
	}

	@Test
	public void testAnnotationAtMethodStartFirstCodeLine() throws IOException {
		assertThat(parseJavaTestSource(AnnotationAtMethodStartFirstCodeLine.class, STATIC_METHOD)) //
				.first().satisfies(hasFirstCodeLineOffset(2));
	}

	public static class EmptyMethodFirstCodeLine {

		public void emptyMethod() {
		}
	}

	@Test
	public void testEmptyMethodFirstCodeLine() throws IOException {
		assertThat(parseJavaTestSource(EmptyMethodFirstCodeLine.class, METHOD)) //
				.first().matches(m -> !m.getFirstCodeLineOffset().isPresent());
	}

	private static Condition<ParsedMethod> hasFirstCodeLineOffset(final int firstCodeLineOffset) {
		return new Condition<>(m -> m.getFirstCodeLineOffset().orElse(-1) == firstCodeLineOffset,
				"firstCodeLineOffset = %d", firstCodeLineOffset);
	}
}
