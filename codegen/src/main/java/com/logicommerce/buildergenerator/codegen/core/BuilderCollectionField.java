package com.logicommerce.buildergenerator.codegen.core;

import com.logicommerce.buildergenerator.codegen.helpers.ClassBuilder;
import com.logicommerce.buildergenerator.codegen.helpers.Singularize;

class BuilderCollectionField extends SimpleCollectionField {
	public BuilderCollectionField(BuilderGenerator parent, String fieldName, CollectionType type,
			boolean requiresFieldDiscriminator) {
		super(parent, fieldName, type);
		setRequiresFieldDiscriminator(requiresFieldDiscriminator);
	}

	@Override
	public void declareBuilderSetters(ClassBuilder cb) {
		boolean addAfterBuild = type == CollectionType.MAP || type == CollectionType.SET;
		for (Class<?> childClass : parent.reverseClassSearch.getChildren(fieldFullType)) {
			parent.addRequiredImport(childClass);
			parent.addRequiredImport(parent.getBuilderFullName(childClass));
			if (type == CollectionType.MAP) {
				addSetterMethod(cb, childClass, addAfterBuild, keyType() + " key", ".put(key, " + TMP_VARIABLE + ");");
			} else {
				addSetterMethod(cb, childClass, addAfterBuild, "", ".add(" + TMP_VARIABLE + ");");
			}
		}
		super.declareBuilderSetters(cb);
	}

	private void addSetterMethod(ClassBuilder cb, Class<?> childClass, boolean addAfterBuild, String args,
			String addValueOperation) {
		String singularFieldName = Singularize.singularize(fieldName);
		String methodName = addPrefix("add", compositeSetter(singularFieldName, fieldFullType, childClass));

		String elementBuilderClass = BuilderGenerator.getBuilderSimpleName(childClass) + "<"
				+ parent.builderClassWithGeneric + ">";

		if (addAfterBuild) {
			// Hashmaps and hashsets compute the hash on insertion, so we need to add the
			// element after it's fully built.
			// Use an anonymous subclass to override _afterBuild.
			cb.addPublicMethod(elementBuilderClass, methodName, args,
					"var " + TMP_VARIABLE + " = new " + childClass.getSimpleName() + "();",
					"final var _builder = this;",
					"return new " + elementBuilderClass + "(this, " + TMP_VARIABLE + ") {",
					"@Override",
					"protected void _afterBuild() {",
					"super._afterBuild();",
					"_builder." + fieldName + addValueOperation,
					"_builder." + BUILT_OBJECT + "." + setterMethodName + "(_builder." + fieldName + ");",
					"}",
					"};").addEmptyLine();
		} else {
			cb.addPublicMethod(elementBuilderClass, methodName, args,
					"var " + TMP_VARIABLE + " = new " + childClass.getSimpleName() + "();",
					"this." + fieldName + addValueOperation,
					BUILT_OBJECT + "." + setterMethodName + "(this." + fieldName + ");",
					"return new " + elementBuilderClass + "(this, " + TMP_VARIABLE + ");").addEmptyLine();
		}
	}
}
