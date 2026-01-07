package com.logicommerce.buildergenerator.annotations;

/**
 * Interceptor interface for builder processes.
 * Allows custom logic to be executed before and after building an object.
 *
 * @param <T> the type of object being built
 */
public interface BuilderInterceptor<T> {

	/**
	 * Called before the build process begins. Override the default (empty)
	 * implementation to add custom behavior.
	 *
	 * @param object the object to be built (after instantiation, before setting
	 *               properties)
	 */
	default void beforeBuild(T object) {
	}

	/**
	 * Called after the build process completes. Override the default (empty)
	 * implementation to add custom behavior.
	 *
	 * @param object the built object
	 */
	default void afterBuild(T object) {
	}

}
