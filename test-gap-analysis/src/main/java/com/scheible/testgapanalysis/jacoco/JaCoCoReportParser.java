package com.scheible.testgapanalysis.jacoco;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
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
public class JaCoCoReportParser {

	public Set<InstrumentedMethod> getInstrumentedMethods(String reportXmlContent) {
		return getInstrumentedMethods(new InputSource(new StringReader(reportXmlContent)));
	}

	public Set<InstrumentedMethod> getInstrumentedMethods(File reportXmlFile) {
		try (InputStream input = Files.newInputStream(reportXmlFile.toPath())) {
			return getInstrumentedMethods(new InputSource(input));
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private Set<InstrumentedMethod> getInstrumentedMethods(InputSource inputSource) {
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			builder.setEntityResolver((String publicId,
					String systemId) -> systemId.contains("report.dtd") ? new InputSource(new StringReader("")) : null);
			Document xmlDocument = builder.parse(inputSource);

			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "/report/package/class/method/counter[@type = 'INSTRUCTION']";

			Set<InstrumentedMethod> result = new HashSet<>(8);
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);

				int coveredInstructionCount = Integer
						.parseInt(node.getAttributes().getNamedItem("covered").getNodeValue());
				String methodName = node.getParentNode().getAttributes().getNamedItem("name").getNodeValue();
				String methodDescription = node.getParentNode().getAttributes().getNamedItem("desc").getNodeValue();
				int methodLine = Integer
						.parseInt(node.getParentNode().getAttributes().getNamedItem("line").getNodeValue());
				String className = node.getParentNode().getParentNode().getAttributes().getNamedItem("name")
						.getNodeValue();

				result.add(new InstrumentedMethod(className, methodName, methodDescription, methodLine,
						coveredInstructionCount));
			}

			return result;
		} catch (ParserConfigurationException | IOException | SAXException | XPathExpressionException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Reads multiple JaCoCo reports that could contain coverage inforamtion for the same methods (e.g. coverage
	 * report for unit and integration tests). This methods takes care of merging multiple entries for the same
	 * method.
	 */
	public Set<InstrumentedMethod> getInstrumentedMethods(Set<File> reportFiles) {
		Map<String, Set<InstrumentedMethod>> methods = new HashMap<>();

		for (File reportFile : reportFiles) {
			for (InstrumentedMethod method : getInstrumentedMethods(reportFile)) {
				String key = method.getClassName() + method.getName() + method.getDescription() + method.getLine();
				methods.computeIfAbsent(key, k -> new HashSet<>()).add(method);
			}
		}

		return methods.entrySet().stream().map(Entry::getValue).map(InstrumentedMethod::merge)
				.collect(Collectors.toSet());
	}

	public static Set<File> findJaCoCoReportFiles(File baseDir, File... excludeDirs) {
		Set<Path> excludeDirsAsPaths = Stream.of(excludeDirs).map(File::toPath).collect(Collectors.toSet());
		Predicate<Path> isNotChildOfExcludeDir = getIsNotChildOfSubDirsPredicate(excludeDirsAsPaths);
		PathMatcher jaCoCoReportMatcher = FileSystems.getDefault().getPathMatcher("glob:**/jacoco.xml");

		try {
			return Files.walk(baseDir.toPath()).filter(jaCoCoReportMatcher::matches).filter(isNotChildOfExcludeDir)
					.map(Path::toFile).collect(Collectors.toSet());
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	static Predicate<Path> getIsNotChildOfSubDirsPredicate(Set<Path> subDirs) {
		return jaCoCoReport -> subDirs.stream().filter(excludeDir -> jaCoCoReport.startsWith(excludeDir)).count() == 0;
	}
}
