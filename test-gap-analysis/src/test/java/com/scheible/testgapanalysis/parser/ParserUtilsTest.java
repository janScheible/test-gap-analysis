package com.scheible.testgapanalysis.parser;

import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.METHOD;
import static com.scheible.testgapanalysis.parser.TestClassSourceJavaParser.parseJavaTestSource;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

/**
 *
 * @author sj
 */
public class ParserUtilsTest {

	public static class EmptyMethodWithCommentOnlyBody {

		public void doIt() {
			// asdf
			// asdf
			// asdf
		}
	}

	@Test
	public void testEmptyMethodWithCommentOnlyBody() throws IOException {
		assertThat(parseJavaTestSource(EmptyMethodWithCommentOnlyBody.class, METHOD)) //
				.first().matches(ParsedMethod::isEmpty, "is empty");
	}
}
