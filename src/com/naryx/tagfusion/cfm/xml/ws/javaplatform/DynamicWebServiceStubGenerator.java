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


package com.naryx.tagfusion.cfm.xml.ws.javaplatform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;

import org.apache.axis.client.Stub;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.wsdl.gen.Parser;
import org.apache.axis.wsdl.symbolTable.BindingEntry;
import org.apache.axis.wsdl.symbolTable.ElementDecl;
import org.apache.axis.wsdl.symbolTable.Parameter;
import org.apache.axis.wsdl.symbolTable.ServiceEntry;
import org.apache.axis.wsdl.symbolTable.SymTabEntry;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.toJava.Utils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.aw20.security.MD5;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.ws.CallParameters;
import com.naryx.tagfusion.cfm.xml.ws.dynws.DynamicCacheClassLoader;
import com.naryx.tagfusion.cfm.xml.ws.dynws.DynamicCacheClassLoaderFactory;
import com.naryx.tagfusion.cfm.xml.ws.dynws.DynamicWebServiceStubGeneratorInterface;
import com.naryx.tagfusion.cfm.xml.ws.dynws.StubInfo;
import com.naryx.tagfusion.cfm.xml.ws.dynws.WSDL2Java;


public class DynamicWebServiceStubGenerator implements DynamicWebServiceStubGeneratorInterface {
	private String javaCacheDir = null;

	private StubInfo si = null;
	
	public void setCacheDir(String javaCache){
		this.javaCacheDir = javaCache;
	}

	public Stub generateStub(String wsdlURL, String portName, CallParameters cp) throws cfmRunTimeException {
		// Gen a MD5 sum of the wsdl contents
		String wsdlContents = getWSDLContents(wsdlURL, cp);
		String wsdlSum = MD5.getDigest( wsdlContents );

		// Check cache first
		DynamicCacheClassLoader cl = null;
		synchronized (DynamicCacheClassLoader.STUB_MUTEX) {
			cl = checkCacheForStub(wsdlURL, wsdlSum, portName);
			if (cl == null)
				cl = buildClientClasses(wsdlURL, portName, wsdlContents, wsdlSum);
		}

		// Create and return a stub
		return createStubInstance(cl, wsdlURL);
	}

	private Stub createStubInstance(DynamicCacheClassLoader cl, String wsdlURL) throws cfmRunTimeException {
		Stub stub = null;
		Object locator = null;

		// Get the locator
		Class[] klasses = cl.findAllClasses();
		for (int i = 0; i < klasses.length; i++) {
			if (klasses[i].getName().endsWith(this.si.getLocatorName()) && org.apache.axis.client.Service.class.isAssignableFrom(klasses[i])) {
				try {
					// Create the service
					locator = klasses[i].newInstance();
					break;
				} catch (InstantiationException ex) {
					throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid service locator generated for " + wsdlURL + ". Cannot create locator. " + ex.getMessage()));
				} catch (IllegalAccessException ex) {
					throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid service locator generated for " + wsdlURL + ". Cannot access locator. " + ex.getMessage()));
				}
			}
		}

		// Make sure we got it.
		if (locator == null)
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid service locator generated for " + wsdlURL + ". Cannot find locator: " + this.si.getLocatorName()));

		// Get a stub from the locator
		try {
			Method stubMethod = locator.getClass().getDeclaredMethod("get" + this.si.getStubName(), new Class[0]);
			stub = (Stub) stubMethod.invoke(locator, new Object[0]);
		} catch (NoSuchMethodException ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid service locator generated for " + wsdlURL + ". No method: " + "get" + this.si.getStubName() + " not found."));
		} catch (IllegalAccessException ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid service locator generated for " + wsdlURL + ". Cannot access method: " + "get" + this.si.getStubName()));
		} catch (InvocationTargetException ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid service locator generated for " + wsdlURL + ". Cannot invoke method: " + "get" + this.si.getStubName() + ". " + ex.getMessage() + " " + ex.getTargetException().getMessage()));
		}

		return stub;
	}

	private DynamicCacheClassLoader checkCacheForStub(String wsdlLocation, String newWSDLSum, String portName) throws cfmRunTimeException {
		// Look for it by WSDL hash in our java class cache
		File dir = this.genWSDLClassPath(wsdlLocation);
		if (dir.exists()) {
			// Now look for the serialized StubInfo
			File siFile = genStubInfoPath(dir);
			if (siFile.exists()) {
				try {
					StubInfo stubInfo = null;
					FileInputStream fis = null;
					ObjectInputStream ois = null;
					try {
						fis = new FileInputStream(siFile);
						ois = new ObjectInputStream(fis);
						stubInfo = (StubInfo) ois.readObject();
					} finally {
						if (fis != null)
							fis.close();
						if (ois != null)
							ois.close();
					}

					// Get the class loader for the gen'd stubs
					DynamicCacheClassLoader rtn = DynamicCacheClassLoader.findClassLoader(dir.getCanonicalPath(), DynamicCacheClassLoader.STUB_CLASSES);
					if (rtn == null)
						throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "No class loader found for dynamic cache dir: " + dir.getCanonicalPath()));

					// Compare the WSDL sums to see if the WSDL has changed. Then check
					// that the portName
					// we need is the same as the one used to generate the stub's
					// operations initially.
					if (newWSDLSum.equals(stubInfo.getWSDLSum())) {
						if (portName == null || (portName.equalsIgnoreCase(stubInfo.getPortName()))) {
							// Nothing's changed, lets go
							this.si = stubInfo;
							return rtn;
						}
					}

					// Otherwise, clear the existing (now invalid) classes and return null
					// (...below)
					rtn.invalidate();
				} catch (IOException ex) {
					// Just ignore and assume we cannot use the cache
					com.nary.Debug.printStackTrace(ex);
				} catch (ClassNotFoundException ex) {
					// Just ignore and assume we cannot use the cache
					com.nary.Debug.printStackTrace(ex);
				}
			}
		}
		// OK, didn't find it
		return null;
	}

	@SuppressWarnings("deprecation")
	private String getWSDLContents(String wsdlURL, CallParameters cp) throws cfmRunTimeException {
		try {
			String wsdlL = wsdlURL.toLowerCase();
			String contents = null;
			if (wsdlL.startsWith("<?xml version")) {
				// The location is the WSDL itself (unexpected)
				contents = wsdlURL;
			} else {
				InputStream is = null;
				InputStreamReader isr = null;
				StringBuilder buffy = null;
				HttpGet method = null;
				try {
					if (wsdlL.startsWith("http:") || wsdlL.startsWith("https:")) {
						// Read from network
						DefaultHttpClient client = new DefaultHttpClient();

						// Set the timeout
						int timeout = cp.getTimeout();
						client.getParams().setParameter( "http.connection.timeout", timeout );
						client.getParams().setParameter( "http.socket.timeout", timeout );

						if (cp.getUsername() != null || cp.getPassword() != null) {
							// Set any credentials
							client.getCredentialsProvider().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( cp.getUsername(), cp.getPassword() ) );
						}

						if (cp.getProxyServer() != null) {
							// Set the proxy
							HttpHost proxy = new HttpHost( cp.getProxyServer(), (cp.getProxyPort() == -1) ? cp.getDefaultProxyPort() : cp.getProxyPort() );
							client.getParams().setParameter( ConnRoutePNames.DEFAULT_PROXY, proxy );

							if (cp.getProxyUser() != null || cp.getProxyPassword() != null) {
								// Set the proxy credentials
								client.getCredentialsProvider().setCredentials( new AuthScope( cp.getProxyServer() , cp.getProxyPort() ), 
										new UsernamePasswordCredentials( cp.getProxyUser(),  cp.getProxyPassword() ) );

							}
						}

						// Create the method and get the response
						method = new HttpGet(wsdlURL);
						client.getParams().setParameter( "http.protocol.handle-redirects", true );
						HttpResponse response = client.execute(method);
						switch ( response.getStatusLine().getStatusCode()) {
						case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
							throw new cfmRunTimeException(catchDataFactory.extendedException("errorCode.runtimeError", "Failed to access WSDL: " + wsdlURL + ". Proxy authentication is required.", response.getStatusLine().toString()));
						case HttpStatus.SC_UNAUTHORIZED:
							throw new cfmRunTimeException(catchDataFactory.extendedException("errorCode.runtimeError", "Failed to access WSDL: " + wsdlURL + ". Authentication is required.", response.getStatusLine().toString()));
						case HttpStatus.SC_USE_PROXY:
							throw new cfmRunTimeException(catchDataFactory.extendedException("errorCode.runtimeError", "Failed to access WSDL: " + wsdlURL + ". The use of a proxy is required.", response.getStatusLine().toString()));
						}
						if ( response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
							throw new cfmRunTimeException(catchDataFactory.extendedException("errorCode.runtimeError", "Failed to access WSDL: " + wsdlURL, response.getStatusLine().toString()));
						is = response.getEntity().getContent();
					} else {
						// Just try to read off disk
						File f = new File(wsdlURL);
						is = new FileInputStream(f);
					}

					// Read the data
					char[] buf = new char[4096];
					int read = -1;
					buffy = new StringBuilder();
					isr = new InputStreamReader(is);
					while ((read = isr.read(buf, 0, buf.length)) != -1)
						buffy.append(buf, 0, read);
					contents = buffy.toString();
				} finally {
					if (isr != null)
						isr.close();
					if (is != null)
						is.close();
					if (method != null)
						method.releaseConnection();
				}
			}

			// Calc the sum and return
			return contents;
		} catch (IOException ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Failed to access WSDL located at " + wsdlURL + ". There may be an error in the target WSDL. " + ex.getMessage()));
		}
	}

	private File genWSDLClassPath(String wsdlLocation) {
		return new File(new File(this.javaCacheDir), MD5.getDigest(wsdlLocation) );
	}

	private File genStubInfoPath(File wsdlClassPath) {
		return new File(wsdlClassPath, wsdlClassPath.getName() + ".ser");
	}

	@SuppressWarnings("static-access")
	private DynamicCacheClassLoader buildClientClasses(String wsdlLocation, String portName, String wsdlContents, String newWSDLSum) throws cfmRunTimeException {
		// Generate the source file(s)
		WSDL2Java w2j = new WSDL2Java();
		File outputDir = genWSDLClassPath(wsdlLocation);
		if (outputDir.exists())
			recursiveDelete(outputDir);
		outputDir.mkdirs();
		WSDL2Java.GenResults results = w2j.genClientClasses(wsdlLocation, wsdlContents, outputDir.getAbsolutePath());

		try {
			// Compile into classes
			java.io.ByteArrayOutputStream javacOut = new java.io.ByteArrayOutputStream();
			if (!w2j.compileOutput(w2j.getOutputDir(), javacOut)) {
				throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Failed to compile web service generated client classes for " + wsdlLocation + "." + "  The compiler error messages follow: " + javacOut.toString()));
			}
		} catch (IOException ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Failed to compile web service generated client classes for " + wsdlLocation + "." + "  The compiler error messages follow: " + ex.getMessage()));
		}

		// Delete the java files
		// w2j.cleanUpSource();

		// Create the StubInfo
		Parser wsdlParser = w2j.getWSDLParser();
		Service service = getWSDLService(wsdlParser);
		Port port = getWSDLPort(service, portName);
		this.si = new StubInfo(findLocatorName(wsdlParser, service), findStubName(port), newWSDLSum, portName, findOperations(wsdlParser, port));

		// Save the StubInfo
		File siFile = genStubInfoPath(outputDir);
		OutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			try {
				if (siFile.exists())
					throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid web service generated for " + wsdlLocation + ". Cannot serialize stub info. File: " + siFile.getAbsolutePath() + " already exists."));
				else
					siFile.createNewFile();
				fos = cfEngine.thisPlatform.getFileIO().getFileOutputStream(siFile);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(this.si);
			} finally {
				if (fos != null)
					fos.close();
				if (oos != null)
					oos.close();
			}

			// Create the DynamicCacheClassLoader
			DynamicCacheClassLoader rtn = DynamicCacheClassLoaderFactory.newClassLoader(outputDir.getCanonicalPath(), DynamicCacheClassLoader.STUB_CLASSES);

			// Register the IComplexObject impls
			Iterator itr = results.iComplexObjects.keySet().iterator();
			while (itr.hasNext()) {
				String cfcName = (String) itr.next();
				rtn.setIComplexObject(cfcName, (String) results.iComplexObjects.get(cfcName));
			}

			// Register the IQueryBean impl
			rtn.setIQueryBean(results.iQueryBean);

			// Register the IStructMap impl
			rtn.setIQueryBean(results.iStructMap);

			// Return the class loader
			return rtn;
		} catch (IOException ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid web service generated for " + wsdlLocation + ". Cannot serialize stub info: " + siFile.getAbsolutePath() + ". " + ex.getMessage()));
		}
	}

	private String findLocatorName(Parser wsdlParser, Service service) {
		SymbolTable symbolTable = wsdlParser.getSymbolTable();
		ServiceEntry sEntry = symbolTable.getServiceEntry(service.getQName());
		return sEntry.getName() + "Locator";
	}

	private String findStubName(Port port) {
		String portName = port.getName();
		if (!JavaUtils.isJavaId(portName))
			portName = Utils.xmlNameToJavaClass(portName);
		return portName;
	}

	private StubInfo.Operation[] findOperations(Parser wsdlParser, Port port) {
		SymbolTable symbolTable = wsdlParser.getSymbolTable();
		BindingEntry bEntry = symbolTable.getBindingEntry(port.getBinding().getQName());
		Set opSet = bEntry.getParameters().keySet();
		Iterator itr = opSet.iterator();

		StubInfo.Operation[] siOps = new StubInfo.Operation[opSet.size()];
		for (int i = 0; itr.hasNext(); i++) {
			// Get the operation and add the parameters
			Operation op = (Operation) itr.next();
			Vector parms = bEntry.getParameters(op).list;

			// Populate the parms
			StubInfo.Parameter[] siParms = new StubInfo.Parameter[parms.size()];
			StubInfo.Operation siOp = new StubInfo.Operation(op.getName(), siParms);
			siOps[i] = siOp;
			Iterator tmpItr = parms.iterator();
			for (int j = 0; tmpItr.hasNext(); j++) {
				Parameter p = (Parameter) tmpItr.next();
				siParms[j] = new StubInfo.Parameter(p.getName(), p.isNillable(), p.isOmittable());
			}

			// If there is only 1 parameter and it's a complex object,
			// gather parameter information for its properties.
			if (parms.size() == 1) {
				Vector elems = ((Parameter) parms.get(0)).getType().getContainedElements();
				if (elems != null && !elems.isEmpty()) {
					StubInfo.Parameter[] siSubParms = new StubInfo.Parameter[elems.size()];
					tmpItr = elems.iterator();
					for (int j = 0; tmpItr.hasNext(); j++) {
						ElementDecl e = (ElementDecl) tmpItr.next();
						siSubParms[j] = new StubInfo.Parameter(e.getName(), e.getNillable(), e.getMinOccursIs0());
					}
					siOp.setSubParameters(siSubParms);
				}
			}
		}

		// Return the operations
		return siOps;
	}


	private Service getWSDLService(Parser wsdlParser) throws cfmRunTimeException {
		SymTabEntry symTabEntry = null;
		Map.Entry entry = null;
		Vector v = null;

		// Iterate through all the entries in the WSDL until we
		// find the first service entry. Assume it's that one.
		Iterator iterator = wsdlParser.getSymbolTable().getHashMap().entrySet().iterator();
		while (iterator.hasNext()) {
			entry = (Map.Entry) iterator.next();
			v = (Vector) entry.getValue();
			for (int i = 0; i < v.size(); ++i) {
				if (ServiceEntry.class.isInstance(v.elementAt(i))) {
					symTabEntry = (SymTabEntry) v.elementAt(i);
					break;
				}
			}
		}

		if (symTabEntry == null)
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid web service operation. Cannot locate service entry in WSDL"));

		// Return the service
		return ((ServiceEntry) symTabEntry).getService();
	}

	/**
	 * Returns the Port binding for the specified Service.
	 * 
	 * @param service
	 *          Service to get a Port binding from
	 * @param portName
	 *          name of the port binding to get (or null)
	 * @return Port binding for the specified Service
	 * @throws cfmRunTimeException
	 */
	private Port getWSDLPort(Service service, String portName) throws cfmRunTimeException {
		Port port = null;
		Map ports = service.getPorts();
		if (portName != null) {
			Iterator itr = ports.keySet().iterator();
			while (itr.hasNext()) {
				// Get the one that has a localpart that matches the specified portName
				port = (Port) ports.get((String) itr.next());
				if (isValidPort(port) && port.getBinding().getPortType().getQName().getLocalPart().equals(portName))
					return port;
			}
		} else {
			Iterator itr = ports.keySet().iterator();
			while (itr.hasNext()) {
				// Pick the first.
				port = (Port) ports.get((String) itr.next());
				if (isValidPort(port))
					return port;
			}
		}

		throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid web service operation. Cannot locate port entry for service " + service.getQName().toString() + " in WSDL" + (portName == null ? "" : " with port name: " + portName)));
	}

	/**
	 * Returns true if the specified Port instance is valid, false otherwise.
	 * 
	 * @param port
	 *          Port instance to validate
	 * @return true if the specified Port instance is valid, false otherwise
	 * @throws cfmRunTimeException
	 */
	private boolean isValidPort(Port port) throws cfmRunTimeException {
		if (port != null) {
			List list = null;
			// There appears to be a bug in JRocket 1.5 such that calling
			// "port.getExtensibilityElements()" causes the JVM to throw a
			// NPE even when the variable is indeed initialized. Using
			// reflection seems to get around the problem.
			// list = port.getExtensibilityElements();
			try {
				Class impl = port.getClass();
				Method getter = impl.getMethod("getExtensibilityElements", new Class[0]);
				list = (List) getter.invoke(port, new Object[0]);
			} catch (NoSuchMethodException ex) {
				com.nary.Debug.printStackTrace(ex);
			} catch (IllegalAccessException ex) {
				com.nary.Debug.printStackTrace(ex);
			} catch (InvocationTargetException ex) {
				com.nary.Debug.printStackTrace(ex);
			}
			if (list != null) {
				Iterator itr = list.iterator();
				while (itr.hasNext()) {
					if (itr.next() instanceof SOAPAddress)
						return true;
				}
			}
		}
		return false;
	}

	private void recursiveDelete(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					files[i].delete();
				} else {
					recursiveDelete(files[i]);
					files[i].delete();
				}
			}
		}
		dir.delete();
	}

	public StubInfo getStubInfo() {
		return this.si;
	}

}
