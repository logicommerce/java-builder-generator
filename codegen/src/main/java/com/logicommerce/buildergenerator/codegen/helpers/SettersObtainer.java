package com.logicommerce.buildergenerator.codegen.helpers;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

// Same as FieldsObtainer, but looking for setters instead of fields
public class SettersObtainer {

	public static record SettersObtainerField(String name, Class<?> type, Type genericType) {
	}

	private static final Pattern SETTER_PATTERN = java.util.regex.Pattern.compile("set([A-Z].*)");

	private Map<String, List<SettersObtainerField>> classFields;

	public SettersObtainer() {
		this.classFields = new HashMap<>();
	}

	public SettersObtainer(Map<String, List<SettersObtainerField>> classFields) {
		this.classFields = classFields;
	}

	public List<SettersObtainerField> getAllClassFields(Class<?> clazz) {
		List<SettersObtainerField> fields = classFields.get(clazz.getName());
		if (fields != null) {
			return fields;
		}
		fields = obtainAllClassFields(clazz);
		classFields.put(clazz.getName(), fields);
		return fields;
	}

	private List<SettersObtainerField> obtainAllClassFields(Class<?> clazz) {
		Map<String, SettersObtainerField> map = new TreeMap<>();
		while (clazz != null) {
			for (Method method : Arrays.asList(clazz.getDeclaredMethods())) {
				if (!isSetterCandiate(method)) {
					continue;
				}
				var matcher = SETTER_PATTERN.matcher(method.getName());
				if (matcher.matches()) {
					String uppercaseName = matcher.group(1);
					String name = Character.toLowerCase(uppercaseName.charAt(0)) + uppercaseName.substring(1);
					Class<?> type = method.getParameterTypes()[0];
					Type genericType = method.getAnnotatedParameterTypes()[0].getType();
					map.putIfAbsent(name, new SettersObtainerField(name, type, genericType));
				}
			}
			clazz = clazz.getSuperclass();
		}
		return new ArrayList<>(map.values());
	}

	private boolean isSetterCandiate(Method method) {
		return !method.isSynthetic() && !Modifier.isStatic(method.getModifiers())
				&& method.getParameterCount() == 1
				&& method.getReturnType().equals(Void.TYPE)
				&& Modifier.isPublic(method.getModifiers());
	}
}
