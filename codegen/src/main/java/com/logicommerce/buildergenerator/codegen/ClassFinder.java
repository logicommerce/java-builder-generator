package com.logicommerce.buildergenerator.codegen;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.logicommerce.buildergenerator.codegen.BuilderGeneratorConfig.PackageInfo;

class ClassFinder {

	public List<Class<?>> classesWithAnnotation(PackageInfo pkg, Class<? extends Annotation> annotation) {
		return findClassesImporting(pkg, annotation).stream()
				.filter(clazz -> clazz.isAnnotationPresent(annotation))
				.toList();
	}

	public List<Class<?>> classesWithAnnotation(Collection<PackageInfo> packages,
			Class<? extends Annotation> annotation) {
		return packages.stream()
				.flatMap(packageName -> classesWithAnnotation(packageName, annotation).stream())
				.toList();
	}

	public List<Class<?>> classesImplementingInterface(PackageInfo pkg, Class<?> interfaceClass) {
		if (pkg == null) {
			return List.of();
		}
		return findClassesImporting(pkg, interfaceClass).stream()
				.filter(interfaceClass::isAssignableFrom)
				.toList();
	}

	private List<Class<?>> findClassesImporting(PackageInfo pkg, Class<?> importedClass) {
		String importedClassName = importedClass.getCanonicalName();
		String includeText = "import " + importedClassName + ";";
		String cmd = """
				find %s -name '*.java' -exec sh -c 'grep -l "%s" "$@" || true' _ {} +
				""".formatted(pkg.dirPath(), includeText);
		return runAndGetLines(cmd, filename -> loadClass(filename, pkg));
	}

	private <T> List<T> runAndGetLines(String command, Function<String, T> mapper) {
		ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command)
				.redirectErrorStream(true);
		List<T> result = new ArrayList<>();
		try {
			Process process = processBuilder.start();
			if (process.waitFor() != 0) {
				throw new RuntimeException("Failed to run command: [" + command + "]");
			}
			String output = new String(process.getInputStream().readAllBytes());
			String[] lines = output.split("\n");
			for (String line : lines) {
				if (line.isEmpty()) {
					continue;
				}
				result.add(mapper.apply(line));
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	private Class<?> loadClass(String filename, PackageInfo pkg) {
		String relativePath = filename.replace(pkg.dirPath() + File.separator, "");
		String relativeClassName = relativePath.substring(0, relativePath.length() - ".java".length()).replace('/',
				'.');
		String className = pkg.packageName() + "." + relativeClassName;
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			System.err.println("Failed to load class: " + filename);
			throw new RuntimeException(e);
		}
	}
}
