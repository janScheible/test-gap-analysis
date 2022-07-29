package com.scheible.testgapanalysis.common;

import static java.util.Collections.emptyMap;

import static com.scheible.testgapanalysis.common.JavaMethodUtils.getNextClassPart;
import static com.scheible.testgapanalysis.common.JavaMethodUtils.getNextPrimitivePart;
import static com.scheible.testgapanalysis.common.JavaMethodUtils.normalizeMethodArguments;
import static com.scheible.testgapanalysis.common.JavaMethodUtils.parseDescriptorArguments;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 *
 * @author sj
 */
public class JavaMethodUtilTest {

	@Test
	public void testPrimitiveType() {
		assertThat(parseDescriptorArguments("(C)V")).containsExactly("char");
	}

	@Test
	public void testMiltiplePrimitiveTypes() {
		assertThat(parseDescriptorArguments("(CZ)V")).containsExactly("char", "boolean");
		assertThat(parseDescriptorArguments("([BI)J")).containsExactly("byte[]", "int");
	}

	@Test
	public void testClassType() {
		assertThat(parseDescriptorArguments("(Lmy/package/MyClass;)V")).containsExactly("MyClass");
	}

	@Test
	public void testArrayType() {
		assertThat(parseDescriptorArguments("([[C)V")).containsExactly("char[][]");
		assertThat(parseDescriptorArguments("([[Lmy/package/MyClass;)V")).containsExactly("MyClass[][]");
	}

	@Test
	public void testNestedClassType() {
		assertThat(parseDescriptorArguments("(Lmy/package/MyClass$Foo;)V")).containsExactly("MyClass.Foo");
	}

	@Test
	public void testMixedTypes() {
		assertThat(parseDescriptorArguments("(IILjava/util/List;)V")).containsExactly("int", "int", "List");

		assertThat(parseDescriptorArguments(
				"(Lcom/scheible/testgapanalysis/parser/ParsedMethod$MethodType;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IILjava/util/List;)V"))
						.containsExactly("ParsedMethod.MethodType", "String", "String", "String", "String", "int",
								"int", "List");

		assertThat(parseDescriptorArguments(
				"(Lcom/scheible/testgapanalysis/parser/ParsedMethod$MethodType;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)V"))
						.containsExactly("ParsedMethod.MethodType", "String", "String", "String", "String", "int",
								"int");

		assertThat(parseDescriptorArguments("(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)V"))
				.containsExactly("String", "String", "String", "int", "int");

		assertThat(parseDescriptorArguments(
				"([Lorg/apache/commons/collections4/Predicate;[Lorg/apache/commons/collections4/Closure;Lorg/apache/commons/collections4/Closure;)V"))
						.containsExactly("Predicate[]", "Closure[]", "Closure");
	}

	@Test
	public void testNextPrimitivePart() {
		assertThat(getNextPrimitivePart("I[JLcom;", 0)).isEqualTo("I");
		assertThat(getNextPrimitivePart("I[JLcom;", 1)).isEqualTo("[J");
		assertThat(getNextPrimitivePart("I[JLcom;", 3)).isEqualTo(null);

		assertThat(getNextPrimitivePart("[JILcom;", 0)).isEqualTo("[J");
		assertThat(getNextPrimitivePart("[JILcom;", 2)).isEqualTo("I");
		assertThat(getNextPrimitivePart("[JILcom;", 3)).isEqualTo(null);

		assertThat(getNextPrimitivePart("[Lcom;JI", 0)).isEqualTo(null);
		assertThat(getNextPrimitivePart("[Lcom;JI", 6)).isEqualTo("J");
		assertThat(getNextPrimitivePart("[Lcom;JI", 7)).isEqualTo("I");
	}

	@Test
	public void testNextClassPart() {
		assertThat(getNextClassPart("I[JLcom;", 0)).isEqualTo(null);
		assertThat(getNextClassPart("I[JLcom;", 1)).isEqualTo(null);
		assertThat(getNextClassPart("I[JLcom;", 3)).isEqualTo("Lcom");

		assertThat(getNextClassPart("[JILcom;", 0)).isEqualTo(null);
		assertThat(getNextClassPart("[JILcom;", 2)).isEqualTo(null);
		assertThat(getNextClassPart("[JILcom;", 3)).isEqualTo("Lcom");

		assertThat(getNextClassPart("[Lcom;JI", 0)).isEqualTo("[Lcom");
		assertThat(getNextClassPart("[Lcom;JI", 6)).isEqualTo(null);
		assertThat(getNextClassPart("[Lcom;JI", 7)).isEqualTo(null);
	}

	@Test
	public void testNormalizeMethodArgumentsNestedClass() {
		assertThat(normalizeMethodArguments(Arrays.asList("Map.Entry"), emptyMap())).containsExactly("Entry");
	}

	@Test
	public void testNormalizeMethodArgumentsGenerics() {
		assertThat(normalizeMethodArguments(Arrays.asList("Map<String, String>"), emptyMap())).containsExactly("Map");
	}

	@Test
	public void testNormalizeMethodArgumentsNestedGenerics() {
		assertThat(normalizeMethodArguments(Arrays.asList("List<Map<String, String>>"), emptyMap()))
				.containsExactly("List");
	}

	@Test
	public void testNormalizeMethodArgumentsNestedClassAndNestedGenerics() {
		assertThat(normalizeMethodArguments(Arrays.asList("Map<Map.Entry, Set<String>>"), emptyMap()))
				.containsExactly("Map");
	}

	@Test
	public void testNormalizeMethodArgumentsWithGenerics() {
		Map<String, String> typeParameters = new HashMap<>();
		typeParameters.put("T", "Object");
		typeParameters.put("K", "Serializable");

		assertThat(normalizeMethodArguments(Arrays.asList("Map.Entry", "Object", "T", "K[]"), typeParameters))
				.containsExactly("Entry", "Object", "Object", "Serializable[]");
	}

	@Test
	public void testNormalizeMethodArgumentsWithGenericsArray() {
		assertThat(normalizeMethodArguments(Arrays.asList("Map<Map.Entry, Set<String>>[]"), emptyMap()))
				.containsExactly("Map[]");
	}
}
