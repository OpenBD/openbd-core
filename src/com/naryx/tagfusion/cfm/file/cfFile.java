/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: cfFile.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.file;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.tag.cfParseTag;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.cfm.tag.tagReader;
import com.naryx.tagfusion.expression.compile.expressionEngine;

public class cfFile implements java.io.Serializable {

	static final long serialVersionUID = 1;

	protected String pathToCFMFile;
	private String rawComponentName = null;

	protected String URI = null;
	protected cfmlURI address = null;

	private String encoding; // WARNING! always set using the setEncoding() method
	private boolean processPageEncoding = false;
	protected cfTag fileBody;
	
	private transient List<userDefinedFunction> udfList;
	private transient Map<String,cfmlURI> componentCache;

	private List<String> importPaths;


	public cfFile() {
		fileBody = null;
		setEncoding("UTF-8"); // default
	}

	public cfFile(cfmlURI uri, File _CFMFile) throws cfmBadFileException {
		init(uri, _CFMFile);
		Reader inFile = null;

		try {
			cfFileEncoding fileEncoding = new cfFileEncoding(_CFMFile);
			setEncoding(fileEncoding.getEncoding());
			inFile = fileEncoding.getReader(_CFMFile);
			readFile(inFile);
			inFile.close();
		} catch (org.alanwilliamson.lang.java.cfScriptCompilationException CFE) {
			throw CFE;
		} catch (cfmBadFileException BF) {
			throw new cfmBadFileException(pathToCFMFile, BF);
		} catch (cfmRunTimeException rte) {
			throw new cfmBadFileException(rte.getCatchData());
		} catch (FileNotFoundException E) {
			throw new cfmBadFileException(pathToCFMFile);
		} catch (IOException e) {
			cfCatchData catchData = new cfCatchData();
			catchData.setMessage(e.toString());
			catchData.setDetail(pathToCFMFile);
			throw new cfmBadFileException(catchData);
		} finally {
			try {
				if (inFile != null)
					inFile.close();
			} catch (IOException ignore) {
			}
		}
	}

	// note: the Reader instance should be i18n friendly
	public cfFile(cfmlURI uri, Reader _inFromWAR, String _encoding) throws cfmBadFileException {
		address = uri;
		if (uri.isRealFile())
			pathToCFMFile = uri.getURI();
		
		Reader inFile = null;
		setEncoding(_encoding);
		try {
			inFile = _inFromWAR;
			readFile(inFile);
			inFile.close();
		} catch (cfmBadFileException BF) {
			throw new cfmBadFileException(pathToCFMFile, BF);
		} catch (cfmRunTimeException rte) {
			throw new cfmBadFileException("[WAR] " + pathToCFMFile);
		} catch (IOException E) {
			throw new cfmBadFileException("[WAR] " + pathToCFMFile);
		} finally {
			try {
				if (inFile != null)
					inFile.close();
			} catch (IOException ignore) {
			}
		}
	}

	// this constructor is used to look for the CFPROCESSINGDIRECTIVE tag
	public cfFile(String fileBody) throws cfmBadFileException {
		try {
			encoding = null;
			processPageEncoding = true;
			Reader inFile = new StringReader(fileBody);
			readFile(inFile);
		} catch (cfmBadFileException bfe) {

			// we only care about exceptions throw by the CFPROCESSINGDIRECTIVE tag,
			// all other exceptions are ignored; the fact that we're only processing the first
			// 4K of the page will cause a variety of other exceptions to be thrown
			if (bfe.isPageEncodingException())
				throw bfe;
			
		} catch (cfmRunTimeException ignore) {
		} catch (IOException ignore) {}
	}

	public void init(cfmlURI uri, File _CFMFile) {
		pathToCFMFile = _CFMFile.getAbsolutePath().replace('\\', '/');
		address = uri;
	}

	public void addUDF(cfTag cftag, userDefinedFunction udf) throws cfmBadFileException {
		if (udfList == null) {
			udfList = new ArrayList<userDefinedFunction>();
		}

		String name = udf.getName();

		// check for built-in function name for UDF; allow override if CFC function
		if (!cftag.isSubordinate("CFCOMPONENT") && expressionEngine.isFunction(name)) {
			throw cftag.newBadFileException("Illegal Function Name", "The name \"" + name + "\" is the name of a built-in CFML function");
		}

		// check for duplicate names
		Iterator<userDefinedFunction> iter = udfList.iterator();
		while (iter.hasNext()) {
			if (name.equalsIgnoreCase(iter.next().getName())) {
				throw cftag.newBadFileException("Illegal Function Name", "The function name \"" + name + "\" is declared more than once");
			}
		}
		udfList.add(udf);
	}
	
	public void addComponentPath( String _name, cfmlURI _path ){
		if ( componentCache == null )
			componentCache = new HashMap<String,cfmlURI>();
				
		componentCache.put( _name, _path );
	}

	public cfmlURI getComponentPath( String _name ){
		if ( componentCache == null ){
			return null;
		}
		
		return componentCache.get( _name );
	}

	public cfTag getFileBody() {
		return fileBody;
	}

	public void setFileBody( cfTag tag ){
		fileBody = tag;
	}
	
	// two cfFile objects are equal if they have the same physical path
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		
		if (obj instanceof cfFile) {
			// if either physical path is null, we cannot determine that the files are
			// equal. the physical path will be null when the file is in a BDA.
			if (pathToCFMFile == null || ((cfFile) obj).pathToCFMFile == null)
				return false;
			else
				return pathToCFMFile.equals(((cfFile) obj).pathToCFMFile);
		}
		
		return false;
	}

	
	// getName() may be overridden by subclasses to return something other than physical path
	public String getName() {
		return pathToCFMFile;
	}

	public String getPath() {
		return pathToCFMFile;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String _encoding) {
		encoding = com.nary.util.Localization.convertCharSetToCharEncoding(_encoding);
	}

	public boolean processPageEncoding() {
		return processPageEncoding;
	}

	public String getURI() {
		return URI;
	}

	public cfFile setURI(String _URI) {
		URI = _URI;
		return this;
	}

	public cfmlURI getCfmlURI() {
		return address;
	}

	public void setCfmlURI(cfmlURI _address) {
		address = _address;
	}

	public String getComponentName() {
		return address.getComponentName();
	}

	public void setComponentName(String _name) {
		address.setComponentName(_name);
	}

	/**
	 * Returns the list of import paths
	 * Returns null if there are none.
	 */
	public List<String> getImportedPaths() {
	  return importPaths;
  }

	public synchronized void addImportPath( String _path ){
		// we only create the importPaths list if it's going to be needed
		// and we can only determine that by these calls
		if ( importPaths == null )
			importPaths = new ArrayList<String>(2);
		
		importPaths.add( _path );
	}

	public synchronized void addImportPaths( List<String> _paths ){
		// we only create the importPaths list if it's going to be needed
		// and we can only determine that by these calls
		if ( importPaths == null )
			importPaths = new ArrayList<String>(_paths.size());
		
		for ( int i = 0; i < _paths.size(); i++ )
			importPaths.add( _paths.get(i) );
	}

	protected void readFile(Reader _inFile) throws IOException, cfmRunTimeException {
		if (isCFENCODED(_inFile)) {
			cfCatchData _cfCatchData = new cfCatchData();
			_cfCatchData.setMessage("CFML templates encoded with the ColdFusion CFENCODE utility are not supported");
			throw new cfmBadFileException(_cfCatchData);
		}

		fileBody = new cfTag(this);
		cfParseTag parseTag = new cfParseTag(fileBody);
		tagReader tagR = new tagReader(_inFile);

		try {
			parseTag.readTag(tagR);
		} catch (cfmBadFileException bfe) {
			// This catch will catch exceptions thrown by cfOutputFilter.write()
			// which doesn't set the fileURI, line or column. In this case we
			// need to set them here.
			cfCatchData catchData = bfe.getCatchData();
			cfmlURI fileURI = catchData.getFileURI();
			if (fileURI == null) {
				catchData.setFileURI(address);
				catchData.setLine(tagR.getLine());
				catchData.setColumn(tagR.getColumn());
			}
			throw bfe;
		} catch (cfmRunTimeException e) {
			cfCatchData catchData = e.getCatchData();
			catchData.setFileURI(address);
			catchData.setLine(tagR.getLine());
			catchData.setColumn(tagR.getColumn());
			throw new cfmBadFileException(catchData);
		}

		tagR.close(address);

		fileBody.normalise(processPageEncoding);
	}

	private static final String CFENCODE_HEADER = "Allaire Cold Fusion Template";

	private static final int CFENCODE_HEADER_LEN = CFENCODE_HEADER.length();

	private static boolean isCFENCODED(Reader _inFile) throws IOException {
		CharArrayWriter buffer = new CharArrayWriter(CFENCODE_HEADER_LEN);

		_inFile.mark(CFENCODE_HEADER_LEN);

		for (int i = 0; i < CFENCODE_HEADER_LEN; i++) {
			buffer.write(_inFile.read());
		}

		if (buffer.toString().equals(CFENCODE_HEADER)) {
			return true;
		}

		_inFile.reset();
		return false;
	}

	public boolean isFlushable() {
		return fileBody.isFlushable();
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		putUDFs(_Session);
		return fileBody.render(_Session);
	}

	public cfTagReturnType renderToString(cfSession _Session) throws cfmRunTimeException {
		putUDFs(_Session);
		return fileBody.renderToString(_Session, cfTag.DEFAULT_OPTIONS);
	}

	/**
	 * This method is only invoked by cfMODULE.renderCustomTagEnd(). Do not invoke
	 * putUDFs (see bug #2609).
	 */
	public cfTagReturnType renderToString(cfSession _Session, int options) throws cfmRunTimeException {
		return fileBody.renderToString(_Session, options);
	}

	public void putUDFs(cfSession _Session) throws cfmRunTimeException {
		if (udfList == null) {
			return;
		}
		// put UDFs defined in this template into the variables scope and the active
		// component's "this" scope
		cfData variablesScope = _Session.getQualifiedData(variableStore.VARIABLES_SCOPE);
		cfComponentData activeComponent = _Session.getActiveComponentData();

		Iterator<userDefinedFunction> iter = udfList.iterator();
		while (iter.hasNext()) {
			userDefinedFunction udf = iter.next();
			String name = udf.getName();

			// check for duplicate function declaration, such as within CFINCLUDE
			cfData cfdata = variablesScope.getData(name);
			if ((cfdata != null) && (cfdata.getDataType() == cfData.CFUDFDATA) && name.equalsIgnoreCase(((userDefinedFunction) cfdata).getName())) {
				throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "cffile.duplicatefunctioname", new String[] { name }));
			}
			variablesScope.setData(name, udf); // put UDF into variables scope
			if (activeComponent != null) {
				activeComponent.setData(name, udf); // put UDF into active component's scope
			}
		}
	}

	/*
	 * This is for BDA originated files to be re-iniateted after loading
	 */
	public void reInitialiseTags() throws cfmBadFileException {
		fileBody.reInitialiseTags();
	}
	
	/**
	 * Added to support deserialization of cfc's on openbd-google
	 * 
	 * @param rawComponentName
	 */
	public void setRawComponentName(String rawComponentName) {
		this.rawComponentName = rawComponentName;
	}

	/**
	 * Added to support deserialization of cfc's on openbd-google
	 * 
	 * @param rawComponentName
	 */
	public String getRawComponentName() {
		return rawComponentName;
	}
	
}
