package com.logicommerce.buildergenerator.codegen.helpers;

import java.util.Set;

public class JavaKeywords {

	private static final Set<String> JAVA_KEYWORDS = Set.of(
			"abstract",
			"assert",
			"boolean",
			"break",
			"byte",
			"case",
			"catch",
			"char",
			"class",
			"const",
			"continue",
			"default",
			"do",
			"double",
			"else",
			"enum",
			"extends",
			"false",
			"final",
			"finally",
			"float",
			"for",
			"goto",
			"if",
			"implements",
			"import",
			"instanceof",
			"int",
			"interface",
			"long",
			"native",
			"new",
			"null",
			"package",
			"private",
			"protected",
			"public",
			"return",
			"short",
			"static",
			"strictfp",
			"super",
			"switch",
			"synchronized",
			"this",
			"throw",
			"throws",
			"transient",
			"true",
			"try",
			"void",
			"volatile",
			"while");

	public static boolean isKeyword(String word) {
		return JAVA_KEYWORDS.contains(word);
	}
}
