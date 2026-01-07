package com.logicommerce.buildergenerator.codegen.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.logicommerce.buildergenerator.annotations.BuilderInterceptor;
import com.logicommerce.buildergenerator.annotations.GenerateBuilder;
import com.logicommerce.buildergenerator.annotations.GeneratedBuilder;
import com.logicommerce.buildergenerator.codegen.BuilderGeneratorConfig;
import com.logicommerce.buildergenerator.codegen.helpers.ClassBuilder;
import com.logicommerce.buildergenerator.codegen.helpers.InterceptorLookup;
import com.logicommerce.buildergenerator.codegen.helpers.SettersObtainer.SettersObtainerField;

public class BuilderGenerator {

	static final String OBJ_VARIABLE = "_obj";

	private final BuilderGeneratorConfig config;

	final ReverseClassSearch reverseClassSearch;

	private final InterceptorLookup interceptorLookup;

	private final String builderPackage;

	private final Set<String> requiredImports;

	private final Class<?> targetClass;

	private final String targetSimpleClass;

	final String builderClassWithoutGeneric;

	final String builderClassWithGeneric;

	private final List<FieldBase> fields = new ArrayList<>();

	public BuilderGenerator(BuilderGeneratorConfig config, Class<?> targetClass, String builderPackage,
			ReverseClassSearch reverseClassSearch, InterceptorLookup interceptorLookup,
			Comparator<String> sortImports) {
		this.config = config;
		this.reverseClassSearch = reverseClassSearch;
		this.builderPackage = builderPackage;
		this.requiredImports = new TreeSet<>(sortImports);
		this.targetClass = targetClass;
		this.targetSimpleClass = targetClass.getSimpleName();
		this.builderClassWithoutGeneric = getBuilderSimpleName(targetClass);
		this.builderClassWithGeneric = builderClassWithoutGeneric + "<T>";
		this.interceptorLookup = interceptorLookup;
		addRequiredImport(targetClass);
		addRequiredImport(GeneratedBuilder.class);
	}

	public static String getBuilderSimpleName(Class<?> clazz) {
		var annotation = clazz.getAnnotation(GenerateBuilder.class);
		if (annotation != null && !annotation.name().isEmpty()) {
			return annotation.name();
		}
		return clazz.getSimpleName() + "Builder";
	}

	public String getBuilderFullName(Class<?> clazz) {
		return config.getBuilderPackage(clazz.getPackageName()) + "." + getBuilderSimpleName(clazz);
	}

	public void addField(SettersObtainerField field, Set<String> fieldsWithOverlappingBuilders) {
		String fieldName = field.name();
		Class<?> fieldClass = field.type();
		CollectionType collectionType = CollectionType.of(fieldClass, field.genericType());
		if (collectionType != null) {
			String elementFullClass = collectionType.getValueClass().getName();
			boolean hasBuilders = reverseClassSearch.containsClass(elementFullClass);
			if (hasBuilders) {
				boolean requiresFieldDiscriminator = fieldsWithOverlappingBuilders.contains(elementFullClass);
				fields.add(new BuilderCollectionField(this, fieldName, collectionType, requiresFieldDiscriminator));
			} else {
				fields.add(new SimpleCollectionField(this, fieldName, collectionType));
			}
		} else {
			String fieldFullClass = fieldClass.getName();
			boolean hasBuilders = reverseClassSearch.containsClass(fieldFullClass);
			boolean requiresFieldDiscriminator = fieldsWithOverlappingBuilders.contains(fieldFullClass);
			if (hasBuilders) {
				fields.add(new BuilderField(this, fieldName, fieldClass, requiresFieldDiscriminator));
			} else {
				fields.add(new SimpleField(this, fieldName, fieldClass));
			}
		}
	}

	public String getSourceCode() {
		ClassBuilder cb = new ClassBuilder();
		cb.addLine("@SuppressWarnings(\"checkstyle:all\")");
		cb.addLine(
				"public class " + builderClassWithGeneric + " implements GeneratedBuilder<" + targetSimpleClass + "> {")
				.addEmptyLine();
		cb.addPrivateField("T", "_parentBuilder", "null").addEmptyLine();
		cb.addLine("private final " + targetSimpleClass + " " + OBJ_VARIABLE + ";").addEmptyLine();

		for (FieldBase field : fields) {
			field.declareBuilderField(cb);
		}

		cb.addConstructor(builderClassWithoutGeneric, "",
				"this." + OBJ_VARIABLE + " = new " + targetSimpleClass + "();",
				"this._beforeBuild();").addEmptyLine();
		cb.addConstructor(builderClassWithoutGeneric, targetSimpleClass + " object",
				"this." + OBJ_VARIABLE + " = object;",
				"this._beforeBuild();").addEmptyLine();
		cb.addConstructor(builderClassWithoutGeneric, "T parentBuilder",
				"this();",
				"this._parentBuilder = parentBuilder;").addEmptyLine();
		cb.addConstructor(builderClassWithoutGeneric, "T parentBuilder, " + targetSimpleClass + " object",
				"this(object);",
				"this._parentBuilder = parentBuilder;").addEmptyLine();

		for (FieldBase field : fields) {
			field.declareBuilderSetters(cb);
		}

		appendBuildMethod(cb);
		cb.addPublicMethod("T", "done", "",
				"this._afterBuild();",
				"return this._parentBuilder;").addEmptyLine();
		appendInterceptorMethods(cb);

		cb.addLine("}");

		return getHeader() + cb.toString();
	}

	private String getHeader() {
		ClassBuilder cb = new ClassBuilder();
		cb.addPackage(builderPackage);
		for (String importStr : requiredImports) {
			cb.addImport(importStr);
		}
		cb.addEmptyLine();
		cb.addLine("// WARNING: Auto-generated code. Do not edit directly.");
		cb.addEmptyLine();
		return cb.toString();
	}

	private void appendInterceptorMethods(ClassBuilder cb) {
		var interceptors = interceptorLookup.getInterceptors(targetClass);
		boolean hasInterceptors = !interceptors.isEmpty();
		if (!hasInterceptors) {
			cb.addLine("protected void _beforeBuild() {}").addEmptyLine();
			cb.addLine("protected void _afterBuild() {}").addEmptyLine();
			return;
		}

		addRequiredImport(List.class);
		addRequiredImport(BuilderInterceptor.class);
		cb.addLine("List<BuilderInterceptor<? super " + targetSimpleClass + ">> _interceptors = List.of(");
		for (int i = 0; i < interceptors.size(); i++) {
			Class<?> interceptor = interceptors.get(i);
			addRequiredImport(interceptor);
			cb.addLine("\tnew " + interceptor.getSimpleName() + "()" + (i < interceptors.size() - 1 ? "," : ""));
		}
		cb.addLine(");");
		cb.addEmptyLine();

		cb.addLine("protected void _beforeBuild() {");
		addTry(cb);
		cb.addLine("for (var interceptor : _interceptors) {");
		cb.addLine("interceptor.beforeBuild(" + OBJ_VARIABLE + ");");
		cb.addLine("}");
		addCatch(cb);
		cb.addLine("}").addEmptyLine();

		cb.addLine("protected void _afterBuild() {");
		addTry(cb);
		cb.addLine("for (var interceptor : _interceptors) {");
		cb.addLine("interceptor.afterBuild(" + OBJ_VARIABLE + ");");
		cb.addLine("}");
		addCatch(cb);
		cb.addLine("}").addEmptyLine();
	}

	private void appendBuildMethod(ClassBuilder cb) {
		cb.addLine("@Override");
		cb.addLine("public " + targetSimpleClass + " build() {");
		cb.addLine("this._afterBuild();");
		cb.addLine("return " + OBJ_VARIABLE + ";");
		cb.addLine("}");
		cb.addEmptyLine();
	}

	void addTry(ClassBuilder cb) {
		cb.addLine("try {");
	}

	void addCatch(ClassBuilder cb) {
		cb.addLine("} catch (Exception e) {");
		cb.addLine("throw new GeneratedBuilderException(\"" + targetSimpleClass + "\", e);");
		cb.addLine("}");
	}

	void addRequiredImport(Class<?> clazz) {
		addRequiredImport(clazz.getName());
	}

	void addRequiredImport(String fullClassName) {
		if (fullClassName == null || fullClassName.isEmpty() || fullClassName.startsWith("java.lang.")
				|| !fullClassName.contains(".")) {
			return;
		}
		int lastDotIndex = fullClassName.lastIndexOf('.');
		if (lastDotIndex > 0) {
			String packageName = fullClassName.substring(0, lastDotIndex);
			if (packageName.equals(builderPackage)) {
				return;
			}
		}
		requiredImports.add(fullClassName);
	}
}
