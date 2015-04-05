/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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
 *  http://www.openbluedragon.org/
 */

/*
 * Created on Dec 28, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.naryx.tagfusion.cfm.xml.ws.dynws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis.utils.CLArgsParser;
import org.apache.axis.utils.CLOption;
import org.apache.axis.wsdl.toJava.Emitter;
import org.apache.axis.wsdl.toJava.GeneratedFileInfo;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * Generates Java files from WSDL.
 * 
 */
public class WSDL2Java extends org.apache.axis.wsdl.WSDL2Java {
	private String outputDir = null;

	/**
	 * Default constructor.
	 */
	public WSDL2Java() {
		super();
	}

	public WSDL2Java.GenResults genClientClasses(String wsdl, String wsdlContents, String outputDir) throws cfmRunTimeException {
		this.outputDir = outputDir;
		String[] args = new String[] { "-a", "-o", outputDir, wsdl };

		// Parse the arguments
		CLArgsParser argsParser = new CLArgsParser(args, options);

		// Print parser errors, if any
		if (null != argsParser.getErrorString()) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid arguments: " + argsParser.getErrorString()));
		}

		// Get a list of parsed options
		List clOptions = argsParser.getArguments();
		int size = clOptions.size();

		try {
			// Parse the options and configure the emitter as appropriate.
			for (int i = 0; i < size; i++) {
				parseOption((CLOption) clOptions.get(i));
			}

			// validate argument combinations
			validateOptions();

			// Get the context
			String ctxt = wsdl;
			int ndx = wsdl.indexOf("//");
			if (ndx != -1)
				ctxt = ctxt.substring(ndx + 2);
			ndx = wsdl.lastIndexOf('/');
			if (ndx == -1)
				ndx = wsdl.lastIndexOf('\\');
			if (ndx != -1)
				ctxt = wsdl.substring(0, ndx);

			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			fact.setNamespaceAware(true);
			fact.setIgnoringElementContentWhitespace(true);
			DocumentBuilder p = fact.newDocumentBuilder();
			Document doc = p.parse(new InputSource(new StringReader(wsdlContents)));
			doc.normalize();

			// Run the parser
			parser.run(ctxt, doc);

			// Gather the results
			GeneratedFileInfo genInfo = ((Emitter) parser).getGeneratedFileInfo();
			GenResults results = new GenResults();
			Iterator itr = genInfo.getList().iterator();
			while (itr.hasNext()) {
				GeneratedFileInfo.Entry e = (GeneratedFileInfo.Entry) itr.next();
				if (e.implType == GeneratedFileInfo.Entry.IQUERYBEAN)
					results.iQueryBean = e.className;
				else if (e.implType == GeneratedFileInfo.Entry.ISTRUCTMAP)
					results.iStructMap = e.className;
				else if (e.implType == GeneratedFileInfo.Entry.ICOMPLEXOBJECT)
					results.iComplexObjects.put(e.altName, e.className);
			}

			// Return the results
			return results;
		} catch (SAXParseException ex) {
			com.nary.Debug.printStackTrace(ex);
			throw new cfmRunTimeException(catchDataFactory.extendedException("errorCode.runtimeError", "No valid WSDL specified at location: " + wsdl, ex.getMessage()));
		} catch (Throwable ex) {
			com.nary.Debug.printStackTrace(ex);
			throw new cfmRunTimeException(catchDataFactory.extendedException("errorCode.runtimeError", "Problem building WSDL classes for " + wsdl, ex.getMessage()));
		}
	}

	/**
	 * The java compiler outputs messages to System.out and System.err so this
	 * method needs to be static and synchronized so the messages for different
	 * compiles won't get mixed up.
	 */
	public static synchronized boolean compileOutput(String outDir, ByteArrayOutputStream javacOut) throws IOException {
		return cfEngine.thisPlatform.compileOutput(outDir, javacOut);
	}

	public String getOutputDir() {
		return this.outputDir;
	}

	public org.apache.axis.wsdl.gen.Parser getWSDLParser() {
		return this.parser;
	}

	public class GenResults {
		public Map iComplexObjects = null;
		public String iQueryBean = null;
		public String iStructMap = null;

		public GenResults() {
			this.iComplexObjects = new FastMap();
		}
	}
}
