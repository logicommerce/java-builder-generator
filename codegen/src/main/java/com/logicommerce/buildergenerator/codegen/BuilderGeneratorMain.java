package com.logicommerce.buildergenerator.codegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.logicommerce.buildergenerator.annotations.BuilderInterceptor;
import com.logicommerce.buildergenerator.annotations.GenerateBuilder;
import com.logicommerce.buildergenerator.codegen.BuilderGeneratorConfig.GeneratedModule;
import com.logicommerce.buildergenerator.codegen.BuilderGeneratorConfig.PackageInfo;
import com.logicommerce.buildergenerator.codegen.core.BuilderGenerator;
import com.logicommerce.buildergenerator.codegen.core.ReverseClassSearch;
import com.logicommerce.buildergenerator.codegen.helpers.DirectoryCleanup;
import com.logicommerce.buildergenerator.codegen.helpers.InterceptorLookup;

public class BuilderGeneratorMain {

	private final BuilderGeneratorConfig config;

	public BuilderGeneratorMain(BuilderGeneratorConfig config) {
		this.config = config;
	}

	public void generate() throws IOException {
		for (BuilderGeneratorConfig.GeneratedModule module : config.getModules()) {
			generate(module);
		}
	}

	public void generate(GeneratedModule args) throws IOException {
		ClassFinder finder = new ClassFinder();
		List<Class<?>> classesToGenerate = finder.classesWithAnnotation(args.scannedInputDirs(), GenerateBuilder.class);
		List<Class<?>> additionalClasses = finder.classesWithAnnotation(getAdditionalPackagesToScan(args),
				GenerateBuilder.class);
		List<Class<?>> allClassesWithGenerateBuilder = union(classesToGenerate, additionalClasses);

		var reverseClassSearch = new ReverseClassSearch(allClassesWithGenerateBuilder);
		List<Class<?>> interceptors = finder.classesImplementingInterface(args.interceptorsPackage(),
				BuilderInterceptor.class);
		var interceptorLookup = new InterceptorLookup(interceptors);
		BuilderGeneratorWrap generator = new BuilderGeneratorWrap(config, reverseClassSearch, interceptorLookup);

		DirectoryCleanup directoryCleanup = new DirectoryCleanup();
		for (Class<?> clazz : classesToGenerate) {
			String builderName = BuilderGenerator.getBuilderSimpleName(clazz);
			String filename = args.generatedBuildersPackage().dirPath() + File.separator + builderName + ".java";
			directoryCleanup.addFile(filename);
			String generatedCode = generator.generate(clazz.getName(), args.generatedBuildersPackage());
			if (!readFileToString(new File(filename)).equals(generatedCode)) {
				try (FileWriter writer = new FileWriter(filename)) {
					writer.write(generatedCode);
				}
			}
		}
		directoryCleanup.deleteNonWrittenFiles();
	}

	private static String readFileToString(File file) throws IOException {
		if (!file.exists()) {
			return "";
		}
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
	}

	private static <T> List<T> union(List<T> list1, List<T> list2) {
		var set = new HashSet<>(list1);
		set.addAll(list2);
		return List.copyOf(set);
	}

	private Set<PackageInfo> getAdditionalPackagesToScan(GeneratedModule args) {
		Set<PackageInfo> additionalPackagesToScan = new HashSet<>();
		for (GeneratedModule module : config.getModules()) {
			if (module != args) {
				additionalPackagesToScan.addAll(module.scannedInputDirs());
			}
		}
		return additionalPackagesToScan;
	}

}
