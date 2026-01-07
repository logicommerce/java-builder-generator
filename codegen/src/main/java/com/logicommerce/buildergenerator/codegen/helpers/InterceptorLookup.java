package com.logicommerce.buildergenerator.codegen.helpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.logicommerce.buildergenerator.annotations.BuilderInterceptor;
import com.logicommerce.buildergenerator.annotations.GeneratedBuilder;

public class InterceptorLookup {

	private Map<Class<?>, Class<?>> interceptors = new HashMap<>();

	public InterceptorLookup(List<Class<?>> interceptors) {
		for (Class<?> interceptor : interceptors) {
			addInterceptor(interceptor);
		}
	}

	public void addInterceptor(Class<?> interceptor) {
		Class<?> clazz = getInterceptedClass(interceptor);
		if (clazz == null) {
			throw new IllegalArgumentException("Interceptor must implement BuilderInterceptor");
		}
		interceptors.put(clazz, interceptor);
	}

	public List<Class<?>> getInterceptors(Class<?> clazz) {
		List<Class<?>> result = new ArrayList<>();
		while (clazz != null) {
			Class<?> interceptor = interceptors.get(clazz);
			if (interceptor != null) {
				result.add(interceptor);
			}
			clazz = clazz.getSuperclass();
		}
		Collections.reverse(result); // Execute interceptors in order from superclass to subclass
		return result;
	}

	public static Map<String, Class<?>> getInjectedGeneratedBuilder(Class<?> clazz) {
		Map<String, Class<?>> result = new HashMap<>();
		while (clazz != null) {
			for (Field field : clazz.getDeclaredFields()) {
				if (isInjectAnnotationPresent(field)) {
					result.put(field.getName(), getGeneratedBuilderType(field));
				}
			}
			clazz = clazz.getSuperclass();
		}
		return result;
	}

	private Class<?> getInterceptedClass(Class<?> clazz) {
		for (Type genericInterface : clazz.getGenericInterfaces()) {
			if (genericInterface instanceof ParameterizedType parameterizedType
					&& parameterizedType.getRawType() instanceof Class<?> parameterizedClass
					&& BuilderInterceptor.class.isAssignableFrom(parameterizedClass)) {
				Type firstArg = parameterizedType.getActualTypeArguments()[0];
				if (firstArg instanceof Class<?> interceptedClass) {
					return interceptedClass;
				} else if (firstArg instanceof ParameterizedType interceptedType
						&& interceptedType.getRawType() instanceof Class<?> interceptedClass) {
					return interceptedClass;
				}
			}
		}
		return null;
	}

	private static Class<?> getGeneratedBuilderType(Field field) {
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType parameterizedType
				&& parameterizedType.getRawType() instanceof Class<?> parameterizedClass
				&& GeneratedBuilder.class.isAssignableFrom(parameterizedClass)) {
			Type firstArg = parameterizedType.getActualTypeArguments()[0];
			if (firstArg instanceof Class<?> clazz) {
				return clazz;
			}
		}
		throw new IllegalArgumentException("@Inject field '" + field + "'' must be of type GeneratedBuilder<>");
	}

	private static boolean isInjectAnnotationPresent(Field field) {
		for (Annotation annotation : field.getAnnotations()) {
			// Check just the simple name so that we are not tied to any specific framework
			if (annotation.annotationType().getSimpleName().equals("Inject")) {
				return true;
			}
		}
		return false;
	}
}
