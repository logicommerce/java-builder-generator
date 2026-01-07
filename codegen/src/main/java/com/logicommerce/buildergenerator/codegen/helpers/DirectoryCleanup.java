package com.logicommerce.buildergenerator.codegen.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DirectoryCleanup {

	private List<String> files = new ArrayList<>();

	public void addFile(String filename) {
		if (files.isEmpty()) {
			ensureDirectoryExists(filename);
		}
		files.add(filename);
	}

	public void deleteNonWrittenFiles() {
		File directory = getDirectory();
		File[] filesInDirectory = directory.listFiles();
		for (File file : filesInDirectory) {
			if (!files.contains(file.getAbsolutePath())) {
				try {
					Files.delete(file.toPath());
				} catch (IOException e) {
					e.printStackTrace();
					// Continue deleting the rest of the files
				}
			}
		}
	}

	private void ensureDirectoryExists(String filename) {
		new File(getDirectory(filename)).mkdirs();
	}

	private File getDirectory() {
		String firstDirectory = getDirectory(files.get(0));
		for (String filename : files) {
			if (!filename.startsWith(firstDirectory)) {
				throw new IllegalArgumentException("All files must be in the same directory");
			}
		}
		return new File(firstDirectory);
	}

	private String getDirectory(String filename) {
		return filename.substring(0, filename.lastIndexOf("/"));
	}
}
