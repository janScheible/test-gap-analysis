package com.scheible.testgapanalysis;

import com.scheible.pocketsaw.api.ExternalFunctionality;

/**
 *
 * @author sj
 */
public class ExternalFunctionalities {

	@ExternalFunctionality(packageMatchPattern = {"org.eclipse.jgit.**"})
	public static class JGit {

	}

	@ExternalFunctionality(packageMatchPattern = {"com.github.javaparser.**"})
	public static class JavaParser {

	}

	@ExternalFunctionality(packageMatchPattern = {"org.w3c.dom.**"})
	public static class DomParser {

	}

	@ExternalFunctionality(packageMatchPattern = {"org.xml.sax.**"})
	public static class SaxParser {

	}

	@ExternalFunctionality(packageMatchPattern = {"org.slf4j.**"})
	public static class Slf4j {

	}
}
