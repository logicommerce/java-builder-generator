package com.logicommerce.buildergenerator.codegen.helpers;

import java.util.Map;

public class Singularize {

	private static final Map<String, String> IRREGULAR_PLURALS = Map.ofEntries(
			Map.entry("children", "child"),
			Map.entry("teeth", "tooth"),
			Map.entry("feet", "foot"),
			Map.entry("geese", "goose"),
			Map.entry("mice", "mouse"),
			Map.entry("men", "man"),
			Map.entry("women", "woman"),
			Map.entry("cacti", "cactus"),
			Map.entry("fungi", "fungus"),
			Map.entry("nuclei", "nucleus"),
			Map.entry("syllabi", "syllabus"),
			Map.entry("analyses", "analysis"),
			Map.entry("diagnoses", "diagnosis"),
			Map.entry("crises", "crisis"),
			Map.entry("phenomena", "phenomenon"),
			Map.entry("data", "data"), // datum, but "addData" sounds better
			Map.entry("media", "medium"),
			Map.entry("indices", "index"),
			Map.entry("appendices", "appendix"),
			Map.entry("parentheses", "parenthesis"),
			Map.entry("alumni", "alumnus"),
			Map.entry("bacteria", "bacterium"),
			Map.entry("criteria", "criteria"), // criterion, but "addCriteria" sounds better
			Map.entry("news", "news"));

	public static String singularize(String word) {
		if (word.endsWith("List")) {
			return replaceSuffix(word, "List", "ListItem");
		}

		for (Map.Entry<String, String> entry : IRREGULAR_PLURALS.entrySet()) {
			if (word.toLowerCase().endsWith(entry.getKey())) {
				return replaceSuffix(word, entry.getKey(), entry.getValue());
			}
		}

		if (word.endsWith("ies") && word.length() > 3) {
			return replaceSuffix(word, "ies", "y");
		}

		if (word.endsWith("ves")) {
			return replaceSuffix(word, "ves", "f");
		}

		if (word.endsWith("oes") || word.endsWith("xes")) {
			return replaceSuffix(word, "es", "");
		}

		if (word.endsWith("ses") && !"bases".equals(word) && !"cases".equals(word)) {
			return replaceSuffix(word, "es", "");
		}

		if (word.endsWith("s") && !word.endsWith("ss")) {
			return replaceSuffix(word, "s", "");
		}

		return word;
	}

	private static String replaceSuffix(String word, String suffix, String replacement) {
		return word.substring(0, word.length() - suffix.length()) + replacement;
	}

}
