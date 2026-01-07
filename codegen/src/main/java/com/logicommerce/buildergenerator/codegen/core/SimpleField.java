package com.logicommerce.buildergenerator.codegen.core;

import java.lang.invoke.MethodType;

import com.logicommerce.buildergenerator.codegen.helpers.ClassBuilder;

class SimpleField extends FieldBase {
	private String actualSimpleType;

	public SimpleField(BuilderGenerator parent, String fieldName, Class<?> fieldClass) {
		super(parent, fieldName, fieldClass.isPrimitive() ? getBoxedType(fieldClass) : fieldClass);
		this.actualSimpleType = fieldClass.getSimpleName();
	}

	@Override
	public void declareBuilderField(ClassBuilder cb) {
	}

	@Override
	public void declareBuilderSetters(ClassBuilder cb) {
		parent.addRequiredImport(fieldFullType);
		cb.addPublicMethod(parent.builderClassWithGeneric, fieldName, (actualSimpleType + " " + fieldName),
				BUILT_OBJECT + "." + setterMethodName + "(" + fieldName + ");",
				"return this;").addEmptyLine();
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getBoxedType(Class<T> c) {
		return (Class<T>) MethodType.methodType(c).wrap().returnType();
	}
}
