package com.scheible.testgapanalysis.jacoco;

import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author sj
 */
public class JaCoCoHelper {

	public static Set<MethodWithCoverageInfo> getMethodCoverage(final String reportXmlContent) {
		return getMethodCoverage(new InputSource(new StringReader(reportXmlContent)));
	}

	public static Set<MethodWithCoverageInfo> getMethodCoverage(final File reportXmlFile) {
		try (InputStream input = Files.newInputStream(reportXmlFile.toPath())) {
			return getMethodCoverage(new InputSource(input));
		} catch (final IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static Set<MethodWithCoverageInfo> getMethodCoverage(final InputSource inputSource) {
		try {
			final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setFeature(FEATURE_SECURE_PROCESSING, true);
			final DocumentBuilder builder = builderFactory.newDocumentBuilder();
			builder.setEntityResolver((String publicId,
					String systemId) -> systemId.contains("report.dtd") ? new InputSource(new StringReader("")) : null);
			final Document xmlDocument = builder.parse(inputSource);

			final XPath xPath = XPathFactory.newInstance().newXPath();
			final String expression = "/report/package/class/method/counter[@type = 'INSTRUCTION']";

			final Set<MethodWithCoverageInfo> result = new HashSet<>(8);
			final NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument,
					XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				final Node node = nodeList.item(i);

				final int coveredInstructionCount = Integer
						.parseInt(node.getAttributes().getNamedItem("covered").getNodeValue());
				final String methodName = node.getParentNode().getAttributes().getNamedItem("name").getNodeValue();
				final String className = node.getParentNode().getParentNode().getAttributes().getNamedItem("name")
						.getNodeValue();

				result.add(new MethodWithCoverageInfo(className.replaceAll(Pattern.quote("/"), "."), methodName,
						coveredInstructionCount));
			}

			return result;
		} catch (final ParserConfigurationException | IOException | SAXException | XPathExpressionException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public static Set<File> findJaCoCoFiles(final File workingDir) {
		try {
			final Path searchRoot = workingDir.toPath().resolve("./target/site");
			if (Files.exists(searchRoot)) {
				return Files.walk(searchRoot).filter(p -> "jacoco.xml".equals(p.getFileName().toString()))
						.map(Path::toFile).collect(Collectors.toSet());
			} else {
				return Collections.emptySet();
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}
