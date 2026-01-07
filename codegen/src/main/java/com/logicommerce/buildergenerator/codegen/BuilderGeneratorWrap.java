package com.logicommerce.buildergenerator.codegen;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.logicommerce.buildergenerator.annotations.GenerateBuilder;
import com.logicommerce.buildergenerator.codegen.BuilderGeneratorConfig.PackageInfo;
import com.logicommerce.buildergenerator.codegen.core.BuilderGenerator;
import com.logicommerce.buildergenerator.codegen.core.ReverseClassSearch;
import com.logicommerce.buildergenerator.codegen.helpers.InterceptorLookup;
import com.logicommerce.buildergenerator.codegen.helpers.SettersObtainer;
import com.logicommerce.buildergenerator.codegen.helpers.SettersObtainer.SettersObtainerField;

class BuilderGeneratorWrap {

	private static Map<String, List<SettersObtainerField>> cachedClassFields = new HashMap<>();

	private final BuilderGeneratorConfig config;

	private final ReverseClassSearch reverseClassSearch;

	private final InterceptorLookup interceptorLookup;

	public BuilderGeneratorWrap(BuilderGeneratorConfig config, ReverseClassSearch reverseClassSearch,
			InterceptorLookup interceptorLookup) {
		this.config = config;
		this.reverseClassSearch = reverseClassSearch;
		this.interceptorLookup = interceptorLookup;
	}

	public String generate(String className, PackageInfo builderPackage) {
		Class<?> targetClass;
		try {
			targetClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Error: Class " + className + " not found.");
		}
		if (!targetClass.isAnnotationPresent(GenerateBuilder.class)) {
			throw new IllegalArgumentException(
					"Error: Class " + className + " is not annotated with @GenerateBuilder.");
		}
		if (!isConcreteClass(targetClass)) {
			throw new IllegalArgumentException("Error: Class " + className + " is not a concrete class.");
		}

		BuilderGenerator builderData = new BuilderGenerator(config, targetClass, builderPackage.packageName(),
				reverseClassSearch, interceptorLookup, this::sortImports);
		List<SettersObtainerField> fields = getAllClassFields(targetClass);
		Set<String> fieldsWithOverlappingBuilders = getFieldsWithOverlappingBuilders(fields);
		for (SettersObtainerField field : fields) {
			builderData.addField(field, fieldsWithOverlappingBuilders);
		}
		return builderData.getSourceCode();
	}

	private static List<SettersObtainerField> getAllClassFields(Class<?> clazz) {
		return new SettersObtainer(cachedClassFields).getAllClassFields(clazz);
	}

	private static boolean isConcreteClass(Class<?> clazz) {
		int mods = clazz.getModifiers();
		return !(Modifier.isAbstract(mods) || clazz.isInterface() || clazz.isEnum());
	}

	private int sortImports(String s1, String s2) {
		int group1 = getImportGroup(s1);
		int group2 = getImportGroup(s2);
		if (group1 != group2) {
			return Integer.compare(group1, group2);
		}
		return s1.compareTo(s2);
	}

	private static int getImportGroup(String importString) {
		if (importString.startsWith("java.")) {
			return 1;
		} else if (importString.startsWith("javax.")) {
			return 2;
		} else if (importString.startsWith("org.")) {
			return 3;
		} else if (importString.startsWith("com.")) {
			return 4;
		} else {
			return 5;
		}
	}

	private Set<String> getFieldsWithOverlappingBuilders(List<SettersObtainerField> fields) {
		Set<String> result = new HashSet<>();
		for (int i = 0; i < fields.size(); i++) {
			for (int j = i + 1; j < fields.size(); j++) {
				String field1Class = fields.get(i).type().getName();
				String field2Class = fields.get(j).type().getName();
				Set<Class<?>> children1 = reverseClassSearch.getChildren(field1Class);
				Set<Class<?>> children2 = reverseClassSearch.getChildren(field2Class);
				if (children1 != null && children2 != null && children1.stream().anyMatch(children2::contains)) {
					result.add(field1Class);
					result.add(field2Class);
				}
			}
		}
		return result;
	}
}
