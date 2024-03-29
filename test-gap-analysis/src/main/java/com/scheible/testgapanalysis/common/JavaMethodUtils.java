package com.scheible.testgapanalysis.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author sj
 */
public class JavaMethodUtils {

	private static final Map<String, String> PRIMITIVE_TYPE_MAPPING = new HashMap<>();

	static {
		PRIMITIVE_TYPE_MAPPING.put("Z", "boolean");
		PRIMITIVE_TYPE_MAPPING.put("B", "byte");
		PRIMITIVE_TYPE_MAPPING.put("C", "char");
		PRIMITIVE_TYPE_MAPPING.put("S", "short");
		PRIMITIVE_TYPE_MAPPING.put("I", "int");
		PRIMITIVE_TYPE_MAPPING.put("J", "long");
		PRIMITIVE_TYPE_MAPPING.put("F", "float");
		PRIMITIVE_TYPE_MAPPING.put("D", "double");
	}

	private JavaMethodUtils() {
	}

	/**
	 * Parses method parameter descriptor (see chapter 4.3.3. in "The Java Virtual Machine Specification (2nd
	 * ed.)") and returns Java notations. Java notations means for example '.' separator between nested classes.
	 */
	public static List<String> convertParameterDescriptor(String descriptor) {
		return parseParameters(descriptor).stream().map(JavaMethodUtils::convertType).collect(Collectors.toList());
	}

	static List<String> parseParameters(String descriptor) {
		List<String> parameterTypes = new ArrayList<>();

		for (int i = descriptor.indexOf('(') + 1; i < descriptor.indexOf(')'); i++) {
			String primitivePart = getNextPrimitivePart(descriptor, i);
			String classPart = getNextClassPart(descriptor, i);

			if (primitivePart != null && classPart != null) {
				throw new IllegalStateException(
						String.format("Found primitive part (%s) and class part (%s) at (%d) in descriptor '%s'!",
								primitivePart, classPart, i, descriptor));
			} else if (primitivePart == null && classPart == null) {
				throw new IllegalStateException(String.format(
						"Found neither primitive part nor class part at (%d) in descriptor '%s'!", i, descriptor));
			}

			if (primitivePart != null) {
				parameterTypes.add(primitivePart);

				if (primitivePart.length() > 1) {
					i += primitivePart.length() - 1;
				}
			} else if (classPart != null) {
				parameterTypes.add(classPart);
				i += classPart.length();
			}
		}

		return parameterTypes;
	}

	static String convertType(String type) {
		int arrayCount = getArrayCount(type, 0);

		type = type.substring(arrayCount);

		String primitiveType = PRIMITIVE_TYPE_MAPPING.get(type);
		if (primitiveType != null) {
			type = primitiveType;
		} else if (type.startsWith("L")) {
			type = type.substring(1);
			type = type.contains("/") ? type.substring(type.lastIndexOf('/') + 1) : type;
			type = type.replaceAll(Pattern.quote("$"), ".");
		} else {
			throw new IllegalArgumentException("The type '" + type + "' is not a valida JVM signature type!");
		}

		return type + String.join("", Collections.nCopies(arrayCount, "[]"));
	}

	static String getNextPrimitivePart(String descriptor, int start) {
		int arrayCount = getArrayCount(descriptor, start);

		String type = descriptor.substring(start, start + arrayCount + 1);
		return type.substring(type.length() - 1).matches("[ZBCSIJFD]") ? type : null;
	}

	static String getNextClassPart(String descriptor, int start) {
		int arrayCount = getArrayCount(descriptor, start);

		String type = descriptor.substring(start, start + arrayCount + 1);
		if (type.charAt(type.length() - 1) == 'L') {
			return descriptor.substring(start, descriptor.indexOf(';', start + arrayCount + 1));
		} else {
			return null;
		}
	}

	static int getArrayCount(String value, int start) {
		int arrayCount = 0;

		for (int i = start; i < value.length(); i++) {
			if (value.charAt(i) == '[') {
				arrayCount++;
			} else {
				break;
			}
		}

		return arrayCount;
	}

	/**
	 * Normalizing means in that context that in case of nested classes only the class on the deepest level is
	 * returned and generic type parameters (enclosed by '<' and '>') are stripped of. If the remaining type is
	 * one of the passed type parameters it is replaced with 'Object'.
	 */
	public static List<String> normalizeMethodParameters(Collection<String> parameters,
			Map<String, String> typeParameters) {
		return normalizeMethodParameters(parameters).stream().map(at -> {
			String name = removeArrayBrackets(at);
			String type = typeParameters.get(name);
			return type != null ? type + (at.contains("[") ? at.substring(at.indexOf('[')) : "") : at;
		}).collect(Collectors.toList());
	}

	private static List<String> normalizeMethodParameters(Collection<String> parameters) {
		return parameters.stream().map(JavaMethodUtils::normalizeParameter).collect(Collectors.toList());
	}

	private static String removeArrayBrackets(String type) {
		return type.replaceAll("\\[", "").replaceAll("\\]", "");
	}

	private static String normalizeParameter(String paramter) {
		String withoutGenerics = paramter.contains("<")
				? paramter.substring(0, paramter.indexOf('<')) + paramter.substring(paramter.lastIndexOf('>') + 1)
				: paramter;
		return withoutGenerics.contains(".")
				? withoutGenerics.substring(withoutGenerics.lastIndexOf('.') + 1)
				: withoutGenerics;
	}

	public static String getSimpleName(String fqn, String separator) {
		return fqn.contains(separator) ? fqn.substring(fqn.lastIndexOf(separator) + 1) : fqn;
	}
}
