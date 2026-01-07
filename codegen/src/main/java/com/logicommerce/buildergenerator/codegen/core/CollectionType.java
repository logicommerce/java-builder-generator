package com.logicommerce.buildergenerator.codegen.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum CollectionType {
	LIST,
	SET,
	MAP;

	private Class<?> valueClass;

	private Class<?> mapKeyClass;

	public Class<?> getValueClass() {
		return valueClass;
	}

	public Class<?> getMapKeyClass() {
		return mapKeyClass;
	}

	public static CollectionType of(Class<?> clazz, Type type) {
		CollectionType result = null;
		if (clazz.isAssignableFrom(List.class)) {
			result = LIST;
			result.valueClass = getGenericParam(type, 0);
		} else if (clazz.isAssignableFrom(Set.class)) {
			result = SET;
			result.valueClass = getGenericParam(type, 0);
		} else if (clazz.isAssignableFrom(Map.class)) {
			result = MAP;
			result.mapKeyClass = getGenericParam(type, 0);
			result.valueClass = getGenericParam(type, 1);
		}
		return result;
	}

	private static Class<?> getGenericParam(Type type, int index) {
		if (type instanceof ParameterizedType parameterizedType) {
			Type[] typeArguments = parameterizedType.getActualTypeArguments();
			if (typeArguments.length > index && typeArguments[index] instanceof Class<?> elementClass) {
				return elementClass;
			}
		}
		return Object.class;
	}

}
