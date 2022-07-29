package com.scheible.testgapanalysis.jacoco;

import static com.scheible.testgapanalysis.jacoco.JaCoCoReportParser.getIsNotChildOfSubDirsPredicate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.newLinkedHashSet;

import java.nio.file.Paths;

import org.junit.Test;

/**
 *
 * @author sj
 */
public class JaCoCoReportParserTest {

	private static final String REPORT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" //
			+ "<!DOCTYPE report PUBLIC \"-//JACOCO//DTD Report 1.1//EN\" \"report.dtd\">\n" //
			+ "<report name=\"com.scheible::test-gap-analysis library\">\n" //
			+ "	<package name=\"com/scheible/testgapanalysis/git\">\n" //
			+ "		<class name=\"com/scheible/testgapanalysis/git/GitHelper\" sourcefilename=\"GitHelper.java\">\n" //
			+ "			<method name=\"&lt;init&gt;\" desc=\"()V\" line=\"37\">\n" //
			+ "				<counter type=\"INSTRUCTION\" missed=\"3\" covered=\"0\"/>\n" //
			+ "				<counter type=\"LINE\" missed=\"1\" covered=\"0\"/>\n" //
			+ "				<counter type=\"COMPLEXITY\" missed=\"1\" covered=\"0\"/>\n" //
			+ "				<counter type=\"METHOD\" missed=\"1\" covered=\"0\"/>\n" //
			+ "			</method>\n" //
			+ "			<method name=\"open\" desc=\"(Ljava/io/File;)Lorg/eclipse/jgit/lib/Repository;\" line=\"42\">\n" //
			+ "				<counter type=\"INSTRUCTION\" missed=\"0\" covered=\"12\"/>\n" //
			+ "				<counter type=\"LINE\" missed=\"0\" covered=\"1\"/>\n" //
			+ "				<counter type=\"COMPLEXITY\" missed=\"0\" covered=\"1\"/>\n" //
			+ "				<counter type=\"METHOD\" missed=\"0\" covered=\"1\"/>\n" //
			+ "			</method>\n" //
			+ "		</class>\n" //
			+ "	</package>\n" //
			+ "</report>";

	@Test
	public void testMethodCoverage() {
		JaCoCoReportParser jaCoCoReportParser = new JaCoCoReportParser();
		assertThat(jaCoCoReportParser.getMethodCoverage(REPORT)).containsOnly( //
				new MethodWithCoverageInfo("com/scheible/testgapanalysis/git/GitHelper", "<init>", "()V", 37, 0),
				new MethodWithCoverageInfo("com/scheible/testgapanalysis/git/GitHelper", "open",
						"(Ljava/io/File;)Lorg/eclipse/jgit/lib/Repository;", 42, 12));
	}

	@Test
	public void testIsNotChildOfSubDirsPredicate() {
		assertThat(getIsNotChildOfSubDirsPredicate(newLinkedHashSet(Paths.get("test", "ignoreA")))
				.test(Paths.get("test", "ignoreA", "file.txt"))).isFalse();

		assertThat(getIsNotChildOfSubDirsPredicate(newLinkedHashSet(Paths.get("test", "ignoreA")))
				.test(Paths.get("test", "some", "file.txt"))).isTrue();
	}
}
