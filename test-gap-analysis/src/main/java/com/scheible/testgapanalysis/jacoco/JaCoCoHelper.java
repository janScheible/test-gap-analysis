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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
				final String methodDescription = node.getParentNode().getAttributes().getNamedItem("desc")
						.getNodeValue();
				final int methodLine = Integer
						.parseInt(node.getParentNode().getAttributes().getNamedItem("line").getNodeValue());
				final String className = node.getParentNode().getParentNode().getAttributes().getNamedItem("name")
						.getNodeValue();

				result.add(new MethodWithCoverageInfo(className, methodName, methodDescription, methodLine,
						coveredInstructionCount));
			}

			return result;
		} catch (final ParserConfigurationException | IOException | SAXException | XPathExpressionException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Reads multiple JaCoCo reports that could contain coverage inforamtion for the same methods (e.g. coverage
	 * report for unit and integration tests). This methods takes care of merging multiple entries for the same
	 * method.
	 */
	public static Set<MethodWithCoverageInfo> getMethodCoverage(final Set<File> reportFiles) {
		final Map<String, Set<MethodWithCoverageInfo>> methods = new HashMap<>();

		for (final File reportFile : reportFiles) {
			for (final MethodWithCoverageInfo method : JaCoCoHelper.getMethodCoverage(reportFile)) {
				final String key = method.getClassName() + method.getName() + method.getDescription()
						+ method.getLine();
				methods.computeIfAbsent(key, k -> new HashSet<>()).add(method);
			}
		}

		return methods.entrySet().stream().map(Entry::getValue).map(MethodWithCoverageInfo::merge)
				.collect(Collectors.toSet());
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
