package com.logicommerce.buildergenerator.annotations;

public interface GeneratedBuilder<T> {
	public T build();

	public static class GeneratedBuilderException extends RuntimeException {
		public GeneratedBuilderException(String className, Exception e) {
			super("Instance of " + className + " could not be built.", e);
		}
	}
}
