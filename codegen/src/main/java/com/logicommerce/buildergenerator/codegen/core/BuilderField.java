package com.logicommerce.buildergenerator.codegen.core;

import com.logicommerce.buildergenerator.codegen.helpers.ClassBuilder;

class BuilderField extends FieldBase {
	public BuilderField(BuilderGenerator parent, String fieldName, Class<?> fieldClass,
			boolean requiresFieldDiscriminator) {
		super(parent, fieldName, fieldClass);
		setRequiresFieldDiscriminator(requiresFieldDiscriminator);
	}

	@Override
	public void declareBuilderField(ClassBuilder cb) {
	}

	@Override
	public void declareBuilderSetters(ClassBuilder cb) {
		parent.addRequiredImport(fieldFullType);
		for (Class<?> childClass : parent.reverseClassSearch.getChildren(fieldFullType)) {
			parent.addRequiredImport(childClass);
			parent.addRequiredImport(parent.getBuilderFullName(childClass));
			String methodName = compositeSetter(fieldName, fieldFullType, childClass);
			addSetterMethod(cb, methodName, childClass);
			boolean hasSpecializedSetters = parent.reverseClassSearch.getChildren(fieldFullType).size() > 1;
			boolean thisIsParentClassBuilder = childClass.getName().equals(fieldFullType);
			if (hasSpecializedSetters && thisIsParentClassBuilder) {
				addSetterMethod(cb, fieldName, childClass);
			}
		}
		cb.addPublicMethod(parent.builderClassWithGeneric, fieldName, (fieldSimpleType + " " + fieldName),
				BUILT_OBJECT + "." + setterMethodName + "(" + fieldName + ");",
				"return this;").addEmptyLine();
	}

	private void addSetterMethod(ClassBuilder cb, String methodName, Class<?> childClass) {
		String elementBuilderClass = BuilderGenerator.getBuilderSimpleName(childClass) + "<"
				+ parent.builderClassWithGeneric + ">";
		cb.addPublicMethod(elementBuilderClass, methodName, "",
				"var " + TMP_VARIABLE + " = new " + childClass.getSimpleName() + "();",
				BUILT_OBJECT + "." + setterMethodName + "(" + TMP_VARIABLE + ");",
				"return new " + elementBuilderClass + "(this, " + TMP_VARIABLE + ");").addEmptyLine();
	}
}
