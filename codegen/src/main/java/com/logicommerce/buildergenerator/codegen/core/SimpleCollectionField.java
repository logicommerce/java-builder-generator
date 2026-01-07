package com.logicommerce.buildergenerator.codegen.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.logicommerce.buildergenerator.codegen.helpers.ClassBuilder;
import com.logicommerce.buildergenerator.codegen.helpers.Singularize;

class SimpleCollectionField extends FieldBase {

	protected CollectionType type;

	public SimpleCollectionField(BuilderGenerator parent, String fieldName, CollectionType type) {
		super(parent, fieldName, type.getValueClass());
		this.type = type;
	}

	@Override
	public void declareBuilderField(ClassBuilder cb) {
		parent.addRequiredImport(fieldFullType);
		String fieldTypeConstructor = "new " + collectionImplementation() + "<>()";
		switch (type) {
			case LIST -> {
				parent.addRequiredImport(List.class);
				parent.addRequiredImport(ArrayList.class);
				cb.addPrivateField(collectionInterface(fieldSimpleType), fieldName, fieldTypeConstructor);
			}
			case SET -> {
				parent.addRequiredImport(Set.class);
				parent.addRequiredImport(LinkedHashSet.class);
				cb.addPrivateField(collectionInterface(fieldSimpleType), fieldName, fieldTypeConstructor);
			}
			case MAP -> {
				parent.addRequiredImport(Map.class);
				parent.addRequiredImport(HashMap.class);
				cb.addPrivateField(collectionInterface(fieldSimpleType), fieldName, fieldTypeConstructor);
			}
			default -> throw new IllegalArgumentException("Unsupported collection type: " + type);
		}
		cb.addEmptyLine();
	}

	private String collectionInterface(String collectionElementType) {
		return switch (type) {
			case LIST -> "List<" + collectionElementType + ">";
			case SET -> "Set<" + collectionElementType + ">";
			case MAP -> "Map<" + keyType() + ", " + collectionElementType + ">";
		};
	}

	private String collectionImplementation() {
		return switch (type) {
			case LIST -> "ArrayList";
			case SET -> "LinkedHashSet";
			case MAP -> "HashMap";
		};
	}

	@Override
	public void declareBuilderSetters(ClassBuilder cb) {
		String methodName = addPrefix("add", Singularize.singularize(fieldName));
		if (type == CollectionType.MAP) {
			addSetterMethod(cb, methodName, (keyType() + " key, " + fieldSimpleType + " value"), ".put(key, value);");
		} else {
			addSetterMethod(cb, methodName, (fieldSimpleType + " element"), ".add(element);");
		}

		cb.addPublicMethod(parent.builderClassWithGeneric, fieldName,
				(collectionInterface(fieldSimpleType) + " newValues"),
				"if (!this." + fieldName + ".isEmpty()) {",
				"throw new IllegalStateException(\"Setting " + fieldName
						+ " would overwrite other values you have added.\");",
				"}",
				"this." + fieldName + " = newValues;",
				BUILT_OBJECT + "." + setterMethodName + "(newValues);",
				"return this;").addEmptyLine();
	}

	private void addSetterMethod(ClassBuilder cb, String methodName, String args, String addValueOperation) {
		cb.addPublicMethod(parent.builderClassWithGeneric, methodName, args,
				"this." + fieldName + addValueOperation,
				BUILT_OBJECT + "." + setterMethodName + "(this." + fieldName + ");",
				"return this;").addEmptyLine();
	}

	protected String keyType() {
		return type.getMapKeyClass().getSimpleName();
	}
}
