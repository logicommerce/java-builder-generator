package com.logicommerce.buildergenerator.codegen.core;

import com.logicommerce.buildergenerator.codegen.helpers.ClassBuilder;
import com.logicommerce.buildergenerator.codegen.helpers.JavaKeywords;

abstract class FieldBase {

	protected BuilderGenerator parent;
	protected static final String BUILT_OBJECT = BuilderGenerator.OBJ_VARIABLE;
	protected static final String TMP_VARIABLE = "_tmp";

	protected String fieldName;
	protected String fieldSimpleType;
	protected String fieldFullType;
	protected String setterMethodName;

	private boolean requiresFieldDiscriminator;

	protected FieldBase(BuilderGenerator parent, String fieldName, Class<?> fieldClass) {
		this.parent = parent;
		this.fieldName = sanitizeFieldName(fieldName);
		this.fieldSimpleType = fieldClass.getSimpleName();
		this.fieldFullType = fieldClass.getName();
		this.setterMethodName = addPrefix("set", fieldName);
	}

	protected void setRequiresFieldDiscriminator(boolean requiresFieldDiscriminator) {
		this.requiresFieldDiscriminator = requiresFieldDiscriminator;
	}

	public abstract void declareBuilderField(ClassBuilder cb);

	public abstract void declareBuilderSetters(ClassBuilder cb);

	protected String addPrefix(String prefix, String name) {
		return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	protected String compositeSetter(String fieldName, String fieldFullType, Class<?> subclass) {
		boolean requiresSubclassDiscriminator = parent.reverseClassSearch.getChildren(fieldFullType).size() > 1;
		if (!requiresSubclassDiscriminator) {
			return fieldName;
		}
		String subclassName = subclass.getSimpleName();
		if (!requiresFieldDiscriminator) {
			return subclassName.substring(0, 1).toLowerCase() + subclassName.substring(1);
		}
		return fieldName + "_" + subclassName;
	}

	private String sanitizeFieldName(String fieldName) {
		if (JavaKeywords.isKeyword(fieldName)) {
			// .default(true) is not accepted, use .setDefault(true) instead
			return addPrefix("set", fieldName);
		}
		return fieldName;
	}
}
