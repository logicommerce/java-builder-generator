package com.logicommerce.buildergenerator.annotations;

public interface BuilderInterceptor<T> {

	default void beforeBuild(T object) {
	}

	default void afterBuild(T object) {
	}

}
