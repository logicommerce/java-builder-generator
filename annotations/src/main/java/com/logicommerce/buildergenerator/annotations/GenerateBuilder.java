package com.logicommerce.buildergenerator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a builder should be generated for the annotated
 * class.
 * <p>
 * Optionally, a custom builder class name can be specified.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface GenerateBuilder {

	/**
	 * Optional custom name for the generated builder class.
	 * If empty, a default name will be used (class Foo -> FooBuilder).
	 *
	 * @return the custom builder class name
	 */
	String name() default "";

}
