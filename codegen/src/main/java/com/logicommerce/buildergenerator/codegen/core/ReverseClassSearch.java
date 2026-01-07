package com.logicommerce.buildergenerator.codegen.core;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ReverseClassSearch {

	private Map<String, Set<Class<?>>> classesLookup = new HashMap<>();

	public ReverseClassSearch(List<Class<?>> classes) {
		for (Class<?> clazz : classes) {
			if (clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation() || clazz.isSynthetic()) {
				throw new IllegalArgumentException(
						clazz.getCanonicalName() + " has @GenerateBuilder but is not a concrete class");
			}
			if (Modifier.isAbstract(clazz.getModifiers())) {
				throw new IllegalArgumentException(clazz.getCanonicalName() + " has @GenerateBuilder but is abstract");
			}
			populateParents(clazz, clazz);
		}
	}

	public boolean containsClass(String className) {
		return classesLookup.containsKey(className);
	}

	public Set<Class<?>> getChildren(String className) {
		return classesLookup.get(className);
	}

	private void populateParents(Class<?> clazz, Class<?> child) {
		if (clazz == null || clazz.equals(Object.class)) {
			return;
		}
		Comparator<Class<?>> classComparator = (c1, c2) -> c1.getCanonicalName().compareTo(c2.getCanonicalName());
		classesLookup
				.computeIfAbsent(clazz.getCanonicalName(), k -> new TreeSet<>(classComparator))
				.add(child);
		populateParents(clazz.getSuperclass(), child);
		for (Class<?> iface : clazz.getInterfaces()) {
			populateParents(iface, child);
		}
	}
}
