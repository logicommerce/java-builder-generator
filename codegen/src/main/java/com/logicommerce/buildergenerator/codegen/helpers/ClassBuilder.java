package com.logicommerce.buildergenerator.codegen.helpers;

public class ClassBuilder {
	private final StringBuilder sb;
	private int indentLevel;
	private static final String INDENT_UNIT = "\t";

	public ClassBuilder() {
		sb = new StringBuilder();
		indentLevel = 0;
	}

	public ClassBuilder addPackage(String packageName) {
		sb.append("package ").append(packageName).append(";\n\n");
		return this;
	}

	public ClassBuilder addImport(String importStr) {
		sb.append("import ").append(importStr).append(";\n");
		return this;
	}

	public ClassBuilder addEmptyLine() {
		sb.append("\n");
		return this;
	}

	public ClassBuilder addLine(String line) {
		if (line.startsWith("}")) {
			indentLevel = Math.max(0, indentLevel - 1);
		}
		String indent = times(INDENT_UNIT, indentLevel);
		sb.append(indent);
		sb.append(line.replace("\n", "\n" + indent));
		sb.append("\n");
		if (line.endsWith("{")) {
			indentLevel++;
		}
		return this;
	}

	private String times(String s, int n) {
		return n == 0 ? "" : s + times(s, n - 1);
	}

	public ClassBuilder addPrivateField(String type, String name, String initializer) {
		String line = "private " + type + " " + name;
		if (initializer != null && !initializer.isEmpty()) {
			line += " = " + initializer;
		}
		line += ";";
		return addLine(line);
	}

	public ClassBuilder addConstructor(String className, String params, String... body) {
		return addPublicMethodImpl(className, params, body);
	}

	public ClassBuilder addPublicMethod(String returnType, String name, String params, String... body) {
		return addPublicMethodImpl(returnType + " " + name, params, body);
	}

	private ClassBuilder addPublicMethodImpl(String returnTypeAndName, String params, String[] body) {
		addLine("public " + returnTypeAndName + "(" + params + ") {");
		for (String line : body) {
			addLine(line);
		}
		return addLine("}");
	}

	@Override
	public String toString() {
		return sb.toString();
	}
}
