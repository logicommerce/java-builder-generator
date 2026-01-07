package com.logicommerce.buildergenerator.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuilderGeneratorConfig {

	private static final Pattern PATH_PATTERN = Pattern.compile("/src/(?:main|test)/java/([\\w/]+)$");

	static record PackageInfo(String packageName, String dirPath) {
	}

	static record GeneratedModule(List<PackageInfo> scannedInputDirs, PackageInfo generatedBuildersPackage,
			PackageInfo interceptorsPackage) {
	}

	private List<GeneratedModule> modules = new ArrayList<>();

	public void addGenerationTask(List<String> scannedInputModules, String generatedBuildersPath,
			String interceptorsPath) {
		modules.add(new GeneratedModule(
				scannedInputModules.stream().map(this::pathToPackage).toList(),
				pathToPackage(generatedBuildersPath),
				pathToPackage(interceptorsPath)));
	}

	public List<GeneratedModule> getModules() {
		return modules;
	}

	public String getBuilderPackage(String inputPackage) {
		for (GeneratedModule module : modules) {
			for (PackageInfo moduleInput : module.scannedInputDirs) {
				if (inputPackage.startsWith(moduleInput.packageName())) {
					return module.generatedBuildersPackage.packageName();
				}
			}
		}
		throw new IllegalArgumentException("Unexpected package: " + inputPackage);
	}

	private PackageInfo pathToPackage(String path) {
		if (path == null) {
			return null;
		}
		Matcher matcher = PATH_PATTERN.matcher(path);
		if (!matcher.find()) {
			throw new IllegalArgumentException(
					"Invalid path: '" + path + "', it should match the pattern: " + PATH_PATTERN.pattern());
		}
		String module = matcher.group(1).replace('/', '.');
		return new PackageInfo(module, path);
	}
}
