/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
 *
 *  This file is part of Open BlueDragon (OpenBD) CFML Server Engine.
 *  
 *  OpenBD is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *  
 *  OpenBD is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with OpenBD.  If not, see http://www.gnu.org/licenses/
 *  
 *  Additional permission under GNU GPL version 3 section 7
 *  
 *  If you modify this Program, or any covered work, by linking or combining 
 *  it with any of the JARS listed in the README.txt (or a modified version of 
 *  (that library), containing parts covered by the terms of that JAR, the 
 *  licensors of this Program grant you additional permission to convey the 
 *  resulting work. 
 *  README.txt @ http://www.openbluedragon.org/license/README.txt
 *  
 *  http://openbd.org/
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.ComponentFactory;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;

public class cfCOMPONENT extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	// component types
	public static final int COMPONENT = 0;

	public static final int INTERFACE = 1;

	public static final int ABSTRACT = 2;

	private static final String OUTPUT = "OUTPUT";

	private static final String EXTENDS = "EXTENDS";

	public static final String IMPLEMENTS = "IMPLEMENTS";

	private static final String TYPE = "TYPE";

	public static final String STYLE = "STYLE";

	public static final String HINT = "HINT";

	public static final String DISPLAYNAME = "DISPLAYNAME";

	public static final String SERVICEPORTNAME = "SERVICEPORTNAME";

	public static final String PORTTYPENAME = "PORTTYPENAME";

	public static final String BINDINGNAME = "BINDINGNAME";

	public static final String NAMESPACE = "NAMESPACE";

	public static final String WSDLFILE = "WSDLFILE";

	// TYPE attribute values
	private static final String COMPONENT_TYPE = "component";

	private static final String INTERFACE_TYPE = "interface";

	private static final String ABSTRACT_TYPE = "abstract";

	private static final cfStringData CF_COMPONENT_TYPE = new cfStringData(COMPONENT_TYPE);

	private static final cfStringData CF_INTERFACE_TYPE = new cfStringData(INTERFACE_TYPE);

	private static final cfStringData CF_ABSTRACT_TYPE = new cfStringData(ABSTRACT_TYPE);

	private cfStructData metaData;

	private cfArrayData propertyMetadata = cfArrayData.createArray(1);

	private cfArrayData functionMetadata = cfArrayData.createArray(1);

	private List<String> implementsList;

	private int componentType = COMPONENT;

	public int getComponentType() {
		return this.componentType;
	}

	public java.util.Map<String,String> getInfo() {
		return createInfo("control", "Used to define a CFC.  Inside this tag, contains the CFFUNCTION tags. Should only ever be one in a single file.");
	}

	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] { createAttInfo("NAME", "Provide a name for the argument, so it can be later referenced within the function.", "", true), createAttInfo("DISPLAYNAME", "The name that is used to describe this CFC when displayed in meta data", "", false), createAttInfo("HINT", "A small description for this component", "", false),
				createAttInfo("EXTENDS", "The CFC that this instance will extend", "", false), createAttInfo("IMPLEMENTS", "The CFC of the CFINTERFACE that this CFC must implement", "", false), createAttInfo("TYPE", "The type of this CFC; component, interface or abstract", "component", false), createAttInfo("OUTPUT", "Controls whether or not this CFC can generate output", "YES", false),
				createAttInfo("WSDLFILE", "The file path of a well formatted WSDL file to use instead of the generated one", "", false), createAttInfo("STYLE", "When the CFC is used as web services determines the encoded style; rpc, document, document-wrapped", "rpc", false), };

	}

	// property metadata added by the CFPROPERTY tag
	public void addPropertyMetaData(cfStructData _metaData) {
		try {
			propertyMetadata.addElement(_metaData);
		} catch (cfmRunTimeException ignore) {
		}
	}

	// function metadata added by the CFFUNCTION and CFSCRIPT tags
	public void addFunctionMetaData(cfStructData _metaData) {
		try {
			functionMetadata.addElement(_metaData);
		} catch (cfmRunTimeException ignore) {
		}
	}

	public String getEndMarker() {
		return "</CFCOMPONENT>";
	}

	public boolean doesTagHaveEmbeddedPoundSigns() {
		return string.convertToBoolean(getConstant(OUTPUT), false);
	}

	protected void defaultParameters(String tagName) throws cfmBadFileException {
		defaultParameters(tagName, -1);
	}

	protected void defaultParameters(String tagName, int type) throws cfmBadFileException {
		parseTagHeader(tagName);
		validateParameters(type);
	}

	public void validateParameters(int type) throws cfmBadFileException {
		// Only determine the type from the attributes if not passed
		if (type != -1) {
			componentType = type;
		} else if (containsAttribute(TYPE)) {
			constantAttribute(TYPE);
			String typeAttr = getConstant(TYPE).toLowerCase();
			if (typeAttr.equals(INTERFACE_TYPE)) {
				componentType = INTERFACE;
			} else if (typeAttr.equals(ABSTRACT_TYPE)) {
				componentType = ABSTRACT;
			}
			// for backwards compatibility, ignore other values for TYPE attribute
		}

		// if specified, OUTPUT attribute must be a constant convertible to a boolean
		if (containsAttribute(OUTPUT)) {
			constantAttribute(OUTPUT);
			booleanAttribute(OUTPUT);
		}

		// if specified, EXTENDS attribute must be constant, non-empty, and not start or end with "."
		if (containsAttribute(EXTENDS)) {
			constantAttribute(EXTENDS);
			String extendsAttr = getConstant(EXTENDS).trim();
			if ((extendsAttr.length() == 0) || extendsAttr.startsWith(".") || extendsAttr.endsWith(".")) {
				throw newBadFileException("Invalid Attribute", "The EXTENDS attribute does not specify a valid component name");
			}
		}

		// if specified, IMPLEMENTS attribute must be constant, non-empty, and not start or end with "."
		if (containsAttribute(IMPLEMENTS)) {
			if (componentType == INTERFACE)
				throw newBadFileException("Invalid Attribute", "The IMPLEMENTS attribute cannot be specified for components of TYPE=\"interface\"");

			constantAttribute(IMPLEMENTS);
			String implementsAttr = getConstant(IMPLEMENTS).trim();
			if (implementsAttr.length() == 0)
				throw newBadFileException("Invalid Attribute", "The IMPLEMENTS attribute cannot be an empty string");

			implementsList = string.split(implementsAttr, ",");
			for (int i = 0; i < implementsList.size(); i++) {
				String impl = implementsList.get(i);
				if (impl.startsWith(".") || impl.endsWith(".")) {
					throw newBadFileException("Invalid Attribute", "The IMPLEMENTS attribute does not specify a valid list of component names");
				}
			}
		}
	}

	public void tagLoadingComplete() throws cfmBadFileException {
		// if TYPE="interface", make sure subordinate tags are only CFFUNCTION
		if (componentType == INTERFACE) {
			for (int i = 0; i < childTagList.length; i++) {
				if (!(childTagList[i] instanceof cfFUNCTION)) {
					throw newBadFileException("Illegal CFC Interface pseudo-constructor", "Components of TYPE=\"interface\" may only contain CFFUNCTION tags");
				}
			}
		} else {
			// make sure CFPROPERTY tags appear first (not really needed for BD, but is compatible with CFMX)
			if (!areSubordinatesFirst("CFPROPERTY")) {
				throw newBadFileException("Illegal Tag Order", "CFPROPERTY tags must appear before any other tags in the CFCOMPONENT body");
			}
		}

		// Initialise meta data
		metaData = getMetaData(true);

		switch (componentType) {
			case COMPONENT:
				metaData.setData(TYPE, CF_COMPONENT_TYPE);
				break;
			case ABSTRACT:
				metaData.setData(TYPE, CF_ABSTRACT_TYPE);
				break;
			case INTERFACE:
				metaData.setData(TYPE, CF_INTERFACE_TYPE);
				break;
			default:
				throw new IllegalStateException("invalid component type - " + componentType);
		}

		if (propertyMetadata.size() > 0)
			metaData.setData("PROPERTIES", propertyMetadata);

		if (functionMetadata.size() > 0)
			metaData.setData("FUNCTIONS", functionMetadata);
	}

	/**
	 * create the super-component if specified, then render the CFC pseudo-constructor
	 */
	public cfTagReturnType render(cfSession session) throws cfmRunTimeException {
		// if specified, STYLE attribute must be either "rpc" or "document"
		if (containsAttribute(STYLE)) {
			String styleVal = getDynamic(session, STYLE).toString().trim().toUpperCase();
			if (!styleVal.equals("RPC") && !styleVal.equals("DOCUMENT") && !styleVal.equals("DOCUMENT-WRAPPED")) {
				throw newRunTimeException(catchDataFactory.invalidTagAttributeException(this, "STYLE attribute contains unsupported value, must either be RPC or DOCUMENT, or DOCUMENT-WRAPPED"));
			}
		}

		// if specified, WSDLFILE attribute must point to a real document
		if (containsAttribute(WSDLFILE)) {
			String wsdlFileVal = getDynamic(session, WSDLFILE).toString().trim().toUpperCase();
			File f = new File(wsdlFileVal);
			if (!f.exists() || !f.isFile())
				f = new File(session.getPresentDirectory(), wsdlFileVal);

			if (!f.exists() || !f.isFile()) {
				throw newRunTimeException(catchDataFactory.invalidTagAttributeException(this, "WSDLFILE attribute contains invalid value. Cannot locate: " + wsdlFileVal));
			}

			// Update the value so it's valid when we read it later
			metaData.setData(WSDLFILE, new cfStringData(f.getAbsolutePath()));
		}

		// push file stack for error reporting and component searching
		cfFile thisFile = this.getFile();
		boolean pushFile = !thisFile.equals(session.activeFile());
		if (pushFile) {
			session.pushActiveFile(thisFile);
		}

		// componentData == null if component file (.cfc) rendered via CFINCLUDE
		cfComponentData componentData = session.getActiveComponentData();
		if (componentData != null) {
			String attribExtends = getConstant(EXTENDS);
			if ((attribExtends == null) && (componentType != INTERFACE)) {
				// all components inherit from the global CFC except for interfaces and the global CFC itself
				if (!componentData.getComponentName().equalsIgnoreCase(ComponentFactory.GLOBAL_CFC_NAME)) {
					attribExtends = ComponentFactory.GLOBAL_CFC_NAME;
				}
			}

			// handle the construction of the parent component
			if (attribExtends != null) {
				renderExtends(session, componentData, attribExtends);
			}

			if (implementsList != null) {
				renderImplements(session, componentData);
			}

			componentData.setComponentType(componentType);
			componentData.setMetaData(metaData);
		}

		String renderResults = super.renderToString(session, cfTag.CF_OUTPUT_ONLY).getOutput();
		if ((renderResults != null) && ((componentData == null) || string.convertToBoolean(getConstant("OUTPUT"), true))) {
			session.forceWrite(renderResults);
		}

		// make sure all UDFs injected into the variables scope (via StructInsert, for example)
		// are added to the component "this" scope; see bug #2849
		if (componentData != null) {
			cfStructData variablesScope = componentData.getVariablesScope();
			Object[] keys = variablesScope.keys();
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i].toString();
				cfData cfdata = variablesScope.getData(key);
				if ((cfdata.getDataType() == cfData.CFUDFDATA) && !componentData.containsKey(key)) {
					componentData.setData(key, cfdata); // put udf into the this scope
				}
			}
		}

		// restore file stack
		if (pushFile) {
			session.popActiveFile();
		}

		return cfTagReturnType.NORMAL;
	}

	private void renderExtends(cfSession session, cfComponentData componentData, String attribExtends) throws cfmRunTimeException, dataNotSupportedException {
		cfComponentData superComponent = new cfComponentData(session, attribExtends, true); // true == allow abstract
		int superType = superComponent.getComponentType();

		// if this CFC is an interface, then the EXTENDS attribute can only specify another interface
		if (componentType == INTERFACE) {
			if (superType != INTERFACE) {
				throw new cfmRunTimeException(catchDataFactory.generalException(session, cfCatchData.TYPE_TEMPLATE, "Illegal EXTENDS Attribute", "The EXTENDS attribute for an interface must specify a component of TYPE=\"interface\""));
			}
		} else {
			// this CFC is not an interface, so the EXTENDS cannot specify an interface
			if (superType == INTERFACE) {
				throw new cfmRunTimeException(catchDataFactory.generalException(session, cfCatchData.TYPE_TEMPLATE, "Illegal EXTENDS Attribute", "The EXTENDS attribute may not specify a component of TYPE=\"interface\""));
			}
		}

		// set supercomponent references for functions
		setFunctionSuperScope(componentData, superComponent);

		// inherit "variables" and "this" scopes from parent
		inherit(superComponent, componentData);

		// set the super scope before rendering the tag body
		session.setQualifiedData(variableStore.SUPER_SCOPE, superComponent);

		// set up metadata
		metaData.setData(EXTENDS, superComponent.getMetaData());
	}

	private void renderImplements(cfSession session, cfComponentData componentData) throws cfmRunTimeException, cfmBadFileException {
		cfArrayData implementsMetadata = cfArrayData.createArray(1);
		for (int i = 0; i < implementsList.size(); i++) {
			String interfaceName = implementsList.get(i);
			cfComponentData interfaceData = new cfComponentData(session, interfaceName, true); // true == allow abstract
			implementsMetadata.addElement(interfaceData.getMetaData());

			// check that all interface methods are implemented
			Object[] keys = interfaceData.keys();
			for (int j = 0; j < keys.length; j++) {
				String key = keys[j].toString();
				// interface "this" scope will only contain UDFs
				if (!componentData.containsKey(key)) {
					throw newBadFileException("Invalid Attribute", "Component must implement function \"" + key + "\" from interface \"" + interfaceName + "\"");
				}
			}
		}
		metaData.setData(IMPLEMENTS, implementsMetadata);
	}

	/**
	 * The supercomponent reference in a UDF is a runtime attribute, so duplicate UDFs in the variables scope, setting the supercomponent reference, and place the duplicate copy back in the variables and CFC "this" scope.
	 */
	private static void setFunctionSuperScope(cfComponentData componentData, cfComponentData superComponent) {
		// UDFs were placed in the variables and "this" scopes by cfFile.render()
		cfStructData variablesScope = componentData.getVariablesScope();
		Object[] keys = variablesScope.keys();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i].toString();
			cfData cfdata = variablesScope.getData(key);
			// there should only be UDFs in variables scope at this point
			if (cfdata.getDataType() == cfData.CFUDFDATA) {
				userDefinedFunction udf = ((userDefinedFunction) cfdata).duplicateAndInherit(superComponent);
				variablesScope.setData(key, udf);
				componentData.setData(key, udf); // put udf into the this scope
			}
		}
	}

	// inherit the "variables" and "this" scopes from the parent
	private void inherit(cfComponentData superComponent, cfComponentData componentData) throws cfmRunTimeException {
		// inherit the variables scope by copying variables from the superclass variables scope
		cfStructData variablesScope = componentData.getVariablesScope();
		cfStructData superVariables = superComponent.getVariablesScope();
		Object[] keys = superVariables.keys();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i].toString();
			// do not copy overridden variables (especially "this")
			if (!variablesScope.containsKey(key)) {
				variablesScope.setData(key, superVariables.getData(key));
			}
		}

		// there's only one variables scope for a component instance
		superComponent.setVariablesScope(null);

		// inherit the "this" scope by copying the contents of the super's
		// this scope to the current object this scope
		keys = superComponent.keys();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i].toString();
			cfData superValue = superComponent.getData(key);
			if (superValue.getDataType() == cfData.CFUDFDATA) {
				// do not copy overridden functions
				if (!componentData.containsKey(key)) {
					// check for abstract function that was not overridden
					if (((userDefinedFunction) superValue).isAbstract()) {
						throw newBadFileException("Invalid Attribute", "Component must implement inherited abstract function \"" + key + "\" or specify TYPE=\"abstract\"");
					}
					componentData.setData(key, superValue);
				}
			} else {
				componentData.setData(key, superValue);
				// leave functions in super scope, remove other variables, because "super."
				// notation is only allowed for functions, not variables
				superComponent.removeData(key);
			}
		}

		componentData.setSuperComponent(superComponent);
	}
}
